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

sys.path.append("../../src/viz")
sys.path.append("../../src/contract")

from unittest	 import TestCase, main
from tsmells2gdf import *
from cStringIO   import StringIO as iostr

SMELL_NODE_COLOR = "red"

class RootTest(TestCase):
	''' abstract root test'''
	def verifyParse(self, parser, toParse, key, location):
		parser.parse(toParse)
		self.assertTrue(parser.hasKey(key))
		self.assertEquals(location, parser.getLocation(key))

	def verifyGenerateNodes(self, generator, expNode):
		generator.parse(self.toParse)
		nodes = generator.generateNodes()
		self.assertEquals(1, len(nodes))
		self.assertTrue(expNode in nodes)

	def verifyGenerateMultiNodes(self, generator, expNode, expNode2, line1, line2):
		generator.parse(line1)
		generator.parse(line2)
		nodes = generator.generateNodes()
		self.assertEquals(2, len(nodes))
		self.assertTrue(expNode in nodes)
		self.assertTrue(expNode2 in nodes)

	def verifyAppendLocationInfo(self, generator, key, toParse, location):
		generator.parse(toParse)
		srcDict = {}
		generator.appendLocationInfo(srcDict)
		self.assertEqual(1, len(srcDict))
		self.assertTrue(key in srcDict)
		self.assertTrue(self.location in srcDict[key])

	def verifyAppendMetricsInfo(self, generator, identif, toParse, metricVal):
		generator.parse(self.toParse)
		metricDict = self.setupMetricDict()
		generator.appendMetricInfo(metricDict)

		self.assertTrue(identif in metricDict)
		metrics = metricDict[identif]
		self.assertEquals(1, len(metrics), \
						"Should contain a single indentedtest metric dict")
		self.assertTrue(identif + "0" in metrics)
		metrics = metrics[identif + "0"]
		for name, val in metricVal:
			self.assertEquals(val, metrics[name]) 

	def setupMetricDict(self):
		metricDict = {}
		metricDict['Translation'] = {}
		return metricDict
	
class TestCasesTest(RootTest):
	def setUp(self):
		self.cases = TestCases()
		self.toParse = ["TestCase","FooTest","FooTest.java","2"]
		self.key = "FooTest"
		self.location = ("FooTest.java", 2)
		self.edgeColor = "lightgray"
		self.expNode = Node(name=self.key, color="black",\
					 label=self.key, entity="testcase", \
					style=1)

	def testParse(self):
		self.verifyParse(self.cases, self.toParse, self.key, self.location)

	def testParsePkged(self):
		toParse = ["TestCase","pkg::FooTest","pkg/FooTest.java","2"]
		key = "pkg::FooTest"
		location = ("pkg/FooTest.java", 2)
		self.verifyParse(self.cases, toParse, key, location)

	def testGenerateNodes(self):
		self.verifyGenerateNodes(self.cases, self.expNode)
		
	def testGenerateNodesMulti(self):
		self.cases.parse(self.toParse)
		toParse2 = ["TestCase","BarTest","BarTest.java","3"]
		self.cases.parse(toParse2)
		toParse3 = ["TestCase","BazTest","BazTest.java","5"]		
		self.cases.parse(toParse3)

		nodes = self.cases.generateNodes()
		self.assertEquals(3, len(nodes))

		self.assertTrue(self.expNode in nodes)
		expNode2 = Node("BarTest", "black", \
						"testcase", "BarTest", 1)
		expNode3 = Node("BazTest", "black", \
						"testcase","BazTest", 1)
		self.assertTrue(expNode2 in nodes)
		self.assertTrue(expNode3 in nodes)

	def testAppendLocationInfo(self):
		self.verifyAppendLocationInfo(self.cases, self.key, self.toParse, self.location)

