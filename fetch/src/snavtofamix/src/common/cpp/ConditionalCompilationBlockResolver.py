#!/usr/bin/python
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

from CppDictionary import CppDictionary

##
# Lightweight preprocessing condition block resolver
# * features:
#	- support for all conditional blocks ifdef,ifndef,if,else,elif,end,endif
#	- builds dictionary with blockname,sourcefile,start,end
#	- supports nesting
#	- checks whether there is a "*/" without "/*" on the line (then the
#		conditional is in a comment block
# 	- support for multiline conditions
#		e.g. (CPU==SPARC || \
#			CPU==POWERPC)
#		will appear as
#			(CPU==SPARC || \
#	- - comment removal on the condition itself
#		e.g. #ifdef _EXTENSION_WRS /* undef for ANSI
# * Limitations (currently):
#	- does not distinguish between compiler defines (e.g. __cplusplus) and
#		user defines
#	- if/else on 1 line such as
#		!(defined(_WINDOWS) || defined(WINDOWS)) && defined(ZLIB_DLL) #else
#
##

class CondBlockResolver:
	combinationSymbol = " && "

	def __init__(self,sourceFile):
		self.lock = False
		self.sourceFile = sourceFile
		self.condBlockStack = []
		self.blockNumberStack = []

	# new item on stack
	def addBlock(self,blockName,curLine):
		self.condBlockStack.append(blockName)
		self.blockNumberStack.append(curLine)

	# replace last item on stack
	def switchCondition(self,blockName,curLine):
		if not self.condBlockStack:
			self.lock = True
			raise Exception()
		condName = self.combineNestedConditions()
		self.condBlockStack.pop()
		blockStart = self.blockNumberStack.pop()
		self.condBlockStack.append(blockName)
		self.blockNumberStack.append(curLine)
		return condName, blockStart

	# replaces the last item on the stack with its negation
	def flipCondition(self,curLine):
		if not self.condBlockStack:
			self.lock = True
			raise Exception()
		combName = self.combineNestedConditions()
		condName = self.condBlockStack.pop()
		i = condName.rfind(self.combinationSymbol)
		if i < 0:
			parentCombs = ""
		else:
			parentCombs = combName[:i]
		flipCondName = parentCombs + "!"+condName
		blockStart = self.blockNumberStack.pop()
		self.condBlockStack.append(flipCondName)
		self.blockNumberStack.append(curLine)
		return condName, blockStart

	# remove last item from stack
	def closeBlock(self):
		if not self.condBlockStack:
			self.lock = True
			raise Exception()
		condName = self.combineNestedConditions()
		self.condBlockStack.pop()
		blockStart = self.blockNumberStack.pop()
		return condName, blockStart

	# concatenates all conditions currently on the stack
	def combineNestedConditions(self):
		combinedCondition = ""
		for condition in self.condBlockStack:
			combinedCondition += condition + self.combinationSymbol
		subtract = 4 # to remove the last ' && '
		return combinedCondition[:len(combinedCondition)-subtract]

	# new sourcefile, empty all stacks (even when they are not empty yet)
	def nextSourceFile(self,sourceFile):
		self.lock = False
		self.sourceFile = sourceFile
		self.condBlockStack = []
		self.blockNumberStack = []

	# in case comments mess up the nesting,this method will prevent the resolver
	# to pop from an empty stack. After setting this lock, no conditional blocks
	# will be reported anymore for the current source file
	def setSourceFileLock(self):
		self.lock = True

def isCommentLine(cppLine):
	if "*/" in cppLine and not "/*" in cppLine:
		return True
	else:
		return False

def processcppBlock(maDict,cbR,fileNr,cppLine):
	cppLine = cppLine.strip()
	cppLine = cppLine.replace("\t"," ")
	cppLine = cppLine.replace("      ", " ")
	cppLine = cppLine.replace("     ", " ")
	cppLine = cppLine.replace("    ", " ")
	cppLine = cppLine.replace("   ", " ")
	cppLine = cppLine.replace("  ", " ")

	if (not cppLine.startswith("#")) or isCommentLine(cppLine):
		return cbR


	if cppLine.startswith("#ifndef"):
		#print cbR.sourceFile,fileNr,cppLine
		condName = cppLine.split("#ifndef")[1].strip()
		cbR.addBlock("!"+condName,fileNr)
	elif cppLine.startswith("#end") or cppLine.startswith("#endif"):
		#print cbR.sourceFile,fileNr,cppLine
		condName, startNr = cbR.closeBlock()
		maDict.add(condName,cbR.sourceFile,startNr,fileNr)
		#print maDict.dict
	elif cppLine.startswith("#ifdef"):
		#print cbR.sourceFile,fileNr,cppLine
		condName = cppLine.split("#ifdef")[1].strip()
		cbR.addBlock(condName,fileNr)
	elif cppLine.startswith("#if"):
		#print cbR.sourceFile,fileNr,cppLine
		condName = cppLine.split("#if")[1].strip()
		cbR.addBlock(condName,fileNr)
	elif cppLine.startswith("#else"):
		#print cbR.sourceFile,fileNr,cppLine
		condName, startNr = cbR.flipCondition(fileNr)
		#maDict.add("!"+condName,cbR.sourceFile,startNr,fileNr)
		maDict.add(condName,cbR.sourceFile,startNr,fileNr)
	elif cppLine.startswith("#elif defined"):
		#print cbR.sourceFile,fileNr,cppLine
		condName = cppLine.split("#elif defined")[1].strip()
		condName, startNr = cbR.switchCondition(condName,fileNr)
		maDict.add(condName,cbR.sourceFile,startNr,fileNr)
	elif cppLine.startswith("#elsif"):
		#print cbR.sourceFile,fileNr,cppLine
		condName = cppLine.split("#elsif defined")[1].strip()
		condName, startNr = cbR.switchCondition(condName,fileNr)
		maDict.add(condName,cbR.sourceFile,startNr,fileNr)
	else:
		pass
		#print "****",cbR.sourceFile,fileNr,cppLine
	return cbR

def read_cpp_table(maDb,maDict):
	mafile = open(maDb, "r")
	cbR = CondBlockResolver("")
	for line in mafile:
		cols = line.split(":")
		sourceFile = cols[0]
		if cbR.sourceFile != sourceFile:
			cbR.nextSourceFile(sourceFile)
		if cbR.lock:
			continue
		try:
			cbR = processcppBlock(maDict,cbR,cols[1],cols[2])
		except Exception:
			pass
	mafile.close()


#maDict = CppDictionary()
#read_cpp_table("IGMPMacros.txt",maDict)
#maDict.printAll()
