# !/usr/bin/python
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

import sys
sys.path.append("../../src/dump")
from DuplicatedCode import *
from StringIO       import StringIO as iostr
from unittest       import TestCase, main


class ReferenceTest(TestCase):
    def setUp(self):
        self.inv1 = Reference(10, 15, 20, 'Class.method()')
        self.inv2 = Reference("9", "30", "20", 'Class.method()')
        self.inv3 = Reference(10, 15, 20, 'Class.method()')

    def testConstructor(self):
        self.assertEquals(10, self.inv1.id_)
        self.assertEquals(15, self.inv1.line)
        self.assertEquals(20, self.inv1.targetId)
        self.assertEquals('Class.method()', self.inv1.qName)

        self.assertEquals(9, self.inv2.id_)
        self.assertEquals(30, self.inv2.line)
        self.assertEquals(20, self.inv2.targetId)
        self.assertEquals('Class.method()', self.inv2.qName)

    def testEqual(self):
        self.assertFalse(self.inv1==self.inv2)
        self.assertEquals(self.inv1, self.inv3)

    def testSimilar(self):
        self.assertTrue(self.inv1.isSimilarTo(self.inv2))
        self.assertTrue(self.inv2.isSimilarTo(self.inv1))
        self.assertFalse(self.inv1.isSimilarTo(self.inv1))
        self.assertFalse(self.inv1.isSimilarTo(self.inv3))

#def buildInvocation(num):
#    ''' higher number should mean location on higher
#        line-number '''
#    if num==1:
#        return Invocation(1, 15, 22, 'Class.method()')
#    elif num==2:
#        return Invocation(2, 30, 40, 'Clazz.mtd()')
#    elif num==3:
#        return Invocation(3, 40, 20, 'Cl.meth()')

class TestMethodTest(TestCase):
    def setUp(self):
        self.tm = TestMethod(1, "MyTest.testOne()", "MyTest.java")
        self.inv1 = Reference(1, 15, 22, 'Class.method()')
        self.inv2 = Reference(2, 30, 40, 'Clazz.mtd()')
        self.inv3 = Reference(3, 40, 20, 'Cl.meth()')

    def testConstructor(self):
        self.assertEquals("MyTest.testOne()", self.tm.getName(), \
            "Method name filled incorrectly")
        self.assertEquals("MyTest.java", self.tm.getSrcFile(), \
            "Sourcefile name filled incorrectly")
        self.assertEquals(1, self.tm.getId())

    def testAddReference(self):
        self.tm.addReference(self.inv1)
        self.assertEquals(1, len(self.tm.getReferences()))
        self.assertEquals(self.inv1, self.tm.getReferences()[0])
        self.tm.addReference(self.inv2)
        self.assertEquals(2, len(self.tm.getReferences()))
        self.assertTrue(self.inv1 in self.tm.getReferences())
        self.assertTrue(self.inv2 in self.tm.getReferences())

    def testSort(self):
        self.tm.addReference(self.inv2)
        self.tm.addReference(self.inv3)
        self.tm.addReference(self.inv1)
        self.tm.sort()
        self.assertEquals(self.inv1, self.tm.getReferences()[0])
        self.assertEquals(self.inv2, self.tm.getReferences()[1])
        self.assertEquals(self.inv3, self.tm.getReferences()[2])

