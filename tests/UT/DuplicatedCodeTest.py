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

import unittest, sys
from StringIO import StringIO as iostr
sys.path.append("../../src")
from DuplicatedCode import *

class ReferenceTest(unittest.TestCase):
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

class TestMethodTest(unittest.TestCase):
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
        self.line11 = 'ComInvoke\t11\t217\t178\t7\tMyTest.testOne()\tUut.a()\tMyTest.java\n'
        self.inv12  = Reference(219, 8, 180, "Uut.b()")
        self.line12 = 'ComInvoke\t11\t219\t180\t8\tMyTest.testOne()\tUut.b()\tMyTest.java\n'
        self.inv13  = Reference(221,9,182,"Uut.c()")
        self.line13 = 'ComInvoke\t11\t221\t182\t9\tMyTest.testOne()\tUut.c()\tMyTest.java\n'
        self.inv14  = Reference(223,10,184,"Uut.d()")
        self.line14 = 'ComInvoke\t11\t223\t184\t10\tMyTest.testOne()\tUut.d()\tMyTest.java\n'
        self.inv15  = Reference(225,11,186,"Uut.e()")
        self.line15 = 'ComInvoke\t11\t225\t186\t11\tMyTest.testOne()\tUut.e()\tMyTest.java\n'
        self.inv16  = Reference(227, 12, 188, "Uut.f()")
        self.line16 = 'ComInvoke\t11\t227\t188\t12\tMyTest.testOne()\tUut.f()\tMyTest.java\n'

        self.mtd2   = TestMethod(13, "MyTest.testTwo()", "MyTest.java")
        self.inv21  = Reference(218, 16, 178, "Uut.a()")
        self.line21 = 'ComInvoke\t13\t218\t178\t16\tMyTest.testTwo()\tUut.a()\tMyTest.java\n'
        self.inv22  = Reference(220, 17, 180, "Uut.b()")
        self.line22 = 'ComInvoke\t13\t220\t180\t17\tMyTest.testTwo()\tUut.b()\tMyTest.java\n'
        self.inv23  = Reference(222,18,182,"Uut.c()")
        self.line23 = 'ComInvoke\t13\t222\t182\t18\tMyTest.testTwo()\tUut.c()\tMyTest.java\n'
        self.inv24  = Reference(224,19,184,"Uut.d()")
        self.line24 = 'ComInvoke\t13\t224\t184\t19\tMyTest.testTwo()\tUut.d()\tMyTest.java\n'
        self.inv25  = Reference(226,20,186,"Uut.e()")
        self.line25 = 'ComInvoke\t13\t226\t186\t20\tMyTest.testTwo()\tUut.e()\tMyTest.java\n'
        self.inv26  = Reference(228,21,188,"Uut.f()")
        self.line26 = 'ComInvoke\t13\t228\t188\t21\tMyTest.testTwo()\tUut.f()\tMyTest.java\n'

        self.mtd3   = TestMethod(15, "MyTest.testThree()", "MyTest.java")
        self.inv31  = Reference(230,30,178, "Uut.a()")
        self.line31 = 'ComInvoke\t15\t230\t178\t30\tMyTest.testThree()\tUut.a()\tMyTest.java\n'
        self.inv32  = Reference(231,31,180, "Uut.b()")
        self.line32 = 'ComInvoke\t15\t231\t180\t31\tMyTest.testThree()\tUut.b()\tMyTest.java\n'
        self.inv33  = Reference(232,32,182,"Uut.c()")
        self.line33 = 'ComInvoke\t15\t232\t182\t32\tMyTest.testThree()\tUut.c()\tMyTest.java\n'
        self.inv34  = Reference(233,33,184,"Uut.d()")
        self.line34 = 'ComInvoke\t15\t233\t184\t33\tMyTest.testThree()\tUut.d()\tMyTest.java\n'
        self.inv35  = Reference(234,34,186,"Uut.e()")
        self.line35 = 'ComInvoke\t15\t234\t186\t34\tMyTest.testThree()\tUut.e()\tMyTest.java\n'
        self.inv36  = Reference(235,35,188,"Uut.f()")
        self.line36 = 'ComInvoke\t15\t235\t188\t35\tMyTest.testThree()\tUut.f()\tMyTest.java\n'

        self.reader = RsfReader()

def failMsgNotIn(mtd, mtds):
    msg = str(mtd) + " not in ["
    for m in mtds: msg += str(m) + ","
    return msg[:-1] + ']'


