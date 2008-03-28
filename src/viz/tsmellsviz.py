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

import os, jarray

from com.hp.hpl.guess.ui    import StatusBar
from java.lang              import Thread, Runnable
from java.awt.geom          import GeneralPath
from java.awt               import Polygon

def createDiamondShape():
    xpoints = jarray.array((10,5,0,5),'i')
    ypoints = jarray.array((5,10,5,0),'i')

    diamond = Polygon(xpoints,ypoints,4);
    shapeDB.addShape(104,diamond)

def writeStats():
    print "nodes\t\t" + str(len(g.nodes))
    print "edges\t\t" + str(len(g.edges))
    print "pkgs \t\t" + str(len(entity == 'package'))
    print "cases\t\t" + str(len(entity == 'testcase'))
    print "cmds \t\t" + str(len(entity == 'testcommand'))
    print "hlprs\t\t" + str(len(entity == 'testhelper'))
    print "fixt \t\t" + str(len(entity == 'testfixture'))
    print "smells\t\t" + str(len(entity == 'smell'))

class Loader(Runnable):
    def run(self):
        StatusBar.runProgressBar(true)
        self.__loadGdf()
        self.__execScripts()
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
        execfile(TSMELLS_VIZ + '/gui/TestCaseList.py')
        execfile(TSMELLS_VIZ + '/gui/SmellIndentiKit.py')
        execfile(TSMELLS_VIZ + '/gui/ToSourceContext.py')
        execfile(TSMELLS_VIZ + '/gui/TestSuiteTree.py')
        execfile(TSMELLS_VIZ + '/gui/RescalePanel.py')

Thread(Loader()).start()
