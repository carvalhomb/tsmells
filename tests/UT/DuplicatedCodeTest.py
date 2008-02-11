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

class ActionTest(unittest.TestCase):
    def setUp(self):
        self.inv1 = Action(10, 15, 20, 'Class.method()')
        self.inv2 = Action("9", "30", "20", 'Class.method()')
        self.inv3 = Action(10, 15, 20, 'Class.method()')

    def testConstructor(self):
        self.assertEquals(10, self.inv1.id_)
        self.assertEquals(15, self.inv1.line)
        self.assertEquals(20, self.inv1.acteeId)
        self.assertEquals('Class.method()', self.inv1.qName)

        self.assertEquals(9, self.inv2.id_)
        self.assertEquals(30, self.inv2.line)
        self.assertEquals(20, self.inv2.acteeId)
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
        self.tm = TestMethod("MyTest.testOne()", "MyTest.java")
        self.inv1 = Action(1, 15, 22, 'Class.method()')
        self.inv2 = Action(2, 30, 40, 'Clazz.mtd()')
        self.inv3 = Action(3, 40, 20, 'Cl.meth()')

    def testConstructor(self):
        self.assertEquals("MyTest.testOne()", self.tm.getName(), \
            "Method name filled incorrectly")
        self.assertEquals("MyTest.java", self.tm.getSrcFile(), \
            "Sourcefile name filled incorrectly")

    def testAddAction(self):
        self.tm.addAction(self.inv1)
        self.assertEquals(1, len(self.tm.getActions()))
        self.assertEquals(self.inv1, self.tm.getActions()[0])
        self.tm.addAction(self.inv2)
        self.assertEquals(2, len(self.tm.getActions()))
        self.assertTrue(self.inv1 in self.tm.getActions())
        self.assertTrue(self.inv2 in self.tm.getActions())

    def testSort(self):
        self.tm.addAction(self.inv2)
        self.tm.addAction(self.inv3)
        self.tm.addAction(self.inv1)
        self.tm.sort()
        self.assertEquals(self.inv1, self.tm.getActions()[0])
        self.assertEquals(self.inv2, self.tm.getActions()[1])
        self.assertEquals(self.inv3, self.tm.getActions()[2])