class RsfInvoFixtureBuilder():
    def refresh(self):
        self.mtd1   = TestMethod(11, "MyTest.testOne()", "MyTest.java")

        #ComInvoke;11;217;178;7;MyTest.testOne();Uut.a();MyTest.java
        #test command id; reference id;  target id / type id; line number; testcommand name; target name; filename

        self.inv11  = Reference(217, 7,  178, "Uut.a()")
        self.line11 = 'ComInvoke\t217\t11\t178\t7\tUut.a()\tMyTest.testOne()\tMyTest.java\n'
        self.inv12  = Reference(219, 8, 180, "Uut.b()")
        self.line12 = 'ComInvoke\t219\t11\t180\t8\tUut.b()\tMyTest.testOne()\tMyTest.java\n'
        self.inv13  = Reference(221,9,182,"Uut.c()")
        self.line13 = 'ComInvoke\t221\t11\t182\t9\tUut.c()\tMyTest.testOne()\tMyTest.java\n'
        self.inv14  = Reference(223,10,184,"Uut.d()")
        self.line14 = 'ComInvoke\t223\t11\t184\t10\tUut.d()\tMyTest.testOne()\tMyTest.java\n'
        self.inv15  = Reference(225,11,186,"Uut.e()")
        self.line15 = 'ComInvoke\t225\t11\t186\t11\tUut.e()\tMyTest.testOne()\tMyTest.java\n'
        self.inv16  = Reference(227, 12, 188, "Uut.f()")
        self.line16 = 'ComInvoke\t227\t11\t188\t12\tUut.f()\tMyTest.testOne()\tMyTest.java\n'
        self.inv1   = [self.inv11,  self.inv12,  self.inv13,  self.inv14,  self.inv15,  self.inv16]
        self.lines1 = [self.line11, self.line12, self.line13, self.line14, self.line15, self.line16]

        self.mtd2   = TestMethod(13, "MyTest.testTwo()", "MyTest.java")
        self.inv21  = Reference(218, 16, 178, "Uut.a()")
        self.line21 = 'ComInvoke\t218\t13\t178\t16\tUut.a()\tMyTest.testTwo()\tMyTest.java\n'
        self.inv22  = Reference(220, 17, 180, "Uut.b()")
        self.line22 = 'ComInvoke\t220\t13\t180\t17\tUut.b()\tMyTest.testTwo()\tMyTest.java\n'
        self.inv23  = Reference(222,18,182,"Uut.c()")
        self.line23 = 'ComInvoke\t222\t13\t182\t18\tUut.c()\tMyTest.testTwo()\tMyTest.java\n'
        self.inv24  = Reference(224,19,184,"Uut.d()")
        self.line24 = 'ComInvoke\t224\t13\t184\t19\tUut.d()\tMyTest.testTwo()\tMyTest.java\n'
        self.inv25  = Reference(226,20,186,"Uut.e()")
        self.line25 = 'ComInvoke\t226\t13\t186\t20\tUut.e()\tMyTest.testTwo()\tMyTest.java\n'
        self.inv26  = Reference(228,21,188,"Uut.f()")
        self.line26 = 'ComInvoke\t228\t13\t188\t21\tUut.f()\tMyTest.testTwo()\tMyTest.java\n'
        self.inv2   = [self.inv21,  self.inv22,  self.inv23,  self.inv24,  self.inv25,  self.inv26]
        self.lines2 = [self.line21, self.line22, self.line23, self.line24, self.line25, self.line26]

        self.reader = RsfReader()

def failMsgNotIn(mtd, mtds):
    msg = str(mtd) + " not in ["
    for m in mtds: msg += str(m) + ","
    return msg[:-1] + ']'


class RsfReaderTest(TestCase, RsfInvoFixtureBuilder):
    def setUp(self):
        self.refresh() # RsfInvoFixture

    def testParse(self):
        rsf = iostr(self.line11 + self.line21)
        self.mtd1.addReference(self.inv11)
        self.mtd2.addReference(self.inv21)
        self.executeParseTest(rsf)

    def testParseMultiple(self):
        self.mtd1.addReference(self.inv11)
        self.mtd1.addReference(self.inv12)
        self.mtd2.addReference(self.inv21)
        self.mtd2.addReference(self.inv22)

        rsf = iostr(self.line11 + self.line12 + self.line21 + self.line22)
        self.executeParseTest(rsf)

        # order of input shouldn't matter
        rsf = iostr(self.line12 + self.line11 + self.line22 + self.line21)
        self.executeParseTest(rsf)

        # idem
        rsf = iostr(self.line21 + self.line22 + self.line11 + self.line12)
        self.executeParseTest(rsf)

    def executeParseTest(self, rsf):
        ''' helper for testParseX '''
        mtds = self.reader.parse(rsf)
        self.assertEquals(2, len(mtds))
        self.verifyContains(mtds, self.mtd1)
        self.verifyContains(mtds, self.mtd2)

    def verifyContains(self, mtds, mtd):
        ''' assert that mtds contains mtd '''
        self.assertTrue(mtd.getId() in mtds, failMsgNotIn(mtd, mtds))
        self.assertEquals(mtd.getReferences(), mtds[mtd.getId()].getReferences(), failMsgNotIn(mtd, mtds))


