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

from DataRecords 		import 		TypedEntity, ContainedEntity
from common.utils 		import 		returnSourceLocation, appendStringList
from common.entity_references.EntityReferences import \
									NamespaceElementEntityReference,\
									ClassMemberEntityReference
from common.ResolveableInformation import \
									TypeReference
from log4py import Logger

##
# An accessible entity is either an attribute or a global variable.
# This class forms an abstract class enabling reuse between
# the implementation of attribute and global variable entities.
##
class AnAccessibleEntity(TypedEntity):
	def __init__(self, line):
		TypedEntity.__init__(self, line)
		self.log = Logger().get_instance(self)

		self.namespaceName = None

		self.cols = self.line.split(";")
		self.sourceIndicationIndex = -1 # to be initialized by subclass
		self.visibilityIndicationIndex = -1 # to be initialized by subclass
		self.nameIndex = -1 # to be initialized by subclass

		self.initialize()
		self.postInitialize() # for subclasses to override

	def initialize(self):
		self.initializeColsIndices()
		if self.cols != ['']:
			self.decomposeData()

	##
	# Initialize at least the following attributes:
	#	self.sourceIndicationIndex
	#	self.visibilityIndicationIndex
	#	self.nameIndex
	##
	def initializeColsIndices(self): abstract

	##
	# Provides an opportunity for subclasses to implement additional
	# initializations. E.g., initialization of additional attirbutes.
	##
	def postInitialize(self):
		pass

	##
	# Decomposes the entity line.
	#
	# @requires: self.cols to be initialized
	# @ensures: class, name and type of both source and destination to be initialized
	##
	def decomposeData(self):
		self.sourceFile = self.cols[self.sourceIndicationIndex]
		self.start = self.cols[self.sourceIndicationIndex+1].split(".")[0]
		self.end = self.start
		#self.sourceFile, self.start, self.end = returnSourceLocation(self.cols[self.sourceIndicationIndex])
		self.owner = ""
		declaredType = self.retrieveDeclaredType()
		typeReference = self.getTypeReference()
		typeReference.setReferencedName(declaredType)
		self.name = self.cols[self.nameIndex]
		self.decomposeVisibilityData()

	def retrieveDeclaredType(self):
		dbfields=self.cols[self.visibilityIndicationIndex+1:]
		contents = appendStringList(dbfields)

		if ( contents[0] != "{" ):
			self.log.error("Cannot deduce declared type from line.")
			return

		i = 1
		attrChars = []
		while contents[i] != "}":
			attrChars.append(contents[i])
			i = i + 1

		return self.cleanType(''.join(attrChars))


	def decomposeVisibilityData(self):
		vis = self.cols[self.visibilityIndicationIndex]

		if vis == "0x1":
			self.visibility = "private"
			self.hasClassScope = False
		elif vis == "0x2":
			self.visibility = "protected"
			self.hasClassScope = False
		elif vis == "0x4":
			self.visibility = "public"
			self.hasClassScope = False
		elif vis == "0x9":
			self.visibility = "private"
			self.hasClassScope = True
		elif vis == "0xa":
			self.visibility = "protected"
			self.hasClassScope = True
		elif vis == "0xc":
			self.visibility = "public"
			self.hasClassScope = True
		elif vis == "0x0": # constant global variables
			self.visibility = "public"
			self.hasClassScope = False
		else:
			typeReference = self.getTypeReference()
			self.log.warn( "Unknown visibility indicator for entity ",\
						typeReference.getReferencedName(),"::",self.name,": ",\
						vis)
			self.visibility = ""
			self.hasClassScope = False

##
# Encapsulation of the information
# required to refer to a global variable or attribute.
##
class AccessibleEntityReferenceEntity(NamespaceElementEntityReference):
	def __init__(self):
		NamespaceElementEntityReference.__init__(self)
		self.typeReference = TypeReference()

	def getSignature(self):
		return self.getName()

	def getOwnerName(self):
		return ""

	def getOwnerTemplateParameters(self):
		return ""

	def getUniqueName(self):
		uniqueName = self.getSignature()

		if self.getNamespaceName() != "":
			uniqueName = self.getNamespaceName() + "::" + uniqueName

		return uniqueName

	def getTypeReference(self):
		return self.typeReference

	def setTypeReference(self, typeReference):
		self.typeReference = typeReference

	def __eq__(self,other):
		isEqual = False

		if other != None:
			isEqual = NamespaceElementEntityReference.__eq__(self,other)
			isEqual = (isEqual and other.getTypeReference() == self.getTypeReference())

		return isEqual

