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

import sys

from log4py import Logger
from common import utils

from QualifiedNameHelperFunctions 			import 	getParentNamespaceName, \
													getNonQualifiedName
from cplusplus.data_types.dictionaries.includeDict 	import 	IncludeDictionary,\
												 	TransitiveIncludeDictionary

from cplusplus.data_types.ClassEntity 				import ClassReferenceEntity
from cplusplus.data_types.NamespaceContainmentChecker import NamespaceContainmentChecker
from Scope 									import Scope
from common.SourceLocation import SourceLocation

from ClassCollector 						import ClassCollector

##
# Given a class-name, a source file, a class dictionary and a transitive include dictionary,
# selects the set of classes that might correspond to that name.
##
class ClassSelector:
	##
	# @param classDict: a complete class dictionary
	# @param inclueDict: a (non-transitive) include dictionary
	# @param namespaceDict: a dictionary of namespace declarations:
	# @param namespaceUsageChecker: a NamespaceCountainmentChecker for verifying
	# which namespace usages are surrounding a line in a source file.,
	# con
	##
	def __init__(self, classDict, scope, typedefDict):
		self.classDict = classDict
		self.scope = scope
		self.typedefDict = typedefDict
		self.classCollector = ClassCollector(self.classDict, self.typedefDict)
		self.log = Logger().get_instance(self)

	def translateAsTypeDef(self, qualifiedClassName):
		translatedName = qualifiedClassName

		if self.typedefDict.containsAlias(qualifiedClassName):
			translatedName = self.typedefDict.getBaseType(qualifiedClassName)

		return translatedName

	##
	# Gathers a list of references to classes that potentially correspond
	# to the given characteristics.
	#
	# @param qualifiedClassName: the qualified name of the classes to select (possible qualified)
	# @param referencingSourceFile: the name of the file from which the reference to className was made
	# @param referencingLineNr: the line number at which the reference is made from the referencingSourceFile
	#
	# @returns a list of  [sourceFile, lineNr] elements
	##
	def selectClasses(self, qualifiedClassName, referencingSourceFile, referencingLineNr):
		relevantClassReferences = []

		translatedName = self.translateAsTypeDef(qualifiedClassName)

		parentNamespaceName = getParentNamespaceName(qualifiedClassName)
		nonQualifiedClassName = getNonQualifiedName(qualifiedClassName)

		#namespacesUsed = self.scope.getUsedNamespaces(parentNamespaceName, referencingSourceFile, referencingLineNr)
		namespacesUsed = self.scope.getUsedNamespaces(referencingSourceFile, referencingLineNr)

		if parentNamespaceName != "":
			index = 0
			while index < len(namespacesUsed):
				usedNamespace = namespacesUsed[index]
				if usedNamespace == "":
					usedNamespace = parentNamespaceName
				else:
					usedNamespace += "::" + parentNamespaceName
				namespacesUsed[index] = usedNamespace
				index += 1

		classReferences = self.classCollector.collectClassReferences(nonQualifiedClassName, namespacesUsed)

		# Which classes are reachable through (transitive) #includes?
		#print qualifiedClassName
		transIncludeDict = self.scope.getTransitiveIncludeDict()
		transitiveIncludedFiles=transIncludeDict.getTransitiveIncludedFiles(referencingSourceFile)
		for classReference in classReferences:
			sourceFile = classReference.getSourceFile()
			#print sourceFile, " in ", transitiveIncludedFiles
			#selectSourceFileMatches(sourceFile,transitiveIncludedFiles)
			if sourceFile in transitiveIncludedFiles:
				if not(classReference in relevantClassReferences):
					relevantClassReferences.append(classReference)
			else:
				conf = transIncludeDict.trySymlinkPath(sourceFile, transitiveIncludedFiles)
				if conf > 1 and not(classReference in relevantClassReferences):
					relevantClassReferences.append(classReference)

		sl = SourceLocation(referencingSourceFile, referencingLineNr, referencingLineNr)

		# TODO: we do an 'or' here, but we should check that (i) it is reachable
		# via include, and (ii) the namespacing is also ok

		# Which classes are reachable through namespacing.
		usedClassesLocations = self.scope.getTransitiveUsedClassesLocationsUsingLoc(sl)
		for usedClassLoc in usedClassesLocations:
			# convert to ClassReferenceEntity
			clsName = usedClassLoc.getNonQualifiedName()
			nspName = utils.spliceLastPackageName(usedClassLoc.getName(),utils.qualSeparator)
			sLoc = usedClassLoc.getSourceLocations()[0]

			cre = ClassReferenceEntity(clsName, nspName, sLoc.sourceFile, sLoc.start, "")
			if clsName == qualifiedClassName and not cre in relevantClassReferences:
				relevantClassReferences.append(cre)

		return relevantClassReferences
