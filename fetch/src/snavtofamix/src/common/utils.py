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
# Author(s):Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>
#

from log4py import Logger

import copy

qualSeparator = "::"
ownerNameSeparator = "."

# system configuration

def getPkgSeparator():
	return qualSeparator

def getClassSeparator():
	return ownerNameSeparator

# package/namespace functions

def spliceLastPackageName(packageName, sep):
	splitIdx = packageName.rfind(sep)
	if splitIdx < 0:
		packageName = ""
	else:
		packageName = packageName[:splitIdx]
	return packageName

# e.g. for file paths separated by /
def spliceLastStringPart(fullString, sep):
	return spliceLastPackageName(fullString,sep)

# e.g. for file paths separated by /
def spliceAllButLastStringPart(fullString, sep):
	return spliceAllButLastPackageName(fullString, sep)

def spliceAllButLastPackageName(packageName, sep):
	splitIdx = packageName.rfind(sep)
	if splitIdx >= 0:
		packageName = packageName[splitIdx+len(sep):]
	return packageName

def hasSubpackage(packageName, sep):
	return sep in packageName

def splicePackage(qualClassName, sep):
	return spliceLastPackageName(qualClassName, sep)

def spliceClass(qualClassName,sep):
	return spliceAllButLastPackageName(qualClassName, sep)

def composeQualifiedName(prefix,name):
	if "" == prefix:
		return name
	else:
		return prefix+qualSeparator+name

def composeQualifiedMethodName(prefix,owner,name):
	if "" == prefix:
		return owner + ownerNameSeparator + name
	else:
		return prefix + qualSeparator + owner + ownerNameSeparator + name

# signature functions
def haveOnlyConstDifferenceInparameters(mi_params, md_params):
	md_param_list = md_params.replace("const ", "")
	mi_param_list = mi_params.replace("const ", "")

	return md_param_list == mi_param_list

def haveSameSignature(md_class, md_name, md_params, mi_class, mi_name, mi_params):
	if md_class == mi_class and md_name == mi_name and md_params == mi_params:
		return True
	else:
		return False

# returns null if method doesn't exist, returns defined method parameters parameters
# in case those are different from the
def returnDefinedMethodParameters(mi_class, mi_name, mi_params, mdDb):
	inf=open(mdDb, "r")
	for line in inf:
		cols = line.strip().split(" ")

		md_class = cols[0]
		md_name = cols[1]
		if not (mi_class == md_class and mi_name == md_name):
			continue

		md_params = returnParamList(cols[5:])
		if haveSameSignature(mi_class, mi_name, mi_params, md_class, md_name, md_params):
			return mi_params
		elif haveOnlyConstDifferenceInparameters(mi_params, md_params):
			return md_params
	# This method is not defined
	#print "did not find match for ", mi_class+"."+mi_name+"("+mi_params+")"
	return None

def returnDefinedFunctionParameters(fu_name, fu_params, fdDb):
	inf=open(fdDb, "r")
	for line in inf:
		cols = line.strip().split(" ")
		fd_name = cols[0]

		if not (fu_name == fd_name):
			continue

		fd_params = returnParamList(cols[4:])
		if haveSameSignature("", fu_name, fu_params, "", fd_name, fd_params):
			return fd_params
		elif haveOnlyConstDifferenceInparameters(fu_params, fd_params):
			#print "const diff", mi_params, md_params
			return fd_params
	# This method is not defined
	#print "did not find match for ", mi_class+"."+mi_name+"("+mi_params+")"
	return None


def returnParamList(mdDefLine):
	params=""
	mdDefLine = appendStringList(mdDefLine)
	i = 0
	# return type
	while mdDefLine[i] != "}":
		i = i + 1
	i = i + 3

	while mdDefLine[i] != "}":
		params = params + mdDefLine[i]
		i = i + 1
	return params

##
# Extracts source and destination parameter lists from the given list.
#
# Removes consts from the parameters.
##
def returnParamLists(paramList):
	destParamList=""
	sourceParamList=""

	i = 1
	while paramList[i] != "}":
		destParamList = destParamList + paramList[i]
		i = i + 1

	i = i + 2
	while paramList[i] != "}":
		sourceParamList = sourceParamList + paramList[i]
		i = i + 1

	sourceParamList.replace("const ", "") # before 11/1: replace("const","")
	destParamList.replace("const ", "") # before 11/1: replace("const","")
	return sourceParamList, destParamList

def retrieveSignature(signature):
	contents = ""
	retType = ""
	parameters = ""
	contents = appendStringList(signature)

	assert contents[0] == "{"
	cols = contents.split("}")
	retType = cols[0].replace("{", "").replace("virtual ", "")
	parameters = cols[1].replace("{", "(").replace(" (","(") + ")"

	parameterNames = ""
	if len(cols) > 2:
		parameterNames = cols[2].replace("{", "(").replace(" (","(") + ")"

	if (parameters == "(const int)") and (parameterNames == "(string)"):
		parameters = "(string)"

	return retType, parameters

