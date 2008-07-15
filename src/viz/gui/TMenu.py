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
from com.hp.hpl.guess.ui     import GuessColor, Colors, GMenuBar
from javax.swing             import JMenu, JMenuItem
from javax.swing.event       import ChangeListener
from java.awt.event          import ActionListener


def viewCase(tcName):
    tc = (name == tcName)[0]
    TreeCaseView(glzz, tc).go()

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

def addViewCaseAction():
    ''' add the view case context action to the guess graph'''
    casev = NodeEditorPopup.addItem("viewCase")
    casev.menuEvent = openCaseView

def openCaseView2(targetNode):
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
    if case != None: TreeCaseView2(glzz, case).go()
    else: print "failed to find owner testcase"

def addViewCase2Action():
    ''' add the view case context action to the guess graph'''
    casev = NodeEditorPopup.addItem("viewCase2")
    casev.menuEvent = openCaseView2

def initMenu():
    global glzz
    tmenu = JMenu("testQ")

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
    gbar.getMenu(3).removeAll() # deface the layout menu
    gbar.revalidate()
    gbar.repaint()

