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

from itertools import izip

class Reference():
    ''' an access or an invocation '''

    def __init__(self, id_, line, targetId, qName):
        ''' id_: reference id, unique
        targetId: identifier for the invokee/accessee
        line: linenumber the reference occurs on
        qName: qualified name of the reference target'''
        self.id_ = int(id_)
        self.line = int(line)
        self.targetId = int(targetId)
        self.qName = qName

    def isSimilarTo(self, other):
        ''' References are similar if they have
            the same target but are still different '''
        return (self.targetId == other.targetId) and\
                (self.id_ != other.id_)

    def __str__(self):
        return "[" + str(self.id_) + ";" + str(self.targetId) + \
               ";" + str(self.line) + "]"

    def __eq__(self,other):
        # bottleneck so safety checks omitted
        return self.id_ == other.id_

def cmpInv(first, second):
    return first.line - second.line

class TestMethod():
    ''' a method with its references '''

    def __init__(self, id_, qName, srcFile):
        ''' id_: method id
            qName: qualified method name
            srcFile: path of the sourcefile this method is defined in '''
        self.qName = qName # qualified method name
        self.srcFile = srcFile # sourcefile this method lives in
        self.references = [] # to be sorted on linenumber
        self.id_ = int(id_)
        self.refLength = None

    def addReference(self, inv):
        self.references.append(inv)

    def addReferences(self, seq):
        self.references.extend(seq.ref)

    def sort(self):
        ''' sort the references on linenumber '''
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
        ''' get the cached references count '''
        return self.refLength

class RsfReader():
    ''' Construct a list of TestMethods from an RSF file '''

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

class Sequence():
    ''' an ordered collection of references '''

    def __init__(self, references):
        self.ref = references
        self.length = len(references)
        self.refHash = ""
        for i in self.ref:
            self.refHash += str(i.targetId)
        self.refHash = hash(self.refHash)
        self.start = references[0].line
        self.end = references[-1].line

    def computeSimilar(self, other):
        ''' look for duplicates of sequence in other.
            other is a list of sequences of same length as seq1'''
        similar = []

        for seq in other:
            if seq.refHash == self.refHash:
                # might be a collision, or self
                #self.__appendSimilar(seq, similar)
                for inv1, inv2 in izip(self.ref, seq.ref):
                    if not inv1.isSimilarTo(inv2):
                        #print "collision"
                        break
                else: similar.append(seq)

        return similar

    def __appendSimilar(self, seq, similar):
        for inv1, inv2 in izip(self.ref, seq.ref):
            if not inv1.isSimilarTo(inv2):
                return
        similar.append(seq)

    def contains(self, other):
        ''' predicate to determine if we contain other.
             based on line numbers '''
        if other.length > self.length:
            return False
        return self.start <= other.start and \
               self.end   >= other.end

    def overlaps(self, other):
        ''' predicate to determin wheter we share a 
            line with other'''
        return (self.start <= other.start <= self.end) or\
               (self.start <= other.end   <= self.end) or\
               (other.start <= self.start <= other.end)

class CloneFinder():
    ''' Search a set of methods with invocations for 
        duplicate parts '''

