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

from DataRecords import TypedEntity, ContainedEntity
from common.entity_references.MultipleLocationEntity import MultipleLocationEntity
from common.utils import returnSourceLocation, retrieveSignature
from common.entity_references.EntityReferences import \
									InvokableEntityReference
from common.entity_references.EntityReferences import MethodEntityReference
from common.famix import cdifReader
from log4py import Logger
from common.SourceLocation import SourceLocation
from cplusplus.invokeable_entities.TypeTranslator import TypeTranslator

class InvokeableEntity(TypedEntity, MultipleLocationEntity):
	sourceLocationColumnIndex = -1
	
	def __init__(self, isImplementation, line):
		TypedEntity.__init__(self, line)
		MultipleLocationEntity.__init__(self)
		self.log = Logger().get_instance(self)
		self.isAbstract = False
		self.isImplementation = isImplementation
		self.namespaceName = None
		

	def isAnImplementation(self):
		return self.isImplementation

	##
	# Decomposes the entity line.
	##
	def decomposeData(self):
		# TODO: invEntityLoc - use declLocation or defLocation
		self.sourceFile = self.cols[self.sourceLocationColumnIndex]
		self.start = self.cols[self.sourceLocationColumnIndex+1].split(".")[0]
		self.end = self.start
		srcLoc = SourceLocation(self.sourceFile, self.start, self.end)
		if self.isAnImplementation():
			self.addDefinition(srcLoc)
		else:
			self.addDeclaration(srcLoc)
	
		i=self.sourceLocationColumnIndex+3
		declaredType, self.parameters = retrieveSignature(self.cols[i:])
		# deal with inlined method declarations
		declaredType = declaredType.replace("inline ", "")
		typeRef = self.getTypeReference()
		typeRef.setReferencedName(declaredType)
		self.preprocessParameters()

	##
	# Note: self.parameters is surrounded with brackets.
	##
	def preprocessParameters(self):
		self.parameters = self.parameters.replace("const ", "")
		self.parameters = self.parameters.replace(" &", "")
		self.parameters = \
			self.parameters.replace("unsigned *", "unsigned int *")
		self.parameters = \
			self.parameters.replace("unsigned [", "unsigned int [")
		self.parameters = self.parameters.replace("unsigned ", "")
		self.parameters = self.parameters.replace(" []", "[]")
		self.parameters = self.parameters.replace(" *", "*")

	##
	# Rephrase parameters as to remove typedef aliases.
	##
	def postProcessParameters(self, typedefDict):
		if self.parameters == "()":
			return

		typeTranslator = TypeTranslator(typedefDict)

		rephrasedParameterList = []

		parameterList = self.parameters.lstrip("(").rstrip(")").strip().split(",")

		for parameter in parameterList:
			parType = typeTranslator.translateTypeName(parameter)
			rephrasedParameterList.append(parType)

		self.parameters = "(" + ",".join(rephrasedParameterList) + ")"

	##
	# Retrieve a reference to this invokeable entity.
	##
	def getReference(self):
		assert False, "Should be overridden by subclasses."