class CloneFinderTest(TestCase, RsfInvoFixtureBuilder):

    #---
    # Fixture
    #---
    def setUp(self):
        self.refresh() # RsfInvoFixture

    def initDupli(self, start1, end1, start2, end2):
        self.dup1 = Sequence(self.inv1[start1:end1])
        self.dup2 = Sequence(self.inv2[start2:end2])

    #---
    # Helpers
    #---
    def initMthds(self, start1, end1, start2, end2):
        self.mtd1.addReferences(Sequence(self.inv1[start1:end1]))
        self.mtd2.addReferences(Sequence(self.inv2[start2:end2]))

        self.rsf = iostr("".join(self.lines1[start1:end1]) + \
                         "".join(self.lines2[start2:end2]))

    def executeCloneTest(self, dup1inv, dup2inv,treshold=3):
        mtds = self.reader.parse(self.rsf)
        cf = CloneFinder(treshold)
        dupli = cf.investigate(mtds)

        #dump_dupli(dupli) 

        self.assertEquals(1, len(dupli), \
            "Should find duplicates in a single clone, but found " + str(len(dupli)))
        self.assertTrue( ((self.mtd1,self.mtd2) in dupli) or\
                         ((self.mtd2,self.mtd1) in dupli))
        self.assertEquals(1, len(dupli [self.mtd1, self.mtd2]),\
            "Should find a single duplicate for (mtd1, mtd2)")
        self.assertTrue( (dup1inv, dup2inv) == dupli[self.mtd1, self.mtd2][0] or\
                         (dup2inv, dup1inv) == dupli[self.mtd1, self.mtd2][0])
    #---
    # Commands
    #---
    def testSequenceRepeated(self):
        ''' a sequence of 6 invocations copied in another method '''
        self.initDupli(0,6,0,6)
        self.initMthds(0,6,0,6)
        self.executeCloneTest(self.dup1, self.dup2)

    def testSubSequenceRepeated(self):
        ''' 3 invocations copied from a length 6 sequence '''
        # method1 length 6; method2 length 3
        self.initDupli(0,3,0,3)
        self.initMthds(0,3,0,6)
        self.executeCloneTest(self.dup1, self.dup2)

    def testSubSequenceRepeated2(self):
        ''' 3 invocations copied from a length 6 sequence '''
        # method1 length 6; method2 length 3
        # order shouldnt matter
        self.initDupli(0,3,0,3)
        self.initMthds(0,6,0,3)
        self.executeCloneTest(self.dup1, self.dup2)

    def testCloneInSelf(self):
        ''' A method where the first half is identical to the second'''
        self.lines1[3] = 'ComInvoke\t223\t11\t178\t10\tUut.a()\tMyTest.testOne()\tMyTest.java\n'
        self.lines1[4] = 'ComInvoke\t225\t11\t180\t11\tUut.b()\tMyTest.testOne()\tMyTest.java\n'
        self.lines1[5] = 'ComInvoke\t227\t11\t182\t13\tUut.c()\tMyTest.testOne()\tMyTest.java\n'
        self.inv1[3] = Reference(223, 10, 178, "Uut.a()")
        self.inv1[4] = Reference(225, 11, 180, "Uut.b()")
        self.inv1[5] = Reference(227, 13, 182, "Uut.c()")

        self.mtd1.addReferences(Sequence(self.inv1[0:6]))
        self.mtd2 = self.mtd1
        self.rsf = iostr("".join(self.lines1[0:6]))

        self.executeCloneTest(Sequence(self.inv1[0:3]), Sequence(self.inv1[3:6]))

