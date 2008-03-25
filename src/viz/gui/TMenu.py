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

from com.hp.hpl.guess import Guess
from com.hp.hpl.guess.layout import GEM, BinPack
from javax.swing import JMenu, JMenuItem
from java.awt.event import ActionListener


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

def displaySmellView():
    global subs

    remove(subs)
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

def displayRadialSuiteView():
    global roots
    global subs

    remove(subs)
    g.nodes.visible = 0
    resetColors()
    smells = remove((entity=='smell'))
    add(roots)
    remove([rroot])
    Guess.setSynchronous(true)
    radialLayout(root)
    rescaleCenter(4)
    Guess.setSynchronous(false)
    remove([root])
    g.nodes.visible = 1
    add(smells)


subs = []
def displayTreeSuiteView():
    global roots
    global subs

    g.nodes.visible = 0
    add(roots)
    other = ((entity=='smell')|(entity=='testcommand')|(entity=='testhelper')|(entity=='testfixture'))
    other.visible = 0
    other = remove(other)
    if len(subs) == 0: subs = initSubs()
    other.extend(remove((entity=='package')->g.nodes))
    add(subs)
    (entity=='package').color='white'
    (entity=='package').labelvisible=1
    Guess.setSynchronous(true)
    sugiyamaLayout()
    Guess.setSynchronous(false)
    g.nodes.visible = 1
    add(other)
    other.visible = 0

def initSubs():
    subs = []
    for pkg in (entity == 'package'):
        sub = addNode(str(pkg) + "sub")
        subs.append(sub)
        addDirectedEdge(pkg, sub)
        for edge in pkg->(g.nodes):
            n2 = edge.getNode2()
            print str(pkg) + " - " + str(n2) + " <> " + str(edge) 
            if n2.name == sub.name: continue
            addDirectedEdge(sub, n2)
    resetColors()
    return remove(subs)

def rescaleCenter(factor):
    Guess.setSynchronous(true)
    rescaleLayout(factor)
    #center()
    Guess.setSynchronous(false)

def plotSmellFrequency():
    other = remove((complement(entity == 'smell')))
    plotSizesPie(label, 1)
    add(other)

def fixRoot():
    ''' add two root nodes & remove them '''

    root = addNode('root')
    rroot = addNode('rroot')
    addDirectedEdge(rroot, root)
    for pkg in (entity == 'package'):
        addDirectedEdge(root, pkg)
    resetColors()
    return remove([root, rroot])

def initMenu():
    tmenu = JMenu("tsmells")

    smellv = JMenuItem("smell view")
    smellv.actionPerformed = lambda event : displaySmellView()
    tmenu.add(smellv)

    radialv = JMenuItem("radial suite view")
    radialv.actionPerformed = lambda event : displayRadialSuiteView()
    tmenu.add(radialv)

    treesv = JMenuItem("tree suite view")
    treesv.actionPerformed = lambda event : displayTreeSuiteView()
    tmenu.add(treesv)

    smellp = JMenuItem("smell pie")
    smellp.actionPerformed = lambda event : plotSmellFrequency()
    tmenu.add(smellp)


    shrink = JMenuItem("grow")
    shrink.actionPerformed = lambda event : rescaleCenter(0.5)
    tmenu.add(shrink)

    grow = JMenuItem("shrink")
    grow.actionPerformed = lambda event : rescaleCenter(2)
    tmenu.add(grow)


    gbar = Guess.getMainUIWindow().getGMenuBar()
    gbar.add(tmenu)
    gbar.revalidate()
    gbar.repaint()


roots = fixRoot()
initMenu()
