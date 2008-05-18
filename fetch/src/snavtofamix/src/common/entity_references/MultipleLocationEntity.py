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

from common.utils import unionForNonHashableElements
from log4py import Logger

##
# Encapsulation of the concept of an entity
# which can be declared and defined at multiple
# locations, e.g., functions and methods.
##
class MultipleLocationEntity:
	def __init__(self):
		self.declarationSourceLocations = []
		self.definitionSourceLocations = []
		self.log = Logger().get_instance(self)

	def addDeclaration(self, sourceLocation):
		added = False

		if not(sourceLocation in self.declarationSourceLocations):
			self.declarationSourceLocations.append(sourceLocation)
			added = True

		return added

	def addDefinition(self, sourceLocation):
		added = False

		if not(sourceLocation in self.definitionSourceLocations):
			self.definitionSourceLocations.append(sourceLocation)
			added = True

		return added

	def getDeclarationLocations(self):
		return self.declarationSourceLocations

	def getDefinitionLocations(self):
		return self.definitionSourceLocations

	def getLocations(self):
		return unionForNonHashableElements(self.getDeclarationLocations(), self.getDefinitionLocations())

	##
	# Return a list of source files declaring or defining this entity.
	##
	def getSourceFiles(self):
		sourceFiles = []

		for srcLoc in self.getLocations():
			srcFile = srcLoc.getSourceFile()
			if not(srcFile in sourceFiles):
				sourceFiles.append(srcFile)

		return sourceFiles

	##
	# Retrieve a SourceLocation contained in this MultipleLocationEntity
	# that surrounds the given line number in the given file.
	#
	# Looks for the SourceLocation in the given sourceFile that preceeds
	# the given lineNr, and for which no other SourceLocations are in between
	# the start of that SourceLocation and the given lineNr.
	##
	def getSurroundingLocation(self, sourceFile, lineNr):
		lineNr = int(lineNr)
		surroundingLocation = None

		locationsInSourceFile = {} # lineNr x SourceLocation

		for location in self.getLocations():
			if location.getSourceFile() == sourceFile:
				locationsInSourceFile[int(location.getStart())] = location

		# iterate reversely across line numbers
		lineNrs = locationsInSourceFile.keys()
		lineNrs.sort()
		index = len(lineNrs) - 1
		while index >= 0:
			curLineNr = lineNrs[index]

			if curLineNr < lineNr:
				surroundingLocation = locationsInSourceFile[curLineNr]
				break

			index = index -1

		return surroundingLocation

	##
	# Retrieve a list of lists. Each contained list represents a set
	# of source locations. The first source location in each list
	# represents a declaration (or a defined declaration).
	# All subsequent source locations in each list are definitions
	# of this declaration.
	# Redeclarations of an existing declaration are ignored and thus
	# do not occur in the lists.
	##
	def getDeclarationDefinitionTuples(self, scope):
		transIncludeDict = scope.getTransitiveIncludeDict()

		tuples = []

		redeclarationLocations = []
		for declLoc in self.getDeclarationLocations():
			declFile = declLoc.getSourceFile()
			transIncludedFiles = \
					transIncludeDict.getTransitiveIncludedFiles(declFile)
			if declFile in transIncludedFiles:
				transIncludedFiles.remove(declFile)
			#elif transIncludeDict.\
			#		foundConfidentSymlinkMatch(declFile, transIncludedFiles):
			#	transIncludedFiles.remove(declFile)

			for otherDeclLoc in self.getDeclarationLocations():
				otherDeclFile = otherDeclLoc.getSourceFile()
				if otherDeclFile in transIncludedFiles:
					redeclarationLocations.append(declLoc)
					break
				#elif transIncludeDict.\
				#	foundConfidentSymlinkMatch(otherDeclFile, transIncludedFiles) \
				#	and not otherDeclFile in transIncludedFiles:
				#		redeclarationLocations.append(declLoc)
				#		break

		for declLoc in self.getDeclarationLocations():
			if not(declLoc in redeclarationLocations):
				tuples.append([declLoc])

		for defLoc in self.getDefinitionLocations():
			defFile = defLoc.getSourceFile()
			transIncludedFiles = transIncludeDict.getTransitiveIncludedFiles(defFile)

			associatedDeclarationLocations = []
			#self.log.warn("deffile: ",defFile)
			# by using only those declarations contained in tuples
			# we ensure that these are not redeclarations
			for atuple in tuples:
				#self.log.warn("curtuple: ",atuple)
				#self.log.warn("locs: ", str(associatedDeclarationLocations))

				declLoc = atuple[0]
				declFile = declLoc.getSourceFile()
				if declFile in transIncludedFiles:
					if not declLoc in associatedDeclarationLocations:
						#self.log.warn(" add")
						associatedDeclarationLocations.append(declLoc)
				elif transIncludeDict.foundConfidentSymlinkMatch(declFile, transIncludedFiles) and \
					not declLoc in associatedDeclarationLocations:
					#self.log.warn(" add after direct include")
					associatedDeclarationLocations.append(declLoc)
			#self.log.warn("locs after t processing: ", str(associatedDeclarationLocations))

			if len(associatedDeclarationLocations) == 0:
				## definition = declaration
				tuples.append([defLoc])
			elif len(associatedDeclarationLocations) > 1:
				self.log.warn(`len(associatedDeclarationLocations)` + " declarations found for definition.")
				#self.log.warn("		- ",associatedDeclarationLocations[0].sourceFile)
				#self.log.warn("		- ",associatedDeclarationLocations[1].sourceFile)
			else:
				singleDeclaration = associatedDeclarationLocations[0]
				for atuple in tuples:
					declLoc = atuple[0]
					if declLoc == singleDeclaration:
						atuple.append(defLoc)

		return tuples

	##
	# Filter out those tuples from self.getDeclarationDefinitionTuples(scope)
	# which do not contain a single location that is included by the
	# referencingSourceFile.
	##
	def getIncludedDeclarationDefinitionTuples(self, scope, referencingSourceFile):
		transIncludeDict = scope.getTransitiveIncludeDict()
		transIncludedFiles = transIncludeDict.getTransitiveIncludedFiles(referencingSourceFile)

		tuples = self.getDeclarationDefinitionTuples(scope)
		includedTuples = []
		for tuple in tuples:
			# verify whether at least one location is included by the reference
			for location in tuple:
				srcFile = location.getSourceFile()
				if srcFile in transIncludedFiles:
					includedTuples.append(tuple)
					break # process next tuple

		return includedTuples


	##
	# Retrieves the source location of the declaration associated with the
	# given definition source lcoation.
	#
	# In case a declaration immediately implements the body, it is considered
	# a definition only. When looking up the declaration for such a definition,
	# the definition itself is returned.
	##
	def getDeclarationForDefinition(self, scope, definitionSrcLocation):
		declarationSourceLocation = None

		tuples = self.getDeclarationDefinitionTuples(scope)
		for tuple in tuples:
			if definitionSrcLocation in tuple:
				declarationSourceLocation = tuple[0] # first is always declaration (potentially also definition)
				break

		return declarationSourceLocation

	def __eq__(self, other):
		isEqual = (other != None) & (type(other) == type(self))

		if isEqual:
			isEqual = isEqual & (len(other.declarationSourceLocations) == len(self.declarationSourceLocations))

			if isEqual:
				for declSrcLoc in self.declarationSourceLocations:
					isEqual = isEqual & (declSrcLoc in other.declarationSourceLocations)

			isEqual = isEqual & (len(other.definitionSourceLocations) == len(self.definitionSourceLocations))

			if isEqual:
				for defSrcLoc in self.definitionSourceLocations:
					isEqual = isEqual & (defSrcLoc in other.definitionSourceLocations)

		return isEqual

	def __repr__(self):
		return `self.__dict__`.replace("',","'\n")

	def printData(self):
		print len(self.declarationSourceLocations), \
				"declarations: ", \
				self.declarationSourceLocations
		print len(self.definitionSourceLocations), \
				"definitions: ", \
				self.definitionSourceLocations

