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
# Copyright 2007 Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>
#

def isSourceLine(toCheck):
    """ verify if toCheck is a legal source line number
        line numbers should be integers and strict positive """
    try:
        int(toCheck)
        return int(toCheck) > 0

    except:
        return False

SMELL_STYLE=3

def isCount(toCheck):
    """ verify if toCheck is a legal count
        counts should be integers and positive """
    try:
        int(toCheck)
        return int(toCheck) >= 0

    except:
        return False

def filterComma(toFilter):
    return toFilter.replace(',', ';')

class SourceEntityz(object):
    def __init__(self):
        self.dict = dict() # { qualName x [(file, line), ... ] }

    def hasKey(self, qualifiedName):
        return self.dict.has_key(qualifiedName)

    def getLocation(self, qualifiedName):
        """
        pre:
            self.hasKey(qualifiedName)
        """
        return self.dict[qualifiedName][0]

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

class Smellz(SourceEntityz):
    def __init__(self, smellName):
        SourceEntityz.__init__(self)
        self.smellName = smellName
        self.nodeColor = "red"
        self.entityType = "smell"
        self.style = SMELL_STYLE

    def generateNodes(self):
        nodes = []
        counter = 0
        for entity in self.dict:
            entity = filterComma(entity)
            nodes.append(Node(name=self.smellName + str(counter), color=self.nodeColor, \
                            label=self.smellName, entity=self.entityType, \
                            style=self.style ))
            counter += 1
        return nodes

    def appendLocationInfo(self, srcDict):
        counter = 0
        for entity in self.dict:
            srcDict[self.smellName + str(counter)] = [self.dict[entity][0]]
            counter += 1

    def generateEdges(self):
        edges = []
        counter = 0
        for mtd in self.dict:
            mtd = filterComma(mtd)
            edges.append(Edge(self.smellName + str(counter), mtd))
            counter += 1
        return edges

class BasicSmellz(Smellz):
    def __init__(self, smellName):
        Smellz.__init__(self, smellName)

    def parse(self, toParse):
        """
        input is a splitted assertionless dump line, eg
        ["AssertionLess", "FooTest.testFoo()", "FooTest.java", "25"]

        pre:
            toParse[0] == self.smellName
            len(toParse) == 4
            isSourceLine(toParse[3])

        post:
            self.hasKey(toParse[1])
            self.getLocation(toParse[1]) == (toParse[2], int(toParse[3]))
        """
        self.dict[toParse[1]] = [(toParse[2], int(toParse[3]))]

class AssertionLesss(BasicSmellz):
    def __init__(self):
        BasicSmellz.__init__(self, "AssertionLess")

class ForTestersOnlys(BasicSmellz):
    def __init__(self):
        BasicSmellz.__init__(self, "ForTestersOnly")

    def generateEdges(self):
        # disable the edges, temporarily since guess crashes on non-existant nodes
        return []

class AssertionRoulettes(Smellz):
    def __init__(self):
        Smellz.__init__(self, "AssertionRoulette")
        self.nodeColor = "red"
        self.entityType = "smell"
        self.style = SMELL_STYLE

    def parse(self, toParse):
        """
        input is a splitted assertionroulette dump line, eg
        [ "AssertionRoulette", "TC", "FooTest.testFoo()", "10", "8", "FooTest.java", "228"]

        pre:
            toParse[0] == "AssertionRoulette"
            len(toParse) == 7
            isSourceLine(toParse[6])
            isCount(toParse[3])
            isCount(toParse[4])
            int(toParse[3]) >= int(toParse[4])

        post:
            self.hasKey(toParse[2])
            self.getLocation(toParse[2]) == (toParse[5], int(toParse[6]))
            isCount(self.getTotalAsserts(toParse[2]))
            isCount(self.getDescriptionlessAsserts(toParse[2]))
        """
        self.dict[toParse[2]] = [(toParse[5], int(toParse[6])), int(toParse[3]), int(toParse[4])]
    
    def getTotalAsserts(self, key):
        """
        key is a qualified testmethod name with asertionroulette smell
        pre:
            self.hasKey(key)
        """
        return self.dict[key][1]
    
    def getDescriptionlessAsserts(self, key):
        """
        key is a qualified testmethod name with asertionroulette smell
        pre:
            self.hasKey(key)
        """
        return self.dict[key][2]

class TypedSmell(Smellz):
    def __init__(self, smellName):
        Smellz.__init__(self, smellName)
        self.smellName = smellName
        self.nodeColor = "red"
        self.entityType = "smell"
        self.style = SMELL_STYLE
    
    def parse(self, toParse):
        """
        input is a splitted indentedtest dump line, eg
        ["IndentedTest","TC","FileTest.testRemove()","1","FileTest.cc","78"]

        pre:
            toParse[0] == self.smellName
            len(toParse) == 6
            isSourceLine(toParse[5])
            isCount(toParse[3])
            toParse[1] in ["TC", "TH"]

        post:
            self.hasKey(toParse[2])
            self.getLocation(toParse[2]) == (toParse[4], int(toParse[5]))
            self.getType(toParse[2]) == toParse[1]
            self.getCount(toParse[2]) == int(toParse[3])
        """
        self.dict[toParse[2]] = [(toParse[4], int(toParse[5])), int(toParse[3]), toParse[1]]

    def getType(self, testMethod):
        """
        pre:
            self.hasKey(testMethod)
        """
        return self.dict[testMethod][2]
    
    def getCount(self, testMethod):
        """
        pre:
            self.hasKey(testMethod)
        """
        return self.dict[testMethod][1]