class TestCommandsTest(RootTest):
	def setUp(self):
		self.commands = TestCommands()
		self.key = "FooTest.testFoo()"
		self.toParse = ["TestCommand", self.key, "FooTest.java", "10", "FooTest"]
		self.expNode = Node(name=self.key, color="black", label="testFoo()", \
							entity="testcommand", style=2)

	def testParse(self):
		location = ("FooTest.java", 10)
		self.verifyParse(self.commands, self.toParse, self.key, location)
		self.assertEquals("FooTest", self.commands.getOwner(self.key))

	def testParseMulti(self):
		location = ("FooTest.java", 10)
		self.verifyParse(self.commands, self.toParse, self.key, location)
		
		toParse2 = ["TestCommand", "BazTest.tesBaz()", "BazTest.java", "15", "BazTest"]
		key2 = "BazTest.tesBaz()"
		location2 = ("BazTest.java", 15)
		self.verifyParse(self.commands, toParse2, key2, location2)

		self.assertTrue(self.commands.hasKey(self.key))
		self.assertEquals(location, self.commands.getLocation(self.key))
		self.assertEquals("FooTest", self.commands.getOwner(self.key))
		self.assertEquals("BazTest", self.commands.getOwner(key2))

	def testGenerateNodes(self):
		self.verifyGenerateNodes(self.commands, self.expNode)

class TestHelpersTest(RootTest):
	def setUp(self):
		self.helpers = TestHelpers()
		
	def testParse(self):
		toParse = ["TestHelper", "FooTest.helper()", "FooTest.java", "15", "FooTest"]
		key = "FooTest.helper()"
		location = ("FooTest.java", 15)
		self.verifyParse(self.helpers, toParse, key, location)
		self.assertEquals("FooTest", self.helpers.getOwner(key))

	def testParseParams(self):
		toParse = ["TestHelper", "FooTest.construct(String,Integer)", "FooTest.java", "15", "FooTest"]
		key = "FooTest.construct(String;Integer)"
		location = ("FooTest.java", 15)
		self.verifyParse(self.helpers, toParse, key, location)
		self.assertEquals("FooTest", self.helpers.getOwner(key))

class TestSetupsTest(RootTest):
	def setUp(self):
		self.setups = TestSetups()

	def testParse(self):
		toParse = ["TestSetup", "FooTest.setUp()", "FooTest.java", "15", "FooTest"]
		key = "FooTest.setUp()"
		location = ("FooTest.java", 15)
		self.verifyParse(self.setups, toParse, key, location)
		self.assertEquals("FooTest", self.setups.getOwner(key))

class TestTeardownsTest(RootTest):
	def setUp(self):
		self.teardowns = TestTeardowns()

	def testParse(self):
		toParse = ["TestTeardown", "FooTest.tearDown()", "FooTest.java", "15", "FooTest"]
		key = "FooTest.tearDown()"
		location = ("FooTest.java", 15)
		self.verifyParse(self.teardowns, toParse, key, location)
		self.assertEquals("FooTest", self.teardowns.getOwner(key))

class AssertionLesssTest(RootTest):
	def setUp(self):
		self.assertionlesss = AssertionLesss()
		self.key = "FooTest.testFoo()"		
		self.toParse = ["AssertionLess", self.key, "FooTest.java", "25"]
		self.expNode = Node(name="AssertionLess0", color=SMELL_NODE_COLOR,\
							 label="AssertionLess", entity="smell", \
							style=3)

	def testParse(self):
		location = ("FooTest.java", 25)
		self.verifyParse(self.assertionlesss, self.toParse, self.key, location)

	def testGenerateNodes(self):
		self.verifyGenerateNodes(self.assertionlesss, self.expNode)

