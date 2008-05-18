# This file is part of snavtofamix (Source Navigator to FAMIX).
#
# snavtofamix is free software; you can redistribute it and/or modify it 
# under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 2 of the License, or (at your 
# option) any later version.
#
# snavtofamix is distributed in the hope that it will be useful, but WITHOUT 
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more 
# details.
#
# You should have received a copy of the GNU General Public License along 
# with snavtofamix; if not, write to the Free Software Foundation, Inc., 
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA 
#
# Copyright 2007 Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>
#

class CtrlStructCountDictionary:
    
    def __init__(self):
        """ Initialize a dictionary. """
        self.dict={}

    def hasKey(self, mtdName):
        """ Verify whether the dictionary contains a given method name. """
        return (mtdName in self.dict)

    def inc(self, mtdName):
        """ Increment the ctrl struct count for a given method """
        if not self.hasKey(mtdName):
            self.dict[mtdName]  = 1
        else:
            self.dict[mtdName] += 1

    def count(self, mtdName):
        """ Get the number of control structures in a method """
        if not self.hasKey(mtdName):
            return 0 
        else:
            return self.dict[mtdName]