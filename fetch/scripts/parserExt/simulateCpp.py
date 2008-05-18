#!/bin/python
# This file is part of Fetch (the Fact Extraction Tool CHain).
#
# Fetch is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# Fetch is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Anastacia; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
# Copyright 2008 University of Antwerp
# Author: Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

import sys

def insideUnwantedBlock(condList, condStack):
	for cond in condStack:
		if not cond in condList:
			#print "***",cond,"in condStack",condStack,"and in condList",condList, cond in condList, not cond in condList
			return True
	return False

def cutCondition(condLine, ppb):
	condLine = condLine.split(ppb)[1]
	condLine = condLine.strip()
	condLine = condLine.split("	")[0]
	return condLine

def endOfBlock(line):
	if line.startswith("#endif") or \
		line.startswith("#else") or \
		line.startswith("#elif"):
			return True
	else:
		return False

def flipCondition(condStack):
	"""flips condition on top of stack"""
	cond = condStack.pop()
	print cond

if not (len(sys.argv) > 1):
	print "USAGE: python simulateCpp.py sourceFile { CONDITION }"
	print "\tsourceFile -- file path of the source file to process"
	print "\t{ CONDITION } -- zero or more conditions to enable"
	sys.exit(2)

args = len(sys.argv)
sourceFile = sys.argv[1]
condList = sys.argv[2:len(sys.argv)]
condList.append("")
condStack = [ ]

#print "preprocessing",sourceFile,"with conditions",','.join(condList)

for line in open(sourceFile, "r"):
	origLine = line
	line = line.strip()
	cols = line.split(" ")

	if line.startswith("#ifndef"):
		condName = cutCondition(line,"#ifndef")
		condStack.append("!"+condName)
	elif line.startswith("#end") or line.startswith("#endif"):
		condStack.pop()
	elif line.startswith("#ifdef"):
		condName = cutCondition(line,"#ifdef")
		condStack.append(condName)
	elif line.startswith("#if"):
		condName = cutCondition(line,"#if")
		condStack.append(condName)
	elif line.startswith("#else"):
		cond = condStack.pop()
		condName = "!"+cond
		condName = condName.replace("!!","")
		if not "!" in condName:
			condStack.append(condName)
		else:
			condStack.append("")
	elif line.startswith("#elif defined"):
		condName = cutCondition(line,"#elif defined")
		condStack.pop()
		condStack.append(condName)
	elif line.startswith("#elsif"):
		#print cbR.sourceFile,fileNr,line
		condName = cutCondition(line,"#elsif")
		condStack.pop()
		condStack.append(condName)

	if not insideUnwantedBlock(condList,condStack):
		if not endOfBlock(line):
			print origLine,
		else:
			print "/*",line.replace("/*","").replace("*/",""),"*/"
			pass #print "dont want",line
	else:
		print "/*",line.replace("/*","").replace("*/",""),"*/"
		pass #print "dont want",line



