# !/usr/bin/python
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
# Copyright 2006,2007  University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>,
#                     Bart Du Bois <bart.dubois@ua.ac.be>

##
# Encapsulation of a source location consisting of:
# +file name
# +starting line number
# +ending line number
##

# Encapsulation of a source location consisting of:
# +file name
# +starting line number
# +ending line number
##
class SourceLocation:
    def __init__(self, sourceFile, startLineNr, endLineNr):
        self.sourceFile = sourceFile
        self.start = startLineNr
        self.end = endLineNr

    def getSourceFile(self):
    	return self.sourceFile

    def getStart(self):
    	return self.start

    def getEnd(self):
    	return self.end

    def surrounds(self, other):
        surrounds = False

        if (other != None) and (type(other) == type(self)):
            start = int(self.start)
            end = int(self.end)
            otherStart = int(other.start)
            otherEnd = int(other.end)
            surrounds = (start <= otherStart) and (end >= otherEnd)

        return surrounds

    def __eq__(self, other):
        isEqual = False

        if (other != None) and (type(other) == type(self)):
            isEqual = other.sourceFile == self.sourceFile
            isEqual = isEqual & (other.start == self.start)
            isEqual = isEqual & (other.end == self.end)

        return isEqual

    def __repr__(self):
        return `self.__dict__`.replace("',","'\n")