class AssertionRoulettesTest(RootTest):
	def setUp(self):
		self.assertionroulettes = AssertionRoulettes()
		self.AR = 'AssertionRoulette'
		self.toParse = [ self.AR , "TC", "FooTest.testFoo()", "10", "8", "FooTest.java", "228"]
		
	def testParse(self):
		key = "FooTest.testFoo()"
		location = ("FooTest.java", 228)
		self.verifyParse(self.assertionroulettes, self.toParse, key, location)
		self.assertEquals(10, self.assertionroulettes.getTotalAsserts(key))
		self.assertEquals(8, self.assertionroulettes.getDescriptionlessAsserts(key))

	def testAppendMetrics(self):
		self.verifyAppendMetricsInfo(self.assertionroulettes, self.AR, self.toParse, [('NrA', 10),('NrDA', 8)])

	def testAppendMultiMetrics(self):
		self.assertionroulettes.parse(self.toParse)
		self.toParse2 = [ self.AR, "TH", "BarTest.testBar()", "15", "5", "BarTest.java", "220"]
		self.assertionroulettes.parse(self.toParse)
		self.assertionroulettes.parse(self.toParse2)
		metricDict = self.setupMetricDict()
		self.assertionroulettes.appendMetricInfo(metricDict)
		self.assertTrue(self.AR in metricDict)
		metrics = metricDict[self.AR]
		self.assertEquals(2, len(metrics), str(metrics))
		self.assertEquals({'NrA':10, 'NrDA':8}, metrics[self.AR + '0'])
		self.assertEquals({'NrA':15, 'NrDA':5}, metrics[self.AR + '1'])

