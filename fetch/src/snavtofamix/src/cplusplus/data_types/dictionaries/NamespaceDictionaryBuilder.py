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


import os
from cplusplus.data_types.NamespaceEntity import isValidNamespaceDeclarationLine, NamespaceEntity
from NamespaceDictionary import NamespaceDictionary
from log4py import Logger

class NamespaceDictionaryBuilder(object):
	def __init__(self, namespaceFileName):
		self.namespaceFileName = namespaceFileName
		self.namespaceDict = NamespaceDictionary()

	##
	# Open the file and return whether the operation was
	# succesful.
	##
	def openFile(self):
		fileExists = os.path.exists(self.namespaceFileName)

		if fileExists:
			self.namespaceFile=open(self.namespaceFileName, 'r')

		return fileExists

	##
	# Seam to enable subclasses to stub file operations.
	##
	def closeFile(self):
		self.namespaceFile.close()

	def build(self):
		fileOpenedSuccessful = self.openFile()

		if not fileOpenedSuccessful:
			return None

		for line in self.namespaceFile:
			self.currentLine = line.strip()
			self.buildCurrentEntity()

		self.closeFile()

	##
	# Create a NamespaceEntity for the
	# self.currentLine.
	##
	def buildCurrentEntity(self):
		added = False
		self.currentEntity = None

		if isValidNamespaceDeclarationLine(self.currentLine):
			self.currentEntity = NamespaceEntity(self.currentLine)
			added = self.namespaceDict.add(self.currentEntity)

		return added

	def getNamespaceDictionary(self):
		return self.namespaceDict