class RsfInvoFixtureBuilder():
    def refresh(self):
        self.mtd1   = TestMethod("MyTest.testOne()", "MyTest.java")
        self.inv11  = Action(217, 7,  178, "Uut.a()")
        self.line11 = 'ComInvoke;217;178;7;MyTest.testOne();Uut.a();MyTest.java\n'
        self.inv12  = Action(219, 8, 180, "Uut.b()")
        self.line12 = 'ComInvoke;219;180;8;MyTest.testOne();Uut.b();MyTest.java\n'
        self.inv13  = Action(221,9,182,"Uut.c()")
        self.line13 = 'ComInvoke;221;182;9;MyTest.testOne();Uut.c();MyTest.java\n'
        self.inv14  = Action(223,10,184,"Uut.d()")
        self.line14 = 'ComInvoke;223;184;10;MyTest.testOne();Uut.d();MyTest.java\n'
        self.inv15  = Action(225,11,186,"Uut.e()")
        self.line15 = 'ComInvoke;225;186;11;MyTest.testOne();Uut.e();MyTest.java\n'
        self.inv16  = Action(227, 12, 188, "Uut.f()")
        self.line16 = 'ComInvoke;227;188;12;MyTest.testOne();Uut.f();MyTest.java\n'

        self.mtd2   = TestMethod("MyTest.testTwo()", "MyTest.java")
        self.inv21  = Action(218, 16, 178, "Uut.a()")
        self.line21 = 'ComInvoke;218;178;16;MyTest.testTwo();Uut.a();MyTest.java\n'
        self.inv22  = Action(220, 17, 180, "Uut.b()")
        self.line22 = 'ComInvoke;220;180;17;MyTest.testTwo();Uut.b();MyTest.java\n'
        self.inv23  = Action(222,18,182,"Uut.c()")
        self.line23 = 'ComInvoke;222;182;18;MyTest.testTwo();Uut.c();MyTest.java\n'
        self.inv24  = Action(224,19,184,"Uut.d()")
        self.line24 = 'ComInvoke;224;184;19;MyTest.testTwo();Uut.d();MyTest.java\n'
        self.inv25  = Action(226,20,186,"Uut.e()")
        self.line25 = 'ComInvoke;226;186;20;MyTest.testTwo();Uut.e();MyTest.java\n'
        self.inv26  = Action(228,21,188,"Uut.f()")
        self.line26 = 'ComInvoke;228;188;21;MyTest.testTwo();Uut.f();MyTest.java\n'

        self.mtd3   = TestMethod("MyTest.testThree()", "MyTest.java")
        self.inv31  = Action(230,30,178, "Uut.a()")
        self.line31 = 'ComInvoke;230;178;30;MyTest.testThree();Uut.a();MyTest.java\n'
        self.inv32  = Action(231,31,180, "Uut.b()")
        self.line32 = 'ComInvoke;231;180;31;MyTest.testThree();Uut.b();MyTest.java\n'
        self.inv33  = Action(232,32,182,"Uut.c()")
        self.line33 = 'ComInvoke;232;182;32;MyTest.testThree();Uut.c();MyTest.java\n'
        self.inv34  = Action(233,33,184,"Uut.d()")
        self.line34 = 'ComInvoke;233;184;33;MyTest.testThree();Uut.d();MyTest.java\n'
        self.inv35  = Action(234,34,186,"Uut.e()")
        self.line35 = 'ComInvoke;234;186;34;MyTest.testThree();Uut.e();MyTest.java\n'
        self.inv36  = Action(235,35,188,"Uut.f()")
        self.line36 = 'ComInvoke;235;188;35;MyTest.testThree();Uut.f();MyTest.java\n'

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
        self.mtd1.addAction(self.inv11)
        self.mtd2.addAction(self.inv21)
        self.assertEquals(2, len(mtds), "Wrong number of methods parsed")
        self.assertTrue(self.mtd1 in mtds, "Failed to parse first method")
        self.assertTrue(self.mtd2 in mtds, "Failed to parse second method")

    def multiInvokesChecker(self, rsf):
        ''' helper for testParseMultiInvokes '''
        mtds = self.reader.parse(rsf)
        self.assertEquals(2, len(mtds))
        self.assertTrue(self.mtd1 in mtds, failMsgNotIn(self.mtd1, mtds))
        self.assertTrue(self.mtd2 in mtds, failMsgNotIn(self.mtd2, mtds))

    def testParseMultInvokes(self):
        self.mtd1.addAction(self.inv11)
        self.mtd1.addAction(self.inv12)
        self.mtd2.addAction(self.inv21)
        self.mtd2.addAction(self.inv22)

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

        inv1 = [self.inv11, self.inv12, self.inv13,\
                self.inv14, self.inv15, self.inv16]
        inv2 = [self.inv21, self.inv22, self.inv23,\
                self.inv24, self.inv25, self.inv26]

        self.findSingleCloneHelper(4, rsf, inv1, inv2, inv1, inv2)

    def testFindCloneFourInSixInvq(self):
        rsf = iostr(self.line11 + self.line12 + self.line13 +\
                    self.line21 + self.line22 + self.line23 +\
                    self.line24 + self.line25 + self.line26)

        inv1a = [self.inv11, self.inv12, self.inv13]
        inv1b = [self.inv14, self.inv15, self.inv16]
        inv2a = [self.inv21, self.inv22, self.inv23]
        inv2b = [self.inv24, self.inv25, self.inv26]

        self.findSingleCloneHelper(3, rsf, inv1a, inv2a + inv2b, inv1a, inv2a)

        self.refresh()
        rsf = iostr(self.line11 + self.line12 + self.line13 +\
                    self.line14 + self.line15 + self.line16 +\
                    self.line21 + self.line22 + self.line23)

        self.findSingleCloneHelper(3, rsf, inv1a+inv1b, inv2a, inv1a, inv2a)

    def findSingleCloneHelper(self, tresh, rsf, m1inv, m2inv, dup1inv, dup2inv):
        mtds = self.reader.parse(rsf)
        cf = CloneFinder(tresh)
        dupli = cf.investigate(mtds)

        self.mtd1.addActions(m1inv)
        self.mtd2.addActions(m2inv)

        #dump_dupli(dupli) 

        self.assertEquals(1, len(dupli), \
            "Should find duplicates in a single method, but found " + str(len(dupli)))
        self.assertTrue( ((self.mtd1,self.mtd2) in dupli) or\
                         ((self.mtd2,self.mtd1) in dupli))
        self.assertEquals(1, len(dupli [self.mtd1, self.mtd2]),\
            "Should find a single duplicate for (mtd1, mtd2)")
        self.assertEquals((dup1inv, dup2inv), dupli[self.mtd1, self.mtd2][0])

    def testDuplicationInSelf(self):
        inv17  = Action(223,10,178,"Uut.a()")
        line17 = 'ComInvoke;223;178;10;MyTest.testOne();Uut.a();MyTest.java\n'
        inv18  = Action(225,11,180,"Uut.b()")
        line18 = 'ComInvoke;225;180;11;MyTest.testOne();Uut.b();MyTest.java\n'
        inv19  = Action(227, 12, 182, "Uut.c()")
        line19 = 'ComInvoke;227;182;12;MyTest.testOne();Uut.c();MyTest.java\n'

        rsf = iostr(self.line11 + self.line12 + self.line13 +\
                    line17 + line18 + line19)
        mtds = self.reader.parse(rsf)
        cf = CloneFinder(3)
        dupli = cf.investigate(mtds)

        #dump_dupli(dupli)

        inv1 = [self.inv11, self.inv12, self.inv13]
        inv2 = [inv17, inv18, inv19]
        self.mtd1.addActions(inv1 + inv2)

        self.assertEquals(1, len(dupli))
        self.assertTrue((self.mtd1, self.mtd1) in dupli)
        self.assertEquals(1, len(dupli[(self.mtd1, self.mtd1)]))
        self.assertTrue((inv1, inv2) in dupli[(self.mtd1, self.mtd1)])

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
        dump_dupli(dupli)

        inv1 = [self.inv11, self.inv12, self.inv13, \
                self.inv14, self.inv15, self.inv16]
        inv2 = [self.inv21, self.inv22, self.inv23, \
                self.inv24, self.inv25, self.inv26]
        inv3 = [self.inv31, self.inv32, self.inv33, \
                self.inv34, self.inv35, self.inv36]

        self.mtd1.addActions(inv1)
        self.mtd2.addActions(inv2)
        self.mtd3.addActions(inv3)

        self.assertEquals(1, len(dupli))
        self.assertTrue((self.mtd1, self.mtd2, self.mtd3) in dupli)
        self.assertEquals(1, len(dupli[(self.mtd1, self.mtd2, self.mtd3)]))
        self.asertTrue((inv1, inv2, inv3) in dupli[(self.mtd1, self.mtd2, self.mtd3)])

def dump_dupli(toDump):
    ''' debug function which prints a duplication result'''
    for mtds in toDump:
        print str(mtds[0]) + "-" + str(mtds[1])
        for i in toDump[mtds]:
            print "["
            for j in i:
                print "<",
                for k in j:
                    print str(k) + "; ",
                print ">"
            print "]"


class PartitionTest(unittest.TestCase):
    def testSunny(self):
        parted = partition(range(0,2))
        for i in [[0,1],[0],[1]]:
            self.assertTrue(i in parted)

        parted = partition(range(0,4))
        expected = set()
        for i in [[0],[1],[2],[3],[0,1],[1,2],[2,3],\
                  [0,1,2],[1,2,3],[0,1,2,3]]:
            self.assertTrue(i in parted)

#if __name__ == "__main__":
#    unittest.main()

unittest.main()
