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

import os, com, java, java.lang, jarray

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


TSMELLS=os.environ['TSMELLS']
TSMELLS_VIZ=TSMELLS + '/src/viz'

loadData()

execfile(TSMELLS_VIZ + '/gui/TestCaseList.py')
execfile(TSMELLS_VIZ + '/gui/SmellIndentiKit.py')
execfile(TSMELLS_VIZ + '/gui/ToSourceContext.py')
execfile(TSMELLS_VIZ + '/gui/TestSuiteTree.py')
execfile(TSMELLS_VIZ + '/gui/TMenu.py')
