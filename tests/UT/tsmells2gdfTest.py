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
from cStringIO import StringIO as iostr
sys.path.append("../../src")
sys.path.append("../../src/contract")
from tsmells2gdf import *

class RootTest(unittest.TestCase):
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

	def testGenerateLocationNodes(self):
		self.cases.parse(self.toParse)
		locations = self.cases.generateLocationNodes()
		self.expLocation = LocationNode("SRC_" + "TestCases0", self.location)

		self.assertEquals(1, len(locations))
		self.assertTrue(self.expLocation in locations)


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
		self.expNode = Node(name="AssertionLess0", color="black",\
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
		
	def testParse(self):
		toParse = [ "AssertionRoulette", "TC", "FooTest.testFoo()", "10", "8", "FooTest.java", "228"]
		key = "FooTest.testFoo()"
		location = ("FooTest.java", 228)
		self.verifyParse(self.assertionroulettes, toParse, key, location)
		self.assertEquals(10, self.assertionroulettes.getTotalAsserts(key))
		self.assertEquals(8, self.assertionroulettes.getDescriptionlessAsserts(key))

class DuplicatedCodesTest(unittest.TestCase):
	def setUp(self):
		self.duplicatedcodes = DuplicatedCodes()
		self.mtd1 = "BtPieceMessageTest.testChokingEvent_inAmAllowedIndexSet()"
		self.mtd2 = "BtPieceMessageTest.testChokingEvent_somwhere()"
		self.dupLoc1 = ("BtPieceMessageTest.cc", 197)
		self.dupLoc2 = ("BtPieceMessageTest.cc", 183)
		self.toParse = ["DuplicatedCode",\
					self.mtd1, "BtPieceMessageTest.cc", "197", "206", 
					self.mtd2, "BtPieceMessageTest.cc", "183", "193"]

	def testParse(self):
		self.duplicatedcodes.parse(self.toParse)

	def testGenerateLocationNodes(self):
		self.duplicatedcodes.parse(self.toParse)
		locations = self.duplicatedcodes.generateLocationNodes()
		dupLocNode1 = LocationNode("SRC_DuplicatedCodes0", self.dupLoc1)
		dupLocNode2 = LocationNode("SRC_DuplicatedCodes1", self.dupLoc2)

		self.assertEquals(2, len(locations))
		
		self.assertTrue(dupLocNode1 in locations)
		self.assertTrue(dupLocNode2 in locations)

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
		self.key = "FileTest.testRemove()"
		self.toParse = ["IndentedTest","TC",self.key,"1","FileTest.cc","78"]
		self.location = ("FileTest.cc", 78)

	def testParse(self):
		self.verifyParse(self.indentedtests, self.toParse, self.key, self.location)
		self.assertEquals("TC", self.indentedtests.getType(self.key))
		self.assertEquals(1, self.indentedtests.getCount(self.key))

	def testGenerateNodes(self):
		self.expNode = Node(name="IndentedTest0", color="black", entity="smell",\
						label="IndentedTest", style=3)
		self.verifyGenerateNodes(self.indentedtests, self.expNode)

	def testGenerateEdges(self):
		self.indentedtests.parse(self.toParse)
		edges = self.indentedtests.generateEdges()
		self.expEdge = Edge(node1="IndentedTest0", node2=self.key)

		self.assertEquals(1, len(edges))
		self.assertTrue(self.expEdge in edges)

	def testGenerateLocationNodes(self):
		self.indentedtests.parse(self.toParse)
		locations = self.indentedtests.generateLocationNodes()
		self.expLocation = LocationNode('SRC_' + "IndentedTests0", self.location)

		self.assertEquals(1, len(locations))
		self.assertTrue(self.expLocation in locations)

class IndirectTestsTest(RootTest):
	def setUp(self):
		self.indirecttests = IndirectTests()

	def testParse(self):
		toParse = ["IndirectTest","BtDependencyTest.testResolve()","4","5","BtDependencyTest.cc","57"]
		key = "BtDependencyTest.testResolve()"
		location = ("BtDependencyTest.cc", 57)
		self.verifyParse(self.indirecttests, toParse, key, location)
		self.assertEquals(4, self.indirecttests.getTreshold(key))
		self.assertEquals(5, self.indirecttests.getNPTU(key))

class SensitiveEqualitysTest(RootTest):
	def setUp(self):
		self.sensitiveequalitys = SensitiveEqualitys()
	
	def testParse(self):
		toParse = ["SensitiveEquality", "TC", "BtInterestedMessageTest.testToString()", "1", "BtInterestedMessageTest.cc", "27"]
		key = "BtInterestedMessageTest.testToString()"
		location = ("BtInterestedMessageTest.cc", 27)
		self.verifyParse(self.sensitiveequalitys, toParse, key, location)
		self.assertEquals(1, self.sensitiveequalitys.getCount(key))
		self.assertEquals("TC", self.sensitiveequalitys.getType(key))

class MysteryGuestsTest(unittest.TestCase):
	def setUp(self):
		self.mysteryguests = MysteryGuests()
		self.command = "MysteryTest.testSomething()"
		self.commandLocation = ("MysteryTest.java", 5)
		self.target = "MyOtherProductionClass.myOtherMethod(String)"
		self.targetLocation = ("MyOtherProductionClass.java",6)
		self.toParse=["MysteryGuest", self.command , "MysteryTest.java", "5",\
					 self.target, "MyOtherProductionClass.java","6","file",\
					 "java::io::BufferedReader.readLine()"]
		self.expCmdLocation = LocationNode("SRC_MysteryGuests0", self.commandLocation)

	def testParse(self):
		self.mysteryguests.parse(self.toParse)

	def testGenerateLocationNodes(self):
		self.mysteryguests.parse(self.toParse)
		locations = self.mysteryguests.generateLocationNodes()
		self.expTargetLocation = LocationNode("SRC_MysteryGuests1", self.targetLocation)
		self.assertEqual(2, len(locations))
		self.assertTrue(self.expCmdLocation in locations)
		self.assertTrue(self.expTargetLocation in locations)

	def testGenLocationDirectMystery(self):
		toParse = self.toParse
		toParse[4] = self.command
		toParse[5] = self.commandLocation[0]
		toParse[6] = str(self.commandLocation[1])
		self.mysteryguests.parse(toParse)
		locations = self.mysteryguests.generateLocationNodes()
		self.assertEqual(1, len(locations))
		self.assertTrue(self.expCmdLocation in locations)

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

class NodeTest(unittest.TestCase):
	def testConstructor(self):
		node = Node(name="TestFoo", color="black", entity="testcase", label="TestFoo", style=1)
		self.assertEquals("TestFoo", node.name)
		self.assertEquals("black", node.color)
		self.assertEquals("testcase", node.entity)
		self.assertEquals("TestFoo", node.label)
		self.assertEquals("1", node.style)

class LocationNodeTest(unittest.TestCase):
	def setUp(self):
		self.name = "srcloc_" + "FooTest"
		self.srcloc = ("TestFoo.java", 2)
		self.locNode = LocationNode(name = self.name, srcloc=self.srcloc)

	def testConstructor(self):
		self.assertEquals(self.name, self.locNode.name)
		self.assertEquals(self.srcloc, self.locNode.srcloc)

	def testWrite(self):
		output = iostr()
		self.locNode.write(output)
		expected = self.name + ",location," + self.srcloc[0] + "," +\
					str(self.srcloc[1]) + ",0\n"
		self.assertEquals(expected, output.getvalue())

unittest.main()
