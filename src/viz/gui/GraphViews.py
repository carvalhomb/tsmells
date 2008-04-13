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
from java.lang           import Thread, Runnable
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
    vis = (visible == 1)
    node.x = (max(vis.x) - min(vis.x)) / 2

def constructIndirection(from_, to, new):
    ''' Construct an intermediate node layer between 'from_' and 'to'
        Remove (and return) the edges between node 'from_' and the nodes 'to'. 
        Add edges between from_->new and new->to '''
    children = (from_->to).destination
    if len(children) == 0: return
    remove([new])
    add([new])
    addDirectedEdge(from_,new)
    for child in children:
        addDirectedEdge(new, child)
    return remove(from_->children)

def shuffleNodesYAxis(toShuffle):
    ''' move one layer of nodes in a tree a bit such that their labels dont overlap'''
    toShuffle.x # needed to initialize, otherwise the sort is messed up
    toShuffle.sort(lambda o,p: int(p.x - o.x))
    cntr = 0
    for node in toShuffle:
        lvl = cntr % 3
        if lvl == 0:   node.y += 25
        elif lvl == 2: node.y -= 25
        cntr += 1


metaCnt = 0
def createMetaNode(name_):
    global metaCnt
    node = addNode(name_ + "_meta" + str(metaCnt))
    node.entity = 'meta'
    node.label = name_
    metaCnt += 1
    return node

#-----------------------------------------------------------------------
#--  classes
#-----------------------------------------------------------------------

class GraphView(Runnable):
    ''' abstract class. performs a layout in a seperate thread '''

    def __init__(self, gl):
        self.graph  = Guess.getGraph()
        self.interp = Guess.getInterpreter()
        self.gl = gl

    def go(self):
        thread = Thread(self)
        thread.start()

    def run(self):
        start = time()
        g.nodes.visible = 0
        self.gl.resetGraph()

        unwanted = self.pre()       # abstract, bring the graph in the correct state.
        unwanted = remove(unwanted) # return unwanted nodes, those that should be hidden
        g.nodes.visible = 0

        Guess.setSynchronous(true)
        self.transform()            # abstract, execute the layout algorithm(s)
        Guess.setSynchronous(false)
        resetLook()

        g.nodes.visible = 1
        g.edges.visible = 1
        self.__restore(unwanted)
        self.post()                 # abstract, fix colors, position etc

        el = str(time() - start)
        el = el.split('.')[0] +  '.' + el.split('.')[1][0:3]
        StatusBar.setStatus("Done. [" + el + "sec]")
        self.__moveOutOfTheWay()

    def __restore(self, unwanted):
        add(unwanted)
        unwanted.visible = 0


    def __moveOutOfTheWay(self):
        ''' move all unwanted node; otherwise they might block selection of 
            visible nodes '''
        mid = x.max - x.min
        for elem in (visible == 0):
            try:
                elem.x = mid
                elem.y = 5000
            except: pass


class SmellView(GraphView):
    ''' All testcases as seperate graphs with methods and smell nodes
        Layout uses GEM followed by binpack'''

    #
    # GraphView interface implemenation
    #

    def pre(self):
        remove(self.gl.tcsubs)
        remove(self.gl.roots)
        return (entity == 'package')

    def transform(self):
        StatusBar.setStatus("Computing GEM smell view [can take a while]")
        fto = remove(label == 'ForTestersOnly') # huge performace boost
        gemLayout()
        StatusBar.setStatus("Transforming to binPack ...")
        add(fto)
        binPackLayout()
        center()

    def post(self):
        pass

class RadialSuiteView(GraphView):
    ''' Complete testsuite shown as concentric circles:
            - packages
            - testcases
            - testmethods '''

    #
    # GraphView interface implemenation
    #

    def pre(self):
        add(self.gl.roots)
        add(self.gl.tcsubs)
        other = (entity=='package')->complement(self.gl.tcsubs)
        other += (entity == 'smell')
        remove([rroot])
        return other

    def transform(self):
        StatusBar.setStatus("computing radial suite view")
        radialLayout(root)
        #try: rescaleLayout(0.5, 0.5)
        #except: pass

    def post(self):
        pass