##
# Encapsulation of the data on a method instance.
# An example of a method line is:
# AbsFormula setArgument 000091.006 spreadsheet/absFormula.cxx;91.29 0x0 {void} {IFormula *} {formula} {}
##
class MethodEntity(InvokeableEntity, ContainedEntity):

	## ATTRIBUTES ##
	# -- initialized in constructor --
	# cols
	# methodImplementationDatabase
	# isAbstract
	# isImplementation
	# isAbstractCalculated
	# ownerTemplateParameters the template parameters of the method owner (ClassEntity.template)
	# -- initialized in decomposeData
	# sourceFile, start, end
	# retType, parameters
	# retClass
	# owner
	# name
	##
	# Initializes a method entity instance with a given
	# line from the methods databases
	#
	# Note that we currently don't verify the isAbstract parameter.
	##
	def __init__(self, isAbstract, isImplementation, methodLine):
		ContainedEntity.__init__(self)
		InvokeableEntity.__init__(self, isImplementation, methodLine)
		self.log = Logger().get_instance(self)
		self.sourceLocationColumnIndex=3
		self.cols = self.line.split(";")
		self.isAbstract = isAbstract
		self.isAbstractCalculated = False
		self.decomposeData()

		self.removeTemplateParametersFromName()
		self.decomposeVisibility()
		self.ownerTemplateParameters=""

		if (self.namespaceName != None) and (self.namespaceName != ""):
			self.owner = self.namespaceName + "::" + self.owner

		self.getParentReference().setReferencedName(self.owner)

		if (self.name == self.owner) or (self.name == ("~" + self.owner)):
			self.getTypeReference()
			self.typeReference.setReferencedName("")

	##
	# Decomposes the entity line.
	##
	def decomposeData(self):
		InvokeableEntity.decomposeData(self)
		self.owner = self.cols[0]
		self.name = self.cols[1]

	# this constructor is used by snavJavaToFamix
	# no constructor overloading in python!
	#def __init__(self, methodLine):
	#	Entity.__init__(self, methodLine)
	#	self.cols = self.line.split(" ")
	#	#self.methodImplementationDatabase = methodImplementationDatabase
	#	self.isAbstract = False
	#	self.decomposeData()

	##
	# Ensure that the method-name does not contain template parameters, such as in
	# e.g., <X>walk
	# or <X,int,D>walk
	##
	def removeTemplateParametersFromName(self):
		# watch out for names as operator< and operator>
		if (self.name.startswith("<") and (">" in self.name)):
			# cut of the part until and including the first > character
			self.name=self.name[self.name.index(">")+1:]

	##
	# Decomposes visibility of the method
	#
	# Access Modifier:
	# - 0x1 stands for private
	# - 0x2 stands for protected
	# - 0x4 stands for public
	# - 0x0 also seems to be private
	##
	def decomposeVisibility(self):
		self.isConstructor = False
		self.hasClassScope = False
		vis = self.cols[5].strip()
		if "0x0" == vis:
			self.accessControlQualifier = ""
			self.isAbstract = False
		elif "0x4000" == vis:
			self.accessControlQualifier = ""
			if not "~" in self.name:
				self.isConstructor = True
			self.isAbstract = False

		elif "0x1" == vis:
			self.accessControlQualifier = "private"
			self.isAbstract = False
		elif "0x1001" == vis:
			self.accessControlQualifier = "private"
			self.isAbstract = False
		elif "0x2001" == vis:
			self.accessControlQualifier = "private"
			self.isAbstract = False
			# also virtual
		elif "0x4001" == vis:
			self.accessControlQualifier = "private"
			self.isConstructor = True
		elif "0x5001" == vis:
			# virtual destructor
			self.accessControlQualifier = "private"
			self.isAbstract = False

		elif "0x2" == vis:
			self.accessControlQualifier = "protected"
			self.isAbstract = False
		elif "0x1002" == vis:
			self.accessControlQualifier = "protected"
			self.isAbstract = False
			# also virtual
		elif "0x4002" == vis:
			self.accessControlQualifier = "protected"
			self.isAbstract = False
			self.isConstructor = True
		elif "0x5002" == vis:
			self.accessControlQualifier = "protected"


		elif "0x4" == vis:
			self.accessControlQualifier = "public"
			self.isAbstract = False
		elif "0x1004" == vis:
			self.accessControlQualifier = "public"
			self.isAbstract = False
			# also virtual
		# all operator overloading seems to fall in this category
		# are these the overriding methods?
		elif "0x2004" == vis:
			self.accessControlQualifier = "public"
			self.isAbstract = False
			# virtual/overridden?
		elif "0x4004" == vis or "0x6004" == vis:
			# difference with 0x4000 ?
			# constructor/destructor
			self.accessControlQualifier = "public"
			typeReference = self.getTypeReference()
			typeReference.setReferencedName("")
			if not "~" in self.name:
				self.isConstructor = True
			self.isAbstract = False
		elif "0x5004" == vis:
			# virtual destructor
			self.accessControlQualifier = "public"
			self.isAbstract = False
		elif "0x9004" == vis:
			self.accessControlQualifier = "public"
			self.hasClassScope = False
			self.isAbstract = True
			# also a virtual function, but that is implicit
			# to the abstractness
		elif "0x9" == vis:
			self.accessControlQualifier = "private"
			self.hasClassScope = True
			self.isAbstract = False
		elif "0xa" == vis:
			self.accessControlQualifier = "protected"
			self.hasClassScope = True
			self.isAbstract = False
		elif "0xc" == vis:
			self.accessControlQualifier = "public"
			self.hasClassScope = True
			self.isAbstract = False
		# directly implemented public static methods
		elif "0x200c" == vis:
			self.accessControlQualifier = "public"
			self.hasClassScope = True
			self.isAbstract = False
		else:
			self.accessControlQualifier = ""
			# 5002, 7004, 2002, 3001, 9002, 3004
			# self.log.warn( "@@TODO@@ T22 add method visibility property for code",vis)

	def isAbstractMethod(self):
		return self.isAbstract

	def getUniqueName(self):
		return self.owner + "." + self.getUniqueMethodName()

	def getParameterDescription(self):
		paramDescr = ""
		if self.isAnImplementation() :
			paramDescr = "("+self.parameters+")"
		else :
			paramDescr = self.parameters
		return paramDescr

	def getUniqueMethodName(self):
		return self.name + self.getParameterDescription()

	def existsInCache(self):
		return cdifReader.isMethod(self.getUniqueName())

	##
	# Retrieve a MethodEntityReference to this MethodEntity.
	##
	def getReference(self):
		reference = MethodEntityReference()
		reference.setName(self.name)
		reference.setFormalParameters(self.parameters)
		reference.setLocation(self)
		reference.setSourceFile(self.sourceFile)
		reference.setLineNr(self.start)
		# TODO: invEntityLoc - isImplementation flag will become redundant
		reference.setIsImplementation(self.isAnImplementation())
		reference.setIsAbstract(self.isAbstractMethod())
		reference.setOwnerName(self.owner)
		reference.setOwnerTemplateParameters(self.ownerTemplateParameters)
		reference.setNamespaceName(self.namespaceName)
		reference.setTypeReference(self.getTypeReference())
		reference.setParentReference(self.getParentReference())
		reference.setModifiers(self.accessControlQualifier)

		return reference

