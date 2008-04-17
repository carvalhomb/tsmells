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

from com.hp.hpl.guess.ui import Dockable

class TDockable(JPanel, Dockable):

    #
    # Implementation of Dockable interface
    #

    def mouseEnterEdge(self, edge):
        pass

    def mouseLeaveNode(self, node):
        pass

    def mouseLeaveEdge(self, edge):
        pass

    def getPreferredSize(self):
        return Dimension(200,600)

    def getDefaultFrameBounds(self):
        return Rectangle(50, 50, 300, 600)

    def getDirectionPreference(self):
        ''' prefer vertical orientation '''
        return 2 # vertical, see com.hp.hpl.guess.ui.MainUIWindow.java

    def opening(self, state):
        self.visible = state

    def attaching(self, state):
        pass

    def getTitle(self):
        return("")

    def getWindow(self):
        return self.myParent

    def setWindow(self,gjf):
        self.myParent = gjf
