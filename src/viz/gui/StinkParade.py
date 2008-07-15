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
                                GridBagConstraints, \
                                Dimension
from java.awt.event      import MouseEvent, \
                                ActionListener, \
                                MouseAdapter, \
                                ComponentAdapter
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

#-----------------------------------------------------------------------
#--  auxiliary functions
#-----------------------------------------------------------------------

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


def resizeColumns(table):
    ''' shrink the smell columns + max out the owner column '''
    # TODO move this to a JTable subclass?
    numCols = table.getColumnCount()
    mtrX = 40 #metric column width
    for col in range(1, numCols):
        # resize evrything but the first 'owner' column
        table.getColumnModel().getColumn(col).setPreferredWidth(mtrX)
    # resize the first column with the space left
    table.getColumnModel().getColumn(0).setPreferredWidth(table.getWidth() - mtrX*(numCols -1))

#-----------------------------------------------------------------------
#--  classes
#-----------------------------------------------------------------------

class HitListModel(AbstractTableModel):
    ''' Model for the hitlist table '''

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
        ''' fill the model with the hitlist for another smell '''
        global SmellSorters # sort callbacks
        self.__reset()
        if SmellSorter.has_key(smell):
            smells = self.__sortOnMetrics(smell)
        else:
            smells = self.__sortOnOccurenceInTestcase(smell)
        self.__initMetrics(smells)
        for i in range(0, 30):
            if i < len(smells):
                self.__fillRow(smells[i], smell)
            else:
                self.__fillEmptyRow()

    #
    # Helpers for fill
    #

    def __sortOnMetrics(self, smellType):
        # this is a smell with associated numerical metrics,
        # go ahead and sort on that
        smells = metricDict[smellType].items() # eg [(AssertionRoulette3, {'NrA': 6, 'NrDA': 6}), (AssertionRoulette2]
        smells.sort(SmellSorter[smellType])
        return smells

    def __sortOnOccurenceInTestcase(self, smellType):
        # no sorter defined, use the nrof occurence in testcases
        smells = []
        for case in (entity == 'testcase'):
            nrofSmells = len((case->g.nodes).destination->(label == smellType))
            if nrofSmells != 0:
                smells.append((case.name, {'NrOf' : nrofSmells}))
        smells.sort(caseNrofSort)
        return smells

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

    #
    # Constructor
    #

    def __init__(self, model, testcases):
        JTable.__init__(self, model)
        self.model = model
        self.testcases = testcases
        self.lastClicked = None
        self.secondRun = 0

    #
    # Event callback
    #

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

class HitListMouseListener(MouseAdapter):
    ''' listen for clicks on the smell hit list '''

    #
    # Event callback
    #

    def mouseClicked(self, event):
        table = event.getSource()
        p = Point(event.getX(), event.getY())
        row = table.rowAtPoint(p)
        owner = table.getValueAt(row, 0)
        if -1 == row : return
        if event.getClickCount() != 1: return
        if event.getButton() != MouseEvent.BUTTON3: return
        tc = self.__extractTestcase(owner)
        n = self.__extractNode(owner)
        TreePopup(tc, n).show(table, event.getX(), event.getY())

    def __extractTestcase(self, owner):
        tc = owner
        if owner.endswith(')'):
            tc = owner.split('.')[0]
        tc = ((name == tc) & (entity == 'testcase'))
        if len(tc): return tc[0]
        else : return None

    def __extractNode(self, owner):
        n = (name == owner)
        if len(n): return n[0]
        else : return None

class PanelSizeChangeListener(ComponentAdapter):

    #
    # Constructor
    #

    def __init__(self, table):
        self.table = table

    #
    # Event callback
    #

    def componentResized(self, event):
        comp = event.getComponent()
        self.table.setSize(Dimension(comp.getWidth()-18, self.table.getHeight()))
        resizeColumns(self.table)
        self.table.revalidate()
        self.table.repaint()

class DropDownListener(ActionListener):
    ''' Smell selector dropdown listener, initiates a recalculation of the hitlist '''

    #
    # Constructor
    #

    def __init__(self, table):
        self.tableModel = table.getModel()
        self.table = table

    #
    # Event callback
    #

    def actionPerformed(self, event):
        item = event.getSource().getSelectedItem()

        self.tableModel.fill(item)
        self.table.setSize(Dimension(self.table.getParent().getParent().getParent().getWidth()-18, self.table.getHeight()))
        self.tableModel.fireTableStructureChanged()
        resizeColumns(self.table)
        self.table.revalidate()
        self.table.repaint()

class RadioListener(ActionListener):
    # not used at the moment.

    #
    # Constructor
    #

    def __init__(self, model):
        self.tableModel = model

    #
    # Event callback
    #

    def actionPerformed(self, event):
        actionCmd = event.getActionCommand()
        if actionCmd in ['cases', 'commands']:
            self.tableModel.setMode(actionCmd)

class StinkParade(TDockable):

    #
    # Constructor
    #

    def __init__(self):
        self.myParent = None
        self.setBounds(self.getDefaultFrameBounds())
        self.setLayout(GridBagLayout())
        self.table = self.__initTable()
        self.__initDropDownBox(self.table)
        #self.__initRadioCaseCmd()
        ui.dock(self)


    #
    # Construction of smell-selector dropdown menu
    #

    def __initDropDownBox(self, table):
        smells = [ x[0].label for x in groupBy((entity == 'smell'), label)]
        smells.sort()
        smells.remove('ForTestersOnly')
        dropdown = JComboBox(jarray.array(smells, String))
        dropdown.addActionListener(DropDownListener(table))
        self.add(dropdown, self.__createDropDownConstraints())

    #
    # Construction of radio buttons
    #

    def __initRadioCaseCmd(self):
        # dead
        listener = RadioListener(self)
        cases = self.__addRadioButton("cases", listener)
        cmds  = self.__addRadioButton("commands", listener)

        group = ButtonGroup()
        group.add(cases)
        group.add(cmds)
        cases.setSelected(1)

    #
    # Construction of the hit-list table
    #

    def __initTable(self):
        table = JTable(HitListModel())
        scrollpane = JScrollPane(table)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF)
        table.addMouseListener(HitListMouseListener())
        self.add(scrollpane, self.__createTableConstraints())
        scrollpane.addComponentListener(PanelSizeChangeListener(table))
        return table

    #
    # All kinds of helpers
    #

    def __createTableConstraints(self):
        constr = GridBagConstraints()
        constr.fill = GridBagConstraints.BOTH
        constr.weighty = 1000
        constr.weightx = 2
        constr.gridwidth = 4
        constr.gridx = 0
        constr.gridy = 2
        return constr

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

    #
    # Panel title
    #

    def getTitle(self):
        return "stink parade"
