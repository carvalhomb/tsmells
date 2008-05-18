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

from ClassDictionary import getNamespaceName
from QualifiedNameHelperFunctions import 	getParentNamespaceName,\
											getNonQualifiedName

##
# Collects references to classes from the class dictionary.
##
class ClassCollector:
	##
	# @param classDict: a complete class dictionary
	##
	def __init__(self, classDict, typeDefDict):
		self.classDict = classDict
		self.typeDefDict = typeDefDict
		self.log = Logger().get_instance(self)

	##
	# Collect references to those classes whose name is className,
	# and whose namespace is contained in the namespaceList.
	#
	# TODO: deal with parameter part
	##
	def collectClassReferences(self, className, namespaceList):
		classReferences = []

		for namespaceName in namespaceList:
			nestedClassReferences = self.classDict.getClassesByNamespace(className, namespaceName)

			for classReference in nestedClassReferences:
				if not(classReference in classReferences):
					classReferences.append(classReference)

			qualifiedName = className

			if namespaceName != "":
				qualifiedName = namespaceName + "::" + className

			if self.typeDefDict.containsAlias(qualifiedName):
				baseType = self.typeDefDict.getBaseType(qualifiedName)
				parentNamespace = getParentNamespaceName(baseType)
				nonQualifiedBaseType = getNonQualifiedName(baseType)

				nestedClassReferences = self.classDict.getClassesByNamespace(nonQualifiedBaseType, parentNamespace)

				for classReference in nestedClassReferences:
					if not(classReference in classReferences):
						classReferences.append(classReference)


		return classReferences
