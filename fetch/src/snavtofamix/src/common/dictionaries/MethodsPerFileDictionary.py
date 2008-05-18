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

from log4py   import Logger
from operator import itemgetter

class MethodsPerFileDictionary:
    """ transforms a MethodSourceDictionary into an dictionary with files as key 
        and ordered methods with their line number. Thus it gets cheap to 
        determine which method a specific source line belongs to. 
        
        Due to the nature of SN's dump tables, giving only the starting line, 
        the info returned here could actually be wrong. There's no way to check
        if a line number is between the end of a method and the next."""

    def __init__(self, mtdSrcDict):
        self.mtdSrcDict = mtdSrcDict
        self.dict = {}
        self.__fill()
        self.__sort()
        self.log = Logger().get_instance(self)
        
    def __fill(self):
        for mtd, val in self.mtdSrcDict.dict.iteritems():
            for loc in val:
                srcFile = loc.split(':')[0]
                lineNr  = loc.split(':')[1]
                if srcFile not in self.dict:
                    self.dict[srcFile] = []
                self.dict[srcFile].append((mtd, int(lineNr)))
    
    def __sort(self):
        """ sort on linenr """
        for srcFile, val in self.dict.iteritems():
            self.dict[srcFile] = sorted(val, key=itemgetter(1))
            
    def hasKey(self,srcFile):
        return srcFile in self.dict

    def getEnclosing(self, srcFile, lineNr):
        """ Find the method containing lineNr in srcFile, 
            None if not found. returns methodName and its starting line number """
        if srcFile not in self.dict: return None, 0
        mtds = self.dict[srcFile]
        prevMtd = None
        prevLineNr = 0
        for mtd in mtds:
            if int(lineNr) < int(mtd[1]): break
            prevMtd = mtd[0]
            prevLineNr = mtd[1]
        return prevMtd, prevLineNr
