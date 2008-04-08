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

import com
from java.awt            import Rectangle, \
                                GridBagLayout, \
                                GridBagConstraints
from java.awt.event      import MouseAdapter, \
                                MouseEvent
from javax.swing         import JPanel, \
                                JTree, \
                                JScrollPane, \
                                JPopupMenu, \
                                JMenuItem
from javax.swing.tree    import DefaultMutableTreeNode,\
                                DefaultTreeCellRenderer
from javax.swing.event   import TreeSelectionListener
from com.hp.hpl.guess.ui import Dockable, \
                                GraphMouseListener, \
                                GraphEvents

#-----------------------------------------------------------------------
#--  auxiliary functions
#-----------------------------------------------------------------------

def extractTestCase(path):
    nodeName = ""
    if len(path) >= 2:
        nodeName += str(path[1]) # the package
    if len(path) >= 3:
        nodeName += "::" + str(path[2]) # test case
    return nodeName

def extractNode(path):
    nodeName = extractTestCase(path)
    #if len(path) >= 2:
        #nodeName += str(path[1]) # the package
    #if len(path) >= 3:
        #nodeName += "::" + str(path[2]) # test case
    if len(path) >= 5:
        nodeName += "." + str(path[4]) # test method
    node = (name == nodeName)
    if len(node) == 1: return node[0]
    else: # try without pkg name
        node = (name == nodeName.split('::')[-1])
        if len(node)==1: return node[0]

def cmpTc(x,y):
    return x.name > y.name

#-----------------------------------------------------------------------
#--  classes
#-----------------------------------------------------------------------

class NodeHighlighter(TreeSelectionListener):
    ''' Highlights nodes in the graph when clicked in the tree case panel '''

    def __init__(self):
        self.lastClicked = None

    def valueChanged(self, event):
        self.__deselect()
        path = event.getPath().getPath()
        node = extractNode(path)
        self.__select(node)

    def __deselect(self):
        if self.lastClicked != None:
            GraphEvents.mouseLeave(self.lastClicked)
            self.lastClicked = None

    def __select(self, node):
        if node != None:
            GraphEvents.mouseEnter(node)
            self.lastClicked = node
            Guess.getMainUIWindow().getCanvas().repaint()

class TreePopup(JPopupMenu):
    ''' Context menu for the tree case panel '''

    def __init__(self, path):
        JPopupMenu.__init__(self, "options")
        self.path = path
        self.__addViewCaseAction()
        self.__addToSourceAction()
        self.__addWriteMetricsAction()

    def __addViewCaseAction(self):
        caseName = extractTestCase(self.path)
        self.case = (name == caseName) # returns list of nodes
        if len(self.case) == 0: # try without pkg names
            self.case = (name == caseName.split('::')[-1])

        if len(self.case): # did we find one?
            self.case = self.case[0]
            casev = JMenuItem("viewCase")
            casev.actionPerformed = self.__showCase
            self.add(casev)

    def __addToSourceAction(self):
        self.node = extractNode(self.path)
        if self.node:
            srca = JMenuItem("toSource")
            srca.actionPerformed = self.__loadSource
            self.add(srca)

    def __addWriteMetricsAction(self):
        self.node = extractNode(self.path)
        if self.node:
            mtra = JMenuItem("writeMetrics")
            mtra.actionPerformed = self.__writeMetrics
            self.add(mtra)

    def __writeMetrics(self, dummy):
        CaseMetrics(self.node).print_()

    def __showCase(self, dummy):
        global glzz # from TMenu.py
        if isinstance(self.case, com.hp.hpl.guess.Node):
            TreeCaseView(glzz, self.case).go() # from TMenu.py

    def __loadSource(self, dummy):
        global srcDict # from sourceContext.py
        root = srcDict['ProjectSourceRootDirectory']
        loc = srcDict[self.node.name]
        openEditor(root + loc[0][0], loc[0][1])  # from ToSourceContext.py

