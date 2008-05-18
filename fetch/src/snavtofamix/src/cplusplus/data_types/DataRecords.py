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


# This file declares a number of records to represent entities.
from sets import Set

from log4py import Logger

import common.cpp
from common.famix import cdifReader, cdifWriter, cdifReaderCache
from common.entity_references import EntityReferences
import common.utils
from common.ResolveableInformation import TypeReference

##
# Encapsulation of an entity.
##
class Entity:
	##
	# Initialize an entity using a given line.
	# Remembers the given line and splits it in pieces.
	##
	def __init__(self, line):
		self.line = line
		self.cols = self.line.split("{")[0].split(" ")
		self.cols.extend(self.line.split("{")[1:])
		for i in range(0,len(self.cols)):
			curCol = self.cols[i].replace("{","").replace("}","")
			self.cols[i] = curCol

	def determineSourceLocation(self):
		# Sample Entry:
		# Byte t Xls postfix mi r 000403 spreadsheet/toXls.cxx;{} {const string &}
		self.sourceFile = self.cols[8].split(";")[0]
		self.start = self.cols[7]

	def getReference(self):
		reference = EntityReference()
		reference.setSourceFile(self.sourceFile)
		reference.setLineNr(self.start)
		return reference

##
# Encapsulation of the data on an inheritance link between classes.
#
# Attributes:
# -- initialized in Entity constructor --
# self.line
# self.cols
# -- initialized in constructor --
# self.subclass - The name of the subclass
# self.superclass - The name of the superclass
# self.dst_sourceFile - The name of the file in which the superclass is resolved (if resolved)
# self.dst_lineNr - The line number at which the superclass is declared in the dst_sourceFile
# -- via decomposeVisibility --
# self.accessControlQualifier - the visibility ({public,private,protected}) of the superclass
# -- via determineSourceLocation --
# self.sourceFile - the name of the file at which the inheritance link is declared
# self.start - the line number at which the inheritance link is declared
##
class InheritanceEntity(Entity):
	##
	# Initialize an entity using a given line.
	##
	def __init__(self, line):
		#Entity.__init__(self, line)
		self.line = line
		self.cols = self.line.split(";")
		self.subclass = self.cols[0]
		self.superclass = self.cols[1]
		self.sourceFile = ""
		self.start = ""
		self.accessControlQualifier = ""
		self.decomposeVisibility()
		self.determineSourceLocation()
		self.dst_sourceFile=""
		self.dst_lineNr=""

	##
	# Determine the visibility of the superclass.
	#
	# Fills in self.accessControlQualifier
	##
	def decomposeVisibility(self):
		vis = self.cols[5].strip()
		self.accessControlQualifier = ""
		if "0x1" == vis:
			self.accessControlQualifier = "private"
		elif "0x2" == vis:
			self.accessControlQualifier = "protected"
		elif "0x4" == vis:
			self.accessControlQualifier = "public"

	##
	# Retrieve the source-location of the entity, consisting of
	# -sourceFile
	# -start-position
	##
	def determineSourceLocation(self):
		# Sample Entry:
		# Bar;Foo;000007.018;src/Ipublic.cpp;7.22;0x4;{};{};{};{}
		self.sourceFile = self.cols[3]
		self.start = self.cols[4].split(".")[0]


##
# Encapsulation of a Class Reference entity.
##
class DepricatedClassReferenceEntity(Entity):
	##
	# Initializes a class reference entity instance with a given
	# line from the access database
	##
	def __init__(self, clRefLine):
		Entity.__init__(self, clRefLine)
		self.cols = self.line.split(";")
		self.dst_class = self.cols[1]
		self.src_class = self.cols[3]
		self.src_name = self.cols[4]
		self.calculateSourceParams()

	##
	# @requires: self.line to be initialized
	# @ensures initialization of sourceParams
	# source are method/function implementations
	##
	def calculateSourceParams(self):
		self.srcParams = self.line.split("{")[2].split("}")[0]

	def sourceSignature(self):
		if "#" == self.src_class:
			return self.src_name+"("+self.srcParams+")"
		else:
			return self.src_class+"."+self.src_name+"("+self.srcParams+")"

##
# Encapsulation of the data on an entity that has a type.
# Examples are:
# 	+Attributes
#	+Methods
#	+Functions
#	+Global variables
#	+(Local variables)
#
# TODO: subclass this entity with a ContainedTypedEntity, with subclasses
# Attribute and Method. GlobalVariable and Function should merely remain
# subclasses of this class.
##
class TypedEntity(Entity):
	##
	# Pass the parameters to the super constructor.
	##
	def __init__(self, typedEntityLine):
		Entity.__init__(self, typedEntityLine)
		self.sourceFile = ""
		self.start = ""
		self.typeReference = TypeReference()

	##
	# Retrieve a reference to the type of this entity.
	##
	def getTypeReference(self):
		return self.typeReference

	def setTypeReference(self, typeRef):
		self.typeReference = typeRef

	##
	# This 'should' become the central place for
	# cleaning a type identifier from non-relevant
	# content such as e.g. 'const' or a space between
	# the type name and a pointer
	##
	def cleanType(self, attributeType):
		attributeType = attributeType.replace("const ","")
		return attributeType

	##
	# Verify whether the source-file and line-nr of the declaredType
	# are resolved
	##
	def isResolved(self):
		typeReference = self.getTypeReference()

		resolved = cpp.isPrimitiveType(typeReference.getReferencedName())

		if (not(resolved) and (typeReference.getResolvedName() != None)):
			resolved = ( (typeReference.getSourceFile() != "") and (typeReference.getLineNr() != "") )

		return resolved

class ContainedEntity:
	def __init__(self):
		self.parentReference = TypeReference()

	def getParentReference(self):
		return self.parentReference

	def setNamespaceName(self, namespaceName):
		self.namespaceName = namespaceName

		if (self.namespaceName != None) and (self.namespaceName != ""):
			self.owner = self.namespaceName + "::" + self.owner

		self.getParentReference().setReferencedName(self.owner)

