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

import cPickle

from com.hp.hpl.guess        import Guess
from com.hp.hpl.guess.layout import GEM, BinPack
from javax.swing             import JMenu, JMenuItem
from javax.swing.event       import ChangeListener
from java.awt.event          import ActionListener

smellColors = \
    { \
        "AssertionRoulette" : "darkgray", \
        "AssertionLess" :     "gray", \
        "DuplicatedCode" :    "lightgray", \
        "EagerTest" :         "white", \
        "ForTestersOnly" :    "blue", \
        "GeneralFixture" :    "CadetBlue", \
        "IndentedTest" :      "NavyBlue", \
        "IndirectTest" :      "Indigo", \
        "MysteryGuest" :      "SkyBlue", \
        "SensitiveEquality" : "ProcessBlue", \
        # some dummy values
        "A" : "Bittersweet", \
        "B" : "RedOrange", \
        "C" : "Mahogany", \
        "D" : "Maroon", \
        "E" : "BrickRed", \
        "F" : "Red", \
        "G" : "OrangeRed", \
        "H" : "RubineRed", \
        "I" : "WildStrawberry", \
    }

def resetLook():
    global smellColors
    g.nodes.color = 'black'
    g.edges.color = 'lightgray'
    smellsz = groupBy((entity == 'smell'), label)
    for smells in smellsz:
        try:
            smells.color = smellColors[smells[0].label]
        except KeyError, e:
            print "Smell color not registered\n" + str(e)
            smells.color = 'green'
    g.nodes.width = 10
    g.nodes.height = 10
    g.nodes.labelvisible = 0

class globalz:
    def __init__(self):
        # 2 root nodes
        self.roots = self.__initRoots()
        # an extra node between testcases and their package
        self.tcsubs = self.__initTcSubs()
        # metric dictionary
        self.metricDict = self.__loadMetricDict()
        self.__initAuxiliaryNodes()
        resetLook()
        g.nodes.visible = 0

    def __initAuxiliaryNodes(self):
        self.sub = addNode("sub_node")
        remove([self.sub])
        self.command = addNode("commands")
        remove([self.command])
        self.helper = addNode("helpers")
        remove([self.helper])
        self.fixture = addNode("fixture")
        remove([self.fixture])

    def __initRoots(self):
        ''' add two root nodes'''
        root = addNode('root')
        rroot = addNode('rroot')
        addDirectedEdge(rroot, root)
        for pkg in (entity == 'package'):
            addDirectedEdge(root, pkg)
        return remove([root, rroot])

    def __initTcSubs(self):
        ''' add a sub node between testcases and their package '''
        subs = []
        for pkg in (entity == 'package'):
            sub = addNode(str(pkg) + "sub")
            subs.append(sub)
            addDirectedEdge(pkg, sub)
            for edge in pkg->(g.nodes):
                n2 = edge.getNode2()
                #print str(pkg) + " - " + str(n2) + " <> " + str(edge) 
                if n2.name == sub.name: continue
                addDirectedEdge(sub, n2)
        return remove(subs)

    def __loadMetricDict(self):
        ''' Load the pickle file which contains the metric
            information dictionary '''
        pcklFile = open(os.environ['TSMELLS_METRICPICKLE'], 'rb')
        metricDict = cPickle.load(pcklFile)
        pcklFile.close()
        return metricDict

    def resetGraph(self):
        remove([self.sub, self.command, self.helper, self.fixture])

def transformToGEM():
    Guess.setSynchronous(true)
    graph = Guess.getGraph()
    graph.layout(GEM(graph))
    Guess.setSynchronous(false)

def transformToBIN():
    Guess.setSynchronous(true)
    graph = Guess.getGraph()
    graph.layout(BinPack(graph, true))
    center()
    Guess.setSynchronous(false)

def centerNodeHorizontal(node):
    node.x = (x.max - x.min) / 2


from com.hp.hpl.guess.ui import StatusBar
from java.lang           import Thread, Runnable, Double
from time                import time

class GraphView(Runnable):
    ''' abstract class. performs a layout '''

    def __init__(self, gl):
        self.graph  = Guess.getGraph()
        self.interp = Guess.getInterpreter()
        self.globalz = gl

    def go(self):
        thread = Thread(self)
        thread.start()

    def run(self):
        start = time()
        g.nodes.visible = 0
        self.globalz.resetGraph()

        self.pre() # abstract, bring the graph in the correct state. remove/add nodes
        self.transform() # abstract, execute the layout algorithm
        resetLook()
        self.post() # abstract, restore the graph

        el = str(time() - start)
        el = el.split('.')[0] +  '.' + el.split('.')[1][0:3]
        StatusBar.setStatus("Done. [" + el + "sec]")


class SmellView(GraphView):
    ''' show the smell view layout '''

    def pre(self):
        remove(self.globalz.tcsubs)
        remove(self.globalz.roots)
        self.pkgs = remove((entity=='package'))
 
    def transform(self):
        StatusBar.setStatus("Computing GEM smell view [can take a while]")
        fto = remove(label == 'ForTestersOnly') # huge performace boost
        transformToGEM()
        StatusBar.setStatus("Transforming to binPack ...")
        add(fto)
        transformToBIN()

    def post(self):
        g.nodes.visible = 1
        add(self.pkgs)

