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

LINE_SEPARATOR="------------------------------------------------------->>"

def printLineSeparator():
    print LINE_SEPARATOR

def printLine(label, value):
    if len(label) > 23:  numTabs = 1
    elif len(label) > 12: numTabs = 2
    else: numTabs = 3
    tabs = "\t" * numTabs
    print label + ":" + tabs + str(value)

def printHeader(what):
    print ""
    printLineSeparator()
    print "--- metrics for " + str(what)
    printLineSeparator()

class CaseMetrics:
    def __init__(self, testcase):
        global glzz # from TMenu.py
        self.tc = testcase # testcase graph node
        self.metrics = glzz.metricDict
        self.mtds = [mtd.getNode2() for mtd in self.tc->(srcentity == 1)]

    def print_(self):
        if not self.tc.entity == 'testcase': return
        printHeader(self.tc.name.split('::')[-1])
        self.__printCodeMetrics()
        printLineSeparator()
        self.__printSmellMetrics()
        printLineSeparator()

    def __printCodeMetrics(self):
        sloc = self.computeSLOC()
        nrofCmds = self.computeNrofCommands()
        nrofMtds = self.computeNrofMethods()

        printLine("SLOC", sloc)
        printLine("#commands", nrofCmds)
        printLine("#helpers", self.computeNrofHelpers())
        if nrofMtds > 0: 
            printLine("SLOC/mtds", float(sloc)/nrofMtds)
            printLine("min(SLOC)", self.computeMinMtdSLOC())
            printLine("max(SLOC)", self.computeMaxMtdSLOC())
        if nrofCmds > 0:
            printLine("SLOC/cmd", float(sloc)/nrofCmds)

    def __printSmellMetrics(self):
        nrofSmells = self.computeNrofSmells()
        nrofMtds = self.computeNrofMethods()

        printLine("#smells", nrofSmells)
        if nrofSmells == 0: return
        if nrofMtds != 0: 
            printLine("#smells/mtd", float(nrofSmells)/nrofMtds)

        smellsGrouped = groupBy(self.smellz, label)
        for group in smellsGrouped:
            printLine("#" + str(group[0].label), len(group))

    def computeSLOC(self):
        sloc = 0
        try:
            sloc = self.metrics['testcase'][self.tc.name]['SLOC']
        except: pass
        return sloc

    def computeNrofCommands(self):
        return len(self.tc->(entity == 'testcommand'))

    def computeNrofHelpers(self):
        return len(self.tc->(entity == 'testhelper'))

    def computeNrofMethods(self):
        # for now only commands, fixture and helpers are included. 
        # con/de-structors and suite() methods are ignored
        return len(self.mtds)

    def computeNrofSmells(self):
        smellz = (entity == 'smell')
        caseSmells = [e.getNode2() for e in self.tc->smellz]
        mtdSmells = ([e.getNode2() for e in (self.tc->g.nodes)] -> smellz)
        mtdSmells =  [e.getNode2() for e in mtdSmells]
        self.smellz = caseSmells + mtdSmells
        return len(self.smellz)

    def computeMinMtdSLOC(self):
        return self.__mtdSLOCHelper(min, 10**5)

    def computeMaxMtdSLOC(self):
        return self.__mtdSLOCHelper(max, 0.1**5)

    def __mtdSLOCHelper(self, gen, initial):
        compSLOC = initial
        for mtd in self.mtds:
            try:
                mtdSLOC = self.metrics[mtd.entity][mtd.name]['SLOC']
                compSLOC = gen(mtdSLOC, compSLOC)
            except: pass
        if compSLOC == initial: compSLOC = "not initialized"
        return compSLOC

class SuiteMetrics:
    def __init__(self):
        global glzz # from TMenu.py
        self.metrics = glzz.metricDict
        self.__initMetrics()

    def __initMetrics(self):
        self.__computeNrofTestEntities()
        self.__computeSLOCMetrics()
        self.__computeAvgNrofTestEntities()

    def __computeNrofTestEntities(self):
        self.nrofPackages = len(entity == 'package')
        self.nrofCases    = len(entity == 'testcase')
        self.nrofCommands = len(entity == 'testcommand')
        self.nrofHelpers  = len(entity == 'testhelper')
        self.nrofFixtures = len(entity == 'testfixture')

    def __computeAvgNrofTestEntities(self):
        self.avgNrofCommands = " no cases"
        self.avgNrofHelpers  = " no cases"
        self.avgNrofFixtures = " no cases"
        if self.nrofCases > 0:
            self.avgNrofCommands = float(self.nrofCommands)/self.nrofCases
            self.avgNrofHelpers = float(self.nrofHelpers)/self.nrofCases
            self.avgNrofFixtures = float(self.nrofFixtures)/self.nrofCases

    def __computeSLOCMetrics(self):
        sloc = 0
        cntr = 0 # record the number of SLOC's
        for ent in ['testcommand','testhelper','testfixture']:
            for mtd in self.metrics[ent].values():
                try:
                    sloc += mtd['SLOC']
                    cntr += 1
                except: pass
        self.totalSLOC = sloc
        totalMtds = self.nrofCommands + self.nrofHelpers + self.nrofFixtures
        missed = totalMtds - cntr
        self.totalSLOCStr = str(sloc) + " [missed " + str(missed) + " methods]"
        self.avgSLOCperCase = " no cases"
        if self.nrofCases > 0 :
            self.avgSLOCperCase = sloc / self.nrofCases
        self.avgSLOCperCmd = " no commands"
        if self.nrofCommands - missed> 0:
            self.avgSLOCperCmd  = sloc / (self.nrofCommands - missed)

    def print_(self):
        printHeader(" complete suite")
        self.__printTestEntities()
        printLineSeparator()
        self.__printSmellMetrics()
        printLineSeparator()

    def __printTestEntities(self):
        printLine("SLOC",      self.totalSLOCStr)
        printLine("SLOC/case", self.avgSLOCperCase)
        printLine("SLOC/cmd",  self.avgSLOCperCmd)
        printLineSeparator()
        printLine("#packages", self.nrofPackages)
        printLine("#cases",    self.nrofCases)
        printLine("#commands", self.nrofCommands)
        printLine("#helpers",  self.nrofHelpers)
        printLine("#fixture",  self.nrofFixtures)
        printLineSeparator()
        printLine("cmds/case", self.avgNrofCommands)
        printLine("hlps/case", self.avgNrofHelpers)
        printLine("fix/case",  self.avgNrofFixtures)

    def __printSmellMetrics(self):
        printLine("#smells", len(entity == 'smell'))
        printLine("#assertionless", len(label == 'AssertionLess'))
        printLine("#assertionroulette", len(label == 'AssertionRoulette'))
        printLine("#duplicatedcode", len(label == 'DuplicatedCode'))
        printLine("#eagertests", len(label == 'EagerTest'))
        printLine("#fortestersonly", len(label == 'ForTestersOnly'))
        printLine("#generfixture", len(label == 'GeneralFixture'))
        printLine("#indentedtest", len(label == 'IndentedTest'))
        printLine("#indirecttests", len(label == 'IndirectTest'))
        printLine("#mysteryguest", len(label == 'MysteryGuest'))
        printLine("#sensitiveequality", len(label == 'SensitiveEquality'))

def addWriteMetricsAction():
    item = NodeEditorPopup.addItem("writeMetrics")
    item.menuEvent = lambda nodes : CaseMetrics(nodes[0]).print_()

def writeSuiteMetrics():
    SuiteMetrics().print_()

def writeCaseMetrics(case):
    CaseMetrics(case).print_()