class TreeSuiteView(GraphView):
    ''' Show a polymetric view of the testsuite '''

    #
    # GraphView interface implemenation
    #

    def pre(self):
        add(self.gl.roots)
        unwanted = ((entity=='smell') |\
                    (entity=='testcommand') |\
                    (entity=='testhelper') | \
                    (entity=='testfixture'))
        unwanted.extend((entity=='package')->complement(self.gl.tcsubs))
        add(self.gl.tcsubs)
        return unwanted

    def transform(self):
        sugiyamaLayout()
        try: rescaleLayout(0.10, 1)
        except: pass

    def post(self):
        (entity=='package').color='white'
        (entity=='package').labelvisible=1
        self.__resizeOnMetrics()
        self.__colorOnCaseType()
        self.__movePackages()
        centerNodeHorizontal(root)
        rroot.x = root.x

    #
    # PostProcessing helpers
    #

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
                sloc = self.gl.metricDict['testcase'][tc.name]['SLOC']
            except KeyError:
                sloc = 1
            if numCmd == 0: numCmd = 1
            tc.width = max(3, sloc/numCmd)

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
    ''' Show the polymetric view of a single test case '''

    #
    # Constructor
    #

    def __init__(self, gl, testcase):
        GraphView.__init__(self, gl)
        self.testcase = testcase

    #
    # GraphView interface implemenation
    #

    def pre(self):
        self.ori   = self.__constructExtraLevel()
        self.fakes = self.__addFakeSmells()
        return self.__computeUnwanted()

    def transform(self):
        sugiyamaLayout()
        try: rescaleLayout(0.25,0.5)
        except: pass

    def post(self):
        self.__fixLook()
        self.__fixPosition()
        self.__hideUnwantedMeta()
        self.__restoreNodes()

    #
    # Preprocessing helpers
    #

    def __constructExtraLevel(self):
        sub = self.gl.sub
        original  = constructIndirection(self.testcase, g.nodes, sub)
        constructIndirection(sub, self.__myCommands(), self.gl.command)
        constructIndirection(sub, self.__myHelpers(),  self.gl.helper)
        constructIndirection(sub, self.__myFixture(),  self.gl.fixture)
        return original

    def __computeUnwanted(self):
        lvl1 = self.testcase.successors
        lvl2 = lvl1.successors
        lvl3 = lvl2.successors
        lvl4 = lvl3.successors
        all = [self.testcase, self.gl.sub] + lvl1 + lvl2 + lvl3 + lvl4
        return complement(all)

    def __addFakeSmells(self):
        ''' add fake smells to balance the tree, since sugiyama-layout gets ugly otherwise '''
        global cnt
        fakes = []
        for mtd in self.__myMethods():
            if len(mtd->g.nodes) == 0:
                fake = addNode('place_holder_node' + str(cnt))
                addDirectedEdge(mtd, fake)
                fakes.append(fake)
                cnt += 1
        fakes.visible = 0
        return fakes

    #
    # Postprocessing helpers
    #

    def __fixLook(self):
        self.__whiteLabel(self.gl.command)
        self.__whiteLabel(self.gl.helper)
        self.__whiteLabel(self.gl.fixture)
        self.testcase.color = 'white'
        self.testcase.labelvisible = 1

    def __fixPosition(self):
        centerNodeHorizontal(self.testcase)
        self.gl.sub.x = self.testcase.x
        self.__centerSingleLeave(self.gl.command)
        self.__centerSingleLeave(self.gl.helper)
        self.__centerSingleLeave(self.gl.fixture)

    def __hideUnwantedMeta(self):
        # work-around for guess glitch
        for meta in [self.gl.command, self.gl.helper, self.gl.fixture]:
            numChildren = len(meta->g.nodes)
            if numChildren == 0: meta.visible = 0

    def __restoreNodes(self):
        add(self.ori)
        self.ori.visible = 0
        remove(self.fakes)

    #
    # Misc helpers
    #

    def __centerSingleLeave(self, super):
        numChildren = len(super->g.nodes)
        if numChildren == 1:
            child = (super->g.nodes)[0].getNode2()
            super.x = child.x

    def __whiteLabel(self, node):
        node.style = 1
        node.labelvisible = 1
        node.color = 'white'

    def __myCommands(self):
        return self.__myMethodsTyped('testcommand')

    def __myHelpers(self):
        return self.__myMethodsTyped('testhelper')

    def __myFixture(self):
        return self.__myMethodsTyped('testfixture')

    def __myMethods(self):
        return self.__myCommands() +\
               self.__myHelpers()  +\
               self.__myFixture()

    def __myMethodsTyped(self, type_):
        supers = [self.testcase, self.gl.sub, \
                  self.gl.command, self.gl.helper, self.gl.fixture]
        return ((supers)->(entity == type_)).destination

