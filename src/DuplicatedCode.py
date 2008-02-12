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

from itertools  import izip

class Reference():
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
        # bottleneck so safety checks omitted
        return self.id_ == other.id_

def cmpInv(first, second):
    return first.line - second.line

class TestMethod():
    ''' A method with its invocations '''

    def __init__(self, id_, qName, srcFile):
        self.qName = qName # qualified method name
        self.srcFile = srcFile # sourcefile this method lives in
        self.references = [] # to be sorted on linenumber
        self.id_ = int(id_)
        self.refLength = None

    def addReference(self, inv):
        self.references.append(inv)

    def addReferences(self, invs):
        self.references.extend(invs)

    def sort(self):
        ''' sort the invocations on linenumber '''
        self.references.sort(cmpInv)

    def getName(self):
        return self.qName

    def getSrcFile(self):
        return self.srcFile

    def getReferences(self):
        return self.references

    def getId(self):
        return self.id_

    def __eq__(self, other):
        # bottleneck so safety checks omitted
        return self.id_ == other.id_

    def __str__(self):
        s = self.qName + "@" + self.srcFile + "["
        for i in self.references:
            s += str(i.id_) + ";"
        return s[:-1] + "]"

    def __hash__(self):
        return self.id_

    def cacheNrofReferences(self):
        ''' cache the number of references, this shouldnt change anyway'''
        self.refLength = len(self.references)

    def getNrofReferences(self):
        ''' get the cached reference length '''
        return self.refLength

class RsfReader():
    ''' Constructs a list of TestMethods from an RSF file '''

    def __parseLine(self, line, mtds):
        ''' add the information from a single line '''
        #ComInvoke;11;217;178;7;MyTest.testOne();Uut.a();MyTest.java
        #         invokerid, iid  invokeeid, line, tc, invo,  file
        splitted = line[:-1].split('\t')

        mtdName  = splitted[5].strip('"')
        mtdId    = int(splitted[1])
        fileName = splitted[7].strip('"')
        if not mtds.has_key(mtdId):
            # add the method
            mtds[mtdId] = TestMethod(mtdId, mtdName, fileName)
        mtd = mtds[mtdId]

        # add the reference
        ref = Reference(splitted[2], splitted[4], splitted[3], splitted[6])
        mtd.addReference(ref)

    def parse(self, rsf):
        ''' Build the testmethods. rsf should be a filehandle. 
            Returns a list of testmethods '''
        mtds = {}
        for line in rsf:
            self.__parseLine(line, mtds)

        # sort all invocations on linenumber
        for mtd in mtds.itervalues():
            mtd.sort()
            mtd.cacheNrofReferences()

        return mtds

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
        processed  = {} # { mtdId1 x { mtd2 x bool } }
        parted = {}     # { method x { seqLength x [parts] } }

        # partition all methods on their invocations
        for mtdId, mtd in mtds.iteritems():
            #parted[mtdId] = partition(mtd.getReferences(), self.treshold)
            parted[mtdId] = partition2(mtd, self.treshold)

        for mtdId in mtds:
            processed[mtdId] = {}

        for mtdId1, mtd1 in mtds.iteritems():
            for mtdId2, mtd2 in mtds.iteritems():
                if processed[mtdId2].has_key(mtdId1):
                    continue # already done this, order doesnt matter

                if mtd1 == mtd2:
                    dups = self.__investigateSingle(mtd1, parted)
                else:
                    dups = self.__investigateDuo(mtd1, mtd2, parted)

                if dups:
                    # now remove sub-duplicates.
                    # to get better performace these should never
                    # be added. but in practice the performance hit 
                    # is not that bad, as long as we'r not in 
                    # clone heaven
                    self.__removeSubDuplicates(dups)
                    # add the remaining clones.
                    duplicates[(mtd1, mtd2)] = dups

                processed[mtdId1][mtdId2] = True

        return duplicates

    def getSimilar(self, seq1, other):
        ''' look for duplicates of sequence in other.
            other is a list of sequences of same length as seq1'''
        similarSeqs = []
        for seq2 in other:
            self.appendSimilar(seq1, seq2, similarSeqs)
        return similarSeqs

    def appendSimilar(self, seq1, seq2, similarSeqs):
        for inv1, inv2 in izip(seq1, seq2):
            if not inv1.isSimilarTo(inv2):
                return
        similarSeqs.append(seq2)

    def __investigateSingle(self, mtd, parted):
        mtdParts = parted[mtd.getId()]

        dups = []
        # walk the different partition-lengths
        for seqs in mtdParts.itervalues():
            # walk these sequences
            for seq1 in seqs:
                # find look-a-like sequences
                for seq2 in self.getSimilar(seq1, seqs):
                    if (seq2, seq1) in dups: 
                        continue # dont add it twice
                    if seq1[0] in seq2 or seq2[0] in seq1:
                        continue # these have an intersection
                    dups.append((seq1, seq2))

        # now remove sub-duplicates.
        # to get better performace this should _really_ be done
        # before comparing, above
        self.__removeSubDuplicates(dups)
        return dups


    def __investigateDuo(self, mtd1, mtd2, parted):

        #debug_part(mtd1Parts)
        #debug_part(mtd2Parts)

        if mtd1.getNrofReferences() > mtd2.getNrofReferences():
            # swap, mtd1 should have the least nrof
            # references
            tmp  = mtd1
            mtd1 = mtd2
            mtd2 = tmp

        mtd1Parts = parted[mtd1.getId()]
        mtd2Parts = parted[mtd2.getId()]

        dups = [] # all couples of parts which are shared
        # walk the different partition lengths of mtd1
        for seqLength, seqs in mtd1Parts.iteritems():
            # mtd2 must have partitions of this length,
            # since its the largest

            other = mtd2Parts[seqLength]
            for seq1 in seqs:
                # ok, lets check if there are clones of 
                # seq1 in mtd2
                for seq2 in self.getSimilar(seq1, other):
                    # whoops -> found
                    dups.append((seq1, seq2))

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

def partition2(mtd, minLength=1):
    ''' Compute all sublists which form a sequence
        in the original of minLength.
        Returns a list of lists. 
        O(len(toSplit)^2)'''
    partitioned = {} # { length x [partition] }
    toSplit = mtd.getReferences()
    l = mtd.getNrofReferences()
    for i in range(minLength,l+1):
        partitioned[i] = []
    for i in range(0,l):
        for j in range(minLength,l-i+1):
            part = toSplit[i:i+j]
            partitioned[j].append(part)
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
