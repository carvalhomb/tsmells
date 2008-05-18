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
# Copyright 2007  University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>
#

class CppDictionary:

	def __init__(self):
		self.dict = {}

	def add(self,block,sourceFile,start,end):
		if not block in self.dict:
			self.dict[block] = { }
		if not sourceFile in self.dict[block]:
			self.dict[block][sourceFile] = [ ]
		self.dict[block][sourceFile].append(start+":"+end)

	def printAll(self):
		for aBlock in self.dict:
			for aFile in self.dict[aBlock]:
				for lineNumbers in self.dict[aBlock][aFile]:
					print aFile,aBlock,lineNumbers

	def getBlocksForSourceFile(self,sourceFile):
		blockDict = {}
		for aBlock in self.dict:
			if sourceFile in self.dict[aBlock]:
				if not aBlock in blockDict:
					blockDict[aBlock] = [ ]
				blockDict[aBlock].append(aBlock)
		return blockDict

	def getFamixSourceAnchorListForBlock(self,ablock):
		sourceAnchorList = []
		for sf in self.dict[ablock]:
			#for block in sf:
			for block in self.dict[ablock][sf]:
				saString = "\""+sf+"\" start "+block.split(":")[0]+" end "+block.split(":")[1]
				sourceAnchorList.append(saString)
		return sourceAnchorList

	def liesWithinBoundaries(self,lineNr,range):
		start = range.split(":")[0]
		end = range.split(":")[1]

		# can it be equal? TODO: CHECK
		return lineNr > start and lineNr < end

	def belongsTocppBlock(self,sourceFile, lineNr):
		condBlock = ""
		# for all known compilation directives
		for block in self.dict:
			# if the current sourceFile is using a block
			if sourceFile in self.dict[block]:
				# for all ranges of that block in the current sourceFile
				for range in self.dict[block][sourceFile]:
					# check whether the entity with line number lineNr falls within the scope of that block
					if self.liesWithinBoundaries(lineNr,range):
						# if yes, add that block to the compound conditional compilation directive
						condBlock += block + " && "
		return condBlock[:len(condBlock)-4]
