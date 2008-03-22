#!/usr/bin/python
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
# Copyright 2007-2008 Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>
#

from entityz import *
from gdfelements import Node, Edge

class TestEntitiez(SourceEntityz):
	def __init__(self):
		SourceEntityz.__init__(self)
	
	def generateNodes(self):
		nodes = []
		for entity in self.dict:
			entity = filterComma(entity)
			nodes.append(Node(name=entity, color=self.nodeColor, \
							label=self.makeLabel(entity), entity=self.entityType, \
							style=self.style ))
		return nodes

	def appendLocationInfo(self, srcDict):
		for entity in self.dict:
			entity = filterComma(entity)
			srcDict[entity] = [self.dict[entity][0]]

class TestCases(TestEntitiez):
	def __init__(self):
		TestEntitiez.__init__(self)
		self.nodeColor = "black"
		self.entityType = "testcase"
		self.style = 1 # square
		self.pkgDict = {}
		self.pkgs = set()

	def makeLabel(self, name):
		return name

	def generateNodes(self):
		nodes = TestEntitiez.generateNodes(self)
		for pkg in self.pkgs:
			if not pkg: continue
			nodes.append(Node(name=pkg, color=self.nodeColor, \
							label=pkg, entity="package",\
							style=self.style))
		return nodes

	def parse(self, toParse):
		"""
		input is a splitted testcase line, eg
			["TestCase","FooTest","FooTest.java","2"]

		pre:
			toParse[0] == "TestCase"
			len(toParse) == 4
			isSourceLine(toParse[3])

		post:
			self.hasKey(toParse[1])
			self.getLocation(toParse[1]) == (toParse[2], int(toParse[3]))
		"""
		self.dict[toParse[1]] = [(toParse[2], int(toParse[3]))]
		self.__addPackage(toParse[1])

	def __addPackage(self, clazz):
		if clazz.rfind('::') == -1: return
		pkg = clazz[0:clazz.rfind("::")]
		if not pkg: return
		if not self.pkgDict.has_key(pkg):
			self.pkgDict[pkg] = []
		self.pkgDict[pkg].append(clazz)

		splittedPkg = pkg.split('::')

		for i in range(1,len(splittedPkg)+1):
			self.pkgs.add("::".join(splittedPkg[0:i]))

	def generateEdges(self):
		edges = []
		for pkg, cases in self.pkgDict.iteritems():
			for case in cases:
				edges.append(Edge(case, pkg))
		return edges


class TestMethodz(TestEntitiez):
	""" TestCommands, Helpers & Fixture """

	def __init__(self, typeName):
		TestEntitiez.__init__(self)
		self.typeName = typeName
		self.nodeColor = "black"
		self.style = 2 # circle

	def parse(self, toParse):
		"""
		input is a splitted testcommand line, eg
		["TestCommand", "FooTest.testFoo()", "FooTest.java", "10", "FooTest"]

		pre:
			toParse[0] == self.typeName
			len(toParse) == 5
			isSourceLine(toParse[3])

		post:
			self.hasKey(filterComma(toParse[1]))
			self.getLocation(filterComma(toParse[1])) == (toParse[2], int(toParse[3]))
			self.getOwner(filterComma(toParse[1])) == toParse[4]
		"""
		self.dict[filterComma(toParse[1])] = [(toParse[2], int(toParse[3])), toParse[4]]

	def generateEdges(self):
		edges = []
		for cmd in self.dict:
			edges.append(Edge(cmd, self.getOwner(cmd)))
		return edges

	def makeLabel(self, name):
		return name.split('.')[1]

	def getOwner(self, qualifiedName):
		"""
		pre:
			self.hasKey(qualifiedName)
		"""
		return self.dict[qualifiedName][1]

class TestCommands(TestMethodz):
	def __init__(self):
		TestMethodz.__init__(self, "TestCommand")
		self.entityType = "testcommand"

class TestHelpers(TestMethodz):
	def __init__(self):
		TestMethodz.__init__(self, "TestHelper")
		self.entityType = "testhelper"

class TestSetups(TestMethodz):
	def __init__(self):
		TestMethodz.__init__(self, "TestSetup")
		self.entityType = "testfixture"

class TestTeardowns(TestMethodz):
	def __init__(self):
		TestMethodz.__init__(self, "TestTeardown")
		self.entityType = "testfixture"