class RsfReaderTest(unittest.TestCase, RsfInvoFixtureBuilder):
    def setUp(self):
        self.refresh() # RsfInvoFixture

    def testParse(self):
        rsf = iostr(self.line11 + self.line21)

        mtds = self.reader.parse(rsf)
        self.mtd1.addReference(self.inv11)
        self.mtd2.addReference(self.inv21)
        self.assertEquals(2, len(mtds), "Wrong number of methods parsed")
        self.assertTrue(self.mtd1 in mtds.values(), "Failed to parse first method")
        self.assertTrue(self.mtd2 in mtds.values(), "Failed to parse second method")

    def multiInvokesChecker(self, rsf):
        ''' helper for testParseMultiInvokes '''
        mtds = self.reader.parse(rsf)
        self.assertEquals(2, len(mtds))
        self.assertTrue(self.mtd1 in mtds.values(), failMsgNotIn(self.mtd1, mtds))
        self.assertTrue(self.mtd2 in mtds.values(), failMsgNotIn(self.mtd2, mtds))

    def testParseMultInvokes(self):
        self.mtd1.addReference(self.inv11)
        self.mtd1.addReference(self.inv12)
        self.mtd2.addReference(self.inv21)
        self.mtd2.addReference(self.inv22)

        rsf = iostr(self.line11 + self.line12 + self.line21 + self.line22)
        self.multiInvokesChecker(rsf)

        rsf = iostr(self.line12 + self.line11 + self.line22 + self.line21)
        self.multiInvokesChecker(rsf)

        rsf = iostr(self.line21 + self.line22 + self.line11 + self.line12)
        self.multiInvokesChecker(rsf)