cnt = 0
class TreeCaseView2(GraphView):
    ''' Show the polymetric view of a single test case '''

    #
    # Constructor
    #

    def __init__(self, gl, testcase):
        GraphView.__init__(self, gl)
        self.testcase = testcase

    #
    # GraphView interface implemenation
    #

    def pre(self):
        self.ori   = self.__constructExtraLevel()
        self.fakes = self.__addFakeSmells()
        return self.__computeUnwanted()

    def transform(self):
        sugiyamaLayout()
        try: rescaleLayout(0.25,0.5)
        except: pass

    def post(self):
        self.__fixLook()
        self.__fixPosition()
        self.__hideUnwantedMeta()
        self.__restoreNodes()

    #
    # Preprocessing helpers
    #

    def __constructExtraLevel(self):
        global cnt
        sub = self.gl.sub
        original  = constructIndirection(self.testcase, g.nodes, sub)
        constructIndirection(sub, self.__myCommands(), self.gl.command)
        constructIndirection(sub, self.__myHelpers(),  self.gl.helper)
        constructIndirection(sub, self.__myFixture(),  self.gl.fixture)
        for mtd in self.__myMethods():
            smells = mtd.successors
            if len(smells) == 0: continue
            #tmp = self.__createTempNode()
            #original += constructIndirection(mtd, smells, tmp)
            smells = groupBy(smells, label)
            for group in smells:
                smellMeta = createMetaNode(str(group[0].label))
                constructIndirection(mtd, group, smellMeta)
        return original

    def __computeUnwanted(self):
        all = [self.testcase]
        for i in range(0,6):
            all += all.successors
        all += [self.gl.sub]
        return complement(all)

    def __addFakeSmells(self):
        ''' add fake smells to balance the tree, since sugiyama-layout gets ugly otherwise '''
        fakes = []
        for mtd in self.__myMethods():
            if len(mtd->g.nodes) == 0:
                fake1 = self.__createFake(mtd)
                fake2 = self.__createFake(fake1)
                fakes.extend([fake1, fake2])
        fakes.visible = 0
        return fakes

    def __createFake(self, super):
        global cnt
        fake = addNode('fake_node_' + str(cnt))
        fake.entity = 'fake'
        fake.visible = 0
        addDirectedEdge(super, fake)
        cnt += 1
        return fake

    #
    # Postprocessing helpers
    #

    def __fixLook(self):
        self.__whiteLabel(self.gl.command)
        self.__whiteLabel(self.gl.helper)
        self.__whiteLabel(self.gl.fixture)
        self.testcase.color = 'white'
        self.testcase.labelvisible = 1
        meta = (entity == 'meta')
        meta.style = 1
        meta.color = 'white'
        meta.labelvisible = 1
        shuffleNodesYAxis(meta)

    def __fixPosition(self):
        centerNodeHorizontal(self.testcase)
        self.gl.sub.x = self.testcase.x
        self.__centerSingleLeave(self.gl.command)
        self.__centerSingleLeave(self.gl.helper)
        self.__centerSingleLeave(self.gl.fixture)

    def __hideUnwantedMeta(self):
        # work-around for guess glitch
        for meta in [self.gl.command, self.gl.helper, self.gl.fixture]:
            numChildren = len(meta->g.nodes)
            if numChildren == 0: meta.visible = 0

    def __restoreNodes(self):
        add(self.ori)
        self.ori.visible = 0
        remove(self.fakes)

    #
    # Misc helpers
    #

    def __createTempNode(self):
        global cnt
        tmp = addNode("tmp_" + str(cnt))
        tmp.entity = 'temp'
        cnt += 1
        return tmp

    def __centerSingleLeave(self, super):
        numChildren = len(super->g.nodes)
        if numChildren == 1:
            child = (super->g.nodes)[0].getNode2()
            super.x = child.x

    def __whiteLabel(self, node):
        node.style = 1
        node.labelvisible = 1
        node.color = 'white'

    def __myCommands(self):
        return self.__myMethodsTyped('testcommand')

    def __myHelpers(self):
        return self.__myMethodsTyped('testhelper')

    def __myFixture(self):
        return self.__myMethodsTyped('testfixture')

    def __myMethods(self):
        return self.__myCommands() +\
               self.__myHelpers()  +\
               self.__myFixture()

    def __myMethodsTyped(self, type_):
        supers = [self.testcase, self.gl.sub, \
                  self.gl.command, self.gl.helper, self.gl.fixture]
        return ((supers)->(entity == type_)).destination

