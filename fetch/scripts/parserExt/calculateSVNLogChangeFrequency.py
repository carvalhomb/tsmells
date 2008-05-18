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

# from: svn log -v > SVNLogFile

import sys

MODIFIEDFILE = "   M"

if not (len(sys.argv) == 2): 
	print "USAGE: python calculateSVNLogChangeFrequency.py SVNLogFile"
	print "\tSVNLogFile -- file path of the source SVN Log File"
	sys.exit(2)

logFile = sys.argv[1]
changeFreqDict = {}
	
for logLine in open(logFile, "r"):
	if not logLine.startswith(MODIFIEDFILE): continue
	
	curFile = logLine.split("M ")[1][1:].strip()
	
	if curFile in changeFreqDict:
		changeFreqDict[curFile] = changeFreqDict[curFile] + 1
	else:
		changeFreqDict[curFile] = 1
	
for i in changeFreqDict:
	print i+";"+str(changeFreqDict[i])
	