class DuplicatedCodesTest(TestCase):
	def setUp(self):
		# GeneralFixture here :))
		self.DP = "DuplicatedCode"
		self.duplicatedcodes = DuplicatedCodes()
		self.mtd1 = "BtPieceMessageTest.testChokingEvent_inAmAllowedIndexSet()"
		self.mtd2 = "BtPieceMessageTest.testChokingEvent_somwhere()"
		self.dupLoc1 = ("BtPieceMessageTest.cc", 197)
		self.dupLoc2 = ("BtPieceMessageTest.cc", 183)
		self.toParse = ["DuplicatedCode",\
					self.mtd1, "BtPieceMessageTest.cc", "197", "206", 
					self.mtd2, "BtPieceMessageTest.cc", "183", "193"]
		self.edge1 = Edge("DuplicatedCode0", self.mtd1)
		self.edge2 = Edge("DuplicatedCode0", self.mtd2)

		self.mtd21 = "Faz.testFaz()"
		self.mtd22 = "Boo.testBoo()"
		self.dupLoc21 = ("Faz.cpp", 20)
		self.dupLoc22 = ("Boo.cpp", 30)
		self.toParse2 = ["DuplicatedCode",\
					self.mtd21, "Faz.cpp", 20, 25,\
					self.mtd22, "Boo.cpp", 30, 35]

	def testParse(self):
		self.duplicatedcodes.parse(self.toParse)
		self.assertEquals(1, len(self.duplicatedcodes.duplicates))

	def testParseMulti(self):
		self.duplicatedcodes.parse(self.toParse)
		self.duplicatedcodes.parse(self.toParse2)
		self.assertEquals(2, len(self.duplicatedcodes.duplicates))

	def testAppendLocationInfo(self):
		self.duplicatedcodes.parse(self.toParse)
		locInfo = {}
		self.duplicatedcodes.appendLocationInfo(locInfo)

		self.assertEqual(1, len(locInfo), "Should have a single entry")
		self.assertTrue("DuplicatedCode0" in locInfo)
		entry = locInfo["DuplicatedCode0"]
		self.assertEqual(2, len(entry))
		self.assertTrue(self.dupLoc1 in entry)
		self.assertTrue(self.dupLoc2 in entry)

	def testAppendLocationInfoQuad(self):
		self.mtd3 = "FooTest.testFoo()"
		self.dupLoc3 = ("FooTest.cpp", 20)
		self.mtd4 = "BazTest.testBaz()"
		self.dupLoc4 = ("BazTest.cpp", 30)
		self.toParse.extend([self.mtd3, "FooTest.cpp", 20, 25,\
							 self.mtd4, "BazTest.cpp", 30, 35])

		self.duplicatedcodes.parse(self.toParse)
		locInfo = {}
		self.duplicatedcodes.appendLocationInfo(locInfo)

		self.assertEqual(1, len(locInfo))
		self.assertTrue("DuplicatedCode0" in locInfo)
		entry = locInfo["DuplicatedCode0"]
		self.assertEqual(4, len(entry))
		self.assertTrue(self.dupLoc1 in entry)
		self.assertTrue(self.dupLoc2 in entry)
		self.assertTrue(self.dupLoc3 in entry)
		self.assertTrue(self.dupLoc4 in entry)

	def testAppendLocationInfo2Nodes(self):
		self.duplicatedcodes.parse(self.toParse)
		self.duplicatedcodes.parse(self.toParse2)
		locInfo = {}
		self.duplicatedcodes.appendLocationInfo(locInfo)

		self.assertEquals(2, len(locInfo))
		entry0 = locInfo["DuplicatedCode0"]
		entry1 = locInfo["DuplicatedCode1"]
		self.assertEqual(2, len(entry0), "Should be a two-way duplicate")
		self.assertTrue(self.dupLoc1 in entry0)
		self.assertTrue(self.dupLoc2 in entry0)
		self.assertEqual(2, len(entry1), "Should be a two-way duplicate")
		self.assertTrue(self.dupLoc21 in entry1)
		self.assertTrue(self.dupLoc22 in entry1)

	def testGenerateEdgesSingleMtd(self):
		self.toParse[5] = self.mtd1
		self.duplicatedcodes.parse(self.toParse)
		edges = self.duplicatedcodes.generateEdges()

		self.assertEqual(1, len(edges))
		self.assertTrue(self.edge1 in edges)

	def testGenerateEdgesMultiMtd(self):
		self.duplicatedcodes.parse(self.toParse)
		edges = self.duplicatedcodes.generateEdges()
 
		self.assertEqual(2, len(edges))
		self.assertTrue(self.edge1 in edges)
		self.assertTrue(self.edge2 in edges)

	def testCalculateANrSLSingle(self):
		self.dupli = [self.DP, "Foo.foo()", "Foo.java", 2, 10]
		ANrSL = self.duplicatedcodes.calculateANrSL(self.dupli)
		self.assertEquals(ANrSL, 9)

	def testCalculateANrSLDuo(self):
		self.dupli = [self.DP, "Foo.foo()", "Foo.java", 2, 10, \
							   "Bar.bar()", "Bar.java", 10, 18]
		ANrSL = self.duplicatedcodes.calculateANrSL(self.dupli)
		self.assertEquals(ANrSL, 9)

		self.dupli = [self.DP, "Foo.foo()", "Foo.java", 2, 10, \
							   "Bar.bar()", "Bar.java", 10, 16]
		ANrSL = self.duplicatedcodes.calculateANrSL(self.dupli)
		self.assertEquals(ANrSL, 8)

	def testCalculateANrSLTriple(self):
		self.dupli = [self.DP, "Foo.foo()", "Foo.java", 2, 10, \
							   "Bar.bar()", "Bar.java", 10, 16, \
							   "Baz.baz()", "Baz.java", 16, 20]
		ANrSL = self.duplicatedcodes.calculateANrSL(self.dupli)
		self.assertEquals(ANrSL, 7)

	def testAppendMetricInfo(self):
		self.duplicatedcodes.parse(self.toParse)
		metricDict = {}
		metricDict['Translation'] = {}
		self.duplicatedcodes.appendMetricInfo(metricDict)

		self.assertTrue(self.DP in metricDict)
		metrics = metricDict[self.DP]
		self.assertEquals(1, len(metrics), \
						"Should contain a single duplicated code metric dict")
		self.assertTrue(self.DP + "0" in metrics)
		metrics = metrics[self.DP + "0"]
		val = self.duplicatedcodes.calculateANrSL(self.toParse)
		self.assertEquals(val, metrics['ANrSL'])

class ForTestersOnlysTest(RootTest):
	def setUp(self):
		self.fortestersonlys = ForTestersOnlys()

	def testParse(self):
		toParse = ["ForTestersOnly","Request.getReferer()","Request.h","111"]
		key = "Request.getReferer()"
		location = ("Request.h", 111)
		self.verifyParse(self.fortestersonlys, toParse, key, location)