# TODO this class is too big
# introduce the concept of a Duplicate
# which is now just represented by a pair
# of sequences

    def __init__(self, treshold):
        ''' treshold is the minimum number of lines a 
            duplication must consist of. Not counting emptylines'''
        assert treshold >= 3
        self.treshold = treshold

    def investigate(self, mtds):
        ''' Find the duplicate code parts in mtds. 
            mtds should be a list of methods with their 
            invocations sorted. As returned by RsfReader. '''
        processed = self.__initProcessedDict(mtds)
        parted    = self.__partitionMtds(mtds)
        duplicates = {} # { (mtd1, mtd2) x [(seq1, seq2)] }
                        # This will contain couples of methods with their
                        # respective clone-sequences

        for mtdId1, mtd1 in mtds.iteritems():
            for mtdId2, mtd2 in mtds.iteritems():
                # was (mtd2, ,mtd1) processed before?
                if processed[mtdId2].has_key(mtdId1): continue

                if mtd1 == mtd2:
                    # compute clones inside a method
                    dups = self.__investigateSingle(mtd1,parted)
                else:
                    # find clones between methods
                    dups = self.__investigateDuo(mtd1,mtd2,parted)

                # add clones, if any
                if dups: duplicates[(mtd1,mtd2)] = dups
                processed[mtdId1][mtdId2] = True

        return duplicates

    def __initProcessedDict(self, mtds):
        ''' Initialize a struct to keep track of which couples are handled.
            This is usefull since clones(mtd1,mtd2) == clones(mtd2,mtd1)'''
        processed  = {} # { mtdId1 x { mtdId2 x bool } }

        for mtdId in mtds:
            processed[mtdId] = {}

        return processed

    def __partitionMtds(self, mtds):
        ''' Split all methods' references in sequences of contigent parts'''
        parted = {}     # { method x { seqLength x [parts] } }

        # partition all methods on their invocations
        for mtdId, mtd in mtds.iteritems():
            parted[mtdId] = partition(mtd, self.treshold)

        return parted

    def __investigateSingle(self, mtd, parted):
        ''' Look for clones in the reference sequences of mtd.
            Return these as a list of couples. '''
        # grab all the sequences owned by mtd
        mtdParts = parted[mtd.getId()]

        # will contain tuples of duplicate sequences
        dups = []
        # a clone cant be larger then half the method
        length = mtd.getNrofReferences()/2

        # walk backwards
        while length >= self.treshold:
             # all sequences with `length' items
            seqs = mtdParts[length]
            for seq in seqs:
                # compute and add all clones
                self.__addClones(seq, seqs, dups)
            length -= 1

        return dups

    def __addClones(self, seq, seqs, dups):
        ''' compute and add all clones for sequence `seq' in
            the sequences `seqs'. Add them to dups.'''
        # Already a clone which completly contains seq?
        if self.__superIsDuplicate(seq, dups):
            # then don't process
            return

        for seq2 in seq.computeSimilar(seqs):
            # do some checking to prevent duplicates
            if not (seq, seq2) in dups and\
               not seq.overlaps(seq2) and\
               not self.__superIsDuplicate(seq2, dups) and\
               not self.__overlapseWithOther(seq2, dups):
                # whoops, got one
                dups.append((seq, seq2))

    def __overlapseWithOther(self, seq, dups):
        ''' states if there are any existing clones which overlap
            with sequence `seq'. Overlap means contain at least
            one sourceLine in common.
            helper for investigating a single method'''
        for dup in dups:
            if seq.overlaps(dup[1]):
                return True
        return False

    def __superIsDuplicate(self, seq, dups):
        ''' predicate which denotes if there's a clone in `dups' which
            completly contains sequence `seq'
            helper for investigating a single method'''
        for dup in dups:
            if dup[0].contains(seq) or \
               dup[1].contains(seq):
                return True
        return False

    def __investigateDuo(self, mtd1, mtd2, parted):
        ''' Look for clones in the reference sequences of mtd1
            and mtd2. Return these as a list of couples. '''
        if mtd1.getNrofReferences() > mtd2.getNrofReferences():
            # swap, mtd1 should have the least nrofreferences
            tmp  = mtd1
            mtd1 = mtd2
            mtd2 = tmp

        mtd1Parts = parted[mtd1.getId()]
        mtd2Parts = parted[mtd2.getId()]

        dups = [] # all couples of parts which are shared
        # walk the different partition lengths of mtd1
        currentLength = mtd1.getNrofReferences()
        while currentLength >= self.treshold:
            # mtd2 must have partitions of this length,
            # since its the largest
            seqs = mtd1Parts[currentLength]
            other = mtd2Parts[currentLength]
            for seq1 in seqs:
                # ok, lets check if there are clones of 
                # seq1 in mtd
                for seq2 in seq1.computeSimilar(other):
                    # whoops -> found
                    if self.__duplicateHasSuperDuplicate(seq1, seq2, dups):
                        continue
                    dups.append((seq1, seq2))
            currentLength -= 1

        return dups

    def __duplicateHasSuperDuplicate(self, seq1, seq2, dups):
        for pair in dups:
            if pair[0].contains(seq1) and \
               pair[1].contains(seq2):
                return True
        return False

class DuplicatePrinter():

    def extensive(self, clones):
        # defect
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
        #dump_dupli(clones)
        delim = "\t"
        for mtds in clones:
            mtd1 = mtds[0]
            mtd2 = mtds[1]
            for cpl in clones[mtds]:
                seq1 = cpl[0]
                seq2 = cpl[1]
                print "DuplicatedCode" + delim + mtd1.getName() +\
                      delim + mtd1.getSrcFile() + delim + str(seq1.start) +\
                      delim + str(seq1.end)+ delim + mtd2.getName() +\
                      delim + mtd2.getSrcFile() + delim + str(seq2.start) +\
                      delim + str(seq2.end) 

def partition(mtd, minLength=1):
    ''' Compute all sublists which form a sequence
        in the original of minLength.
        Returns a dictionary with partition lengths 
        as keys and all partitions of this length
        as values.'''
    partitioned = {} # { length x [partition] }
    toSplit = mtd.getReferences()
    l = mtd.getNrofReferences()

    # initialize partitioned dict on the expected
    # partition-lengths
    for i in range(minLength,l+1):
        partitioned[i] = []

    # walk the start indeces of partitions
    for i in range(0,l):
        # walk the end indeces, construct and append
        for j in range(minLength,l-i+1):
            partitioned[j].append(Sequence(toSplit[i:i+j]))

    return partitioned

def print_list(l):
        print "[",
        for j in l:
            print str(j) + ", ",
        print "]"

def debug_part(parted):
    ''' debug helper which prints a parted structure'''
    for i in parted:
        print_list(i)

def dump_dupli(toDump):
    ''' debug function which prints a duplication result'''
    for mtds in toDump:
        print str(mtds[0]) + "-" + str(mtds[1])
        for i in toDump[mtds]:
            print "["
            for j in i:
                print "<",
                for k in j.ref:
                    print str(k) + "; ",
                print ">"
            print "]"

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