class CloneFinderTest(unittest.TestCase, RsfInvoFixtureBuilder):
    def setUp(self):
        self.refresh() # RsfInvoFixture

    def testFindCloneSixIdentiqInvo(self):
        rsf = iostr(self.line11 + self.line12 + self.line13 +\
                    self.line14 + self.line15 + self.line16 +\
                    self.line21 + self.line22 + self.line23 +\
                    self.line24 + self.line25 + self.line26)

        inv1 = Sequence([self.inv11, self.inv12, self.inv13,\
                self.inv14, self.inv15, self.inv16])
        inv2 = Sequence([self.inv21, self.inv22, self.inv23,\
                self.inv24, self.inv25, self.inv26])

        self.findSingleCloneHelper(4, rsf, inv1, inv2, inv1, inv2)

    def testFindCloneFourInSixInvq(self):
        rsf = iostr(self.line11 + self.line12 + self.line13 +\
                    self.line21 + self.line22 + self.line23 +\
                    self.line24 + self.line25 + self.line26)

        inv1a = [self.inv11, self.inv12, self.inv13]
        inv1b = [self.inv14, self.inv15, self.inv16]
        inv2a = [self.inv21, self.inv22, self.inv23]
        inv2b = [self.inv24, self.inv25, self.inv26]

        self.findSingleCloneHelper(3, rsf, Sequence(inv1a),\
                    Sequence(inv2a + inv2b), Sequence(inv1a),\
                    Sequence(inv2a))

        self.refresh()
        rsf = iostr(self.line11 + self.line12 + self.line13 +\
                    self.line14 + self.line15 + self.line16 +\
                    self.line21 + self.line22 + self.line23)

        self.findSingleCloneHelper(3, rsf, Sequence(inv1a+inv1b),\
                    Sequence(inv2a), Sequence(inv1a), Sequence(inv2a))

    def findSingleCloneHelper(self, tresh, rsf, m1inv, m2inv, dup1inv, dup2inv):
        mtds = self.reader.parse(rsf)
        cf = CloneFinder(tresh)
        dupli = cf.investigate(mtds)

        self.mtd1.addReferences(m1inv)
        self.mtd2.addReferences(m2inv)

        #dump_dupli(dupli) 

        self.assertEquals(1, len(dupli), \
            "Should find duplicates in a single method, but found " + str(len(dupli)))
        self.assertTrue( ((self.mtd1,self.mtd2) in dupli) or\
                         ((self.mtd2,self.mtd1) in dupli))
        self.assertEquals(1, len(dupli [self.mtd1, self.mtd2]),\
            "Should find a single duplicate for (mtd1, mtd2)")
        self.assertTrue( (dup1inv, dup2inv) == dupli[self.mtd1, self.mtd2][0] or\
                         (dup2inv, dup1inv) == dupli[self.mtd1, self.mtd2][0])

    def testDuplicationInSelf(self):
        inv17  = Reference(223,10,178,"Uut.a()")
        line17 = 'ComInvoke\t11\t223\t178\t10\tMyTest.testOne()\tUut.a()\tMyTest.java\n'
        inv18  = Reference(225,11,180,"Uut.b()")
        line18 = 'ComInvoke\t11\t225\t180\t11\tMyTest.testOne()\tUut.b()\tMyTest.java\n'
        inv19  = Reference(227, 13, 182, "Uut.c()")
        line19 = 'ComInvoke\t11\t227\t182\t13\tMyTest.testOne()\tUut.c()\tMyTest.java\n'

        rsf = iostr(self.line11 + self.line12 + self.line13 +\
                    line17 + line18 + line19)
        mtds = self.reader.parse(rsf)
        cf = CloneFinder(3)
        dupli = cf.investigate(mtds)

        #dump_dupli(dupli)

        inv1 = [self.inv11, self.inv12, self.inv13]
        inv2 = [inv17, inv18, inv19]
        self.mtd1.addReferences(Sequence(inv1 + inv2))
        inv1 = Sequence(inv1)
        inv2 = Sequence(inv2)

        self.assertEquals(1, len(dupli))
        self.assertTrue((self.mtd1, self.mtd1) in dupli)
        self.assertEquals(1, len(dupli[(self.mtd1, self.mtd1)]))
        self.assertTrue(((inv1, inv2) == dupli[(self.mtd1, self.mtd1)][0]) or \
                        ((inv2, inv1) == dupli[(self.mtd1, self.mtd1)][0]))

    def testTripleDuplication(self):
        rsf = iostr(self.line11 + self.line12 + self.line13 +\
                    self.line14 + self.line15 + self.line16 +\
                    self.line21 + self.line22 + self.line23 +\
                    self.line24 + self.line25 + self.line26 +\
                    self.line31 + self.line32 + self.line33 +\
                    self.line34 + self.line35 + self.line36)
        mtds = self.reader.parse(rsf)
        cf = CloneFinder(4)
        dupli = cf.investigate(mtds)
        dupli = cf.squashCombinations(dupli)
        #dump_dupli(dupli)

        inv1 = Sequence([self.inv11, self.inv12, self.inv13, \
                self.inv14, self.inv15, self.inv16])
        inv2 = Sequence([self.inv21, self.inv22, self.inv23, \
                self.inv24, self.inv25, self.inv26])
        inv3 = Sequence([self.inv31, self.inv32, self.inv33, \
                self.inv34, self.inv35, self.inv36])

        self.mtd1.addReferences(inv1)
        self.mtd2.addReferences(inv2)
        self.mtd3.addReferences(inv3)

        self.assertEquals(1, len(dupli))
        self.assertTrue(inv1.refStr in dupli)
        self.assertTrue(inv2.refStr in dupli)
        self.assertTrue(inv3.refStr in dupli)
        refStr = inv1.refStr
        self.assertTrue((self.mtd1, inv1.start, inv1.end) in dupli[refStr])
        self.assertTrue((self.mtd2, inv2.start, inv2.end) in dupli[refStr])
        self.assertTrue((self.mtd3, inv3.start, inv3.end) in dupli[refStr])


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

#class PartitionTest(unittest.TestCase):
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

class SequenceTest(unittest.TestCase):
    def setUp(self):
        self.seq1 = Sequence([RefStub(1,1), RefStub(2,2), RefStub(3,3), RefStub(4,4)])
        self.seq2 = Sequence([RefStub(2,2), RefStub(3,3)])
        self.seq3 = Sequence([RefStub(1,1), RefStub(2,2)])
        self.seq4 = Sequence([RefStub(3,3), RefStub(4,4)])
        self.seq5 = Sequence([RefStub(10,10), RefStub(15, 15)])

    def testContains(self):
        self.assertTrue(self.seq1.contains(self.seq2))
        self.assertFalse(self.seq2.contains(self.seq1))
        self.assertTrue(self.seq1.contains(self.seq3))
        self.assertFalse(self.seq3.contains(self.seq1))
        self.assertTrue(self.seq1.contains(self.seq4))
        self.assertFalse(self.seq4.contains(self.seq1))
        self.assertFalse(self.seq5.contains(self.seq1))
        self.assertFalse(self.seq1.contains(self.seq5))

    def testOverlaps(self):
        self.assertTrue(self.seq1.overlaps(self.seq2))
        self.assertTrue(self.seq2.overlaps(self.seq1))
        self.assertTrue(self.seq2.overlaps(self.seq4))
        self.assertFalse(self.seq1.overlaps(self.seq5))
        self.assertFalse(self.seq5.overlaps(self.seq1))

unittest.main()