class IndentedTests(TypedSmell):
    def __init__(self):
        TypedSmell.__init__(self, "IndentedTest")

class SensitiveEqualitys(TypedSmell):
    def __init__(self):
        TypedSmell.__init__(self, "SensitiveEquality")

class IndirectTests(Smellz):
    def __init__(self):
        Smellz.__init__(self, "IndirectTest")
        self.nodeColor = "red"
        self.entityType = "smell"
        self.style = SMELL_STYLE

    def parse(self, toParse):
        """
        input is a splitted indirectests dump line, eg
        ["IndirectTest","BtDependencyTest.testResolve()","4","5","BtDependencyTest.cc","57"]

        pre:
            toParse[0] == "IndirectTest"
            len(toParse) == 6
            isSourceLine(toParse[5])
            isCount(toParse[2])
            isCount(toParse[3])

        post:
            self.hasKey(toParse[1])
            self.getLocation(toParse[1]) == (toParse[4], int(toParse[5]))
            self.getTreshold(toParse[1]) == int(toParse[2])
            self.getNPTU(toParse[1]) == int(toParse[3])
        """
        self.dict[toParse[1]] = [(toParse[4], int(toParse[5])), int(toParse[2]), int(toParse[3])]

    def getTreshold(self, testMethod):
        """
        pre:
            self.hasKey(testMethod)
        """
        return self.dict[testMethod][1]
    
    def getNPTU(self, testMethod):
        """
        pre:
            self.hasKey(testMethod)
        """
        return self.dict[testMethod][2]

class MysteryGuests(object):
    def __init__(self):
        self.mysteries = []

    def generateEdges(self):
        edges = []
        counter = 0
        for mystery in self.mysteries:
            edges.append(Edge("MysteryGuest" + str(counter), mystery[1]))
            counter += 1
        return edges

    def generateNodes(self):
        nodes = []
        counter = 0
        for mystery in self.mysteries:
            nodes.append(Node(name="MysteryGuest" + str(counter),\
                         color="black", label="MysteryGuest", \
                         entity="smell", style=SMELL_STYLE ))
            counter += 1
        return nodes

    def appendLocationInfo(self, srcDict):
        counter = 0
        for mystery in self.mysteries:
            srcDict["MysteryGuest" + str(counter)] = [(mystery[2], mystery[3]), mystery[5], mystery[6]]
            counter += 1

    def __isDirectMystery(self, mystery):
        # a mystery guest is direct if the blacklisted method gets called
        # in the testcommand/helper/setup.
        return (mystery[2] == mystery[5]) and (mystery[3] == mystery[6])


    def parse(self, toParse):
        """
        input is a splitted mysteryguest dump line eg
        ["MysteryGuest",
         "MysteryTest.testSomething()", "MysteryTest.java", "5",\
         "MyOtherProductionClass.myOtherMethod(String)","MyOtherProductionClass.java","6",\
         "file","java::io::BufferedReader.readLine()"]
        
        pre:
            toParse[0] == "MysteryGuest"
            isSourceLine(toParse[3])
            isSourceLine(toParse[6])
        """
        self.mysteries.append(toParse)

class DuplicatedCodes(object):
    def __init__(self):
        self.duplicates = []

    def generateEdges(self):
        edges = []
        counter = 0
        for duplicate in self.duplicates:
            processedMtds = set()
            for i in range(1, len(duplicate), 4):
                currMtd = filterComma(duplicate[i])
                if currMtd in processedMtds: continue
                edges.append(Edge("DuplicatedCode" + str(counter), currMtd))
                processedMtds.add(currMtd)
            counter += 1
        return edges

    def generateNodes(self):
        nodes = []
        counter = 0
        for duplicate in self.duplicates:
            nodes.append(Node(name="DuplicatedCode" + str(counter),\
                         color="red", label="DuplicatedCode", \
                         entity="smell", style=SMELL_STYLE ))
            counter += 1
        return nodes

    def appendLocationInfo(self, srcDict):
        counter = 0
        for duplicate in self.duplicates:
            srcDict["DuplicatedCode" + str(counter)] = []
            entry = srcDict["DuplicatedCode" + str(counter)]
            for i in range(2, len(duplicate), 4):
                entry.append((duplicate[i], int(duplicate[i+1])))
            counter += 1

    def parse(self, toParse):
        """
        input is a splitted duplicatedcode dump line, eg
        ["DuplicatedCode", \
            "FooTest.testFoo()", "FooTest.java", "197", "206", 
            "BazTest.testBaz()", "BazTest.java", "183", "193"]
        pre:
            toParse[0] == "DuplicatedCode"
            len(toParse) % 4  == 1
            forall(isSourceLine(i) for i in range(3,len(toParse),3))
            forall(isSourceLine(i) for i in range(4,len(toParse),3))

        """
        self.duplicates.append(toParse)

