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

from java.awt            import Rectangle, \
                                GridBagLayout, \
                                GridBagConstraints
from javax.swing         import JPanel, \
                                JTable, \
                                JLabel, \
                                JScrollPane
from javax.swing.table   import AbstractTableModel
from com.hp.hpl.guess.ui import Dockable, \
                                GraphMouseListener, \
                                GraphEvents

class TestCaseModel(AbstractTableModel):

    def __init__(self, testcases):
        self.testcases = testcases

    def getColumnCount(self):
        return 1

    def getRowCount(self):
        return len(self.testcases)

    def getValueAt(self,row, col):
        return self.testcases[row]

    def getColumnName(self, col):
        return "test case list"

    def isCellEditable(self, row, col):
        if (col == 0):
            return 0
        else:
            return 1

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


class TestCasePanel(JPanel, Dockable, GraphMouseListener):

    def __init__(self):
        self.myParent = None
        self.testcases = (entity == 'testcase')
        #com.hp.hpl.guess.ui.GraphEvents.getGraphEvents().addGraphMouseListener(self)
        self.__initTable()
        self.setBounds(self.getDefaultFrameBounds())
        ui.dock(self)

    def __initTable(self):
        ''' construct the test case table '''
        self.model = TestCaseModel(self.testcases)
        self.table = TestCaseTable(self.model, self.testcases)
        self.scrollpane = JScrollPane(self.table)
        self.setLayout(GridBagLayout())
        self.constr = GridBagConstraints()
        self.constr.weighty = 1
        self.constr.weightx = 1
        self.constr.gridx = 0
        self.constr.gridy = 1
        self.constr.fill = GridBagConstraints.BOTH
        self.add(self.scrollpane,self.constr)

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

    #def getIndex(self, name):
        #index = 0
        #while (index < len(self.testcases)) and\
              #(str(self.testcases[index]) != str(name)):
            #print "\t" + self.testcases[index] + "<>" + name
            #index+=1
        #return index

    #def mouseEnterEdge(self, edge):
        #pass

    #def mouseLeaveNode(self, node):
        #pass

    #def mouseLeaveEdge(self, edge):
        #pass

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
#TestCasePanel() deprecated using the TestCase Tree