class TreeMouseListener(MouseAdapter):
    ''' listen for clicks on the testsuite panel '''

    def mouseClicked(self, event):
        tree = event.getSource()
        row = tree.getRowForLocation(event.getX(), event.getY())
        path = tree.getPathForLocation(event.getX(), event.getY())
        if row == -1: return
        if event.getClickCount() != 1: return
        if event.getButton() != MouseEvent.BUTTON3: return
        TreePopup(path.getPath()).show(tree, event.getX(), event.getY())

class TestSuitePanel(JPanel, Dockable, GraphMouseListener):
    ''' Show an expandable list of the test entites {suites,cases,methods} '''

    def __init__(self):
        self.myParent = None
        #com.hp.hpl.guess.ui.GraphEvents.getGraphEvents().addGraphMouseListener(self)
        self.__initTree()
        self.setBounds(self.getDefaultFrameBounds())
        ui.dock(self)

    def __initTree(self):
        ''' construct the suite tree '''
        top = DefaultMutableTreeNode("RootSuite")
        self.__createNodes(top)
        self.tree = JTree(top)
        self.__setupRenderer()
        self.scrollpane = JScrollPane(self.tree)
        self.add(self.scrollpane,self.__setupLayout())
        self.tree.addTreeSelectionListener(NodeHighlighter())
        self.tree.addMouseListener(TreeMouseListener())

    def __setupRenderer(self):
        renderer = DefaultTreeCellRenderer()
        renderer.setOpenIcon(None)
        renderer.setClosedIcon(None)
        renderer.setLeafIcon(None)
        self.tree.setCellRenderer(renderer);

    def __setupLayout(self):
        self.setLayout(GridBagLayout())
        constr = GridBagConstraints()
        constr.weighty = 1
        constr.weightx = 1
        constr.gridx = 0
        constr.gridy = 1
        constr.fill = GridBagConstraints.BOTH
        return constr

    def __createNodes(self, top):
        ''' build the tree, by adding packages, testcases and commands '''
        for pkg in (entity == 'package'):
            pkgNode = DefaultMutableTreeNode(pkg.name)
            top.add(pkgNode)
            self.__appendCases(pkg, pkgNode)

    def __appendCases(self, pkg, pkgNode):
        ''' append the test cases of a single package to the tree '''
        testcases = [edge.getNode2() for edge in (pkg->g.nodes)]
        testcases.sort(cmpTc)
        for tc in testcases:
            tcNode = DefaultMutableTreeNode(tc.label.split('::')[-1])
            pkgNode.add(tcNode)
            self.__appendCommands(tc, tcNode)
            self.__appendFixture(tc, tcNode)
            self.__appendHelpers(tc, tcNode)

    def __appendCaseMethodsHelper(self, case, caseNode, metaName, entityName):
        ''' helper for appendCommands, appendFixture & appendHelpers '''
        mtdMeta = DefaultMutableTreeNode(metaName)
        caseNode.add(mtdMeta)
        mtds = [edge.getNode2() for edge in case->(entity == entityName)]
        mtds.sort(cmpTc)
        for mtd in mtds:
            mtdNode = DefaultMutableTreeNode(mtd.label)
            mtdMeta.add(mtdNode)

    def __appendCommands(self, case, caseNode):
        ''' append the test commands of a single testcase to the tree '''
        self.__appendCaseMethodsHelper(case, caseNode, "commands", "testcommand")

    def __appendFixture(self, case, caseNode):
        ''' append the test fixture methods of a single testcase to the tree'''
        self.__appendCaseMethodsHelper(case, caseNode, "fixture", "testfixture")

    def __appendHelpers(self, case, caseNode):
        #''' append the test helper methods of a single testcase to the tree'''
        self.__appendCaseMethodsHelper(case, caseNode, "helpers", "testhelper")

    ####
    # Implementation of the Dockable interface
    ####

    def getDefaultFrameBounds(self):
        return Rectangle(50, 50, 300, 600)

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
