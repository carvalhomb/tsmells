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

import jarray
from java.lang           import String
from java.awt            import Rectangle, \
                                BorderLayout, \
                                Point,\
                                GridBagLayout, \
                                GridBagConstraints
from java.awt.event      import MouseEvent, \
                                ActionListener
from javax.swing         import JPanel, \
                                JTable, \
                                JScrollPane, \
                                JComboBox, \
                                JRadioButton, \
                                ButtonGroup, \
                                JPopupMenu,\
                                JMenuItem
from javax.swing.table   import AbstractTableModel
from com.hp.hpl.guess.ui import Dockable, \
                                GraphMouseListener, \
                                GraphEvents

def compAroulRelMetric(x):
    return (x[1]['NrDA']*2 + x[1]['NrA'])

def aRoulSort(x, y):
    return compAroulRelMetric(y) - compAroulRelMetric(x)

def compGenFixRelMetric(x):
    m = x[1]
    return (m['NFPT'] + m['NOBU'] + m['NFOB'])

def genFixSort(x,y):
    return compGenFixRelMetric(y) - compGenFixRelMetric(x)

def singleMetricSort(x,y, mtrName):
    return y[1][mtrName] - x[1][mtrName]

verboseSort = lambda x,y: singleMetricSort(x,y, "LOC")
dupliSort = lambda x,y: singleMetricSort(x,y, "ANrSL")
eagerSort = lambda x,y: singleMetricSort(x,y, "PTMI")
indenSort = lambda x,y: singleMetricSort(x,y, "NrCS")
indirSort = lambda x,y: singleMetricSort(x,y,"NPTU")
sensEqualSort = lambda x,y: singleMetricSort(x,y,"NrTS")
caseNrofSort = lambda x,y: singleMetricSort(x,y,"NrOf")

SmellSorter = \
    {\
        'AssertionRoulette' : aRoulSort, \
        'VerboseTest' : verboseSort, \
        'DuplicatedCode' : dupliSort, \
        'GeneralFixture' : genFixSort, \
        'EagerTest' : eagerSort, \
        'IndentedTest' :indenSort, \
        'IndirectTest' : indirSort, \
        'SensitiveEquality' : sensEqualSort, \
    }


class HitListModel(AbstractTableModel):

    #
    # Constructor
    #

    def __init__(self):
        self.__reset()
        #self.fill("AssertionRoulette")
        self.fill('VerboseTest')

    def __reset(self):
        self.owner   = [] # smell owner, first column
        self.metrics = {} # {metrName x [metrInstance]} 
                          # data for multiple metric colums
        self.smellId = [] # guess node id's; not a column
        self.metricNames = [] # list of metric abbreviations
                              # sorted keys of self.metrics

    #
    # TableModel interface implementation
    #

    def getColumnCount(self):
        return len(self.metricNames) + 1

    def getRowCount(self):
        return 30

    def getValueAt(self,row, col):
        if col == 0: return self.owner[row]
        else: return self.metrics[self.metricNames[col-1]][row]

    def getColumnName(self, col):
        if col == 0: return "owner"
        else: return self.metricNames[col-1]

    def isCellEditable(self, row, col):
        return 0

    #
    # Update the model
    #

    def fill(self, smell):
        global SmellSorters
        ''' fill the model with new data '''
        self.__reset()
        if SmellSorter.has_key(smell):
            # this is a smell with associated numerical metrics,
            # go ahead and sort on that
            smells = metricDict[smell].items() # eg [(AssertionRoulette3, {'NrA': 6, 'NrDA': 6}), (AssertionRoulette2]
            smells.sort(SmellSorter[smell])
        else:
            # no sorter defined, use the nrof occurence in testcases
            smells = []
            for case in (entity == 'testcase'):
                 nrofSmells = len((case->g.nodes).destination->(label == smell))
                 if nrofSmells != 0:
                    smells.append((case.name, {'NrOf' : nrofSmells}))
            smells.sort(caseNrofSort)

        self.__initMetrics(smells)
        for i in range(0, 30):
            if i < len(smells):
                self.__fillRow(smells[i], smell)
            else:
                self.__fillEmptyRow()

    #
    # Helpers for fill
    #

    def __fillEmptyRow(self):
        self.smellId.append("")
        self.owner.append("")
        for metric in self.metricNames:
            self.metrics[metric].append("")

    def __fillRow(self, smellInstance, smellType):
        # smellInstance, eg (AssertionRoulette5, {'NrA': 6, 'NrDA': 6})
        si = smellInstance
        for metric in self.metricNames:
            self.metrics[metric].append(si[1][metric])
        self.smellId.append(si[0])
        if not SmellSorter.has_key(smellType) or smellType == "DuplicatedCode":
            self.owner.append(si[0])
        else: 
            self.owner.append(((name == si[0])<-(g.nodes))[0].source.name)

    def __initMetrics(self, smells):
        if len(smells) > 0:
            self.metricNames = smells[0][1].keys()[:]
        for metric in self.metricNames:
            self.metrics[metric] = []

