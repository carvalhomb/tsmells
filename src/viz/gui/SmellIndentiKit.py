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

import sys, cPickle

from java.awt            import Rectangle, \
                                GridBagLayout, \
                                GridBagConstraints, \
                                Point
from java.awt.event      import MouseEvent
from javax.swing         import JPanel, \
                                JTable, \
                                JScrollPane
from javax.swing.table   import AbstractTableModel
from javax.swing.event   import ListSelectionListener
from com.hp.hpl.guess.ui import Dockable, \
                                GraphMouseListener, \
                                GraphEvents

def loadSrcDict():
    ''' Load the pickle file which contains the entity-source
        mapping dictionary '''
    pcklFile = open(os.environ['TSMELLS_SRCPICKLE'],'rb')
    srcDict =  cPickle.load(pcklFile)
    pcklFile.close()
    return srcDict

def loadMetricDict():
    ''' Load the pickle file which contains the metric
        information dictionary '''
    pcklFile = open(os.environ['TSMELLS_METRICPICKLE'], 'rb')
    metricDict = cPickle.load(pcklFile)
    pcklFile.close()
    return metricDict

class SmellIdentiKitModel(AbstractTableModel):
    ''' A simple table model for the smell information table'''
    def __init__(self):
        self.labels = [] # first column
        self.values = [] # second column

    def getColumnCount(self):
        return 2

    def getRowCount(self):
        return len(self.labels)

    def getValueAt(self,row, col):
        if col == 0: return self.labels[row]
        else: return self.values[row]

    def getColumnName(self, col):
        if col == 0: return "label"
        else: return "value"

    def isCellEditable(self, row, col):
        return 0

    def fill(self, node, srcLocs, metrics):
        ''' fill the model with new data '''
        self.labels = []
        self.values = []
        self.__fillName(node)
        self.__fillOwner(node)
        self.__fillSourceLoc(srcLocs)
        self.__fillMetrics(metrics)

    def __fillName(self, node):
        self.labels.append("smell")
        self.values.append(str(node.label))
        self.labels.append("id")
        self.values.append(str(node.name))

    def __fillSourceLoc(self, srcLocs):        
        ''' fill the table with source locations for a given node '''
        # fill the labels
        if (len(srcLocs)) > 0:
            self.labels.append("file")
            self.labels.append("line")
        for i in range(1,len(srcLocs)):
            self.labels.append("file" + str(i+1))
            self.labels.append("line" + str(i+1))
        # fill the values
        for i in range(0,len(srcLocs)):
            self.values.append(str(srcLocs[i][0]))
            self.values.append(str(srcLocs[i][1]))

    def __fillOwner(self, node):
        ''' fetch the owner(s) of this smell, if any '''
        owners = [x.getNode2() for x in (node)-(g.nodes)]
        cnt = 0
        for owner in owners:
            label = "owner"
            if cnt != 0: label += str(cnt+1)
            self.labels.append(label)
            self.values.append(owner.name)
            cnt += 1

    def __fillMetrics(self, metrics):
        ''' fill the table with metric info for a given node '''
        for name, val in metrics.items():
            self.labels.append(name)
            self.values.append(val)

isSelectEnter = 0 # used to distinguish between real mouse-enter-node events
                  # and those caused by the SelectionListener

class SelectionListener(ListSelectionListener):
    ''' Responds to selection events on the smell info table '''
    def __init__(self, table):
        self.table = table
        self.lastEntered = None
        self.toggle = 0

    def __isColumnSelection(self, event):
        src = event.getSource()
        return (src == self.table.getSelectionModel()) and \
                self.table.getRowSelectionAllowed() and \
                not event.getValueIsAdjusting()

    def valueChanged(self, event):
        ''' callback fired when something gets selected '''
        global isSelectEnter

        if not self.__isColumnSelection(event): return

        if self.lastEntered != None:
             GraphEvents.mouseLeave(self.lastEntered)
        new = (name == self.table.getValueAt(1,1))[0]
        if (new == self.lastEntered) and (self.toggle == 1):
            # this is an even click on the same node
            # just hide the label and reset the toggle
            self.toggle = 0
        else:
            # show the label and set the toggle
            self.toggle = 1
            isSelectEnter = 1 # smother SmellIndentiKitPanel.mouseEnterNode()
            GraphEvents.mouseEnter(new)
            isSelectEnter = 0 # release
            self.lastEntered = new

