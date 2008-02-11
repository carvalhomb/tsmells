#
# This file is part of TSmells
#
# TSmells is free software; you can redistribute it and/or modify it 
# under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 2 of the License, or (at your 
# option) any later version.
#
# TSmells is distributed in the hope that it will be useful, but WITHOUT 
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more 
# details.
#
# You should have received a copy of the GNU General Public License along 
# with TSmells; if not, write to the Free Software Foundation, Inc., 
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA 
#
# Copyright 2007 Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>
#

from copy import deepcopy

class Action():
    ''' an access or an invocation '''

    def __init__(self, id_, line, acteeId, qName):
        ''' invId: invocation id, unique over all invocations
        invokedId: identifier for the invoked method/function
        line: linenumber of this invocation in the testmethod
        invokedQName: qualified name of the invoked method/function'''
        self.id_ = int(id_)
        self.line = int(line)
        self.acteeId = int(acteeId)
        self.qName = qName

    def isSimilarTo(self, other):
        ''' Predicates to determine if two invocations have
            the same invokee but are still different '''
        return (self.acteeId == other.acteeId) and\
                (self.id_ != other.id_)

    def __str__(self):
        return str(self.id_) + ":" + str(self.acteeId) + ":" + self.qName

    def __eq__(self,other):
        if other == None: return False
        return self.id_ == other.id_

def cmpInv(first, second):
    return first.line - second.line

class TestMethod():
    ''' A method with its invocations '''

    def __init__(self, qName, srcFile, invoc=[]):
        self.qName = qName # qualified method name
        self.srcFile = srcFile # sourcefile this method lives in
        self.actions = deepcopy(invoc) # to be sorted on linenumber

    def addAction(self, inv):
        self.actions.append(inv)

    def addActions(self, invs):
        self.actions.extend(invs)

    def sort(self):
        ''' sort the invocations on linenumber '''
        self.actions.sort(cmpInv)

    def getName(self):
        return self.qName

    def getSrcFile(self):
        return self.srcFile

    def getActions(self):
        return self.actions

    def __eq__(self, other):
        ''' Methods are equal if they have the same name,
            sourcefile and invocations '''
        if other == None: return False
        if self is other: return True
        if self.getName() != other.getName():
            return False
        if self.getSrcFile() != other.getSrcFile():
            return False
        if len(self.getActions()) != len(other.getActions()):
            return False
        if self.getActions() == other.getActions():
            return True
        return False

    def __str__(self):
        s = self.qName + "@" + self.srcFile + "["
        for i in self.actions:
            s += str(i.id_) + ";"
        return s[:-1] + "]"

    def __hash__(self):
        return hash(self.qName)

class RsfReader():
    ''' Constructs a list of TestMethods from an RSF file '''

    def __init__(self):
        self.mtds = {}

    def __parseLine(self, line):
        ''' add the information from a single line '''
        #ComInvoke;217;178;7;MyTest.testOne();Uut.a();MyTest.java
        #         iid  invid, line, tc,       invo,     file
        splitted = line[:-1].split(';')

        mtdName = splitted[4]
        if not self.mtds.has_key(mtdName):
            # add the method
            self.mtds[mtdName] = TestMethod(mtdName, splitted[6])
        mtd = self.mtds[mtdName]

        # add the invocation
        act = Action(splitted[1], splitted[3], splitted[2], splitted[5])
        mtd.addAction(act)

    def parse(self, rsf):
        ''' Build the testmethods. rsf should be a filehandle. 
            Returns a list of testmethods '''
        self.mtds = {}
        for line in rsf:
            self.__parseLine(line)

        # sort all invocations on linenumber
        for mtdName in self.mtds:
            self.mtds[mtdName].sort()

        return self.mtds.values()