class IndentedTestsTest(RootTest):
	def setUp(self):
		self.indentedtests = IndentedTests()
		self.IT = "IndentedTest"
		self.key = "FileTest.testRemove()"
		self.toParse = ["IndentedTest","TC",self.key,"1","FileTest.cc","78"]
		self.location = ("FileTest.cc", 78)

	def testParse(self):
		self.verifyParse(self.indentedtests, self.toParse, self.key, self.location)
		self.assertEquals("TC", self.indentedtests.getType(self.key))
		self.assertEquals(1, self.indentedtests.getCount(self.key))

	def testGenerateNodes(self):
		self.expNode = Node(name="IndentedTest0", color=SMELL_NODE_COLOR, \
							entity="smell", label="IndentedTest", style=3)
		self.verifyGenerateNodes(self.indentedtests, self.expNode)

	def testGenerateEdges(self):
		self.indentedtests.parse(self.toParse)
		edges = self.indentedtests.generateEdges()
		self.expEdge = Edge(node1="IndentedTest0", node2=self.key)

		self.assertEquals(1, len(edges))
		self.assertTrue(self.expEdge in edges)

	def testAppendLocationInfo(self):
		self.verifyAppendLocationInfo(self.indentedtests, "IndentedTest0", self.toParse, self.location)

	def testAppendMetricsInfo(self):
		self.verifyAppendMetricsInfo(self.indentedtests, self.IT, self.toParse, [('NrCS', 1)])

class IndirectTestsTest(RootTest):
	def setUp(self):
		self.indirecttests = IndirectTests()
		self.toParse = ["IndirectTest","BtDependencyTest.testResolve()","4","5","BtDependencyTest.cc","57"]

	def testParse(self):
		key = "BtDependencyTest.testResolve()"
		location = ("BtDependencyTest.cc", 57)
		self.verifyParse(self.indirecttests, self.toParse, key, location)
		self.assertEquals(4, self.indirecttests.getTreshold(key))
		self.assertEquals(5, self.indirecttests.getNPTU(key))

	def testAppendMetricsInfo(self):
		self.verifyAppendMetricsInfo(self.indirecttests, 'IndirectTest', self.toParse, [('NPTU', 5)])

class EagerTestsTest(RootTest):
	def setUp(self):
		self.eagertests = EagerTests()
		self.toParse = ["EagerTest","FooTest.testFoo()", "5", "6","FooTest.java","89"]
		self.location = ("FooTest.java", 89)

	def testParse(self):
		key = "FooTest.testFoo()"
		self.verifyParse(self.eagertests, self.toParse, key, self.location)
		self.assertEquals(5, self.eagertests.getTreshold(key))
		self.assertEquals(6, self.eagertests.getPTMI(key))

	def setupExpNode(self):
		self.expNode = Node(name="EagerTest0", color=SMELL_NODE_COLOR, \
							entity="smell", label="EagerTest", style=3)

	def testGenerateNodes(self):
		self.setupExpNode()
		self.verifyGenerateNodes(self.eagertests, self.expNode)

	def testGenerateMultiNodes(self):
		self.setupExpNode()
		self.toParse2 = ["EagerTest","FuuTest.testFuu()", "5", "8","FuuTest.java","90"]
		self.expNode2 = Node(name="EagerTest1", color=SMELL_NODE_COLOR,\
							entity="smell", label="EagerTest", style=3)
		self.verifyGenerateMultiNodes(self.eagertests, self.expNode, self.expNode2, self.toParse, self.toParse2)

	def testAppendLocationInfo(self):
		self.verifyAppendLocationInfo(self.eagertests, "EagerTest0", self.toParse, self.location)

	def testAppendMetricsInfo(self):
		self.verifyAppendMetricsInfo(self.eagertests, 'EagerTest', self.toParse, [('PTMI', 6)])

class SensitiveEqualitysTest(RootTest):
	def setUp(self):
		self.sensitiveequalitys = SensitiveEqualitys()
		self.toParse = ["SensitiveEquality", "TC", "BtInterestedMessageTest.testToString()", "1", "BtInterestedMessageTest.cc", "27"]
	
	def testParse(self):
		key = "BtInterestedMessageTest.testToString()"
		location = ("BtInterestedMessageTest.cc", 27)
		self.verifyParse(self.sensitiveequalitys, self.toParse, key, location)
		self.assertEquals(1, self.sensitiveequalitys.getCount(key))
		self.assertEquals("TC", self.sensitiveequalitys.getType(key))

	def testAppendMetricInfo(self):
		self.verifyAppendMetricsInfo(self.sensitiveequalitys, "SensitiveEquality", self.toParse, [('NrTS', 1)])

	def testAppendMetricInfo2(self):
		self.toParse[3] = "10"
		self.verifyAppendMetricsInfo(self.sensitiveequalitys, "SensitiveEquality", self.toParse, [('NrTS', 10)])


