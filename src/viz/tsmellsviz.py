import os, com, java, javax.swing, java.lang, jarray, cPickle, time
import com.hp.hpl.guess.Guess
import com.hp.hpl.guess.Graph
import javax.swing.JTable
from java.awt.geom import GeneralPath
from java.awt import Polygon

srcDict = {}

def openeditor(file, line):
    toExec = "kwrite " + file + " --line " + str(line)
    print "executing " + toExec
    Runtime.getRuntime().exec(toExec)

def toSourceAction(targetNode):
    root = srcDict['ProjectSourceRootDirectory']
    print targetNode.name
    for location in srcDict[targetNode.name[0]]:
        print root
        print location
        openeditor(root + location[0], location[1])

def createToSourceContextAction():
    newMenuItem = NodeEditorPopup.addItem("toSource")
    newMenuItem.menuEvent = toSourceAction

def createDiamondShape():
    xpoints = jarray.array((10,5,0,5),'i')
    ypoints = jarray.array((5,10,5,0),'i')

    diamond = Polygon(xpoints,ypoints,4);
    shapeDB.addShape(104,diamond)

def loadData():
    global srcDict

    setDisplayBackground("black")
    makeFromGDF(os.environ['TSMELLS_GDF'])
    pcklFile = open(os.environ['TSMELLS_SRCPICKLE'],'rb')
    srcDict = cPickle.load(pcklFile)
    g.edges.color = 'lightgray'
    g.nodes.visible = 0

def transformToGEM():
    print "Creating GEM layout"
    graph = Guess.getGraph()
    gem = com.hp.hpl.guess.layout.GEM(graph)
    graph.layout(gem)
    while not gem.done:
        print ".",
        time.sleep(0.2)
    print " done"
    time.sleep(0.5)

def transformToBIN():
    graph = Guess.getGraph()
    binPack = com.hp.hpl.guess.layout.BinPack(graph, true)
    graph.layout(binPack)
    print "Transforming to BinPack"
    while not binPack.incrementsAreDone():
        print "."
        time.sleep(0.2)
    print " done"
    time.sleep(1)
    center()

def displaySmellView():
    g.nodes.visible = 0
    pkgs = remove((entity=='package'))
    resetColors()
    (entity == 'smell').color = 'red'
    transformToGEM()
    transformToBIN()

    g.nodes.visible = 1
    add(pkgs)

def resetColors():
    g.nodes.color = 'black'
    g.edges.color = 'lightgray'

colors = ['red', 'green', 'blue', 'yellow', 'purple', 'orange', 'brown']

def displaySuiteView():
    global colors
    g.nodes.visible = 0

    resetColors()
    counter = 0
    smells = remove((entity=='smell'))
    for pkg in (entity == 'package'):
        for e in pkg.getOutEdges():
            for edge in e.getNode1().getOutEdges():
                edge.color = colors[counter]
        counter += 1
    pkgs = remove((entity == 'package'))
    transformToGEM()
    transformToBIN()

    g.nodes.visible = 1
    add(smells)
    add(pkgs)

counter = 0

def displayRadialSuiteView():
    global counter

    g.nodes.visible = 0
    resetColors()

    smells = remove((entity=='smell'))
    root = addNode('root' + str(counter))
    counter += 1
    for pkg in (entity == 'package'):
        addDirectedEdge(root, pkg)
    radialLayout(root)
    time.sleep(2)
    remove([root])

    g.nodes.visible = 1
    add(smells)

def rescaleCenter(factor):
    rescaleLayout(factor)
    #center()

import com.hp.hpl.guess.ui.MainUIWindow

class ControlPanel(com.hp.hpl.guess.ui.DockableAdapter):

    def __init__(self):
        smellButton = JButton("Smell view")
        smellButton.actionPerformed = lambda event: displaySmellView()
        self.add(smellButton)
        suiteButton = JButton("Suite view")
        suiteButton.actionPerformed = lambda event : displaySuiteView()
        self.add(suiteButton)
        rescaleMinus = JButton("-")
        rescaleMagni = JButton("+")
        rescaleMinus.actionPerformed = lambda event : rescaleCenter(0.5)
        rescaleMagni.actionPerformed = lambda event : rescaleCenter(2)
        self.add(rescaleMagni)
        self.add(rescaleMinus)
        radialSuite = JButton('Radial Suite view')
        radialSuite.actionPerformed = lambda event : displayRadialSuiteView()
        self.add(radialSuite)
        ui.dock(self)

    def getTitle(self):
        return("control")

class OtherStuff(com.hp.hpl.guess.ui.DockableAdapter):

    def __init__(self):
        Guess.getMainUIWindow().dock(self)

    def getTitle(self):
        return("stuff")

    def getDirectionPreference(self):
        #return MainUIWindow.VERTICAL_DOCK
        return 2

def plotSmellFrequency():
    other = remove((complement(entity == 'smell')))
    plotSizesPie(label, 1)
    add(other)

import javax.swing.JTable
import javax.swing.JScrollPane
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.Rectangle
import javax.swing.JLabel
import com.hp.hpl.guess.ui.Dockable

class StuffModel(javax.swing.table.AbstractTableModel):

    #def __init__(self):
        #self.nRowCount = 0;
        #self.nFields = java.util.Vector
        #self.eRowCount = 0
        #self.eFields = java.util.Vector
        #self.lastSel = GraphElement()
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
            return false
        else:
            return true

lastClicked = None
testcases = None
secondRun = false

class StuffTable(javax.swing.JTable):

    def valueChanged(self, event):
#        javax.swing.JTable(self).valueChanged(event)
        global lastClicked
        global testcases
        global secondRun

        if secondRun:
            secondRun = false
            return

        if lastClicked == event.getFirstIndex():
            row = event.getLastIndex()
        else:
            row = event.getFirstIndex()

        if lastClicked != None:
            com.hp.hpl.guess.ui.GraphEvents.mouseLeave(testcases[lastClicked])
        com.hp.hpl.guess.ui.GraphEvents.mouseEnter(testcases[row])
        lastClicked = row
        self.repaint()
        secondRun = true

class Stuff(JPanel,com.hp.hpl.guess.ui.Dockable,com.hp.hpl.guess.ui.GraphMouseListener):

    def __init__(self):
        global testcases

        self.model = StuffModel()
        #GuessTableModel gtm = null;
        self.jl = JLabel("TestCaseList", JLabel.CENTER)
        self.myParent = None

        #com.hp.hpl.guess.ui.GraphEvents.getGraphEvents().addGraphMouseListener(self)
        self.table = StuffTable(self.model)
        self.scrollpane = JScrollPane(self.table)
        self.setLayout(GridBagLayout())
        self.constr = GridBagConstraints()
        self.fnt = self.jl.getFont()
        #self.fnt = fnt.deriveFont(Font.BOLD,15);
        #jl.setFont(fnt);

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

loadData()
Stuff()
ControlPanel()
plotSmellFrequency()
createToSourceContextAction()

