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

from com.hp.hpl.guess        import Guess
from com.hp.hpl.guess.layout import GEM, BinPack
from javax.swing             import JMenu, JMenuItem
from javax.swing.event       import ChangeListener
from java.awt.event          import ActionListener

class globalz:
    def __init__(self):
        # 2 root nodes
        self.roots = self.__initRoots()
        # an extra node between testcases and their package
        self.tcsubs = self.__initTcSubs()
        g.nodes.visible = 0

    def __initRoots(self):
        ''' add two root nodes'''
        root = addNode('root')
        rroot = addNode('rroot')
        addDirectedEdge(rroot, root)
        for pkg in (entity == 'package'):
            addDirectedEdge(root, pkg)
        reset()
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
        reset()
        return remove(subs)

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

def reset():
    g.nodes.color = 'black'
    g.edges.color = 'lightgray'
    (entity == 'smell').color = 'red'
    g.nodes.width = 10
    g.nodes.height = 10

def initPkgSubs(gl):
    other = ((entity=='smell')|(entity=='testcommand')|(entity=='testhelper')|(entity=='testfixture'))
    other = remove(other)
    other.extend(remove((entity=='package')->complement(gl.tcsubs)))
    add(gl.tcsubs)
    (entity=='package').color='white'
    (entity=='package').labelvisible=1
    return other


from com.hp.hpl.guess.ui import StatusBar
from java.lang           import Thread, Runnable, Double
from time import time

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

        self.pre() # abstract, bring the graph in the correct state. remove/add nodes
        self.transform() # abstract, execute the layout algorithm
        self.post() # abstract, restore the graph

        el = str(time() - start)
        el = el.split('.')[0] +  '.' + el.split('.')[1][0:3]
        StatusBar.setStatus("Done. [" + el + "sec]")


class SmellView(GraphView):
    ''' show the smell view layout '''

    def pre(self):
        remove(self.globalz.tcsubs)
        remove(self.globalz.roots)
        g.nodes.visible = 0
        self.pkgs = remove((entity=='package'))
        reset()
        (entity == 'smell').color = 'red'

    def transform(self):
        StatusBar.setStatus("Computing GEM smell view [can take a while]")
        transformToGEM()
        StatusBar.setStatus("Transforming to binPack ...")
        transformToBIN()

    def post(self):
        g.nodes.visible = 1
        add(self.pkgs)

class RadialSuiteView(GraphView):
    ''' thread which performs the smell view layout '''

    def pre(self):
        g.nodes.visible = 0
        reset()
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
    ''' Build a test suite tree '''

    def pre(self):
        g.nodes.visible = 0
        add(self.globalz.roots)
        self.other = initPkgSubs(self.globalz)

    def transform(self):
        Guess.setSynchronous(true)
        sugiyamaLayout()
        Guess.setSynchronous(false)

    def __numCommandsMetric(self):
        ''' adapt the testcase node's height based on its number of testcommands'''
        for tc in (entity == 'testcase'):
            numCmd = len(tc->(entity=='testcommand'))
            tc.height = numCmd * 10
            tc.color = 'white'

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

    def post(self):
        g.nodes.visible = 1
        g.edges.visible = 1
        add(self.other)
        self.__numCommandsMetric()
        self.__colorOnCaseType()
        self.other.visible = 0

def rescaleCenter(factor):
    Guess.setSynchronous(true)
    rescaleLayout(factor)
    Guess.setSynchronous(false)

def plotSmellFrequency():
    other = remove((complement(entity == 'smell')))
    plotSizesPie(label, 1)
    add(other)

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
