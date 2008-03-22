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

from java.awt import Rectangle, \
                     GridBagLayout, \
                     GridBagConstraints
from javax.swing import JPanel, \
                        JTable, \
                        JLabel, \
                        JScrollPane
from javax.swing.table import AbstractTableModel
from com.hp.hpl.guess.ui import Dockable, \
                                GraphMouseListener, \
                                GraphEvents

class TestCaseModel(AbstractTableModel):

    def __init__(self):
        self.testcases = (entity == 'testcase')

    def getColumnCount(self):
        return 1

    def getRowCount(self):
        return len(self.testcases)

    def getValueAt(self,row, col):
        return self.testcases[row]

    def getColumnName(self, col):
        return "qualified test cases"

    def isCellEditable(self, row, col):
        if (col == 0):
            return 0
        else:
            return 1

lastClicked = None
testcases = None
secondRun = 0

class TestCaseTable(JTable):

    def valueChanged(self, event):
#        javax.swing.JTable(self).valueChanged(event)
        global lastClicked
        global testcases
        global secondRun

        if secondRun:
            secondRun = 0
            return

        if lastClicked == event.getFirstIndex():
            row = event.getLastIndex()
        else:
            row = event.getFirstIndex()

        if lastClicked != None:
            GraphEvents.mouseLeave(testcases[lastClicked])
        GraphEvents.mouseEnter(testcases[row])
        lastClicked = row
        self.repaint()
        secondRun = 1

class TestCasePanel(JPanel, Dockable, GraphMouseListener):

    def __init__(self):
        global testcases

        #GuessTableModel gtm = null;
        self.jl = JLabel("TestCaseList", JLabel.CENTER)
        self.myParent = None

        #com.hp.hpl.guess.ui.GraphEvents.getGraphEvents().addGraphMouseListener(self)
        self.model = TestCaseModel()
        self.table = TestCaseTable(self.model)
        self.scrollpane = JScrollPane(self.table)
        self.setLayout(GridBagLayout())
        self.constr = GridBagConstraints()
        self.fnt = self.jl.getFont()

        self.constr.fill = GridBagConstraints.HORIZONTAL
        self.constr.weighty = 0
        self.constr.weightx = 1
        self.constr.gridx = 0
        self.constr.gridy = 0
        self.add(self.jl,self.constr)

        self.constr.fill = GridBagConstraints.BOTH
        self.constr.weighty = 1
        self.constr.gridy = 1
        self.add(self.scrollpane,self.constr)
        self.setBounds(self.getDefaultFrameBounds())

        self.testcases = (entity == 'testcase')
        testcases = self.testcases
        it = 0
        for case in self.testcases:
            self.table.setValueAt(case.name, it, 0)
            it += 1
        ui.dock(self)

    def getDefaultFrameBounds(self):
        return Rectangle(50, 50, 300, 600)

    #def mouseEnterNode(self, node):
        #print node
        #if node.entity == 'testcase':
            ##print "ISATESTCASE"
            #row = self.getIndex(node.name)
            #print row
            #self.table.clearSelection()
            #self.table.changeSelection(row, 0, false, false)

    def getIndex(self, name):
        index = 0
        while (index < len(self.testcases)) and\
              (str(self.testcases[index]) != str(name)):
            print "\t" + self.testcases[index] + "<>" + name
            index+=1
        return index

    def mouseEnterEdge(self, edge):
        pass

    def mouseLeaveNode(self, node):
        pass

    def mouseLeaveEdge(self, edge):
        pass

    def getPreferredSize(self):
        return Dimension(200,600)

    def getDirectionPreference(self):
        return 2 # vertical

    def opening(self, state):
        self.visible = state

    def attaching(self, state):
        pass

    def getTitle(self):
        return("testcases")

    def getWindow(self):
        return self.myParent

    def setWindow(self,gjf):
        self.myParent = gjf

# construct the panel
TestCasePanel()
