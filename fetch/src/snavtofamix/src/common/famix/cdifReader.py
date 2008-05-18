#!/usr/bin/python
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
# Copyright 2006  University of Antwerp
# Author(s):Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

#
# famix CDIF Reader
from sets import Set
import cdifReaderCache
from log4py import Logger

log = Logger().get_instance()

inf = "" # cdif handle
classCache = None
attributeCache = None
globalVarCache = None
methodCache = None
functionCache = None
inhCache = {}
typeDefCache = {}
localVarCache = {}

def set_cdifhandle(cdifhandle):
	global inf
	inf = cdifhandle

################# Cache functionality ##########################################

#def buildClassCache(cdifFile):
#	global classCache
#	global inf
#	classCache = common.famix.cdifReaderCache.Cache()
#	inf.seek(0)
#	classEntity = "(Class FM"
#
#	line = inf.readline()
#	while line != "":
#		if classEntity in line:
#			nextLine = inf.readline()
#			nextLine = inf.readline()
#			if "uniqueName" in nextLine:
#				classUniqName = nextLine.split("\"")[1]
#				classCache.cache_addEntry(classUniqName)
#		line = inf.readline()
	#attributeCache.debug()

# TODO remove the cdifFile parameter everywhere
# TODO maybe replace global handle with parameter ...

#def buildInheritanceCache(cdifFile):
#	global inhCache
#	global inf
#	inf.seek(0)
#	inhEntity = "(InheritanceDefinition FM"
#
#	line = inf.readline()
#	while line != "":
#		if inhEntity in line:
#			nextLine = inf.readline()
#			nextNextLine = inf.readline()
#			if "subclass" in nextLine and "superclass" in nextNextLine:
#				subclass = nextLine.split("\"")[1]
#				superclass = nextNextLine.split("\"")[1]
#				assert subclass != None
#				assert superclass != None
#				assert superclass != ""
#				assert subclass != ""
#				assert subclass != superclass
#
#				if subclass in inhCache:
#					assert inhCache[subclass] != None
#					inhCache[subclass].append(superclass)
#				else:
#					assert superclass != None
#					inhCache[subclass] = [ superclass ]
#					assert inhCache[subclass] != None
#			else:
#				assert False;
#		line = inf.readline()

#def buildTypeDefCache(cdifFile):
#	global typeDefCache
#	global inf
#	inf.seek(0)
#	typeDefEntity = "(TypeDef FM"
#
#	line = inf.readline()
#	while line != "":
#		if typeDefEntity in line:
#			nextLine = inf.readline()
#			nextNextLine = inf.readline()
#
#			if "declaredReturnClass" in nextLine and "baseReturnType" in nextNextLine:
#				declared = nextLine.split("\"")[1]
#				base = nextNextLine.split("\"")[1]
#
#				if base in inhCache:
#					typeDefCache[base].append(declared)
#				else:
#					typeDefCache[base] = [ declared ]
#			else:
#				assert False;
#		line = inf.readline()

def buildAttributeCache(cdifFile):
	global attributeCache
	global inf
	attributeCache = cdifReaderCache.Cache()
	
	inf.seek(0)
	attrEntity = "(Attribute FM"

	line = inf.readline()
	while line != "":
		if attrEntity in line:
			nextLine = inf.readline()
			if "uniqueName" in nextLine:
				attrUniqName = nextLine.split("\"")[1]
				attributeCache.cache_addEntry(attrUniqName)
		line = inf.readline()
	#attributeCache.debug()

def buildGlobalVarCache(cdifFile):
	global globalVarCache
	global inf
	globalVarCache = cdifReaderCache.Cache()
	#globalVarCache.debug()
	inf.seek(0)
	globVarEntity = "(GlobalVariable FM"

	line = inf.readline()
	while line != "":
		if globVarEntity in line:
			nextLine = inf.readline()
			if "uniqueName" in nextLine:
				globVarUniqName = nextLine.split("\"")[1]
				globalVarCache.cache_addEntry(globVarUniqName)
		line = inf.readline()

#def buildMethodCache(cdifFile):
#	global methodCache
#	global inf
#	methodCache = cdifReaderCache.Cache()
#	inf.seek(0)
#	methodEntity = "(Method FM"
#
#	line = inf.readline()
#	while line != "":
#		if methodEntity in line:
#			nextLine = inf.readline()
#			nextLine = inf.readline()
#			nextLine = inf.readline()
#			if "uniqueName" in nextLine:
#				methodUniqName = nextLine.split("\"")[1]
#				methodCache.cache_addEntry(methodUniqName)
#		line = inf.readline()

#def buildFunctionCache(cdifFile):
#	global functionCache
#	global inf
#	functionCache = cdifReaderCache.Cache()
#	inf.seek(0)
#	functionEntity = "(Function FM"
#
#	line = inf.readline()
#	while line != "":
#		if functionEntity in line:
#			nextLine = inf.readline()
#			nextLine = inf.readline()
#			if "uniqueName" in nextLine:
#				functionUniqName = nextLine.split("\"")[1]
#				functionCache.cache_addEntry(functionUniqName)
#		line = inf.readline()

def addClassToCache(classUniqName):
	if not classCache.cache_containsEntry(classUniqName):
		classCache.cache_addEntry(classUniqName)