class SmellIdentiKitTable(JTable):
    ''' Table implementation '''
    def __init__(self, model, metricDict):
        JTable.__init__(self, model)
        self.metricDict = metricDict

    def getToolTipText(self, event):
        ''' show the full metric names in a tool tip'''
        tip = ""
        p = event.getPoint()
        rowIndex = self.rowAtPoint(p)
        colIndex = self.convertColumnIndexToModel(self.columnAtPoint(p))

        #smellType = self.getModel().getValueAt(0,1)
        #if not self.metricDict.has_key(smellType):
            #return tip # only interested in showing metric explanation
        #metr = metricDictp[smellType]
        #smellName = self.getModel().getValueAt(1,1)
        #if not self.metricDict.has_key(smellName):
            #return tip # not in dictionary, shouldnt happen ?
        #metr = metr[smellName]

        if not self.metricDict.has_key('Translation'):
            return tip # weid, shouldnt be possible.
        metrName = self.getModel().getValueAt(rowIndex, 0)
        if not self.metricDict['Translation'].has_key(metrName):
            return tip # not a metric line, or no translation available
        return self.metricDict['Translation'][metrName]

class SmellIdentiKitPanel(JPanel, Dockable, GraphMouseListener):
    ''' A dockable panel which holds the smell information table '''

    def __init__(self, srcDict, metricDict):
        # listen for changes in the graph
        GraphEvents.getGraphEvents().addGraphMouseListener(self)
        self.myParent = None
        self.setBounds(self.getDefaultFrameBounds())
        self.srcDict = srcDict
        self.metricDict = metricDict
        self.__initTable()
        # add this panel to the guess ui (vertically)
        ui.dock(self)

    def __initTable(self):
        ''' Construct the smell information table and add it '''
        self.model = SmellIdentiKitModel()
        self.table = SmellIdentiKitTable(self.model, self.metricDict)
        self.listener = SelectionListener(self.table)
        self.table.getSelectionModel().addListSelectionListener(self.listener)
        self.table.getColumnModel().getSelectionModel().addListSelectionListener(self.listener)
        self.scrollpane = JScrollPane(self.table)
        self.setLayout(GridBagLayout())
        self.constr = GridBagConstraints()
        self.constr.fill = GridBagConstraints.BOTH
        self.constr.weighty = 1
        self.constr.weightx = 1
        self.constr.gridx = 0
        self.constr.gridy = 1
        self.add(self.scrollpane,self.constr)

    def getDefaultFrameBounds(self):
        return Rectangle(50, 50, 300, 600)

    def mouseEnterNode(self, node):
        ''' callback fired when the mouse cursor enters a node '''
        global isSelectEnter
        if node.entity != 'smell': return # only interested in smells
        if isSelectEnter: return # a programmatic enter caused by the selection listener

        srcLocs = []
        if self.srcDict.has_key(node.name):
            srcLocs = self.srcDict[node.name]

        metrics = {}
        if self.metricDict.has_key(node.label):
            # this smell-type has metrics
            mtr = self.metricDict[node.label]
            if mtr.has_key(node.name):
                # this smell has metrics
                metrics = mtr[node.name]

        self.model.fill(node, srcLocs, metrics)
        self.updateView()

    def updateView(self):
        ''' repaint the table, remember the previous column width '''
        width0 = self.table.getColumnModel().getColumn(0).getWidth()
        width1 = self.table.getColumnModel().getColumn(1).getWidth()

        self.model.fireTableStructureChanged()

        col0 = self.table.getColumnModel().getColumn(0)
        col0.setPreferredWidth(width0)
        col1 = self.table.getColumnModel().getColumn(1)
        col1.setPreferredWidth(width1)

    def mouseEnterEdge(self, edge):
        pass

    def mouseLeaveNode(self, node):
        pass

    def mouseLeaveEdge(self, edge):
        pass

    def getPreferredSize(self):
        return Dimension(200,600)

    def getDirectionPreference(self):
        ''' prefer vertical orientation '''
        return 2 # vertical, see com.hp.hpl.guess.ui.MainUIWindow.java

    def opening(self, state):
        self.visible = state

    def attaching(self, state):
        pass

    def getTitle(self):
        return("smell info")

    def getWindow(self):
        return self.myParent

    def setWindow(self,gjf):
        self.myParent = gjf

# construct the panel
srcDict = loadSrcDict()
metricDict = loadMetricDict()
SmellIdentiKitPanel(srcDict, metricDict)
