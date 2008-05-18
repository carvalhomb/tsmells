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


from DataRecords import Entity
from common.utils import returnSourceLocation
from common.SourceLocation import SourceLocation
from common.entity_references.EntityReferences import NamespaceElementEntityReference
import common.cpp

##
# Encapsulation of the data on a class.
#
# Attributes:
# -- initialized in Entity constructor --
# self.line
# self.cols
# -- initialized in constructor --
# self.name - the name of the class
# self.template - the template parameters of the class including lt/gt-brackets (e.g., "X")
# -- via determineSourceLocation --
# self.sourceFile - the name of the file at which the class is declared.
# self.start - the line number at which the class is declared in the sourceFile
##
class ClassEntity(Entity):
	##
	# Initialize a class entity given a line from the
	# SourceNavigator dbdump file (.classes).
	##
	def __init__(self, line):
		#Entity.__init__(self, line)	# entity will initalize self.line and self.cols
		self.line = line
		self.cols = self.line.split(";")
		self.name = self.cols[0]
		self.sourceLocation = None
		self.determineSourceLocation()	# will initialize self.soureFile and self.start
		self.template=""
		self.checkForTemplate() # will initialize self.template

	##
	# Retrieve the source-location of the entity, consisting of
	# -sourceFile
	# -start-position
	##
	def determineSourceLocation(self):
		# Bar;000001.006;src/AbstractClass.cpp;1.9;0x0;{};{};{};{}
		# the old way -- to be removed
		sourceFile = self.cols[2]
		start = self.cols[3].split(".")[0]
		# the new way
		self.sourceLocation = SourceLocation(sourceFile, start, start)

	##
	# Remembers the template parameters of the class, and stores them in self.template.
	# The format of self.template does not include the brackets, e.g. "X".
	##
	def checkForTemplate(self):
		tplName = self.cols[6].strip()
		tplName = tplName.replace("{","")
		tplName = tplName.replace("}","")
		tplName = tplName.replace("class ", "")
		tplName = tplName.replace("typename ", "")
		tplName = tplName.replace(" ", "")
		if "" != tplName:
			self.template = tplName.strip().lstrip("<").rstrip(">")

	def isAbstract(self):
		""" Verify whether the class is abstract. """
		# todo, see task 39
		return False

def emptyIfNone(givenValue):
	returnValue = ""

	if givenValue != None:
		returnValue = givenValue

	return returnValue

##
# Encapsulation of the information
# required to refer to a class.
##
class ClassReferenceEntity :
	def __init__(self, className, namespaceName, sourceFile, lineNr, templateParameters):
		self.className = emptyIfNone(className)
		self.namespaceName = emptyIfNone(namespaceName)
		self.sourceFile = emptyIfNone(sourceFile)
		self.lineNr = emptyIfNone(lineNr)
		self.templateParameters = emptyIfNone(templateParameters)

	def getClassName(self):
		return self.className

	def getNamespaceName(self):
		return self.namespaceName

	def getUniqueName(self):
		uniqueName = self.getClassName()

		if self.getNamespaceName() != "":
			uniqueName = self.getNamespaceName() + "::" + self.getClassName()

		#if (self.getTemplateParameters() != None) and (self.getTemplateParameters() != ""):
		#	uniqueName = uniqueName + "<" + self.getTemplateParameters() + ">"

		return uniqueName

	def getSourceFile(self):
		return self.sourceFile

	def getLineNr(self):
		return self.lineNr

	def getTemplateParameters(self):
		return self.templateParameters

	def __eq__(self, other):
		""" returns true if self equals other, false otherwise """
		isEqual = False

		if type(self) == type(other):
			nameEqual = self.getClassName() == other.getClassName()
			namespaceNameEqual = self.getNamespaceName() == other.getNamespaceName()
			fileNameEqual = self.getSourceFile() == other.getSourceFile()
			lineNrEqual = self.getLineNr() == other.getLineNr()
			isEqual = (nameEqual and namespaceNameEqual) and ( fileNameEqual and lineNrEqual)

		return isEqual

class TypeDefEntity(Entity):
	##
	# Note: the self.sourceFile and self.start fields
	# inherited from Entity are no longer used
	#
	#
	# e.g. Word;000038.027;spreadsheet/toXls.h;38.31;0x0;{unsigned short};{};{};{}
	##
	def __init__(self, line):
		cols = line.strip().split(";")
		self.aliasName = cols[0]
		self.sourceFile = cols[2]
		self.start = cols[3].split(".")[0]
		self.end = self.start
		self.baseType = cols[5].replace("{","").replace("}","") 
		self.baseSourceFile = ""
		self.baseLineNr = ""

	def getReference(self):
		reference = TypeDefEntityReference()

		reference.setBaseType(self.baseType)
		reference.setName(self.aliasName)
		sourceLocation = SourceLocation(self.sourceFile, self.start, self.start)
		reference.setSourceLocation(sourceLocation)
		baseSourceLocation = SourceLocation(self.baseSourceFile, self.baseLineNr, self.baseLineNr)
		reference.setBaseSourceLocation(baseSourceLocation)

		return reference

class TypeDefEntityReference(NamespaceElementEntityReference):
	def __init__(self):
		NamespaceElementEntityReference.__init__(self)
		self.baseType = None
		self.sourceLocation = None
		self.baseLocation = None

	def isValid(self):
		isValidAlias = (self.getName() != None) and (self.getName() != "")
		isValidBaseType = (self.getBaseType() != None) and (self.getBaseType() != "")
		return isValidAlias and isValidBaseType

	def setBaseType(self, baseType):
		self.baseType = baseType

	def getBaseType(self):
		baseType = self.baseType

		if not(self.isAliasToPrimitiveType()) and (self.getNamespaceName() != ""):
			baseType = self.getNamespaceName() + "::" + baseType

		return baseType

	def getBaseClass(self):
		baseClass = None

		if not( self.isAliasToPrimitiveType() ) and not( self.isMacroRelatedTypeDef() ):
			baseClass = self.getCleanedType()

		return baseClass

	def getCleanedType(self):
		baseType = self.baseType.replace("const ", "")
		baseType = baseType.replace(" *","").replace(" []","")
		baseType = baseType.replace("*","").replace("[]","")
		baseType = baseType.strip()

		return baseType

	def isAliasToPrimitiveType(self):
		return common.cpp.isPrimitiveType(self.getCleanedType())

	def getQualifiedAliasName(self):
		if (self.getNamespaceName() != None) and (self.getNamespaceName() != ""):
			return self.getNamespaceName() + "::" + self.getName()
		else:
			return self.getName()

	def setSourceLocation(self, sourceLocation):
		self.sourceLocation = sourceLocation

	def getSourceLocation(self):
		return self.sourceLocation

	def setBaseSourceLocation(self, sourceLocation):
		self.baseLocation = sourceLocation

	def getBaseSourceLocation(self):
		return self.baseLocation

	def isMacroRelatedTypeDef(self):
		return self.getName() == self.getBaseType()

	def __eq__(self,other):
		isEqual = False

		if (other != None) & (type(other) == type(self)):
			isEqual = other.getName() == self.getName()
			isEqual = isEqual & (other.getBaseType() == self.getBaseType())
			isEqual = isEqual & (other.getNamespaceName() == self.getNamespaceName())
			isEqual = isEqual & (other.getSourceLocation() == self.getSourceLocation())
			isEqual = isEqual & (other.getBaseSourceLocation() == self.getBaseSourceLocation())

		return isEqual


