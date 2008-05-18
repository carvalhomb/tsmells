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

from cplusplus.data_types.dictionaries.ClassUsageDictionary import ClassUsageDictionary
from cplusplus.data_types.dictionaries.NamespaceUsageDictionaryBuilder import NamespaceUsageDictionaryBuilder
from cplusplus.data_types.ClassUsageEntity import ClassUsageEntity, isValidClassUsageLine
from cplusplus.data_types.NamespaceContainmentChecker import NamespaceContainmentChecker

class ClassUsageDictionaryBuilder(NamespaceUsageDictionaryBuilder):
	def __init__(self, namespaceFileName):
		self.namespaceFileName = namespaceFileName
		self.namespaceDict = ClassUsageDictionary()

	##
	# Create a ClassUsageEntity for the
	# self.currentLine.
	##
	def buildCurrentEntity(self):
		added = False
		self.currentEntity = None

		if isValidClassUsageLine(self.currentLine):
			self.currentEntity = ClassUsageEntity(self.currentLine)
			added = self.namespaceDict.add(self.currentEntity)

		return added

	##
	# Mark for each class usage reference whether it is surrounded by a namespace
	# or not. By calculating this information beforehand, verifications of
	# transitively included class usages can be made less expensive.
	##
	def refineDictWithNamespaceInfo(self, namespaceContainmentChecker):
		for classUsageRef in self.namespaceDict.getAllReferences():
			srcLoc = classUsageRef.getSourceLocation()
			parentNamespaceName = namespaceContainmentChecker.getSurroundingNamespaceName(srcLoc)
			classUsageRef.setParentNamespaceName(parentNamespaceName)
