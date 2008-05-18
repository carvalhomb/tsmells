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
# Copyright 2007   University of Antwerp
# Author(s): Bart Du Bois <bart.dubois@ua.ac.be>


from DataRecords import Entity
from common.SourceLocation import SourceLocation
from common.entity_references import EntityReferences

## STATIC
# Verifies whether the given line represents a namespace declaration,
# and can therefore be given to initialize a NamespaceEntity.
##
def isValidNamespaceDeclarationLine(line):
	isValid = True

	dummyEntity = NamespaceEntity(line)

	if len(dummyEntity.cols) != 4:
		isValid = False

	return isValid

##
# Encapsulation of a namespace entity.
##
class NamespaceEntity(Entity):
	##
	# Initialize an entity using a given line.
	##
	def __init__(self, line):
		Entity.__init__(self, line) # does not accurately decompose line into cols
		self.cols=self.line.split("\t")

	def getNamespaceName(self):
		return self.cols[0].strip()

	def getStartLineNumber(self):
		return self.cols[1].strip()

	def getEndLineNumber(self):
		return self.cols[2].strip()

	def getDeclaringFileName(self):
		return self.cols[3].strip()

	'''returns true if self equals other, false otherwise'''
	def __eq__(self, other):
		isEqual = False

		if type(self) == type(other):
			nameEqual = self.getNamespaceName() == other.getNamespaceName()
			fileNameEqual = self.getDeclaringFileName() == other.getDeclaringFileName()
			startLineNrEqual = self.getStartLineNumber() == other.getStartLineNumber()
			endLineNrEqual = self.getEndLineNumber() == other.getEndLineNumber()
			isEqual = (nameEqual and fileNameEqual) and ( startLineNrEqual and endLineNrEqual)

		return isEqual

	##
	# Create a string representation of this NamespaceEntity.
	##
	def getStringRep(self):
		return self.getNamespaceName() + "\t" + self.getStartLineNumber() + "\t" + self.getEndLineNumber() + "\t" + self.getDeclaringFileName()

	def getReference(self):
		reference = self.createReference()
		reference.setName(self.getNamespaceName())
		sourceLocation = SourceLocation(self.getDeclaringFileName(), \
									self.getStartLineNumber(), \
									self.getEndLineNumber())
		reference.addSourceLocation(sourceLocation)
		return reference

	##
	# Seam
	##
	def createReference(self):
		return NamespaceEntityReference()

nsInvolvesCache = {} # nsName x { fileName x bool }

##
# Encapsulation of the information
# required to refer to a function or method.
##
class NamespaceEntityReference:
	def __init__(self):
		self.name = None
		self.sourceLocations = [] # list of SourceLocation instances

	def setName(self, name):
		self.name = name

	def getName(self):
		return self.name

	def getNonQualifiedName(self):
		nonQualifiedName = None

		if "::" in self.name:
			nameList = self.name.split("::")
			nonQualifiedName=nameList[len(nameList)-1]
		else:
			nonQualifiedName = self.name

		return nonQualifiedName

	def addSourceLocation(self, sourceLocation):
		added = False

		if not(sourceLocation in self.sourceLocations):
			self.sourceLocations.append(sourceLocation)
			if not nsInvolvesCache.has_key(self.name):
				nsInvolvesCache[self.name] = {}
			nsInvolvesCache[self.name][sourceLocation.sourceFile] = True			
			added = True

		return added

	def add(self, sourceLocations):
		added = False

		for sourceLocation in sourceLocations:
			curAdded = self.addSourceLocation(sourceLocation)
			added = added or curAdded
		return added

	##
	# Extend the source locations of this NamespaceEntityReference
	# with those of the otherNamespaceEntityReference.
	##
	def extendWith(self, otherNamespaceEntityReference):
		extended = False

		if (otherNamespaceEntityReference != None) and ( type(otherNamespaceEntityReference) == type(self) ):
			if otherNamespaceEntityReference.getName() == self.getName():
				extended = self.add(otherNamespaceEntityReference.getSourceLocations())

		return extended

	##
	# Verify whether this NamespaceEntityReference surrounds
	# the given other reference. This condition evaluates to True in case:
	# +the other reference is merely declared in a single location
	# +this reference contains a source location that surrounds
	#  the single location of the other reference.
	##
	def surrounds(self, other):
		surrounds = False

		if (other == None) or not(type(other) == type(self)):
			return False

		otherLocations = other.getSourceLocations()
		if len(otherLocations) == 1:
			otherLocation = otherLocations[0]
			otherFileName = otherLocation.sourceFile
			if self.involves(otherFileName):
				locations = self.getSourceLocationsInvolving(otherFileName)
				for location in locations:
					if location.surrounds(otherLocation):
						surrounds = True
						break

		return surrounds

	def involves(self, fileName):
		try:
			return nsInvolvesCache[self.name][fileName]

		except KeyError:	
			if not nsInvolvesCache.has_key(self.name):
				nsInvolvesCache[self.name] = {}
			#l = len(self.sourceLocations)
			#if not nsInvolvesCache[self.name].has_key(l):
			#	nsInvolvesCache[self.name][l] = {}
				
			involves = False
			for sourceLocation in self.sourceLocations:
				if sourceLocation.sourceFile == fileName:
					involves = True
					break
			#nsInvolvesCache[self.name][l][fileName] = involves
			nsInvolvesCache[self.name][fileName] = involves
			return involves 
			
		#involves = False

		#for sourceLocation in self.sourceLocations:
		#	if sourceLocation.sourceFile == fileName:
		#		involves = True
		#		break

		#return involves

	def getSourceFiles(self):
		srcFiles = []

		for sourceLocation in self.sourceLocations:
			srcFiles.append(sourceLocation.sourceFile)

		return srcFiles


	def getSourceLocations(self):
		#print "\n\n>>>>>" + self.name
		#for i in self.sourceLocations:
		#	print i.sourceFile + " " + i.start + " ; ",
		return self.sourceLocations

	def nestNameWithin(self, reference):
		self.setName(reference.getName() + "::" + self.getNonQualifiedName())

	def getSourceLocationsInvolving(self, fileName):
		locations = []

		for sourceLocation in self.sourceLocations:
			if sourceLocation.sourceFile == fileName:
				locations.append(sourceLocation)

		return locations

	##
	# Verifies whether the given sourceLocation is contained
	# in the namespace
	##
	def surroundsLocation(self, sourceLocation):
		surrounds = False

		for curLocation in self.getSourceLocationsInvolving(sourceLocation.sourceFile):
			surrounds = curLocation.surrounds(sourceLocation)
			if surrounds:
				break

		return surrounds

	def __eq__(self, other):
		isEqual = False

		if (other != None) and (type(other) == type(self)):
			isEqual = other.getName() == self.getName()
			otherLocations = other.getSourceLocations()
			selfLocations = self.getSourceLocations()
			isEqual = isEqual & (len(otherLocations) == len(selfLocations))
			for location in selfLocations:
				isEqual = isEqual & (location in otherLocations)

		return isEqual

	def __repr__(self):
		return `self.__dict__`.replace("',","'\n")