class GeneralFixturesTest(RootTest):
	def setUp(self):
		self.generalfixtures = GeneralFixtures()
		self.toParse = ["GeneralFixture","DefaultPackageTest",
						"5","6","10","DefaultPackageTest.java","0"]
		
	def testParse(self):
		self.key = "DefaultPackageTest"
		self.location = ("DefaultPackageTest.java", 0)
		self.verifyParse(self.generalfixtures, self.toParse, self.key, self.location)
		self.assertEquals(5, self.generalfixtures.getNFOB(self.key))
		self.assertEquals(6, self.generalfixtures.getNFPT(self.key))
		self.assertEquals(10, self.generalfixtures.getNOBU(self.key))

	def testAppendMetricInfo(self):
		self.verifyAppendMetricsInfo(self.generalfixtures, "GeneralFixture", self.toParse, \
									 [('NFOB', 5), ('NFPT', 6), ('NOBU', 10)])

	def testAppendMetricInfo2(self):
		self.toParse[2] = "8"
		self.toParse[3] = "3"
		self.toParse[4] = "20"
		self.verifyAppendMetricsInfo(self.generalfixtures, "GeneralFixture", self.toParse, \
									 [('NFOB', 8), ('NFPT', 3), ('NOBU', 20)])

class MysteryGuestsTest(TestCase):
	def setUp(self):
		self.mysteryguests = MysteryGuests()
		self.command = "MysteryTest.testSomething()"
		self.commandLocation = ("MysteryTest.java", 5)
		self.target = "MyOtherProductionClass.myOtherMethod(String)"
		self.targetLocation = ("MyOtherProductionClass.java",6)
		self.toParse=["MysteryGuest", self.command , "MysteryTest.java", "5",\
					 self.target, "MyOtherProductionClass.java","6","file",\
					 "java::io::BufferedReader.readLine()"]

	def testParse(self):
		self.mysteryguests.parse(self.toParse)

	def testAppendLocationInfo(self):
		self.mysteryguests.parse(self.toParse)
		srcDict = {}
		self.mysteryguests.appendLocationInfo(srcDict)
		self.assertEquals(1, len(srcDict))
		self.assertTrue("MysteryGuest0" in srcDict)
		self.assertTrue(2, srcDict["MysteryGuest0"])
		val = srcDict["MysteryGuest0"]
		self.assertTrue(self.commandLocation in val)
		self.assertTrue(self.targetLocation in val)

	#def testGenLocationDirectMystery(self):
		#toParse = self.toParse
		#toParse[4] = self.command
		#toParse[5] = self.commandLocation[0]
		#toParse[6] = str(self.commandLocation[1])
		#self.mysteryguests.parse(toParse)
		#locations = self.mysteryguests.generateLocationNodes()
		#self.assertEqual(1, len(locations))
		#self.assertTrue(self.expCmdLocation in locations)

	#def testGenLocationComma(self):
		#toParse = self.toParse
		#toParse[1] = "MysteryTest.someHelper(String,Integer)"
		#self.mysteryguests.parse(toParse)
		#locations = self.mysteryguests.generateLocationNodes()
		#expLocation = LocationNode("srcloc_MysteryTest.someHelper(String;Integer)", self.commandLocation)
		#self.assertEquals(2, len(locations))
		#print locations[0]
		#print expLocation
		#self.assertTrue(expLocation in locations)

class NodeTest(TestCase):
	def testConstructor(self):
		node = Node(name="TestFoo", color="black", entity="testcase", label="TestFoo", style=1)
		self.assertEquals("TestFoo", node.name)
		self.assertEquals("black", node.color)
		self.assertEquals("testcase", node.entity)
		self.assertEquals("TestFoo", node.label)
		self.assertEquals("1", node.style)

if __name__=='__main__':
	main() # fetches & executes all tests