class TestCaseTable(JTable):

    def __init__(self, model, testcases):
        JTable.__init__(self, model)
        self.model = model
        self.testcases = testcases
        self.lastClicked = None
        self.secondRun = 0

    def valueChanged(self, event):
        #javax.swing.JTable(self).valueChanged(event)
        if self.secondRun:
            self.secondRun = 0
            return

        if self.lastClicked == event.getFirstIndex():
            row = event.getLastIndex()
        else:
            row = event.getFirstIndex()

        if self.lastClicked != None:
            GraphEvents.mouseLeave(self.testcases[self.lastClicked])
        GraphEvents.mouseEnter(self.testcases[row])
        self.lastClicked = row
        self.secondRun = 1
        self.repaint()

class DropDownListener(ActionListener):
    def __init__(self, table):
        self.tableModel = table.getModel()
        self.table = table

    def actionPerformed(self, event):
        item = event.getSource().getSelectedItem()
        print item
        self.tableModel.fill(item)
        self.tableModel.fireTableStructureChanged()
        self.tableModel.fireTableDataChanged()
        self.__resizeColumns()

    def __resizeColumns(self):
       numCols = self.table.getColumnCount()
       mtrX = 40 #metric column width
       for col in range(1, numCols):
           # resize evrything but the first 'owner' column
           self.table.getColumnModel().getColumn(col).setPreferredWidth(mtrX)
       # resize the first column with the space left
       self.table.getColumnModel().getColumn(0).setPreferredWidth(self.table.getWidth() - mtrX*(numCols -1))

class RadioListener(ActionListener):
    def __init__(self, model):
        self.tableModel = model

    def actionPerformed(self, event):
        actionCmd = event.getActionCommand()
        if actionCmd in ['cases', 'commands']:
            self.tableModel.setMode(actionCmd)

class SmellHitLists(TDockable, GraphMouseListener):

    def __init__(self):
        GraphEvents.getGraphEvents().addGraphMouseListener(self)
        self.myParent = None
        self.setBounds(self.getDefaultFrameBounds())
        self.setLayout(GridBagLayout())
        self.table = self.__initTable()
        self.__initDropDownBox(self.table)
        #self.__initRadioCaseCmd()
        ui.dock(self)

    def getTitle(self):
        return "smell top"

    def __initDropDownBox(self, table):
        smells = [ x[0].label for x in groupBy((entity == 'smell'), label)]
        smells.sort()
        dropdown = JComboBox(jarray.array(smells, String))
        dropdown.addActionListener(DropDownListener(table))
        self.add(dropdown, self.__createDropDownConstraints())

    def __createDropDownConstraints(self):
        constr = GridBagConstraints()
        constr.fill = GridBagConstraints.HORIZONTAL
        constr.weighty = 1
        constr.gridwidth = 4
        constr.gridx = 0
        constr.gridy = 0
        return constr

    def __createRadioConstraints(self, mode):
        constr = GridBagConstraints()
        constr.fill = GridBagConstraints.HORIZONTAL
        constr.weighty = 1
        constr.weightx = 1
        if   mode == 'cases':    constr.gridx = 2
        elif mode == 'commands': constr.gridx = 3
        constr.gridy = 1
        return constr

    def __addRadioButton(self, label, listener):
        button = JRadioButton(label)
        button.setActionCommand(label)
        button.addActionListener(listener)
        self.add(button, self.__createRadioConstraints(label))
        return button

    def __initRadioCaseCmd(self):
        listener = RadioListener(self)
        cases = self.__addRadioButton("cases", listener)
        cmds  = self.__addRadioButton("commands", listener)

        group = ButtonGroup()
        group.add(cases)
        group.add(cmds)
        cases.setSelected(1)

    def __initTable(self):
        table = JTable(HitListModel())
        scrollpane = JScrollPane(table)
        constr = GridBagConstraints()
        constr.fill = GridBagConstraints.BOTH
        constr.weighty = 1000
        constr.weightx = 2
        constr.gridwidth = 4
        constr.gridx = 0
        constr.gridy = 2
        self.add(scrollpane, constr)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return table

    def getDefaultFrameBounds(self):
        return Rectangle(50, 50, 300, 600)

    def mouseEnterNode(self, node):
        ''' callback fired when the mouse cursor enters a node '''
        global isSelectEnter
        pass
        #if node.entity != 'smell': return # only interested in smells
        #if isSelectEnter: return # a programmatic enter caused by the selection listener

        #srcLocs = []
        #if self.srcDict.has_key(node.name):
            #srcLocs = self.srcDict[node.name]

        #metrics = {}
        #if self.metricDict.has_key(node.label):
            ## this smell-type has metrics
            #mtr = self.metricDict[node.label]
            #if mtr.has_key(node.name):
                ## this smell has metrics
                #metrics = mtr[node.name]

        #self.model.fill(node, srcLocs, metrics)
        #self.updateView()


