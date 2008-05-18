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

##
# Modified McCabe Cyclomatic Complexity
# |   Traditional McCabe Cyclomatic Complexity
# |       |    # Statements in function
# |       |        |   First line of function
# |       |        |       |   # lines in function
# |       |        |       |       |  filename(definition line number):function
# |       |        |       |       |           |
# 1       1       1       31      5       ./examples/helloworld/helloworld.c(31): destroy
# 1       1       15      37      63      ./examples/helloworld/helloworld.c(37): main
# 2       2       7       6       18      ./examples/clist/clist.c(6): button_add_clicked
# 1       1       2       26      9       ./examples/clist/clist.c(26): button_clear_clicked
# 2       2       7       37      20      ./examples/clist/clist.c(37): button_hide_show_clicked
# 1       1       4       59      21      ./examples/clist/clist.c(59): selection_made
# 1       1       42      81      93      ./examples/clist/clist.c(81): main
##
class PmcMetricEntity:
	""" represents a measurement for an invokable entity reported by pmccabe """
	classSep = "::"
	pkgSep = "."

	def __init__(self, line):
		line = line.strip()
		cols = line.split("	") # a tab

		self.filename = cols[5].split("(")[0].replace("./","")
		self.lineNr = cols[5].split("(")[1].split(")")[0]

		qualName = cols[5].split(": ")[1]
		if not self.pkgSep in qualName:
			self.packageName = ""
		else:
			self.packageName = qualName[:qualName.rfind(self.pkgSep)]

		if not self.classSep in qualName:
			self.className = ""
			self.invEntName = qualName[qualName.rfind(self.pkgSep)+1:]
		else:
			self.className = qualName[qualName.rfind(self.pkgSep)+1:\
									  qualName.rfind(self.classSep)]
			self.invEntName = qualName[qualName.rfind(self.classSep)+2:]

		self.metricCC = cols[1]
		self.metricNOS = cols[2]
		self.metricLOC = cols[4]

		self.invEntRef = None # ref to invokable entity; to be resolved later

	def isMethod(self):
		return "" != self.className

	def isFunction(self):
		return not self.isMethod()

	def getInvokableEntityName(self):
		return self.invEntName

	def getClassOwnerName(self):
		"""" Returns method owner; empty string for functions"""
		if self.isFunction():
			return ""
		else:
			return self.className

	def matchesLocation(self, sourceFile, lineNr):
		#print sourceFile+":"+lineNr,"versus",self.filename+":"+self.lineNr,
		if sourceFile == self.filename and lineNr == self.lineNr:
			#print "found!"
			return True
		#print "not!"
		return False

	def isKnownEntity(self, invEntDict):
		#print "----------------"
		#print "package: "+self.packageName
		#print "class: "+self.className
		#print "naem: "+self.invEntName
		invEntWMatchingNames = invEntDict.retrieveMultiLocReferencesFor(\
											self.getClassOwnerName(),\
											self.getInvokableEntityName())
		if [] == invEntWMatchingNames:
			#print "not found"
			return False

		# invEntRef is of type InvokableEntityReference
		for invEntRef in invEntWMatchingNames:
			mulLocations = invEntRef.getLocation()
			locations = mulLocations.getDefinitionLocations()
			for loc in locations:
				sourceFile = loc.getSourceFile()
				lineNr = loc.getStart()
				#print sourceFile+":"+lineNr,self.filename+":"+self.lineNr
				if self.matchesLocation(sourceFile, lineNr):
					self.invEntRef = invEntRef
					#print "resolved"
					return True
		#print "not resolved"
		return False

	def getInvEntReference(self):
		return self.invEntRef




