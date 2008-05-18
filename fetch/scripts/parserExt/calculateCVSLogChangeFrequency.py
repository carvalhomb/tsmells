#!/usr/bin/python
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
# Copyright 2007 Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

import sys

FILESPLIT = "============================================================================="
REVISIONSPLIT = "----------------------------"

if not (len(sys.argv) == 2): 
	print "USAGE: python calculateCVSLogChangeFrequency.py CVSLogFile"
	print "\tCVSLogFile -- file path of the source CVS Log File"
	sys.exit(2)

logFile = sys.argv[1]
changeFreqDict = {}
curFile = ""
curCF = 0	
	
for logLine in open(logFile, "r"):
	if logLine.startswith("Working file: "):
		curFile = logLine.split(": ")[1].strip()
	elif REVISIONSPLIT in logLine:
		curCF = curCF + 1
	elif FILESPLIT in logLine:
		changeFreqDict[curFile] = curCF
		curFile = ""
		curCF = 0
	
for i in changeFreqDict:
	print i+";"+str(changeFreqDict[i])
	