def retrieveFormalParameters(signature):
	par_types=[]
	par_names=[]
	paramList = appendStringList(signature)

	assert paramList[0] == "{"
	i = 1
	# skip return type
	while paramList[i] != "}":
		i = i + 1
	i = i + 3
	while paramList[i] != "}":
		param=""
		while paramList[i] != "," and paramList[i] != "}":
			param = param + paramList[i]
			i = i + 1
		par_types.append(param)
		if paramList[i] == "}": break
		else: i = i + 1
	i = i + 3
	while paramList[i] != "}":
		param=""
		while paramList[i] != "," and paramList[i] != "}":
			param = param + paramList[i]
			i = i + 1
		par_names.append(param)
		if paramList[i] == "}": break
		else: i = i + 1
	return par_types, par_names

def cleanCol(col):
	return col.replace("{", "").replace("}","")

def appendStringList(signature):
	fullString = ""
	for x in signature:
		fullString += x + " "
	return fullString

# source location functions

def returnSourceLocation(sourceIndication):
	# Sample Entry:
	#	Constant 000023.007 spreadsheet/constant.h;23.15 0x0 {} {} {} {}
	sourceIndicationParts = sourceIndication.split(";")
	sourceFile=""
	start = ""

	if len(sourceIndicationParts) >= 2:
		sourceFile = sourceIndicationParts[0]

		sourceIndicationParts=sourceIndicationParts[1].split(".")

		if len(sourceIndicationParts) >= 1:
			start = sourceIndicationParts[0]
		else:
			log=Logger().get_instance()
			log.error("Invalid sourceIndication(start): \"",sourceIndication,"\"")
	else:
		log=Logger().get_instance()
		log.error("Invalid sourceIndication(sourceFile): \"",sourceIndication,"\"")

	end = start # sadly, not end value in SN Db
	return sourceFile, start, end

# also called interface
def isInterface(className, mdDbLoc):
	return False

def isAbstractMethod(className, methodName, miDb):
	inf=open(miDb, "r")
	for line in inf:
		line = line.strip()
		cols = line.split(" ")

		if cols[0] == className and cols[1] == methodName:
			inf.close()
			return False
	inf.close()

	return True

def isAbstractClass(className, mdDb, miDb):
	inf=open(mdDb, "r")
	for line in inf:
		line = line.strip()
		cols = line.split(" ")

		if cols[0] == className and "virtual" in cols[5] and isAbstractMethod(className, cols[1], miDb):
			inf.close()
			return True
	inf.close()
	return False

##
# Calculate the intersection between the two given lists.
##
def intersection(list1, list2):
	int_dict = {}
	list1_dict = {}
	for e in list1:
		list1_dict[e] = 1

	for e in list2:
		if list1_dict.has_key(e):
			int_dict[e] = 1

	return int_dict.keys()

##
# Calculate the set difference of the two given lists.
##
def setdifference(list1, list2):
	setdiff_dict = []
	for e in list1:
		if not(e in list2):
			setdiff_dict.append(e)

	return setdiff_dict

##
# Calculate the union between the two given lists.
##
def union(list1, list2):
	union_dict = {}

	for e in list1:
		union_dict[e] = 1

	for e in list2:
		union_dict[e] = 1

	return union_dict.keys()

def unionForNonHashableElements(list1, list2):
	theUnion = copy.deepcopy(list1)

	for element in list2:
		if not(element in theUnion):
			theUnion.append(element)

	return theUnion

##
# Ensures that .. and . path-elements are removed from the path of the file-name.
# Evidently, in case .. is the first path element, it is not removed.
##
def normalizeFileName(fileName):
	#adjustedPath=""
	adjustedPath = []
	
	path=fileName.replace("\\","/").split("/")
	mereFileName=path[len(path)-1]
	path=path[0:len(path)-1]
	currentPath=[]

	for pathElement in path:
		if ( pathElement == ".." ):
			# if we can still peal of one directory from the currentPath
			if ( not(not currentPath) and (currentPath[len(currentPath) - 1 ] != "..") ):
					currentPath=currentPath[0:len(currentPath)-1]
			else:
				currentPath.append("..")
		else:
			if ( pathElement != "." ):
				currentPath.append(pathElement)

	for pathElement in currentPath:
		#adjustedPath+=pathElement + "/"
		adjustedPath.append(pathElement)
		adjustedPath.append("/")

	#adjustedPath += mereFileName
	adjustedPath.append(mereFileName)

	#return adjustedPath
	return ''.join(adjustedPath)

def removeUpfrontZeroes(lineNr):
		i=0
		while "0" == lineNr[i]:
			i = i+1
		return lineNr[i:]