class RadialSuiteView(GraphView):
    ''' A testsuite radial layout view '''

    def pre(self):
        add(self.globalz.roots)
        add(self.globalz.tcsubs)
        other = (entity=='package')->complement(self.globalz.tcsubs)
        other += (entity == 'smell')
        self.other = remove(other)
        remove([rroot])

    def transform(self):
        StatusBar.setStatus("computing radial suite view")
        Guess.setSynchronous(true)
        radialLayout(root)
        #rescaleCenter(4)
        Guess.setSynchronous(false)

    def post(self):
        g.nodes.visible = 1
        g.edges.visible = 1
        add(self.other)
        self.other.visible = 0

class TreeSuiteView(GraphView):
    ''' Show a polymetric view of the testsuite '''

    def pre(self):
        add(self.globalz.roots)
        self.other = self.__initPkgSubs(self.globalz)

    def transform(self):
        Guess.setSynchronous(true)
        sugiyamaLayout()
        rescaleLayout(0.25, 1)
        Guess.setSynchronous(false)

    def post(self):
        g.nodes.visible = 1
        g.edges.visible = 1
        (entity=='package').color='white'
        (entity=='package').labelvisible=1
        add(self.other)
        self.__resizeOnMetrics()
        self.__colorOnCaseType()
        self.__movePackages()
        centerNodeHorizontal(root)
        rroot.x = root.x
        self.other.visible = 0

    def __initPkgSubs(self, gl):
        other = ((entity=='smell')|(entity=='testcommand')|(entity=='testhelper')|(entity=='testfixture'))
        other = remove(other)
        other.extend(remove((entity=='package')->complement(gl.tcsubs)))
        add(gl.tcsubs)
        return other

    def __resizeOnMetrics(self):
        ''' adapt the testcase node's height based on its number of testcommands
            adapt the testcase node's width based on a relative SLOC metric 
                RTCLOC= SLOC / #COMMANDS'''
        for tc in (entity == 'testcase'):
            numCmd = len(tc->(entity=='testcommand'))
            # heigth
            tc.height = max(numCmd,1) * 10
            tc.color = 'white'
            # width
            try:
                sloc = self.globalz.metricDict['testcase'][tc.name]['SLOC']
            except KeyError:
                sloc = 1
            if numCmd == 0: numCmd = 1
            tc.width = max(3, sloc/numCmd)

    def __movePackages(self):
        ''' move the packages a bit so that the labels dont overlap '''
        pkgs = (entity == 'package')
        pkgs.x # needed to initialize, otherwise the sort is messed up
        pkgs.sort(lambda o,p: int(p.x - o.x))
        cntr = 0
        for pkg in pkgs:
            lvl = cntr % 3
            if lvl == 0:   pkg.y += 50
            elif lvl == 2: pkg.y -= 50
            cntr += 1

    def __colorOnCaseType(self):
        ''' give a different color based on the presence of fixture and helpers '''
        # nothing -> white
        # fixture -> blue
        # helpers -> green
        # fix+hlp -> red
        for tc in (entity == 'testcase'):
            hasFixture = 0 != len(tc -> (entity == 'testfixture'))
            hasHelper  = 0 != len(tc -> (entity == 'testhelper'))
            if hasFixture and hasHelper: tc.color = 'red'
            elif hasFixture: tc.color = 'blue'
            elif hasHelper: tc.color = 'green'
            else: tc.color = 'white'

def constructIndirection(from_, to, new):
    ''' Construct an intermediate node layer between 'from_' and 'to'
        Remove (and return) the edges between node 'from_' and the nodes 'to'. 
        Add edges between from_->new and new->to '''
    children = [ x.getNode2() for x in from_->to]
    if len(children) == 0: return
    remove([new])
    add([new])
    addDirectedEdge(from_,new)
    for child in children:
        addDirectedEdge(new, child)
    return remove(from_->children)

cnt = 0