colors = ['black', 'lightgray', 'red']

class Node():
    def __init__(self, name, color, entity, label, style):
        """
        pre:
            isinstance(name, str)
            color in colors
            isinstance(entity, str)
            isinstance(label, str)
            isinstance(style, int)
        """
        self.name = name
        self.color = color
        self.entity = entity
        self.label = label
        self.style = str(style)

    def __eq__(self, other):
        if self is other: return True
        if other is None: return False
        if not (isinstance(other, Node)): return False
        if self.name == other.name and\
            self.color == other.color and\
            self.entity == other.entity and\
            self.label == other.label and\
            self.style == other.style:
            return True
        return False

    def __str__(self):
        return "Node[" + self.name + "," + self.color + "," + \
                self.entity + "," + self.label + "," + self.style + "]"

    def write(self, output):
        output.write(self.name + "," + self.color + "," + self.entity + "," +\
                     self.label + "," + self.style + "\n")

    def writeHeader(output):
        output.write('nodedef>name,color,entity VARCHAR(32),label,style\n')
    writeHeader = staticmethod(writeHeader)

class Edge():
    def __init__(self, node1, node2, directed="false", visible="true", color="lightgray"):
        self.node1 = node1
        self.node2 = node2
        self.directed = directed
        self.visible = visible
        self.color = color

    def __eq__(self, other):
        if self is other: return True
        if other is None: return False
        if not (isinstance(other, Edge)): return False
        if self.node1 == other.node1 and\
            self.node2 == other.node2 and\
            self.directed == other.directed and\
            self.visible == other.visible and\
            self.color == other.color:
            return True
        return False

    def __str__(self):
        return "Edge[" + self.node1 + "," + self.node2 + "," +\
                self.directed + "," + self.visible + "," +\
                self.color + "]"

    def write(self, output):
        output.write(self.node1 + "," + self.node2 + "," +\
                     self.directed + "," + self.visible + "," +\
                     self.color + "\n")

    def writeHeader(output):
        output.write('edgedef>node1,node2,directed,visible,color\n')
    writeHeader = staticmethod(writeHeader)

import inspect
import cPickle

def isDumpEntityCollection(object):
    """ introspection predicate to filter only those classes
        of this module which are entity dump processors """
    return inspect.isclass(object) and\
            object.__name__.endswith('s') and\
            hasattr(object, 'parse')

class EntityConverter():
    def __init__(self):
        me = __import__(__name__) # this module
        self.entities = {} # instantations of all test entites & smells
                           # with their classname as key
        for entity in inspect.getmembers(me, isDumpEntityCollection):
            # introspect all processor classes
            # eg TestCases, TestCommands, AssertionRoulettes etc
            self.entities[entity[0]] = entity[1]()

    def readDump(self, filename):
        dump = open(filename, 'r')
        for line in dump:
            splitted = [filterComma(elem.strip('"')) for elem in line.split('\t')]
            if splitted[-1] == '\n': splitted = splitted[:-1]
            #dispatch
            try: self.entities[splitted[0] + "s"].parse(splitted)
            except KeyError, e: 
                print "keyerror: ",
                print e
        dump.close()

    def __writeSpacer(self, output):
        output.write(5*'\n')

    def __generateGdfPart(self, generator, output):
        for entity in self.entities.itervalues():
            for gdfEntity in getattr(entity, generator)():
                gdfEntity.write(output)

    def writeGdf(self, filename):
        output = open(filename, 'w')

        Node.writeHeader(output)
        self.__generateGdfPart('generateNodes', output)
        self.__writeSpacer(output)

        Edge.writeHeader(output)
        self.__generateGdfPart('generateEdges', output)
        output.close()

    def writeSourceLocationInfo(self, filename, root):
        output = open(filename, 'wb')
        srcDict = {}
        srcDict['ProjectSourceRootDirectory'] = root
        for entity in self.entities.itervalues():
            entity.appendLocationInfo(srcDict)
        cPickle.dump(srcDict, output)
        output.close()

#import os, sys
#sys.path.append(os.environ['TSMELLS']+ "/src/contract")
#import contract
#contract.checkmod(__name__)

import sys, os

def usage():
    print "usage " + sys.argv[0] + " <tsmell-dump> [<src-root>]"
    print "\t<tsmell-dump>:\ttest smell dump as generated by rsf2tsmell.py"
    print "\t<src-root>:\tproject source directory."

if __name__=='__main__':
    if len(sys.argv) < 1:
        usage()
        sys.exit(-1)
    root = ""

    smellDump = sys.argv[1]
    if len(sys.argv) > 2:
        root = os.path.abspath(sys.argv[2])
        if root[-1] != '/': root = root + '/'

    converter = EntityConverter()
    converter.readDump(smellDump)
    prefix = smellDump[0:smellDump.rfind('.')]
    converter.writeGdf( prefix + ".gdf")
    converter.writeSourceLocationInfo( prefix + ".srcloc", root)
