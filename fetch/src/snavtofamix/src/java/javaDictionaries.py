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
# Copyright 2007  University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>
#

from log4py import Logger

import common

##
# Encapsulation of a dictionary of Packages.
#
# Key: 		sourceFile
# Value:	Package name
#
class PackageDictionary:
	##
	# Initialize a dictionary.
	##
	def __init__(self):
		self.dict={}
		self.log = Logger().get_instance(self)

	##
	# Verify whether the dictionary contains a given Package-name.
	##
	def hasKey(self, nsName):
		return (nsName in self.dict)

	##
	# Add a Package contained in the given sourceFile at the given line-nr
	# to the dictionary.
	#
	# @nsName - the name of the Package
	# @sourceFile - the name of the file in which the Package is declared
	#
	# @returns True/False indicating whether the Package was added
	##
	def add(self,sourceFile, nsName):
		isAdded = False

		if ( not(sourceFile in self.dict) ):
			self.dict[sourceFile] = nsName
			isAdded = True
		else:
			self.log.warn("Ignoring additional package declaration "+nsName+"for file ",\
							sourceFile+" already packaged in "+self.dict[sourceFile])

		return isAdded

	##
	# Retrieve a list of [sourceFile, lineNr] elements for which it holds
	# that in sourceFile at lineNr a class with name className is declared.
	#
	# @param className - the class name for which to find source locations.
	#
	# @returns a list of elements [sourceFile, lineNr]
	##
	def getPackageForFile(self, sourceFile):
		noNS = ""
		if ( sourceFile in self.dict ):
			return self.dict[sourceFile]
		else:
			return noNS

class ImportDictionary:
	##
	# Initialize a dictionary.
	##
	def __init__(self):
		self.clsImportDict={}
		self.pkgImportDict={}
		self.log = Logger().get_instance(self)

	##
	# Add a class import contained in the given sourceFile to the dictionary.
	# @precondition: class is known
	# @className - the name of the import
	# @sourceFile - the name of the file in which the Class is declared
	#
	# @returns True/False indicating whether the Package was added
	##
	def addClass(self, sourceFile, className):
		isAdded = False

		if ( not(sourceFile in self.clsImportDict) ):
			self.clsImportDict[sourceFile] = []
			self.clsImportDict[sourceFile].append(className)
			isAdded = True
		elif ( not(className in self.clsImportDict[sourceFile]) ):
			self.clsImportDict[sourceFile].append(className)
			isAdded = True
		else:
			self.log.warn("Duplicate import of ",className, " in ", sourceFile)
			isAdded = False
		return isAdded

	def addPackage(self, sourceFile, pkgName):
		isAdded = False

		if ( not(sourceFile in self.pkgImportDict) ):
			self.pkgImportDict[sourceFile] = []
			self.pkgImportDict[sourceFile].append(pkgName)
			isAdded = True
		elif ( not(pkgName in self.pkgImportDict[sourceFile]) ):
			self.pkgImportDict[sourceFile].append(pkgName)
			isAdded = True
		else:
			self.log.warn("Duplicate import of ",pkgName, " in ", sourceFile)
			isAdded = False
		return isAdded

	##
	# Verify whether the dictionary contains a given sourceFile.
	##
	def hasKey(self, sourceFile):
		return (sourceFile in self.clsImportDict or sourceFile in self.pkgImportDict)

	def hasImportFor(self, srcFile, clsName):
		# via class import
		if 	srcFile in self.clsImportDict and \
			clsName in self.clsImportDict[srcFile]:
			return True

		# or via package import
		pkgName = common.utils.splicePackage(\
							clsName, common.utils.getPkgSeparator())
		if 	srcFile in self.pkgImportDict and \
			pkgName in self.pkgImportDict[srcFile]:
			return True

		return False

methodsNamedCache = {} # methodName x { signature x { clName x pkgName }}

