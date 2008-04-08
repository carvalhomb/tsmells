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

from com.hp.hpl.guess.ui import StatusBar
from java.lang           import Thread, Runnable, Double
from time                import time

#-----------------------------------------------------------------------
#--  global variables
#-----------------------------------------------------------------------

myColors = \
    { \
        "sun1" :     GuessColor(51, 51, 102), \
        "sun2" :     GuessColor(102, 102, 154), \
        "sun3" :     GuessColor(153, 153, 203), \
        "seablue_" : GuessColor(0, 102, 203), \
        "magenta1" : GuessColor(107, 35, 148), \
        "magenta2" : GuessColor(102, 0, 102), \
        "gray20" :   GuessColor(179, 179, 179), \
        "gray40" :   GuessColor(153, 153, 154), \
        "gray60" :   GuessColor(102, 102, 102), \
        "gray80" :   GuessColor(51, 51, 51), \
    }

smellColors = \
    { \
        "AssertionRoulette" : "sun1", \
        "AssertionLess" :     "sun2", \
        "DuplicatedCode" :    "sun3", \
        "EagerTest" :         "seablue_", \
        "ForTestersOnly" :    "magenta1", \
        "GeneralFixture" :    "magenta2", \
        "IndentedTest" :      "gray20", \
        "IndirectTest" :      "gray40", \
        "MysteryGuest" :      "gray60", \
        "SensitiveEquality" : "gray80"\
    }

#-----------------------------------------------------------------------
#--  auxiliary functions
#-----------------------------------------------------------------------

def initMyColors():
    for clrName, color in myColors.items():
        Colors.putColor(clrName, color)

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
            smells.color = 'red'
    g.nodes.width = 10
    g.nodes.height = 10
    g.nodes.labelvisible = 0

def centerNodeHorizontal(node):
    node.x = (x.max - x.min) / 2

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

#-----------------------------------------------------------------------
#--  classes
#-----------------------------------------------------------------------

class GraphView(Runnable):
    ''' abstract class. performs a layout in a seperate thread '''

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
    ''' All testcases as seperate graphs with methods and smell nodes
        Layout uses GEM followed by binpack'''

    def pre(self):
        remove(self.globalz.tcsubs)
        remove(self.globalz.roots)
        self.pkgs = remove((entity=='package'))

    def transform(self):
        StatusBar.setStatus("Computing GEM smell view [can take a while]")
        fto = remove(label == 'ForTestersOnly') # huge performace boost
        Guess.setSynchronous(true)
        graph = Guess.getGraph()
        graph.layout(GEM(graph))
        StatusBar.setStatus("Transforming to binPack ...")
        add(fto)
        graph.layout(BinPack(graph, true))
        center()
        Guess.setSynchronous(false)

    def post(self):
        g.nodes.visible = 1
        add(self.pkgs)

class RadialSuiteView(GraphView):
    ''' Complete testsuite shown as concentric circles:
            - packages
            - testcases
            - testmethods '''

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
        try: rescaleLayout(0.25, 1)
        except: pass
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
        try: rescaleLayout(0.25,0.5)
        except: pass
        Guess.setSynchronous(false)

    def post(self):
        self.__fixLook()
        self.__fixPosition()
        self.__restoreNodes()

    def __fixLook(self):
        g.nodes.visible = 1
        g.edges.visible = 1
        self.__whiteLabel(self.globalz.command)
        self.__whiteLabel(self.globalz.helper)
        self.__whiteLabel(self.globalz.fixture)
        self.testcase.color = 'white'
        self.testcase.labelvisible = 1

    def __fixPosition(self):
        centerNodeHorizontal(self.testcase)
        self.globalz.sub.x = self.testcase.x
        self.__centerSingleLeave(self.globalz.command)
        self.__centerSingleLeave(self.globalz.helper)
        self.__centerSingleLeave(self.globalz.fixture)
        self.__hideUnwantedMeta()

    def __centerSingleLeave(self, super):
        numChildren = len(super->g.nodes)
        if numChildren == 1:
            child = (super->g.nodes)[0].getNode2()
            super.x = child.x

    def __hideUnwantedMeta(self):
        # work-around for guess glitch
        for meta in [self.globalz.command, self.globalz.helper, self.globalz.fixture]:
            numChildren = len(meta->g.nodes)
            if numChildren == 0: meta.visible = 0

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
        fakes.visible = 0
        return fakes

