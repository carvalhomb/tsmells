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

from com.hp.hpl.guess.ui import DockableAdapter
from javax.swing         import JButton
from java.awt            import GridBagLayout,\
                                GridBagConstraints

class RescalePanel(DockableAdapter):

    def __init__(self):
        self.setLayout(GridBagLayout())

        shrinkX = JButton("shrinkX")
        shrinkX.actionPerformed = lambda event : rescaleLayout(0.5, 1)
        constr = GridBagConstraints()
        constr.gridx = 0
        constr.gridy = 0
        self.add(shrinkX, constr)

        growX = JButton("growX")
        growX.actionPerformed = lambda event : rescaleLayout(1.5, 1)
        constr = GridBagConstraints()
        constr.gridx = 2
        constr.gridy = 0
        self.add(growX, constr)

        shrinkY = JButton("shrinkY")
        shrinkY.actionPerformed = lambda event : rescaleLayout(1, 0.5)
        constr = GridBagConstraints()
        constr.gridx = 0
        constr.gridy = 2
        self.add(shrinkY, constr)

        growY = JButton("growY")
        growY.actionPerformed = lambda event : rescaleLayout(1, 1.5)
        constr = GridBagConstraints()
        constr.gridx = 2
        constr.gridy = 2
        self.add(growY, constr)

        centerB = JButton("center")
        centerB.actionPerformed = lambda event : center()
        constr = GridBagConstraints()
        constr.gridx = 1
        constr.gridy = 1
        self.add(centerB, constr)

        ui.dock(self)

    def getTitle(self):
        return("Rescale Graph")