def addMethodToCache(methodUniqName):
	global methodCache
	if methodCache is None:
		methodCache = cdifReaderCache.Cache()
	if not methodCache.cache_containsEntry(methodUniqName):
		methodCache.cache_addEntry(methodUniqName)

def addFunctionToCache(functionUniqName):
	if not functionCache.cache_containsEntry(functionUniqueName):
		functionCache.cache_addEntry(functionUniqName)

def flushClassCache():
	global classCache
	classCache = None

def flushAttributeCache():
	global attributeCache
	attributeCache = None

def flushGlobalVarCache():
	global globalVarCache
	globalVarCache = None

def flushMethodCache():
	global methodCache
	methodCache = None

def flushFunctionCache():
	global functionCache
	functionCache = None

################################################################################

def checkForClass(className, cdifFile):
	#input_file=open(cdifFile, 'r')
	global inf 
	inf.seek(0)
	input_file = inf
	for line in input_file:
		if className in line:
			return True
	return False

def existsClass(classUniqName):
	global classCache
	return classCache.cache_containsEntry(classUniqName)

def getSuperClassesOf(entry, superclasses):
	global inhCache
	if not entry in inhCache: return superclasses
	entrySC = inhCache[entry]

	if entrySC is None: return superclasses

	for i in entrySC:
		superclasses.append(i)
		superclasses = getSuperClassesOf(i, superclasses)
	return superclasses

def getAlternativesFor(entry, alternatives):
	global typeDefCache
	if not entry in typeDefCache: return alternatives
	entryAlt = typeDefCache[entry]
	# don't know why it can get None
	if entryAlt is None: return alternatives

	for i in alternatives:
		alternatives.add(i)
		for j in getSuperClassesOf(i, alternatives):
			alternatives.add(j)
	return alternatives


def getMethodsOfClass(classUniqName):
	global methodCache
	return methodCache.entriesStartingWith(classUniqName)

def isMethod(methodUniqName):
	global methodCache
	return methodCache.cache_containsEntry(methodUniqName)

def getMethodParams(methodUniqName):
	global methodCache
	if methodCache.cache_containsEntry(methodUniqName):
		# special case for cast operator, see #1519926
		methodUniqName.replace("operator()", "")
		i = methodUniqName.find("(")
		j = methodUniqName.rfind(")")
		return methodUniqName[i+1:j]
	else:
		log.error( "Method ",methodUniqName, " doesn't exist.")
		return None

# checks whether given function (as defined by its unique name) is already
# known in this Famix model
def existsFunction(functionUniqName):
	global functionCache
	return functionCache.cache_containsEntry(functionUniqName)

def isAttribute(className, attrCand):
	global attributeCache
	attrUniqName = className+"."+attrCand

	return attributeCache.cache_containsEntry(attrUniqName)

def getAttributesOf(className):
	global attributeCache
	# provide string "classname."
	return attributeCache.entriesStartingWith(className+".")

def isGlobalVar(uniqName):
	global globalVarCache

	if globalVarCache == None:
		return False
	else:
		return globalVarCache.cache_containsEntry(uniqName)

# deprecated, use cache instead for better performance
#def containsFunction(cdifFile, funcName, funcParams):
#	global inf
#	funcEntity = "Function FM"
#	funcSign = funcName + "("+funcParams+")"
#	
#	inf.seek(0)
#	line = inf.readline()
#	while line != "":
#		if funcEntity in line:
#			anotherLine = inf.readline()
#			anotherLine = inf.readline()
#			if funcSign in anotherLine:
#				return True
#		line = inf.readline()
#	return False

#def containsMethodInvocation(cdifFile,
#		sourceClass, sourceMethod, sourceParams,
#		destClass, destMethod, destParams):
#	global inf
#	invokedBy = "invokedBy \""+sourceClass+"."+sourceMethod+"("+sourceParams+")\""
#	invokes = "invokes \""+destMethod+"("+destParams+")\""
#	base = "base \""+destClass+"\""
#	inf.seek(0)
#	line = inf.readline()
#	while line != "":
#		if invokedBy in line:
#			nextLine = inf.readline()
#			anotherLine = inf.readline()
#			if invokes in nextLine and base in anotherLine:
#				return True
#		line = inf.readline()
#	return False

def containsAccess(cdifFile, accessedIn, accesses):
	global inf
	inf.seek(0)
	access = "(Access FM"
	accIn = "(accessedIn \""+accessedIn+"\")"
	acc = "(accesses \""+accesses+"\")"
	line = inf.readline()
	while line != "":
		if access in line:
			nextLine = inf.readline()
			anotherLine = inf.readline()
			if accIn in nextLine:
				if acc in anotherLine:
					return True
		line = inf.readline()
	return False

def getClassOfAttribute(cdifFile, attrUniqName):
	global inf
	inf.seek(0)
	attrEnt = "(Attribute FM"
	thisattr = "(uniqueName \""+attrUniqName+"\")"
	line = inf.readline()
	while line != "":
		if thisattr in line:
			# clumsy way to proceed four lines
			nextLine = inf.readline()
			nextLine = inf.readline()
			nextLine = inf.readline()
			nextLine = inf.readline()
			return nextLine.split("\"")[1]
		line = inf.readline()
	return None

