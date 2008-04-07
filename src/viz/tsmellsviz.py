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

import os, jarray, cPickle

from com.hp.hpl.guess.ui    import StatusBar
from java.lang              import Thread, Runnable
from java.awt.geom          import GeneralPath
from java.awt               import Polygon

#TODO get rid of this
class Globalz:

    def __init__(self):
        global metricDict

        self.metricDict = metricDict
        self.roots = self.__initRoots() # 2 root nodes
        self.tcsubs = self.__initTcSubs() # an extra node between testcases and package
        self.__initMetaNodes()
        #resetLook()
        g.nodes.visible = 0

    def __initMetaNodes(self):
        self.sub = addNode("sub_node")
        self.command = addNode("commands")
        self.helper = addNode("helpers")
        self.fixture = addNode("fixture")
        remove([self.sub, self.command, self.helper, self.fixture])

    def __initRoots(self):
        ''' add two root nodes'''
        root  = addNode('root')
        rroot = addNode('rroot')
        addDirectedEdge(rroot, root)
        for pkg in (entity == 'package'):
            addDirectedEdge(root, pkg)
        return remove([root, rroot])

    def __initTcSubs(self):
        ''' add a sub node between testcases and their package '''
        # TODO remove this; use 'ConstructExtraLevel'
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

    def resetGraph(self):
        remove([self.sub, self.command, self.helper, self.fixture])

def createDiamondShape():
    xpoints = jarray.array((10,5,0,5),'i')
    ypoints = jarray.array((5,10,5,0),'i')

    diamond = Polygon(xpoints,ypoints,4);
    shapeDB.addShape(104,diamond)

def constructTMenu():
    global glzz

    initMyColors()
    initMenu()

def constructPanels():
    global srcDict
    global metricDict

    RescalePanel()
    TestSuitePanel()
    SmellIdentiKitPanel(srcDict, metricDict)

def constructContextActions():
    global srcDict
    global rootDir
    rootDir = srcDict['ProjectSourceRootDirectory']

    addToSourceAction()
    addDumpDupliAction()
    addWriteMetricsAction()
    addViewCaseAction()

class Loader(Runnable):

    def run(self):
        global glzz

        StatusBar.runProgressBar(true)
        self.__loadGdf()
        self.__execScripts()
        self.__loadPickleFiles()
        glzz = Globalz()
        self.__initGui()

        StatusBar.runProgressBar(false)
        StatusBar.setStatus("done.")

    def __loadGdf(self):
        StatusBar.setStatus("loading GDF [can take a while]")
        setDisplayBackground("black")
        makeFromGDF(os.environ['TSMELLS_GDF'])
        g.edges.color = 'lightgray'
        g.nodes.visible = 0

    def __execScripts(self):
        StatusBar.setStatus("initializing scripts")
        TSMELLS_VIZ= os.environ['TSMELLS'] + '/src/viz'
        execfile(TSMELLS_VIZ + '/gui/TMenu.py')
        execfile(TSMELLS_VIZ + "/gui/Metrics.py")
        execfile(TSMELLS_VIZ + "/gui/GraphViews.py")
        #execfile(TSMELLS_VIZ + '/gui/TestCaseList.py')
        execfile(TSMELLS_VIZ + '/gui/SmellIndentiKit.py')
        execfile(TSMELLS_VIZ + '/gui/ToSourceContext.py')
        execfile(TSMELLS_VIZ + '/gui/TestSuiteTree.py')
        execfile(TSMELLS_VIZ + '/gui/RescalePanel.py')

    def __loadPickleFiles(self):
        global srcDict
        global metricDict

        srcDict = loadSrcDict()
        metricDict = loadMetricDict()

    def __initGui(self):
        constructTMenu()
        constructContextActions()
        constructPanels()

# Some global variables
srcDict = {}
metricDict = {}
glzz = None

# Go
Thread(Loader()).start()