##
# Encapsulation of the data on a function instance.
# An example of a function line is:
# main;000039.004;spreadsheet/main.cxx;39.8;0x0;{int};{int,char *[]};{argc,argv};{}
##
class FunctionEntity(InvokeableEntity):
	

	##
	# TODO: similar to the MethodEntity, a parameter should be given
	# to the constructor to indicate whether this function entity is an
	# implementation or a declaration
	##
	def __init__(self, isImplementation, functionLine):
		InvokeableEntity.__init__(self, isImplementation, functionLine)
		self.log = Logger().get_instance(self)
		self.sourceLocationColumnIndex=2
		self.cols = self.line.split(";")
		self.decomposeData()

	##
	# Decomposes the entity line.
	##
	def decomposeData(self):
		InvokeableEntity.decomposeData(self)
		self.name = self.cols[0]


	##
	# Retrieve an InvokeableEntityReference to this Functionentity.
	##
	def getReference(self):
		reference = InvokableEntityReference()
		reference.setName(self.name)
		reference.setFormalParameters(self.parameters)
		reference.setLocation(self)
		reference.setSourceFile(self.sourceFile)
		reference.setLineNr(self.start)
		# TODO: invEntityLoc - isImplementation flag will become redundant
		reference.setIsImplementation(self.isImplementation)
		reference.setNamespaceName(self.namespaceName)
		reference.setTypeReference(self.getTypeReference())

		return reference