class CloneFinder():
    ''' Search a set of methods with invocations for 
        duplicate parts '''

    def __init__(self, treshold):
        ''' treshold is the minimum number of lines a 
            duplication must consist of. Not counting emptylines'''
        assert treshold >= 3
        self.treshold = treshold

    def investigate(self, mtds):
        ''' Find the duplicate code parts in mtds. 
            mtds should be a list of methods with their 
            invocations sorted. As returned by RsfReader. '''
        duplicates = {} # { (mtd1, mtd2) x [(part1, part2)] }
        processed  = []
        parted = {} # {methodName x [parts] }

        # partition all methods on their invocations
        for mtd in mtds:
            parted[mtd] = partition(mtd.getActions(), self.treshold)

        for mtd1 in mtds:
            for mtd2 in mtds:
                if (mtd1, mtd2) in processed: continue
                if mtd1 == mtd2:
                    dups = self.__investigateSingle(mtd1, parted)
                else:
                    dups = self.__investigateDuo(mtd1, mtd2, parted)
                if dups: duplicates[(mtd1, mtd2)]=dups
                processed.append((mtd1,mtd2))
                processed.append((mtd2,mtd1))
        return duplicates

    def getSimilar(self, sequence, other):
        ''' look for duplicates of sequence in other'''
        similarSeqs = []
        for seq in other:
            #print ">>"
            #print_list(sequence)
            #print_list(seq)
            if len(seq) != len(sequence):
                continue
            similar = True
            for inv1, inv2 in zip(sequence, seq):
                if not inv1.isSimilarTo(inv2):
                    similar = False
                    break
            if similar: similarSeqs.append(seq)
        return similarSeqs

    def __investigateSingle(self, mtd, parted):
        mtdParts = parted[mtd]

        dups = []
        for seq1 in mtdParts:
            for seq2 in self.getSimilar(seq1, mtdParts):
                if not (seq2, seq1) in dups:
                    dups.append((seq1, seq2))

        # now remove sub-duplicates.
        # to get better performace this should _really_ be done
        # before comparing, above
        self.__removeSubDuplicates(dups)
        return dups


    def __investigateDuo(self, mtd1, mtd2, parted):
        mtd1Parts = parted[mtd1]
        mtd2Parts = parted[mtd2]

        # DEBUG
        #debug_part(mtd1Parts)
        #debug_part(mtd2Parts)

        dups = [] # all couples of parts which are shared
        for seq1 in mtd1Parts:
            for seq2 in self.getSimilar(seq1, mtd2Parts):
                dups.append((seq1, seq2))

        # now remove sub-duplicates.
        # to get better performace this should _really_ be done
        # before comparing, above
        self.__removeSubDuplicates(dups)

        return dups

    def __removeSubDuplicates(self, dups):
        # warning: ugly & wasteful code ahead
        toRemove = []
        for seq in dups:
            first = seq[0][0]
            last  = seq[0][-1]
            for seq2 in dups:
                #print ">>>>"
                #print_list(seq[0])
                #print_list(seq2[0])
                if seq == seq2: continue
                if (first in seq2[0] and
                    last  in seq2[0]):
                    # fully contained so remove it
                    toRemove.append(seq)
                    break
                #print "not removed"

        for tr in toRemove:
            dups.remove(tr)

class DuplicatePrinter():

    def extensive(self, clones):
        for k,v in clones:
            mtd1 = k[0]
            mtd2 = k[1]
            seq1 = v[0]
            seq2 = v[1]
            print str(mtd1) + " <--> " + str(mtd2)
            print_list(seq1)
            print_list(seq2)

    def briefCsv(self, clones):
        #DuplicatedCode;6;MyTest.firstCom();MyTest.java;7;MyTest.secondCom();MyTest.java;16
        for mtds in clones:
            mtd1 = mtds[0]
            mtd2 = mtds[1]
            for cpl in clones[mtds]:
                seq1 = cpl[0]
                seq2 = cpl[1]
                print "DuplicatedCode;" + mtd1.getName() + ";" + mtd1.getSrcFile() +\
                      ";" + str(seq1[0].line) + ";"+ str(seq1[-1].line) + ";" +\
                      mtd2.getName() + ";" + mtd2.getSrcFile() + ";" + str(seq2[0].line)\
                      + ";" + str(seq2[-1].line) 

def partition(toSplit, minLength=1):
    ''' Compute all sublists which form a sequence
        in the original of minLength.
        Returns a list of lists. 
        O(len(toSplit)^2)'''
    partitioned = []
    l = len(toSplit)
    for i in range(0,l):
        for j in range(0,l-i):
            if j+1 >= minLength:
                partitioned.append(toSplit[i:i+j+1])
    return partitioned

#
# to optmize this try either:
#   + return a tree/graph with 'contains' relation
#       [1,2,3] -->[1,2]--->[1]
#          |         |----->[2]
#          ------->[2,3]--->[3]
#   + use sets + intersect. need to declare a 
#              hash for the part
#   + some dict approach with hashes for parts?
#

def print_list(l):
        print "[",
        for j in l:
            print str(j) + ", ",
        print "]"

def debug_part(parted):
    ''' debug helper which prints a parted structure'''
    for i in parted:
        print_list(i)


if __name__=='__main__':
    import sys
    rsf = open(sys.argv[1])
    tresh = 5
    if len(sys.argv) > 2: 
        tresh = int(sys.argv[2])

    reader = RsfReader()
    mtds = reader.parse(rsf)
    cf = CloneFinder(tresh)
    dupli = cf.investigate(mtds)
    dp = DuplicatePrinter()
    #dp.extensive(dupli)
    dp.briefCsv(dupli)
