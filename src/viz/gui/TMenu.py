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
    Guess.setSynchronous(true)
    radialLayout(root)
    rescaleCenter(4)
    Guess.setSynchronous(false)
    remove([root])
    g.nodes.visible = 1
    add(smells)

def rescaleCenter(factor):
    Guess.setSynchronous(true)
    rescaleLayout(factor)
    #center()
    Guess.setSynchronous(false)

def plotSmellFrequency():
    other = remove((complement(entity == 'smell')))
    plotSizesPie(label, 1)
    add(other)

tmenu = JMenu("tsmells")

smellv = JMenuItem("smell view")
smellv.actionPerformed = lambda event : displaySmellView()
tmenu.add(smellv)

radialv = JMenuItem("radial suite view")
radialv.actionPerformed = lambda event : displayRadialSuiteView()
tmenu.add(radialv)

shrink = JMenuItem("grow")
shrink.actionPerformed = lambda event : rescaleCenter(0.5)
tmenu.add(shrink)

grow = JMenuItem("shrink")
grow.actionPerformed = lambda event : rescaleCenter(2)
tmenu.add(grow)

smellp = JMenuItem("smell pie")
smellp.actionPerformed = lambda event : plotSmellFrequency()
tmenu.add(smellp)

gbar = Guess.getMainUIWindow().getGMenuBar()
gbar.add(tmenu)
gbar.revalidate()
gbar.repaint()