class TreeCaseView(GraphView):
    ''' Show a polymetric view of a single test case '''

    def __init__(self, gl, testcase):
        GraphView.__init__(self, gl)
        self.testcase = testcase

    def pre(self):
        self.other = self.__removeOther()
        self.__constructExtraLevel()
        self.fakes = self.__addFakeSmells()

    def transform(self):
        Guess.setSynchronous(true)
        sugiyamaLayout()
        rescaleLayout(0.25,0.5)
        Guess.setSynchronous(false)

    def post(self):
        self.__fixLook()
        self.__restoreNodes()

    def __fixLook(self):
        g.nodes.visible = 1
        g.edges.visible = 1
        self.__whiteLabel(self.globalz.command)
        self.__whiteLabel(self.globalz.helper)
        self.__whiteLabel(self.globalz.fixture)
        self.testcase.color = 'white'
        self.testcase.labelvisible = 1
        #self.testcase.x = (x.max - x.min) / 2
        centerNodeHorizontal(self.testcase)
        self.globalz.sub.x = self.testcase.x
        self.__centerSingleLeave(self.globalz.command)
        self.__centerSingleLeave(self.globalz.helper)
        self.__centerSingleLeave(self.globalz.fixture)

    def __centerSingleLeave(self, super):
        numChildren = len(super->g.nodes)
        if numChildren == 1:
            child = (super->g.nodes)[0].getNode2()
            super.x = child.x
        elif numChildren == 0:
            remove([super])
 
    def __restoreNodes(self):
        add(self.other)
        self.other.visible = 0
        add(self.ori)
        self.ori.visible = 0
        remove(self.fakes)

    def __whiteLabel(self, node):
        node.style = 1
        node.labelvisible = 1
        node.color = 'white'

    def __constructExtraLevel(self):
        sub = self.globalz.sub
        self.ori  = constructIndirection(self.testcase, g.nodes, sub)
        constructIndirection(sub, (entity == 'testcommand'), self.globalz.command)
        constructIndirection(sub, (entity == 'testhelper'),  self.globalz.helper)
        constructIndirection(sub, (entity == 'testfixture'), self.globalz.fixture)

    def __removeOther(self):
        lvl1 = [x.getNode2() for x in self.testcase->g.nodes]
        lvl2 = [x.getNode2() for x in lvl1->g.nodes]
        lvl3 = [x.getNode2() for x in lvl2->g.nodes]
        all = [self.testcase, self.globalz.sub] + lvl1 + lvl2 + lvl3
        return remove(complement(all))

    def __testmethods(self):
        return ((entity == 'testcommand') | (entity == 'testfixture') | (entity == 'testhelper'))

    def __addFakeSmells(self):
        global cnt
        fakes = []
        for mtd in self.__testmethods():
            if len(mtd->g.nodes) == 0:
                fake = addNode('place_holder_node' + str(cnt))
                addDirectedEdge(mtd, fake)
                fakes.append(fake)
                cnt += 1
        return fakes

def viewCase(tcName):
    tc = (name == tcName)[0]
    TreeCaseView(glzz, tc).go()

def vc():
    viewCase('testmessenger::UtilsTest')

def rescaleCenter(factor):
    Guess.setSynchronous(true)
    rescaleLayout(factor)
    Guess.setSynchronous(false)

def plotSmellFrequency():
    other = remove((complement(entity == 'smell')))
    plotSizesPie(label, 1)
    add(other)

def tcMenu():
    caseSubMenu = JMenu("case view")
    for pkg in (entity == 'package'):
        pkgSubMenu = JMenu(pkg.name)
        cases = [ x.getNode2() for x in (pkg->(entity == 'testcase'))]
        for case in cases:
            caseItem = JMenuItem(case.name)
            caseItem.actionPerformed = lambda event : viewCase(event.getActionCommand())
            pkgSubMenu.add(caseItem)
        caseSubMenu.add(pkgSubMenu)
    return caseSubMenu

def getParent(node):
    # returns None if no or multiple parents
    parent = None
    if len(targetNode<-g.nodes) == 1:
        parent = (node<-g.nodes)[0].getNode2()
    return parent

def openCaseView(targetNode):
    global glzz
    case = None
    ent = targetNode.entity[0]
    if ent == 'testcase':
        # we'r good
        case = targetNode[0]
    elif ent in ['testcommand', 'testhelper', 'testfixture']:
        # get owner
        case = getParent(targetNode[0])
    elif ent == 'smell':
        owner = getParent(targetNode[0])
        if owner != None: case = getParent(owner)
    if case != None: TreeCaseView(glzz, case).go()
    else: print "failed to find owner testcase"

def createCaseViewContextAction():
    ''' add the view case context action to the guess graph'''
    casev = NodeEditorPopup.addItem("viewCase")
    casev.menuEvent = openCaseView

def initMenu():
    global glzz
    tmenu = JMenu("tsmells")

    smellv = JMenuItem("smell view")
    smellv.actionPerformed = lambda event : SmellView(glzz).go()
    tmenu.add(smellv)

    radialv = JMenuItem("radial suite view")
    radialv.actionPerformed = lambda event : RadialSuiteView(glzz).go()
    tmenu.add(radialv)

    treesv = JMenuItem("tree suite view")
#    treesv.actionPerformed = lambda event : displayTreeSuiteView(glzz)
    treesv.actionPerformed = lambda event : TreeSuiteView(glzz).go()
    tmenu.add(treesv)

    tmenu.add(tcMenu())

    smellp = JMenuItem("smell pie")
    smellp.actionPerformed = lambda event : plotSmellFrequency()
    tmenu.add(smellp)

    gbar = Guess.getMainUIWindow().getGMenuBar()
    gbar.add(tmenu)
    gbar.revalidate()
    gbar.repaint()

# construct it
glzz = globalz()
initMenu()
createCaseViewContextAction()
