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

from cplusplus.data_types.dictionaries.includeDict import IncludeDictionary, TransitiveIncludeDictionary
from cplusplus.data_types.dictionaries.ClassUsageDictionary import ClassUsageDictionary
from cplusplus.data_types.ClassUsageEntity import ClassUsageEntity
from cplusplus.data_types.NamespaceContainmentChecker import NamespaceContainmentChecker
from cplusplus.data_types.dictionaries.ClassUsageChecker import ClassUsageChecker
from cplusplus.InheritanceDictionary import InheritanceDictionary, TransitiveInheritanceDictionary
from common.SourceLocation import SourceLocation


##
# This class aggregates the concepts relevant to a scope.
##
class Scope:
	def __init__(self, includeDict, namespaceContainmentChecker, \
				namespaceUsageChecker,\
				classUsageChecker=ClassUsageChecker(ClassUsageDictionary())):
		self.transIncludeDict = TransitiveIncludeDictionary(includeDict)
		self.namespaceContainmentChecker = namespaceContainmentChecker
		self.namespaceUsageChecker = namespaceUsageChecker
		self.classUsageChecker = classUsageChecker
		self.transInhDict = TransitiveInheritanceDictionary(InheritanceDictionary())

	def setTransitiveInheritanceDict(self, transInhDict):
		self.transInhDict = transInhDict

	def getTransitiveInheritanceDict(self):
		return self.transInhDict

	def getTransitiveIncludeDict(self):
		return self.transIncludeDict

	def getNamespaceContainmentChecker(self):
		return self.namespaceContainmentChecker

	def getNamespaceUsageChecker(self):
		return self.namespaceUsageChecker

	def getClassUsageChecker(self):
		return self.classUsageChecker

	##
	# Wrapper for other method.
	##
	#def getUsedNamespacesUsingLoc(self, parentNamespaceName, refSrcLoc):
	def getUsedNamespacesUsingLoc(self, refSrcLoc):
		#return self.getUsedNamespaces(parentNamespaceName, refSrcLoc.getSourceFile(), refSrcLoc.getStart())
		return self.getUsedNamespaces(refSrcLoc.getSourceFile(), refSrcLoc.getStart())

	##
	# Retrieve a list of qualified names of classes that are imported through
	# usage declarations in transitively included files
	##
	def getTransitiveUsedClassesUsingLoc(self, refSrcLoc):
		transIncludedUsedClasses = []

		# get the transitively included using directives (excluding current file)
		transIncludedFiles = self.transIncludeDict.getTransitiveIncludedFiles(refSrcLoc.getSourceFile())
		#transIncludedFiles.remove(refSrcLoc.getSourceFile())

		classUsageChecker = self.getClassUsageChecker()

		for transIncludedFile in transIncludedFiles:
			for classUsageRef in classUsageChecker.getUsingDeclarationsInFile(transIncludedFile):
				if not(classUsageRef.isContainedInANamespace()):
					transIncludedUsedClasses.append(classUsageRef.getName())

		return transIncludedUsedClasses

	def getTransitiveUsedClassesLocationsUsingLoc(self, refSrcLoc):
		transIncludedUsedClasses = []

		# get the transitively included using directives (excluding current file)
		transIncludedFiles = self.transIncludeDict.getTransitiveIncludedFiles(refSrcLoc.getSourceFile())
		#transIncludedFiles.remove(refSrcLoc.getSourceFile())

		classUsageChecker = self.getClassUsageChecker()

		for transIncludedFile in transIncludedFiles:
			for classUsageRef in classUsageChecker.getUsingDeclarationsInFile(transIncludedFile):
				if not(classUsageRef.isContainedInANamespace()):
					transIncludedUsedClasses.append(classUsageRef)

		return transIncludedUsedClasses


	def getUsedClassesUsingLoc(self, parentNamespaceName, refSrcLoc, \
							referencingClassName= ""):
		return self.getUsedClasses(parentNamespaceName, \
								refSrcLoc.getSourceFile(), refSrcLoc.getStart(),\
								referencingClassName)

	##
	# Returns a list of qualified class names used at the given location.
	#
	# TODO: also append superclasses.
	##
	def getUsedClasses(self, parentNamespaceName, referencingSourceFile,\
					referencingLineNr,referencingClassName=""):
		nestedUsedClasses = []

		classUsageChecker = self.getClassUsageChecker()
		classesUsed = classUsageChecker.getSurroundingNamespaceNames(referencingSourceFile, referencingLineNr)

		for usedClass in classesUsed:
			nestedUsedClasses.append(usedClass)

		usedNamespaces = self.getUsedNamespaces(referencingSourceFile, referencingLineNr)
		for usedNamespace in usedNamespaces:
			if usedNamespace == "":
				continue

			for usedClass in classesUsed:
				nestedUsedClass = usedClass

				if usedNamespace != "":
					nestedUsedClass = usedNamespace + "::" + usedClass

				if not(nestedUsedClass in nestedUsedClasses):
					nestedUsedClasses.append(nestedUsedClass)

		classesUsed = self.getTransitiveSuperclasses(referencingClassName)
		for usedClass in classesUsed:
			if not(usedClass in nestedUsedClasses):
				nestedUsedClasses.append(usedClass)


		return  nestedUsedClasses

	def getTransitiveSuperclasses(self,referencingClassName):
		return self.transInhDict.getTransitiveSuperclasses(referencingClassName)

	##
	# Returns a list of namespaces used at the given location.
	##
	#def getUsedNamespaces(self, parentNamespaceName, referencingSourceFile, referencingLineNr):
	def getUsedNamespaces(self, referencingSourceFile, referencingLineNr):
		usedNamespaces = []

		namespaceUsageChecker = self.getNamespaceUsageChecker()
		namespacesUsed = namespaceUsageChecker.getSurroundingNamespaceNames(referencingSourceFile, referencingLineNr)

		namespaceContainmentChecker = self.getNamespaceContainmentChecker()
		namespacesDeclared = namespaceContainmentChecker.getSurroundingNamespaceNames(referencingSourceFile, referencingLineNr)

		for declaredNamespace in namespacesDeclared:
			if not(declaredNamespace in usedNamespaces):
					usedNamespaces.append(declaredNamespace)

		for usedNamespace in namespacesUsed:
			if not(usedNamespace in usedNamespaces):
				usedNamespaces.append(usedNamespace)

			for declaredNamespace in namespacesDeclared:
				if declaredNamespace == "":
					continue

				nestedNamespace = declaredNamespace + "::"+ usedNamespace
				if not(nestedNamespace in usedNamespaces):
					usedNamespaces.append(nestedNamespace)

		if not("" in usedNamespaces):
			usedNamespaces.append("")

		return  usedNamespaces
