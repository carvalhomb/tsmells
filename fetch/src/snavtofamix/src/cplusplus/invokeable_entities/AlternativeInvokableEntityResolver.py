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
# Author(s): Bart Du Bois <bart.dubois@ua.ac.be>

from cplusplus.typed_entities.Scope import Scope
from cplusplus.invokeable_entities.InvokeableEntityDictionary import InvokeableEntityDictionary
from cplusplus.data_types.NamespaceContainmentChecker import NamespaceContainmentChecker
from cplusplus.data_types.dictionaries.includeDict import TransitiveIncludeDictionary
from common.entity_references.MultipleLocationEntity import MultipleLocationEntity
from cplusplus.typed_entities.TypedefDictionary import TypedefDictionary
from cplusplus.InheritanceDictionary import TransitiveInheritanceDictionary
from cplusplus.invokeable_entities.AlternativeParameterTypeMatcher import AlternativeParameterTypeMatcher
from common.utils import intersection
from cplusplus.accessible_entities.AccessDictionary import AccessDictionary
from log4py import Logger

##
# For a given characterization of an invokable entity,
# this class identifies a set of InvokeableEntityReference instances
# which might correspond. To restrict this set, the scope is used.
##
class AlternativeInvokeableEntityResolver:
	def __init__(self, scope, transInhDict, typedefDict, invEntityDict, accessDict=AccessDictionary()):
		self.transInhDict = transInhDict
		self.typedefDict = typedefDict
		self.scope = scope
		self.invEntityDict = invEntityDict
		self.accessDict = accessDict
		self.log = Logger().get_instance(self)

	##
	# Selects a set of invokeable entity references associated with the given data.
	##
	def select(self, classNameOrNone, name, actualParams, referenceSourceLocation,\
			referencingClassName=""):
		selection = self.selectBasedOnNameOnly(referenceSourceLocation, name, \
											classNameOrNone, referencingClassName)
		self.filterOnParameterTypes(actualParams, selection)
		self.filterUnreachableEntities(referenceSourceLocation, selection)

		if len(selection) > 1 :
			selection = self.filterOnAccessedTypes(selection, referenceSourceLocation)

		return selection

	##
	# Make a selection of InvokableEntityReferences satisfying the given names.
	##
	def selectBasedOnNameOnly(self, referenceSourceLocation, name, \
							classNameOrNone, referencingClassName=""):
		namespaceContainmentChecker = self.scope.getNamespaceContainmentChecker()
		namespaceName = namespaceContainmentChecker.getSurroundingNamespaceName(referenceSourceLocation)
		namespacesUsed = self.scope.getUsedNamespacesUsingLoc(referenceSourceLocation)

		# retrieve a list of qualified class names in the current scope of this file
		classesUsed = self.scope.getUsedClassesUsingLoc(namespaceName, \
													referenceSourceLocation, referencingClassName)

		# retrieve a list of qualified class names used in included files
		transInclClassesUsed = self.scope.getTransitiveUsedClassesUsingLoc(referenceSourceLocation)
		for usedClass in transInclClassesUsed:
			if not(usedClass in classesUsed):
				classesUsed.append(usedClass)

		# first retrieve all references to entities with appropriate names
		selection = self.invEntityDict.retrieveMultiLocReferencesForQualifiedName(namespacesUsed, name, classesUsed)
		selection = self.filterOnClassName(namespaceName, classNameOrNone, selection, classesUsed)

		return selection

##
	# Keep only those InvokableEntityReferences that correspond to types
	# of attributes/global variables accessed on that line.
	##
	def filterOnAccessedTypes(self, selection, referenceSourceLocation):
		fileName = referenceSourceLocation.getSourceFile()
		lineNr = referenceSourceLocation.getStart()

		accessedClasses = []
		if self.accessDict.hasKeyFor(fileName, lineNr):
			className = self.accessDict.getValue(fileName, lineNr)
			if not className in accessedClasses:
				accessedClasses.append(className)

		newSelection = []

		for reference in selection:
			if reference.getOwnerName() in accessedClasses:
				newSelection.append(reference)

		if not newSelection:
			newSelection = selection

		return newSelection

	##
	# filter out those references to unreachable entities
	##
	def filterUnreachableEntities(self, referenceSourceLocation, selection):
		transIncludeDict = self.scope.getTransitiveIncludeDict()
		transIncludedFiles = transIncludeDict.getTransitiveIncludedFiles(referenceSourceLocation.getSourceFile())
		for reference in selection:
			multiLoc = reference.getLocation()
			srcFiles = multiLoc.getSourceFiles()
			includedSrcFiles = intersection(srcFiles, transIncludedFiles)
			if not includedSrcFiles:
				selection.remove(reference)
	##
	# filter out those references to entities with different parameter types
	##
	def filterOnParameterTypes(self, actualParams, selection):
		parTypeMatcher = AlternativeParameterTypeMatcher(self.transInhDict, \
														self.typedefDict, \
														actualParams)
		for reference in selection:
			if not(parTypeMatcher.matches(reference)):
				selection.remove(reference)

	##
	#
	##
	def filterOnClassName(self, namespaceName, classNameOrEmpty, selection, classesUsed=[]):
		newSelection = []

		# if the given name is unknown
		if (classNameOrEmpty == "") or (classNameOrEmpty == "#"):
			for reference in selection:
				# classes used using class usage directives are always qualified
				if reference.getNamespaceName() != "":
					if reference.getOwnerName() in classesUsed:
						newSelection.append(reference)

			if newSelection != []:
				return newSelection
			else:
				return selection

		qualifiedClassName = classNameOrEmpty
		if (namespaceName != "") and (classNameOrEmpty != ""):
			qualifiedClassName = namespaceName + "::" + classNameOrEmpty

		for reference in selection:
			if reference.getOwnerName() == qualifiedClassName:
				newSelection.append(reference)
			elif reference.getOwnerName().endswith("::" + qualifiedClassName):
				if reference.getOwnerName() in classesUsed:
					newSelection.append(reference)
			#elif reference.getNamespaceName() != "" or namespaceName == "":
			#	if reference.getOwnerName() in classesUsed:
			#		newSelection.append(reference)

		return newSelection
