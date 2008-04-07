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

class TestCaseMetrics:
    def __init__(self, testcase):
        global glzz # from TMenu.py
        self.tc = testcase # testcase graph node
        self.metrics = glzz.metricDict
        self.mtds = [mtd.getNode2() for mtd in self.tc->(srcentity == 1)]

    def print_(self):
        if not self.tc.entity == 'testcase': return
        self.__printHeader()
        self.__printCodeMetrics()
        self.__printLineSeparator()
        self.__printSmellMetrics()
        self.__printLineSeparator()

    def __printLineSeparator(self):
        print LINE_SEPARATOR

    def __printLine(self, label, value):
        if len(label) > 22:  numTabs = 1
        elif len(label) > 11: numTabs = 2
        else: numTabs = 3
        tabs = "\t" * numTabs
        print label + ":" + tabs + str(value)

    def __printHeader(self):
        print ""
        self.__printLineSeparator()
        print "--- metrics for " + self.tc.name.split('::')[-1]
        self.__printLineSeparator()

    def __printCodeMetrics(self):
        sloc = self.computeSLOC()
        nrofCmds = self.computeNrofCommands()
        nrofMtds = self.computeNrofMethods()

        self.__printLine("SLOC", sloc)
        self.__printLine("#commands", nrofCmds)
        self.__printLine("#helpers", self.computeNrofHelpers())
        if nrofMtds > 0: 
            self.__printLine("SLOC/mtds", float(sloc)/nrofMtds)
            self.__printLine("min(SLOC)", self.computeMinMtdSLOC())
            self.__printLine("max(SLOC)", self.computeMaxMtdSLOC())
        if nrofCmds > 0:
            self.__printLine("SLOC/cmd", float(sloc)/nrofCmds)

    def __printSmellMetrics(self):
        nrofSmells = self.computeNrofSmells()
        nrofMtds = self.computeNrofMethods()

        self.__printLine("#smells", nrofSmells)
        if nrofSmells == 0: return
        if nrofMtds != 0: 
            self.__printLine("#smells/mtd", float(nrofSmells)/nrofMtds)
        smellsGrouped = groupBy(self.smellz, label)
        for group in smellsGrouped:
            self.__printLine("#" + str(group[0].label), len(group))

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
        mtdSmells = [e.getNode2() for e in mtdSmells]
        self.smellz = caseSmells + mtdSmells
        return len(self.smellz)

    def __mtdSLOCHelper(self, gen, initial):
        compSLOC = initial
        for mtd in self.mtds:
            try:
                mtdSLOC = self.metrics[mtd.entity][mtd.name]['SLOC']
                compSLOC = gen(mtdSLOC, compSLOC)
            except: pass
        if compSLOC == initial: compSLOC = "not initialized"
        return compSLOC

    def computeMinMtdSLOC(self):
        return self.__mtdSLOCHelper(min, 10**5)

    def computeMaxMtdSLOC(self):
        return self.__mtdSLOCHelper(max, 0.1**5)


def writeStats():
    toPrint =  "#------------------------------------>\n"
    toPrint += "nodes \t\t" + str(len(g.nodes)) + "\n"
    toPrint += "edges\t\t" + str(len(g.edges)) + "\n"
    toPrint += "#------------------------------------>\n"
    toPrint += "pkgs \t\t" + str(len(entity == 'package')) + "\n"
    toPrint += "cases\t\t" + str(len(entity == 'testcase')) + "\n"
    toPrint += "cmds \t\t" + str(len(entity == 'testcommand')) + "\n"
    toPrint += "hlprs\t\t" + str(len(entity == 'testhelper')) + "\n"
    toPrint += "fixt \t\t" + str(len(entity == 'testfixture')) + "\n"
    toPrint += "#------------------------------------>\n"
    toPrint += "smells\t\t"+ str(len(entity == 'smell')) + "\n"
    toPrint += "aless\t\t" + str(len(label == 'AssertionLess')) + "\n"
    toPrint += "aroul\t\t" + str(len(label == 'AssertionRoulette')) + "\n"
    toPrint += "dupli\t\t" + str(len(label == 'DuplicatedCode')) + "\n"
    toPrint += "eager\t\t" + str(len(label == 'EagerTest')) + "\n"
    toPrint += "forte\t\t" + str(len(label == 'ForTestersOnly')) + "\n"
    toPrint += "gefix\t\t" + str(len(label == 'GeneralFixture')) + "\n"
    toPrint += "inden\t\t" + str(len(label == 'IndentedTest')) + "\n"
    toPrint += "indir\t\t" + str(len(label == 'IndirectTest')) + "\n"
    toPrint += "mysty\t\t" + str(len(label == 'MysteryGuest')) + "\n"
    toPrint += "sensi\t\t" + str(len(label == 'SensitiveEquality')) + "\n"
    toPrint += "#------------------------------------>\n"
    print toPrint

def addWriteMetricsAction():
    item = NodeEditorPopup.addItem("writeMetrics")
    item.menuEvent = lambda nodes : TestCaseMetrics(nodes[0]).print_()
