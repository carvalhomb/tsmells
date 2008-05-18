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
# Copyright 2006,2007   University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>,
#                     Bart Du Bois <bart.dubois@ua.ac.be>

from cplusplus.invokeable_entities.AlternativeInvokableEntityResolver import AlternativeInvokeableEntityResolver
from common.SourceLocation import SourceLocation
from cplusplus.accessible_entities.AccessDictionary import AccessDictionary
from log4py import Logger

def getKeyFor(srcLoc):
	return srcLoc.getSourceFile() + srcLoc.getStart()

##
# Base class for the AlternativeInvocationResolver and the AlternativeAccessResolver.
# Provides functionality for resolving the source method/function that is the
# source of the access/invocation
#
# Resolves the following information.
# 	invocation.src_class
#	invocation.formalSrcParams
#	invocation.src_sourceFile
#	invocation.src_start
##
class ParentInvokableEntityResolver:
	def __init__(self, scope, transInhDict, typedefDict, invEntityDict, accessDict = AccessDictionary()):
		self.scope = scope
		self.resolver = AlternativeInvokeableEntityResolver(scope, transInhDict, typedefDict, invEntityDict, accessDict)
		self.log = Logger().get_instance()
		self.refCache = {} # src_file + lineNr x reference (max. 1 element)

	def addSurroundingDefinitionToCache(self, srcLoc, invEntityRef):
		# empty cache
		self.refCache = {}
		self.refCache[getKeyFor(srcLoc)] = invEntityRef

	def getSurroundingDefinitionFromCache(self, srcLoc):
		if self.inCache(srcLoc):
			return self.refCache[getKeyFor(srcLoc)]
		else:
			return None

	def inCache(self, srcLoc):
		key = getKeyFor(srcLoc)
		if key in self.refCache:
			return True
		else:
			return False


	##
	# Retrieve a reference to the invokable entity surrounding the given
	# invocation or access.
	##
	def getSurroundingDefinition(self, invOrAccess, refLoc):
		selection = self.resolver.select(invOrAccess.src_class, invOrAccess.src_name, invOrAccess.actualSrcParams, refLoc)

		surroundingEntities = []
		for reference in selection:
			referenceLoc = reference.getLocation()
			for defLoc in referenceLoc.getDefinitionLocations():
				if defLoc.getSourceFile() == refLoc.getSourceFile():
					surroundingEntities.append(reference)

		if (not surroundingEntities) or (len(surroundingEntities) > 1):
			return None

		# merely one invokable entity
		return surroundingEntities[0]

	##
	# Retrieve the source location of the definition
	# that is included at the given refLoc,  which
	# is defined above the given refLoc, and
	# is the nearest one to the given refLoc.
	##
	def getDefinitionLocation(self, invokableEntityRef, refLoc):
		definitionLocation = None

		multiLoc = invokableEntityRef.getLocation()

		refLineNr = int(refLoc.getStart())

		nearestDefinitionLineNumber = None
		for defLoc in multiLoc.getDefinitionLocations():
			if defLoc.getSourceFile() == refLoc.getSourceFile():
				lineNr = int(refLoc.getStart())
				if lineNr <= refLineNr:
					if (nearestDefinitionLineNumber == None) or (lineNr > nearestDefinitionLineNumber):
						nearestDefinitionLineNumber = lineNr
						definitionLocation = defLoc

		return definitionLocation

	##
	# Resolve the source of an invocation/access to a definition.
	#
	# Returns whether the resolution succeeded.
	##
	def resolveSource(self, invOrAccess):
		resolutionReport = invOrAccess.getReport()

		refLoc = SourceLocation(invOrAccess.sourceFile, invOrAccess.start, invOrAccess.start)

		invokableEntityRef = None

		if self.inCache(refLoc):
			invokableEntityRef = self.getSurroundingDefinitionFromCache(refLoc)
		else:
			# ensure that # is not regarded as a class name
			if ( invOrAccess.src_class == "#" ):
				invOrAccess.src_class = ""

			invokableEntityRef = self.getSurroundingDefinition(invOrAccess, refLoc)

		if invokableEntityRef == None:
			return False

		resolutionReport.setSurroundingDefinitionFound()

		defLoc = self.getDefinitionLocation(invokableEntityRef, refLoc)
		if defLoc == None:
			return False

		resolutionReport.setDefinitionLocationFound()

		self.fillInReferenceSource(invOrAccess, invokableEntityRef, defLoc)
		return True

	def fillInReferenceSource(self, invOrAccess, invokableEntityRef, srcLoc):
		invOrAccess.src_class = invokableEntityRef.getOwnerName()
		invOrAccess.formalSrcParams = invokableEntityRef.getFormalParameters().lstrip("(").rstrip(")")
		invOrAccess.src_sourceFile = srcLoc.getSourceFile()
		invOrAccess.src_start = srcLoc.getStart()
		invOrAccess.src_namespace = invokableEntityRef.getNamespaceName()