##
# Encapsulation of the data on an attribute instance
##
class AttributeEntity(AnAccessibleEntity, ContainedEntity):
	##
	# Initializes an attribute entity instance with a given
	# line from the attributes database
	#
	# @attrLine - line from the attributes part of SourceNavigator's
	# database-dump, e.g.:
	# Foo;f;000003.006;src/Aprivate.cpp;3.7;0x1;{int};{};{};{}
	##
	def __init__(self, attrLine):
		ContainedEntity.__init__(self)
		AnAccessibleEntity.__init__(self, attrLine)
		self.log = Logger().get_instance(self)

	def initializeColsIndices(self):
		self.sourceIndicationIndex = 3
		self.visibilityIndicationIndex = 5
		self.nameIndex = 1
		self.ownerIndex = 0

	def postInitialize(self):
		self.owner = self.cols[self.ownerIndex]

		if (self.namespaceName != None) and (self.namespaceName != ""):
			self.owner = self.namespaceName + "::" + self.owner

		self.getParentReference().setReferencedName(self.owner)
		self.ownerTemplateParameters = ""

	def getReference(self):
		reference = AttributeReferenceEntity()
		reference.setName(self.name)
		reference.setOwnerName(self.owner)
		reference.setOwnerTemplateParameters(self.ownerTemplateParameters)
		reference.setSourceFile(self.sourceFile)
		reference.setLineNr(self.start)
		reference.setTypeReference(self.getTypeReference())
		reference.setNamespaceName(self.namespaceName)
		return reference

##
# Encapsulation of the information
# required to refer to an attribute.
##
class AttributeReferenceEntity(AccessibleEntityReferenceEntity, ClassMemberEntityReference):
	def __init__(self):
		AccessibleEntityReferenceEntity.__init__(self)
		ClassMemberEntityReference.__init__(self)

	def getOwnerName(self):
		return ClassMemberEntityReference.getOwnerName(self)

	def getOwnerTemplateParameters(self):
		return ClassMemberEntityReference.getOwnerTemplateParameters(self)

	def getUniqueName(self):
		uniqueName = self.getName()

		ownerName = self.getOwnerName()

		if self.getOwnerTemplateParameters() != "":
			ownerName += "<" + self.getOwnerTemplateParameters() + ">"

		uniqueName = ownerName + "::" + uniqueName

		if self.getNamespaceName() != "":
			uniqueName = self.getNamespaceName() + "::" + uniqueName

		return uniqueName

	def __eq__(self,other):
		isEqual = False

		if other != None:
			isEqual = AccessibleEntityReferenceEntity.__eq__(self,other)
			isEqual = isEqual & ClassMemberEntityReference.__eq__(self,other)

		return isEqual


class GlobalVariableEntity(AnAccessibleEntity):
	##
	# Initializes an attribute entity instance with a given
	# line from the attributes database
	#
	# @globalVarLine - a line from the global variables file as retrieved
	# from SourceNavigators database-dump, e.g.:
	# i;000001.004;src/Gv.cpp;1.5;0x0;{int};{};{};{}
	##
	def __init__(self, globalVarLine):
		AnAccessibleEntity.__init__(self, globalVarLine)
		self.log = Logger().get_instance(self)

	def initializeColsIndices(self):
		self.sourceIndicationIndex = 2
		self.visibilityIndicationIndex = 4
		self.nameIndex = 0

	def postInitialize(self):
		pass

	##
	# Retrieve an AccessibleEntityReference to this GlobalVariableEntity.
	##
	def getReference(self):
		reference = AccessibleEntityReferenceEntity()
		reference.setName(self.name)
		reference.setSourceFile(self.sourceFile)
		reference.setLineNr(self.start)
		reference.setNamespaceName(self.namespaceName)

		return reference
