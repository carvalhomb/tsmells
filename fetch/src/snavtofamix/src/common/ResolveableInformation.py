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

# This file declares encapsulations of information necessary to resolve
# entities

import cpp

##
# The idea is to use aggregation instead of inheritance to deal with type
# and containment relations between entities. Accordingly, a TypedEntity
# can contain a TypeReference instance to refer to the type of the entity.
# Similarly, a ContainedEntity can contain a TypeReference to refer to the
# parent of the entity.
##
class TypeReference:
	##
	# self.referencedName -- The name of the type as referenced.
	# self.resolvedName -- The unique name of the type as resolved.
	# self.sourceFile -- The name of the source file in which the type
	#						 is declared.
	# self.lineNr -- The line number at which the source file is declared.
	##
	def __init__(self):
		self.referencedName = None
		self.resolvedName = None
		self.sourceFile = ""
		self.lineNr = ""

	def getReferencedName(self):
		return self.referencedName

	def getCleanName(self):
		cleanName = self.getReferencedName().replace("const ","")
		cleanName = cleanName.replace(" *","").replace(" []","")
		cleanName = cleanName.replace("*","").replace("[]","")
		cleanName = cleanName.replace(" &","").replace("&","")
		return cleanName

	def isPrimitive(self):
		return cpp.isPrimitiveType(self.getCleanName()) or (self.getCleanName() == "")

	def setReferencedName(self, name):
		self.referencedName = name

	def getResolvedName(self):
		return self.resolvedName

	def setResolvedName(self, name):
		self.resolvedName = name

	def getSourceFile(self):
		return self.sourceFile

	def setSourceFile(self, sourceFile):
		self.sourceFile = sourceFile

	def getLineNr(self):
		return self.lineNr

	def setLineNr(self, lineNr):
		self.lineNr = lineNr

	def __eq__(self, other):
		isEqual = True

		if other == None:
			return False

		if type(self) != type(other):
			isEqual = False

		#if self.getReferencedName() != other.getReferencedName():
		#	isEqual = False

		if self.getResolvedName() != other.getResolvedName():
			isEqual = False

		if self.getSourceFile() != other.getSourceFile():
			isEqual = False

		if self.getLineNr() != other.getLineNr():
			isEqual = False

		return isEqual
