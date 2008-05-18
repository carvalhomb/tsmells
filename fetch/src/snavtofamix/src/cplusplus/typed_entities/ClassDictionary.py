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

from log4py import Logger
from cplusplus.data_types.ClassEntity import ClassReferenceEntity
from QualifiedNameHelperFunctions 		import 	getParentNamespaceName,\
												getNonQualifiedName

def getTemplateParameters(classData):
	return classData[0]

def getNamespaceName(classData):
	return classData[1]

def createClassData(templateParameters, namespaceName):
	return [templateParameters, namespaceName]

##
# Encapsulation of a dictionary of classes.
#
# Key: 		className
# Value:	dictionary with
#		Key:	sourceFile
#		Value: [lineNr1xClassData1,..,lineNrNxClassDataN]
#	with lineNr x classData = a dictionary with key lineNr and value classData
# 	with classData = [ templateParameters, namespaceName ]
#		with
#			templateParameters = e.g., "X" the template parameter description in string-format
#			namespaceName = the fully qualified name of the parent namespace
#
# Additional data that needs to be remembered for a class can be appended to classData.
##
class ClassDictionary:
	##
	# Initialize a dictionary.
	##
	def __init__(self):
		self.dict = {}
		self.log = Logger().get_instance(self)

	##
	# Verify whether the dictionary contains a given class-name.
	##
	def hasKey(self, className):
		return (className in self.dict)

	##
	# Add a class contained in the given sourceFile at the given line-nr to the dictionary.
	#
	# @className - the name of the class
	# @sourceFile - the name of the file in which the class is declared
	# @lineNr - the line nr at which the class is declared in the source file
	# @classData - a list with data specific for the given class
	#
	# @returns True/False indicating whether the class was added
	##
	def add(self, className, sourceFile, lineNr, classData):
		isAdded = False

		nonQualifiedClassName = getNonQualifiedName(className)
		parentNamespaceName = getParentNamespaceName(className)

		# adjust the given namespace name in case the classname
		# was qualified
		if parentNamespaceName != "":
			givenNamespaceName = getNamespaceName(classData)

			if givenNamespaceName != "":
				parentNamespaceName = givenNamespaceName + "::" + parentNamespaceName

			classData[1] = parentNamespaceName

		if ( not(nonQualifiedClassName in self.dict) ):
			self.dict[nonQualifiedClassName] = {}

		if ( not(sourceFile in self.dict[nonQualifiedClassName]) ):
			self.dict[nonQualifiedClassName][sourceFile] = {}

		if ( not(lineNr in self.dict[nonQualifiedClassName][sourceFile]) ):
			self.dict[nonQualifiedClassName][sourceFile][lineNr]=classData
			isAdded = True

		return isAdded

	def getClassesByName(self, className):
		classList = []

		if className in self.dict:
			for sourceFile in self.dict[className]:
				for lineNr in self.dict[className][sourceFile]:
					classData = self.dict[className][sourceFile][lineNr]
					classRefEntity = ClassReferenceEntity(className, getNamespaceName(classData), sourceFile, lineNr, getTemplateParameters(classData))
					classList.append(classRefEntity)

		return classList

	##
	# Retrieve a list of [sourceFile, lineNr] elements for which it holds that in
	# sourceFile at lineNr a class with name className is declared.
	#
	# @param className - the class name for which to find source locations.
	#
	# @returns a list of elements [sourceFile, lineNr]
	##
	def getSourceLocations(self, className):
		sourceLocations=[]

		if ( className in self.dict ):
			for sourceFile in self.dict[className]:
				for lineNr in self.dict[className][sourceFile]:
					sourceLocations.append([sourceFile,lineNr])

		return sourceLocations

	def getClassesByNamespace(self, className, namespaceName):
		classList = []

		classNameWithoutTemplates = className
		templatePars = ""

		if "<" in className:
			classNameWithoutTemplates = className.split("<")[0]
			templatePars = "<".join(className.split("<")[1:])
			if ">" in templatePars:
				parts = templatePars.split(">")
				templatePars = ">".join(parts[0:len(parts)-1])

		if classNameWithoutTemplates in self.dict:
			for sourceFile in self.dict[classNameWithoutTemplates]:
				for lineNr in self.dict[classNameWithoutTemplates][sourceFile]:
					classData = self.dict[classNameWithoutTemplates][sourceFile][lineNr]

					if getNamespaceName(classData) == namespaceName:
						classRefEntity = ClassReferenceEntity(className, getNamespaceName(classData), sourceFile, lineNr, getTemplateParameters(classData))
						classList.append(classRefEntity)

		return classList

	##
	# Retrieve a list with data on the class with the given properties.
	# If no such class exists, [] is returned.
	#
	# Currently, classData =  [ templateParameters ]
	# with templateParameters = e.g., "X" the template parameter description in string-format
	##
	def getClassData(self, className, sourceFile, lineNr):
		classData=[]

		if  className in self.dict :
			if sourceFile in self.dict[className]:
				if lineNr in self.dict[className][sourceFile]:
					classData = self.dict[className][sourceFile][lineNr]

		return classData

	##
	# Retrieve a reference to the unique class satisfying the given properties.
	##
	def getClassReference(self, className, sourceFile, lineNr):
		classReference = None

		if  className in self.dict :
			if sourceFile in self.dict[className]:
				if lineNr in self.dict[className][sourceFile]:
					classData = self.dict[className][sourceFile][lineNr]
					classReference = ClassReferenceEntity(className, getNamespaceName(classData), sourceFile, lineNr, getTemplateParameters(classData))

		return classReference

	##
	# Verify whether the dictionary contains an enty for the given
	# classname in the given namespaceName at the given lineNr in the given
	# sourceFile.
	#
	# This is relevant in scenario's where you do not wish to add two
	# declarations for the same parameters at different line numbers.
	##
	def containsClass(self, className, sourceFile, namespaceName):
		exists = False

		# In case there exist multiple class definitions with identical method signatures
		# in the same file, we will never be able to retrieve the right target and just
		# drop the invocations.
		if ( (className in self.dict) and (sourceFile in self.dict[className]) ):
			for anyLineNrDeclaringClassInFile in self.dict[className][sourceFile]:
				classData = self.dict[className][sourceFile][anyLineNrDeclaringClassInFile]

				if getNamespaceName(classData) == namespaceName:
					exists = True

		return exists
	
	def createLocationBasedDictionary(self):
		"""Creates a class dictionary source location as key and class name as value"""
		locClDict = {}
		for className in self.dict:
			for sourceFile in self.dict[className]:
				for lineNr in self.dict[className][sourceFile]:
					loc = sourceFile+":"+lineNr
					if loc in locClDict:
						locClDict[loc].append(className)
					else:
						locClDict[loc] = [ className ]
		return locClDict
					
	##
	# Print the contents of the dictionary in the following formats.
	##
	def printContent(self):
		self.log.info( "ClassDictionary contents:")
		for className in self.dict:
			for sourceFile in self.dict[className]:
				for lineNr in self.dict[className][sourceFile]:
					self.log.info( "\t",className,"in file",sourceFile,"@lineNr",lineNr,self.dict[className][sourceFile][lineNr])
