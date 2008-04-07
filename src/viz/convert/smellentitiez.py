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

SMELL_STYLE=3

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
            edges.append(Edge(mtd, self.smellName + str(counter)))
            counter += 1
        return edges

    def appendMetricInfoHelper(self, metricDict, metricBuilders):
        # metricBuilders is a list of metricNames with their generator method
        metricDict[self.smellName] = {}
        smellMtr = metricDict[self.smellName]
        cnt = 0
        for entity in self.dict:
            key = self.smellName + str(cnt)
            smellMtr[key] = {}
            for metric, gen in metricBuilders:
                smellMtr[key][metric] = gen(entity)
            cnt += 1


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

    #def appendMetricInfo(self, metricDict):
        #cnt = 0
        #metricDict["AssertionRoulette"] = {}
        #arMtr = metricDict["AssertionRoulette"]
        #for entity in self.dict:
            #key = 'AssertionRoulette' + str(cnt)
            #arMtr[key] = {'NrA' : self.getTotalAsserts(entity), \
                          #'NrDA' : self.getDescriptionlessAsserts(entity)}
            #cnt += 1

    def appendMetricInfo(self, metricDict):
        metricDict['Translation']['NrA'] = "Number of assert invocations"
        metricDict['Translation']['NrDA'] = "Number of Descriptionless assert invocations"
        self.appendMetricInfoHelper(metricDict, [('NrA', self.getTotalAsserts), \
                                                 ('NrDA', self.getDescriptionlessAsserts)]) # Number of Control Structures

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

    def appendMetricInfo(self, metricDict):
        metricDict['Translation']['NrCS'] = "Number of control structures"
        self.appendMetricInfoHelper(metricDict, [('NrCS', self.getCount)]) # Number of Control Structures

class SensitiveEqualitys(TypedSmell):
    def __init__(self):
        TypedSmell.__init__(self, "SensitiveEquality")

    def appendMetricInfo(self, metricDict):
        metricDict['Translation']['NrTS'] = "Number of toString invocations"
        self.appendMetricInfoHelper(metricDict, [('NrTS', self.getCount)]) # Number of toStrings

class SingleMetricSmellz(Smellz):
    def __init__(self, smellName):
        Smellz.__init__(self, smellName)
        
    def parse(self, toParse):
        """
        input is a splitted indirectests dump line, eg
        ["IndirectTest","BtDependencyTest.testResolve()","4","5","BtDependencyTest.cc","57"]

        pre:
            toParse[0] == self.smellName
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
    
    def getMetric(self, testMethod):
        """
        pre:
            self.hasKey(testMethod)
        """
        return self.dict[testMethod][2]

class IndirectTests(SingleMetricSmellz):
    def __init__(self):
        SingleMetricSmellz.__init__(self, "IndirectTest")

    def getNPTU(self, testMethod):
        return self.getMetric(testMethod)

    def appendMetricInfo(self, metricDict):
        metricDict['Translation']['NPTU'] = "Number of production type uses"
        self.appendMetricInfoHelper(metricDict, [('NPTU', self.getNPTU)]) # Number of Production Type Uses

class EagerTests(SingleMetricSmellz):
    def __init__(self):
        SingleMetricSmellz.__init__(self, "EagerTest")

    def getPTMI(self, testMethod):
        return self.getMetric(testMethod)

    def appendMetricInfo(self, metricDict):
        metricDict['Translation']['PTMI'] = "Number of production method invocations"
        self.appendMetricInfoHelper(metricDict, [('PTMI', self.getPTMI)]) # Number of Production Type Method Invocations

class MysteryGuests(object):
    def __init__(self):
        self.mysteries = []

    def generateEdges(self):
        edges = []
        counter = 0
        for mystery in self.mysteries:
            edges.append(Edge(mystery[1], "MysteryGuest" + str(counter)))
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
            srcDict["MysteryGuest" + str(counter)] = [(mystery[2], int(mystery[3])), (mystery[5], int(mystery[6]))]
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

    def appendMetricInfo(self, metricDict):
        pass

class GeneralFixtures(Smellz):
    def __init__(self):
        Smellz.__init__(self, "GeneralFixture")

    def parse(self, toParse):
        """ ["GeneralFixture","DefaultPackageTest",
                        "5","6","10","DefaultPackageTest.java","0"]"""
        self.dict[toParse[1]] = [(toParse[-2], int(toParse[-1])), int(toParse[2]), int(toParse[3]), int(toParse[4])]

    def getNFOB(self, testcase):
        return self.dict[testcase][1]

    def getNFPT(self, testcase):
        return self.dict[testcase][2]

    def getNOBU(self, testcase):
        return self.dict[testcase][3]

    def appendMetricInfo(self, metricDict):
        metricDict['Translation']['NFOB'] = "Number of fixture objects"
        metricDict['Translation']['NFPT'] = "Number of fixture production types"
        metricDict['Translation']['NOBU'] = "Number of object uses"
        self.appendMetricInfoHelper(metricDict, [('NFOB', self.getNFOB),\
                                                 ('NFPT', self.getNFPT),\
                                                 ('NOBU', self.getNOBU)])

class DuplicatedCodes(object):
    def __init__(self):
        self.duplicates = []
        self.smellName = "DuplicatedCode"

    def generateEdges(self):
        edges = []
        counter = 0
        for duplicate in self.duplicates:
            processedMtds = set()
            for i in range(1, len(duplicate), 4):
                currMtd = filterComma(duplicate[i])
                if currMtd in processedMtds: continue
                edges.append(Edge(currMtd, self.smellName + str(counter)))
                processedMtds.add(currMtd)
            counter += 1
        return edges

    def generateNodes(self):
        nodes = []
        counter = 0
        for duplicate in self.duplicates:
            nodes.append(Node(name=self.smellName + str(counter),\
                         color="red", label=self.smellName, \
                         entity="smell", style=SMELL_STYLE ))
            counter += 1
        return nodes

    def appendLocationInfo(self, srcDict):
        counter = 0
        for duplicate in self.duplicates:
            srcDict[self.smellName + str(counter)] = []
            entry = srcDict[self.smellName + str(counter)]
            for i in range(2, len(duplicate), 4):
                entry.append((duplicate[i], int(duplicate[i+1]), int(duplicate[i+2])))
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

    def calculateANrSL(self, duplicate):
        ''' Average Number of Sourcelines '''
        total = 0
        for i in range(3, len(duplicate), 4):
                total += int(duplicate[i+1]) - int(duplicate[i]) + 1
        return total / (len(duplicate)/4)

    def appendMetricInfo(self, metricDict):
        metricDict['Translation']['ANrSL'] = "Average number of source lines"
        metricDict[self.smellName] = {}
        smellMtr = metricDict[self.smellName]
        cnt = 0
        for duplicate in self.duplicates:
            key = self.smellName + str(cnt)
            smellMtr[key] = {}
            smellMtr[key]['ANrSL'] = self.calculateANrSL(duplicate)
            cnt += 1
