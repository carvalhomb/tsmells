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

from log4py                      import Logger
from common.famix.cdifWriter     import generateMetrics
from common.SourceLocation       import SourceLocation
from common.dictionaries         import CtrlStructCountDictionary

log = Logger().get_instance()

COND="COND"
LOOP="LOOP"

def parse(cDb, mtdSrcDict, mtdpfDict, type):
    """ Parse control structure information and write to cdif"""
    
    ctrlDict  = CtrlStructCountDictionary.CtrlStructCountDictionary()
    #mtdpfDict = MethodsPerFileDictionary(mtdSrcDict)
        
    for line in open(cDb, 'r'):
        splitted = line.split(":")
        srcFile  = splitted[0]
        lineNr   = int(splitted[1])
        mtd, loc = mtdpfDict.getEnclosing(srcFile, lineNr)
        if mtd: 
            ctrlDict.inc(mtd)
        else:
            log.warn( "Failed to find owner method of cs: " + srcFile + " " + str(lineNr) )
        
    for mtd,val in ctrlDict.dict.iteritems():
        src  = mtdSrcDict.dict[mtd][0].split(":")
        sloc = SourceLocation(src[0], src[1] , str(int(src[1]) + 1) )
        generateMetrics(type, str(val), mtd, sloc)
        