class MethodDictionary:

	def __init__(self):
		self.dict={} # methodSignature x className x pkgName
		self.log = Logger().get_instance(self)

	def dictHasMethodInPackage(self,pkgName, className, methodName):
		""" checks presence of method based upon package, class and signature"""

		if self.hasKey(methodName) and \
			className in self.dict[methodName] and\
			pkgName in self.dict[methodName][className]:
			return True
		else:
			return False

	def getSignaturesWithMethodName(self, methodName):
		signatures = []
		for i in self.dict:
			if methodName in i: signatures.append(i)
		return signatures

	def dictHasMethod(self, className, methodName):
		""" checks presence of method based upon class owner and signature """
		if self.hasKey(methodName) and \
			className in self.dict[methodName]:
			return True
		else:
			return False

	def getMethodsNamed(self, methodName):
		""" returns all methods with name (no signature) """
		try:
			return methodsNamedCache[methodName]
		except KeyError:
			matchingNamesDict = {}
			for i in self.dict:
				name = i.split("(")[0] # split method name from signature
				if methodName == name: matchingNamesDict[i] = self.dict[i]
			methodsNamedCache[methodName] = matchingNamesDict
			return matchingNamesDict

	def getMethodInClass(self, className, methodName):
		""" returns entities matching given class owner and method name """
		if self.dictHasMethod(className, methodName):
			return self.dict[methodName][className]
		else:
			return []

	def hasKey(self, methodName):
		""" Verifies whether the dictionary contains a given methodName """
		return methodName in self.dict

	def __addMethodName(self, methodName):
		isAdded = False

		if not methodName in self.dict:
			self.dict[methodName] = {}
			isAdded = True
		return isAdded

	def __addClassName(self, className, methodName):
		self.__addMethodName(methodName)
		if not className in self.dict[methodName]:
			self.dict[methodName][className] = []
			return True
		else:
			return False

	##
	# methodName is defined as name+parameters
	def add(self, pkgName, className, methodName):
		isAdded = False

		self.__addClassName(className, methodName)
		if self.dictHasMethodInPackage(pkgName, className, methodName):
			self.log.warn(	"Duplicate method ",pkgName+"::"+className+"::"+methodName)
		else:
			self.dict[methodName][className].append(pkgName)
			isAdded = True

		return isAdded

class MethodSourceDictionary:

	def __init__(self):
		self.dict={}
		self.log = Logger().get_instance(self)

	def hasKey(self, qMtdName):
		""" Verifies whether the dictionary contains a given qualified methodName """
		return qMtdName in self.dict

	def getSourceLocFor(self, qMtdName):
		if not qMtdName in self.dict:
			return []
		else:
			return self.dict[qMtdName]

	##
	# methodName is defined as package+class+methodName
	# qSrcFile is defined as sourcefile+":"+lineNr
	def add(self, qMtdName, qSrcFile):
		isAdded = False

		if qMtdName in self.dict:
			self.dict[qMtdName].append(qSrcFile)
		else:
			self.dict[qMtdName] = [ qSrcFile ]
			isAdded = True

		return isAdded

class FormalParameterDictionary:

	def __init__(self):
		pass

class AttributeDictionary:

	def __init__(self):
		self.dict={}
		self.log = Logger().get_instance(self)

	##
	# Verify whether the dictionary contains a given attributeName
	##
	def hasKey(self, attributeName):
		return attributeName in self.dict

	def dictHasAttributeInClass(self,attributeName, className):
		if self.hasKey(attributeName) and className in self.dict[attributeName]:
			return True
		else:
			return False

	##
	# methodName is defined as name+parameters
	def add(self, attributeName, className):
		isAdded = False

		if attributeName in self.dict:
			if self.dictHasAttributeInClass(attributeName,className):
				self.log.warn("Duplicate attribute ",\
									className+"."+attributeName)
				isAdded = False
			else:
				self.dict[attributeName].append(className)
				isAdded = True
		else:
			self.dict[attributeName] = []
			self.dict[attributeName].append(className)
			isAdded = True
		return isAdded
	
##
# e.g. testmessenger::UtilsTest;u;000036.000;
#/home/bvromp/Desktop/work/fetch/samples/anastacia/testmessenger/UtilsTest.java;
#36.00;0x4;{Utils};{};{};{}
##
def buildAttrTypeDict(attrTypeDbLoc):
	"""builds a dictionary with attribute name as key and its type as value"""
	expNrOfCols = 7
	attrTypeDict = {}

	for line in open(attrTypeDbLoc, "r"):
		cols = line.split(";")
		if len(cols) < expNrOfCols: 
			 log.warn("Invalid line in attribute type db: "+line.strip())
			 continue
		name = cols[0]+"."+cols[1]
		attrType = ""
		
		if " " in cols[5]:
			attrType = common.utils.cleanCol(cols[6]).split(" ")[1]
		else:
			attrType = common.utils.cleanCol(cols[6])
		attrTypeDict[name] = attrType
	return attrTypeDict

log = Logger().get_instance()