cnt = 0
class TreeCaseView3(GraphView):
    ''' Show the polymetric view of a single test case '''

    #
    # Constructor
    #

    def __init__(self, gl, testcase):
        GraphView.__init__(self, gl)
        self.testcase = testcase

    #
    # GraphView interface implemenation
    #

    def pre(self):
        self.ori   = self.__constructExtraLevel()
        unwanted =  self.__computeUnwanted()
        self.__removeSmellEdges()
        self.__addMetaSmells()
        self.fakes = self.__addFakeSmells()
        return unwanted

    def transform(self):
        sugiyamaLayout()
        try: rescaleLayout(0.25,0.5)
        except: pass

    def post(self):
        add(self.smellEdges)
        self.smellEdges.visible = 1
        self.__fixLook()
        self.__fixPosition()
        self.__hideUnwantedMeta()
        self.__restoreNodes()


    #
    # Preprocessing helpers
    #

    def __constructExtraLevel(self):
        sub = self.gl.sub
        original  = constructIndirection(self.testcase, g.nodes, sub)
        self.smellMeta = createMetaNode('smells')
        addDirectedEdge(sub, self.smellMeta)
        constructIndirection(sub, self.__myCommands(), self.gl.command)
        constructIndirection(sub, self.__myHelpers(),  self.gl.helper)
        constructIndirection(sub, self.__myFixture(),  self.gl.fixture)
        return original

    def __removeSmellEdges(self):
        smellEdges = (self.__myMethods()->(entity == 'smell'))
        smellEdges += ((self.testcase)->(entity == 'smell'))
        self.smells = smellEdges.destination
        self.smellEdges = remove(smellEdges)

    def __addMetaSmells(self):
        grouped = groupBy(self.smells, label)
        for smellz in grouped:
            meta = createMetaNode(smellz[0].label)
            addDirectedEdge(self.smellMeta, meta)
            for smell in smellz:
                addDirectedEdge(meta, smell)

    def __computeUnwanted(self):
        lvl1 = self.testcase.successors
        lvl2 = lvl1.successors
        lvl3 = lvl2.successors
        lvl4 = lvl3.successors
        all = [self.testcase, self.gl.sub] + lvl1 + lvl2 + lvl3 + lvl4
        return complement(all)

    def __addFakeSmells(self):
        ''' add fake smells to balance the tree, since sugiyama-layout gets ugly otherwise '''
        global cnt
        fakes = []
        for mtd in self.__myMethods():
            if len(mtd->g.nodes) == 0:
                fake = addNode('place_holder_node' + str(cnt))
                addDirectedEdge(mtd, fake)
                fakes.append(fake)
                cnt += 1
        fakes.visible = 0
        return fakes

    #
    # Postprocessing helpers
    #

    def __fixLook(self):
        self.__whiteLabel(self.gl.command)
        self.__whiteLabel(self.gl.helper)
        self.__whiteLabel(self.gl.fixture)
        self.testcase.color = 'white'
        self.testcase.labelvisible = 1
        meta = (entity == 'meta')
        meta.color = 'white'
        meta.style = 1
        meta.labelvisible = 1

    def __fixPosition(self):
        centerNodeHorizontal(self.testcase)
        self.gl.sub.x = self.testcase.x
        self.__centerSingleLeave(self.gl.command)
        self.__centerSingleLeave(self.gl.helper)
        self.__centerSingleLeave(self.gl.fixture)

    def __hideUnwantedMeta(self):
        # work-around for guess glitch
        for meta in [self.gl.command, self.gl.helper, self.gl.fixture]:
            numChildren = len(meta->g.nodes)
            if numChildren == 0: meta.visible = 0

    def __restoreNodes(self):
        add(self.ori)
        self.ori.visible = 0
        remove(self.fakes)

    #
    # Misc helpers
    #

    def __centerSingleLeave(self, super):
        numChildren = len(super->g.nodes)
        if numChildren == 1:
            child = (super->g.nodes)[0].getNode2()
            super.x = child.x

    def __whiteLabel(self, node):
        node.style = 1
        node.labelvisible = 1
        node.color = 'white'

    def __myCommands(self):
        return self.__myMethodsTyped('testcommand')

    def __myHelpers(self):
        return self.__myMethodsTyped('testhelper')

    def __myFixture(self):
        return self.__myMethodsTyped('testfixture')

    def __myMethods(self):
        return self.__myCommands() +\
               self.__myHelpers()  +\
               self.__myFixture()

    def __myMethodsTyped(self, type_):
        supers = [self.testcase, self.gl.sub, \
                  self.gl.command, self.gl.helper, self.gl.fixture]
        return ((supers)->(entity == type_)).destination
