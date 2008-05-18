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
# Copyright 2006  University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

# pmccabe Reader
#Modified McCabe Cyclomatic Complexity
#|   Traditional McCabe Cyclomatic Complexity
#|       |    # Statements in function
#|       |        |   First line of function
#|       |        |       |   # lines in function
#|       |        |       |       |  filename(definition line number):function
#|       |        |       |       |           |

class PmccabeReader:

	def __init__(self):
		self.inf = None

	def openMetricsFile(self, metricsDbLoc):
		self.inf = open(metricsDbLoc, 'r')

	def parseNextLine(self):
		'''Returns lastLine, identifier, CYCLO, NOS, LOC'''
		line = self.inf.readline()
		if line == "": return True, None, None, None, None

		cols = line.strip().split("\t")
		return False, self.prepareIdentifier(cols[5]), cols[1], cols[2], cols[4]

	def prepareIdentifier(self, identifier):
		# fetches the filename spreadsheetApp.cxx out of a string looking like this:
		# /home/bvromp/Desktop/teamspirit/teamspirit-1.0.1-src/spreadsheet/spreadsheetApp.cxx(222): SpreadsheetApp::scrollUp
		fileName = identifier.split("/")[-1].split("(")[0]
		lineNumber = identifier.split(")")[0].split("(")[1]
		methodName = identifier.split(" ")[1]
		#print fileName, lineNumber, methodName
		return fileName+lineNumber+methodName

	def close(self):
		self.inf.close()
