#!/usr/bin/python
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

import os, com, java, java.lang, jarray, cPickle, time

import com.hp.hpl.guess.Guess
import com.hp.hpl.guess.Graph

import java.awt.geom.GeneralPath
import java.awt.Polygon

def createDiamondShape():
    xpoints = jarray.array((10,5,0,5),'i')
    ypoints = jarray.array((5,10,5,0),'i')

    diamond = Polygon(xpoints,ypoints,4);
    shapeDB.addShape(104,diamond)

def loadData():
    setDisplayBackground("black")
    makeFromGDF(os.environ['TSMELLS_GDF'])
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

def plotSmellFrequency():
    other = remove((complement(entity == 'smell')))
    plotSizesPie(label, 1)
    add(other)


TSMELLS=os.environ['TSMELLS']
TSMELLS_VIZ=TSMELLS + '/src/viz'

loadData()
ControlPanel()
plotSmellFrequency()

execfile(TSMELLS_VIZ + '/gui/TestCaseList.py')
execfile(TSMELLS_VIZ + '/gui/ToSourceContext.py')