class CloneSquashingTest(TestCase, RsfInvoFixtureBuilder):

    #---
    # Fixture
    #---
    def setUp(self):
        self.refresh() # RsfInvoFixture

    #---
    # Helper
    #---
    def setUpThirdMethod(self):
        self.mtd3   = TestMethod(15, "MyTest.testThree()", "MyTest.java")
        self.inv31  = Reference(230,30,178, "Uut.a()")
        self.line31 = 'ComInvoke\t230\t15\t178\t30\tUut.a()\tMyTest.testThree()\tMyTest.java\n'
        self.inv32  = Reference(231,31,180, "Uut.b()")
        self.line32 = 'ComInvoke\t231\t15\t180\t31\tUut.b()\tMyTest.testThree()\tMyTest.java\n'
        self.inv33  = Reference(232,32,182,"Uut.c()")
        self.line33 = 'ComInvoke\t232\t15\t182\t32\tUut.c()\tMyTest.testThree()\tMyTest.java\n'
        self.inv34  = Reference(233,33,184,"Uut.d()")
        self.line34 = 'ComInvoke\t233\t15\t184\t33\tUut.d()\tMyTest.testThree()\tMyTest.java\n'
        self.inv35  = Reference(234,34,186,"Uut.e()")
        self.line35 = 'ComInvoke\t234\t15\t186\t34\tUut.e()\tMyTest.testThree()\tMyTest.java\n'
        self.inv36  = Reference(235,35,188,"Uut.f()")
        self.line36 = 'ComInvoke\t235\t15\t188\t35\tUut.f()\tMyTest.testThree()\tMyTest.java\n'
        self.inv3   = [self.inv31,  self.inv32,  self.inv33,  self.inv34,  self.inv35,  self.inv36]
        self.lines3 = [self.line31, self.line32, self.line33, self.line34, self.line35, self.line36]

        self.inv1 = Sequence(self.inv1[0:6])
        self.inv2 = Sequence(self.inv2[0:6])
        self.inv3 = Sequence(self.inv3[0:6])

        self.mtd1.addReferences(self.inv1)
        self.mtd2.addReferences(self.inv2)
        self.mtd3.addReferences(self.inv3)

    #---
    # Commands
    #---
    def testTripleDuplication(self):
        self.setUpThirdMethod()

        self.rsf = iostr("".join(self.lines1[0:6]) +\
                         "".join(self.lines2[0:6]) +\
                         "".join(self.lines3[0:6]))

        mtds = self.reader.parse(self.rsf)
        cf = CloneFinder(4)
        dupli = cf.investigate(mtds)
        dupli = cf.squashCombinations(dupli)

        self.assertTrue(self.inv1.refStr == self.inv2.refStr == self.inv3.refStr)
        refStr = self.inv1.refStr
        self.assertEquals(1, len(dupli))
        self.assertTrue(refStr in dupli)
        self.assertTrue((self.mtd1, self.inv1.start, self.inv1.end) in dupli[refStr])
        self.assertTrue((self.mtd2, self.inv2.start, self.inv2.end) in dupli[refStr])
        self.assertTrue((self.mtd3, self.inv3.start, self.inv3.end) in dupli[refStr])


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


class MtdStub():
    def __init__(self, ref, length):
        self.ref = ref
        self.length = length

    def getReferences(self):
        return self.ref

    def getNrofReferences(self):
        return self.length

#class PartitionTest(TestCase):
#    def testSunny(self):
#        stub = MtdStub(range(0,2), 2)
#        parted = partition(stub, 1)
#        self.assertEquals(2, len(parted))
#        self.assertEquals([[0,1]], parted[2])
#        self.assertEquals([[0],[1]], parted[1])

#        stub = MtdStub(range(0,4), 4)
#        parted = partition(stub, 1)
#        self.assertEquals(4, len(parted))
#        self.assertEquals([[0,1,2,3]], parted[4])
#        self.assertEquals([[0,1,2],[1,2,3]], parted[3])
#        self.assertEquals([[0,1],[1,2],[2,3]], parted[2])
#        self.assertEquals([[0],[1],[2],[3]], parted[1])

class RefStub():
    def __init__(self, targetId, line):
        self.targetId = targetId
        self.line = line

class SequenceTest(TestCase):
    def setUp(self):
        self.seq1 = Sequence([RefStub(1,1), RefStub(2,2), RefStub(3,3), RefStub(4,4)])
        self.seq2 = Sequence([RefStub(2,2), RefStub(3,3)])
        self.seq3 = Sequence([RefStub(1,1), RefStub(2,2)])
        self.seq4 = Sequence([RefStub(3,3), RefStub(4,4)])
        self.seq5 = Sequence([RefStub(10,10), RefStub(15, 15)])

    def testContains(self):
        self.assertTrue(self.seq1.contains(self.seq2))
        self.assertTrue(self.seq1.contains(self.seq3))
        self.assertTrue(self.seq1.contains(self.seq4))

    def testDoesNotContain(self):
        self.assertFalse(self.seq2.contains(self.seq1))
        self.assertFalse(self.seq3.contains(self.seq1))
        self.assertFalse(self.seq4.contains(self.seq1))
        self.assertFalse(self.seq5.contains(self.seq1))
        self.assertFalse(self.seq1.contains(self.seq5))

    def testOverlaps(self):
        self.assertTrue(self.seq1.overlaps(self.seq2))
        self.assertTrue(self.seq2.overlaps(self.seq1))
        self.assertTrue(self.seq2.overlaps(self.seq4))

    def testDoesNotOverlap(self):
        self.assertFalse(self.seq1.overlaps(self.seq5))
        self.assertFalse(self.seq5.overlaps(self.seq1))

if __name__ == '__main__':
    main() # fetch and run all tests
