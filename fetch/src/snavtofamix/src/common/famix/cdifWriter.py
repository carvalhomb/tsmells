#!/usr/bin/python
# snavtofamix parses SourceNavigator's database to generate CDIF exchange files
# of the FAMIX source code meta model.
# Copyright (C) 2006 Bart Van Rompaey
#
# This file is part of snavtofamix.
#
# snavtofamix is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# snav2famix is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Anastacia; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
# Copyright 2007 University of Antwerp
# Author(s): Bart Van Rompaey - bart.vanrompaey2@ua.ac.be
#

# FAMIX CDIF Writer

from log4py import Logger

import common.cpp

log = Logger().get_instance()
# global variables
IDCounter = 0
MOOSECOMPLIANCE = False
CppDict = None

# output file
output_path= ""
unique_method_names=[]
of=""

### HELPER FUNCTIONS
def initializeIDCounter():
	global IDCounter
	IDCounter=0

#deprecated
def set_outputfile(filename):
	global output_path
	output_path = filename

def set_outputhandle(handle):
	global of
	of = handle

def get_outputhandle():
	return of

#deprecated
def get_outputfile():
	return output_path

def writeMooseCompliantCdif():
	global MOOSECOMPLIANCE
	MOOSECOMPLIANCE=True

def setCppDict(cppDict):
	global CppDict
	CppDict = cppDict

##
# Note: this is a mere syntactical operation.
# Clients should also resolve the class to a true class entry in the
# ClassDictionary.
##
def declaredTypeToClass(declaredType):
	declaredType = declaredType.replace("*", "")
	declaredType = declaredType.replace("&", "")
	declaredType = declaredType.replace("const", "")
	declaredType = declaredType.replace("mutable", "")
	declaredType = declaredType.replace("[]", "")
	declaredType = declaredType.replace(" ", "")

	if declaredType in common.cpp.C_PRIMITIVES: declaredType = ""
	return declaredType

def spliceLastPackageName(packageName):
	splitIdx = packageName.rfind("::")
	if splitIdx < 0:
		packageName = ""
	else:
		packageName = packageName[:splitIdx]
	return packageName

def spliceAllButLastPackageName(packageName):
	splitIdx = packageName.rfind("::")
	if splitIdx >= 0:
		packageName = packageName[splitIdx+2:]
	return packageName

def hasSubpackage(packageName):
	return "::" in packageName

### CDIF OUTPUT FUNCTIONS
def writeRecordPrefix(of, typeName):
	global IDCounter
	of.write("\n(")
	of.write(typeName)
	of.write(" FM")
	of.write(str(IDCounter))
	IDCounter=IDCounter+1

def writeStringField(of, fieldName,fieldValue):
	of.write("\n\t(")
	of.write(fieldName)
	of.write(" \"")
	of.write(fieldValue)
	of.write("\")")

def writeBooleanField (of, fieldName, fieldValue):
	of.write("\n\t(")
	of.write(fieldName)
	of.write(" ")
	if (fieldValue): of.write("-TRUE-")
  	else: of.write("-FALSE-")

	of.write(")")

def writeIntField (of, fieldName, fieldValue):
	of.write("\n\t(")
	of.write(fieldName)
	of.write(" ")
	of.write(str(fieldValue))
	of.write(")")

def writeSourceAnchor (of, sourcefile, start, end):
	of.write("\n\t(")
	of.write("sourceAnchor #[file \"")
	of.write(sourcefile)
	of.write("\" start ")
	of.write(start)
	of.write(" end ")
	of.write(end)
	of.write("|]#)")

def writeSourceAnchors(of, sourceLocations):
	of.write("\n\t(")
	of.write("sourceAnchor ")

	nrOfSourceLocs = len(sourceLocations)
	index = 0

	while index < nrOfSourceLocs:
		sourceLoc = sourceLocations[index]

		of.write("#[file \"")
		of.write(sourceLoc.sourceFile)
		of.write("\" start ")
		of.write(sourceLoc.start)
		of.write(" end ")
		of.write(sourceLoc.end)
		of.write("|]#")

		if index < nrOfSourceLocs-1:
			of.write("\n\t")

		index += 1

	of.write(")")


def generateAccessControlInfo(of, modifiers):
	writeStringField(of, "accessControlQualifier", modifiers)

def writeRecordPostfix(of):
	of.write("\n)\n")

def writeSourceLocationEntries(of, sourceFile, start, end):
	if "" == sourceFile: return

	writeSourceAnchor(of, sourceFile, start, end)
	ccBlock = CppDict.belongsTocppBlock(sourceFile, start)
	if not ccBlock is "":
		writeStringField(of, "condCompiled", ccBlock)

# FAMIX entities
def generatePackageInfo(pkgRef):
	
	writeRecordPrefix(of, "Package")
	writeStringField(of, "uniqueName", pkgRef.getName())

	if hasSubpackage(pkgRef.getName()):
	    writeStringField(of, "name", spliceAllButLastPackageName(pkgRef.getName()))
	    writeStringField(of, "belongsTo", spliceLastPackageName(pkgRef.getName()))
	else:
		writeStringField(of, "name", pkgRef.getName())

	sourceLocations = pkgRef.getSourceLocations()
	if not(not sourceLocations):
		writeSourceAnchors(of, pkgRef.getSourceLocations())

	# if not ppBlock is "":
	#	writeStringField(of, "condCompiled", ppBlock)

	writeRecordPostfix(of)

def generateClassInfo (className, cur_namespace, isAbstract,
						sourceFile, start, end):
	
	writeRecordPrefix(of, "Class")
	writeStringField(of, "name", className)

	if cur_namespace is "":
		writeStringField(of, "uniqueName", className)
	else:
		writeStringField(of, "uniqueName", cur_namespace+"::"+className)
		writeStringField(of, "belongsTo", cur_namespace)
	# TODO: check abstract, interface
	writeBooleanField(of, "isAbstract", isAbstract)
	#writeBooleanField(of, "isInterface", False)
	writeSourceLocationEntries(of, sourceFile, start, end)
	writeRecordPostfix(of)

def generateInheritanceInfo (subClassName, superClassName, cur_namespace,
				accessControlQualifier, sourceFile, start, end, dst_sourcefile, dst_start):
	
	writeRecordPrefix(of, "InheritanceDefinition")

	# TODO: the namespace for the superclass is not correct when there are
	# using directives
	if cur_namespace is "":
		writeStringField(of, "subclass", subClassName)
		writeStringField(of, "superclass", superClassName)
	else:
		writeStringField(of, "subclass", cur_namespace+"::"+subClassName)
		#writeStringField(of, "superclass", cur_namespace+"::"+superClassName)
		writeStringField(of, "superclass", superClassName)
	if accessControlQualifier != "":
		writeStringField(of, "accessControlQualifier", accessControlQualifier)

	writeSourceLocationEntries(of, sourceFile, start, end)
	writeSuperclassSourceAnchor(of, dst_sourcefile, dst_start)
	writeRecordPostfix(of)

##
# Generates an Attribute CDIF entity.
##
def generateFieldInfo(field, \
					parentReference,\
					typeReference,\
					modifiers, hasClassScope,\
					sourceFile, start, end):
	
	writeRecordPrefix(of, "Attribute")
	writeStringField(of, "uniqueName", 	parentReference.getReferencedName() \
										+ '.' + field)
	writeStringField(of, "belongsTo", parentReference.getReferencedName())
	writeStringField(of, "name", field)

    # optional attributes
	writeStringField(of, "declaredType", typeReference.getReferencedName())

	declaredClass = ""
	if typeReference.getSourceFile() != "":
		declaredClass = typeReference.getResolvedName()

	writeStringField(of, "declaredClass", declaredClass)

	if modifiers is not "": generateAccessControlInfo(of, modifiers.strip())
	writeBooleanField(of, "hasClassScope", hasClassScope)
	if not sourceFile is "":
		writeSourceAnchor(of, sourceFile, start, end)

	if ( parentReference.getSourceFile() != "" ):
		writeParentSourceAnchor(of, parentReference.getSourceFile(),\
							parentReference.getLineNr())

	if (typeReference.getSourceFile() != ""):
		writeTypeSourceAnchor(of, typeReference.getSourceFile(),\
									typeReference.getLineNr())
	writeRecordPostfix(of)

def generateGlobalVarInfo(globalVariable):
	
	ref = globalVariable.getReference()
	typeReference = globalVariable.getTypeReference()
	writeRecordPrefix(of, "GlobalVariable")
	writeStringField(of, "uniqueName", ref.getUniqueName())
	writeStringField(of, "name", ref.getName())
	writeStringField(of, "declaredType", typeReference.getReferencedName())

	declaredClass = ""

	if typeReference.getSourceFile() != "":
		declaredClass = typeReference.getResolvedName()

	writeStringField(of, "declaredClass", declaredClass)

	if not ref.getSourceFile() is "":
		writeSourceAnchor(of, ref.getSourceFile(), ref.getLineNr(), ref.getLineNr())

	if typeReference.getSourceFile() != "":
		writeTypeSourceAnchor(of, typeReference.getSourceFile(), \
							typeReference.getLineNr())

	writeRecordPostfix(of)

def generateInvocationEntityReference(ref,scope):
	if ref.getOwnerName() != "":
		generateMultiLocMethod(ref,scope)
	else:
		generateMultiLocFunction(ref,scope)

def generateMultiLocMethod(methodRef,scope):
	parentRef = methodRef.getParentReference()
	typeRef = methodRef.getTypeReference()
	multiLoc = methodRef.getLocation()
	defIndex=1
	for mtuple in multiLoc.getDeclarationDefinitionTuples(scope):
		declLoc = mtuple[0]
		generateMethodDeclarationInfo(declLoc, methodRef, parentRef, typeRef)

		if len(mtuple) == 1:
			continue

		# there are definitions!
		defLocs=mtuple[1:len(mtuple)]
		for defLoc in defLocs:
			generateMethodDefinitionInfo(defLoc, declLoc, methodRef, parentRef, typeRef, defIndex)
			defIndex+=1


def generateMethodDefinitionInfo(defLoc, declLoc, methodRef, parentRef, typeRef, defIndex):
	""" generates a method definition entity in FAMIX CDIF style """

	writeRecordPrefix(of, "MethodDefinition")
	writeStringField(of, "name", methodRef.getName())
	declarationUniqueName = parentRef.getReferencedName() + "." + methodRef.getName() \
		+ methodRef.getFormalParameters()
	writeStringField(of, "declaredBy", declarationUniqueName)
	uniqueName = declarationUniqueName + "_" + `defIndex`
	writeStringField(of, "uniqueName", uniqueName)

	# write source anchors
	writeSourceLocationEntries(of, defLoc.getSourceFile(), defLoc.getStart(), defLoc.getEnd())
	writeSomeSourceAnchor(of, "declSourceAnchor", declLoc.getSourceFile(), declLoc.getStart())

	writeRecordPostfix(of)

def generateMethodDeclarationInfo(declLoc, methodRef, parentRef, typeRef):
	""" generates a method (declaration) entity in FAMIX CDIF 2.0 style """

	writeRecordPrefix(of, "Method")
	writeStringField(of, "name", methodRef.getName())
	writeStringField(of, "belongsTo", parentRef.getReferencedName())
	uniqueName = parentRef.getReferencedName() + "." + methodRef.getName() + methodRef.getFormalParameters()
	writeStringField(of, "uniqueName", uniqueName)

	# write attributes
	if methodRef.isAbstract():
		writeBooleanField(of, "isAbstract", True)
	else:
		writeBooleanField(of, "isAbstract", False)

  	generateAccessControlInfo(of, methodRef.getModifiers())

	writeStringField(of, "signature", methodRef.getName()+methodRef.getFormalParameters())

	# todo: snav reports constructors and destricutors as having int as return type
	if methodRef.isConstructor():
		writeBooleanField(of, "isConstructor", True)
	else:
		writeStringField(of, "declaredReturnType", typeRef.getReferencedName())
		declaredReturnClass = ""

		if typeRef.getSourceFile() != "":
			declaredReturnClass = typeRef.getResolvedName()

		writeStringField(of, "declaredReturnClass", declaredReturnClass)

		#hasClassScope = False # we ignore this for now
		#
		#if hasClassScope:
		#	writeBooleanField(of, "hasClassScope", True)
		if methodRef.getClassScope():
			writeBooleanField(of, "hasClassScope", True)
		else:
			writeBooleanField(of, "hasClassScope", False)

	# write source anchors
	writeSourceLocationEntries(of, declLoc.getSourceFile(), declLoc.getStart(), declLoc.getEnd())

	if parentRef.getSourceFile() != "":
		writeParentSourceAnchor(of, parentRef.getSourceFile(), \
							parentRef.getLineNr())

	if not(methodRef.isConstructor()) and (typeRef.getSourceFile() != ""):
		writeTypeSourceAnchor(of, typeRef.getSourceFile(), \
									typeRef.getLineNr())

	writeRecordPostfix(of)
	#of.close()



def generateFunctionDeclarationInfo(of, declLoc, functionRef, typeRef):
	writeRecordPrefix(of, "Function")
	writeStringField(of, "name", functionRef.getName())
	writeStringField(of, "uniqueName", functionRef.getUniqueName())
	writeStringField(of, "signature", functionRef.getSignature())

	writeStringField(of, "declaredReturnType", typeRef.getReferencedName())

	declaredReturnClass = ""
	if typeRef.getSourceFile() != "":
			declaredReturnClass = typeRef.getResolvedName()

	writeStringField(of, "declaredReturnClass", declaredReturnClass)
	writeSourceLocationEntries(of, declLoc.getSourceFile(), declLoc.getStart(), declLoc.getEnd())

	if (typeRef.getSourceFile() != ""):
		writeTypeSourceAnchor(of, typeRef.getSourceFile(), typeRef.getLineNr())

	writeRecordPostfix(of)

def generateFunctionDefinitionInfo(of, defLoc, declLoc, functionRef, typeRef, defIndex):
	writeRecordPrefix(of, "FunctionDefinition")
	writeStringField(of, "name", functionRef.getName())
	writeStringField(of, "declaredBy", functionRef.getUniqueName())
	uniqueName = functionRef.getUniqueName() + "_" + `defIndex`
	writeStringField(of, "uniqueName", uniqueName)

	# write source anchors
	writeSourceLocationEntries(of, defLoc.getSourceFile(), defLoc.getStart(), defLoc.getEnd())

	# write source anchors
	writeSomeSourceAnchor(of, "declSourceAnchor", declLoc.getSourceFile(), declLoc.getStart())

	writeRecordPostfix(of)

def generateMultiLocFunction(functionRef,scope):
	typeRef = functionRef.getTypeReference()

	multiLoc = functionRef.getLocation()
	for tuple in multiLoc.getDeclarationDefinitionTuples(scope):
		declLoc = tuple[0]
		generateFunctionDeclarationInfo(of, declLoc, functionRef, typeRef)

		if len(tuple) == 1:
			continue

		# there are definitions!
		defLocs=tuple[1:len(tuple)]
		defIndex=1
		for defLoc in defLocs:
			generateFunctionDefinitionInfo(of, defLoc, declLoc, functionRef, typeRef, defIndex)
			defIndex+=1

def writeSuperclassSourceAnchor(of, dst_sourcefile, dst_start):
	writeSomeSourceAnchor(of, "superclassSourceAnchor", dst_sourcefile, dst_start)

def writeParentSourceAnchor(of, dst_sourcefile, dst_start):
	writeSomeSourceAnchor(of, "parentSourceAnchor", dst_sourcefile, dst_start)

def writeTypeSourceAnchor(of, dst_sourcefile, dst_start):
	writeSomeSourceAnchor(of, "typeSourceAnchor", dst_sourcefile, dst_start)

def writeDestinationSourceAnchor(of, dst_sourcefile, dst_start):
	writeSomeSourceAnchor(of, "destinationSourceAnchor", dst_sourcefile, dst_start)

def writeSourceSourceAnchor(of, src_sourcefile, src_start):
	writeSomeSourceAnchor(of, "sourceSourceAnchor", src_sourcefile, src_start)

def writeSomeSourceAnchor(of, anchorName, dst_sourcefile, dst_start):
	# write destination source anchor
	of.write("\n\t(")
	of.write(anchorName + " #[file \"")
	of.write(dst_sourcefile)
	of.write("\" start ")
	of.write(dst_start)
	of.write(" end ")
	of.write(dst_start)
	of.write("|]#)")

def generateJavaInvocationInfo(	source_type, source_method, source_sig,\
							dest_type, dest_method, dest_sig,\
							sourcefile, begin, end,\
							src_sourcefile, src_start,\
							dst_sourcefile, dst_start):
	
	writeRecordPrefix(of, "Invocation")
	if source_type != "":
		writeStringField(of, "invokedBy", source_type+"."+source_method+"("+source_sig+")")
	else:
		writeStringField(of, "invokedBy", source_method+"("+source_sig+")")
	writeStringField(of, "invokes", dest_method+"("+dest_sig+")")
	if MOOSECOMPLIANCE:
		writeStringField(of, "base", dest_type)
		writeStringField(of, "receivingVariable", dest_type)
		writeStringField(of, "receivingClass", dest_type)
	else:
		writeStringField(of, "base", dest_type)
	writeSourceLocationEntries(of, sourcefile, begin, end)

	writeSourceSourceAnchor(of, src_sourcefile, src_start)
	writeDestinationSourceAnchor(of, dst_sourcefile, dst_start)

	writeRecordPostfix(of)

def generateCplusPlusInvocationInfoFromEntity(methInv):
	
	writeRecordPrefix(of, "Invocation")
	writeStringField(of, "invokedBy", methInv.getSrcUniqueName())
	writeStringField(of, "invokes", methInv.getTrgName())

	if MOOSECOMPLIANCE:
		writeStringField(of, "base", methInv.dst_class)
		writeStringField(of, "receivingVariable", methInv.dst_class)
		writeStringField(of, "receivingClass", methInv.dst_class)
	else:
		writeStringField(of, "base", methInv.dst_class)

	writeSourceLocationEntries(of, methInv.sourceFile, methInv.start, methInv.start)

	writeSourceSourceAnchor(of, methInv.src_sourceFile, methInv.src_start)
	writeDestinationSourceAnchor(of, methInv.dst_sourceFile, methInv.dst_start)

	writeRecordPostfix(of)
	
def generateFileInfo(fileDict):
	for filePath in fileDict.list:
		writeRecordPrefix(of, "SourceFile")
		writeStringField(of, "uniqueName", filePath)
		
		filePathParts = filePath.split("/")
		fileName = filePathParts[len(filePathParts)-1]
		
		writeStringField(of, "name", fileName)
		writeRecordPostfix(of)

def generateAccess(access):

	if ((access.src_class == None) | (access.src_name == None) | (access.formalSrcParams == None)):
		log.error("Preconditions violated:",[access.src_class,access.src_name,access.formalSrcParams])
		return # preconditions failed

	writeRecordPrefix(of, "Access")
	writeStringField(of, "accessedIn", access.getSrcUniqueName())

	if access.dst_type == "gv":
		writeStringField(of, "accesses", access.dst_name)
		writeStringField(of, "receivingClass", "",)
	else:
		if access.dst_class != "":
			writeStringField(of, "accesses", access.dst_owner+"."+access.dst_name)
			writeStringField(of, "receivingClass", access.dst_owner,)
		else:
			writeStringField(of, "accesses", access.dst_name)
			writeStringField(of, "receivingClass", "")

	if access.acc_type == "w":
		writeBooleanField(of, "isAccessLValue", True)
	else:
		writeBooleanField(of, "isAccessLValue", False)

	writeSourceLocationEntries(of, access.sourceFile, access.start, access.start)
	writeSourceSourceAnchor(of, access.src_sourceFile, access.src_start)
	writeDestinationSourceAnchor(of, access.dst_sourceFile, access.dst_start)

	writeRecordPostfix(of)


def generateFormalParInfo(parName, parType, behEntName, pos):
	writeRecordPrefix(of, "FormalParameter")
	writeStringField(of, "name", parName)
	writeStringField(of, "declaredType", parType)
	writeStringField(of, "declaredClass", declaredTypeToClass(parType))
	if MOOSECOMPLIANCE:
		writeStringField(of, "belongsTo", behEntName)
	else:
		writeStringField(of, "belongsToBehaviour", behEntName)
	writeIntField(of, "position", pos)
	writeStringField(of, "uniqueName", behEntName+"."+parName)
	writeRecordPostfix(of)
	
def generateAnnotation(owner, content, sourceFile, lineNr):
	writeRecordPrefix(of, "Annotation")
	writeStringField(of, "name", content)
	writeStringField(of, "belongsTo", owner)
	writeSourceAnchor(of, sourceFile, lineNr, lineNr)
	writeRecordPostfix(of)

def generateMetrics(metric, value, uniqName, sLoc):
	writeRecordPrefix(of, "Measurement")
	writeStringField(of, "name", metric)
	writeStringField(of, "value", value)
	writeStringField(of, "belongsTo", uniqName)
	writeSourceSourceAnchor(of, sLoc.sourceFile, sLoc.start)
	writeRecordPostfix(of)

# FAMIX preprocessor entities extension from
#	Matthias Rieger, Bart Van Rompaey and Roel Wuyts.
#	Teaching FAMIX about the Preprocessor
#	In Proceedings of the 1st Workshop on FAMIX and Moose in Reengineering
#	June, 2007
def generateCompilationCondition(condition,sourceAnchorList):
	writeRecordPrefix(of, "CompilationCondition")
	writeStringField(of, "condition", condition)
	# write sourceAnchor with multiple entries
	of.write("\n\t(sourceAnchor\t")
	aSourceAnchor = ""

	for aSrcFile in sourceAnchorList:
		aSourceAnchor += "#[file " + aSrcFile + "|]#,\n\t\t\t"
	of.write(aSourceAnchor[:len(aSourceAnchor)-5])
	of.write(")")
	writeRecordPostfix(of)

def generateMacroDefinition(name, value, sourceAnchor, start):
	writeRecordPrefix(of, "MacroDefinition")
	writeStringField(of, "name", name)
	writeStringField(of, "value", value)
	writeSourceAnchor(of, sourceAnchor, start, start)
	writeRecordPostfix(of)

def generatePostamble():
	of.write("\n)\n\n")

def generateHeader(longPreamble, exporterName, systemName, extractionLevel, sourceLang, sourceDialect):
  	import time
	now = time.localtime(time.time())

	of.write("CDIF, SYNTAX \"SYNTAX.1\" \"02.00.00\", ENCODING \"ENCODING.1\" \"02.00.00\"\n#| FAMOOS Information Exchange (FAMIX) Model, see http://www.iam.unibe.ch/~famoos/FAMIX/ |#\n\n")

	if longPreamble:
    		of.write("\n(:HEADER\n")
    		of.write("\t(:SUMMARY\n")
    		of.write("\t\t(ExporterName  \""+exporterName+"\")\n")
    		of.write("\t\t(ExporterVersion  \""+version+"\")\n")
    		of.write("\t\t(ExporterDate \"")
    		of.write(time.strftime("%Y/%m/%d", now))
    		of.write("\")\n\t\t(ExporterTime \"")
    		of.write(time.strftime("%H:%M:%S", now))
    		of.write("\")\n\t\t(PublisherName \"unknown\")\n\t\t(ParsedSystemName \"")
    		of.write(systemName)
    		of.write("\")\n\t\t(ReificationLevel \"1\")\n\t\t(SourceLanguage \"C++\")")
    		of.write("\n\t)\n)\n")
    		of.write("\n(:META-MODEL")
    		of.write("\n\t(:SUBJECTAREAREFERENCE Foundation\n\t\t(:VERSIONNUMBER \"01.00\")\n\t)")
		of.write("\n\t(:SUBJECTAREAREFERENCE FAMIX     \n\t\t(:VERSIONNUMBER \"2.2\"  )\n\t)")
		of.write("\n)\n\n\n")

	of.write("\n(:MODEL\n\n")

	writeRecordPrefix(of, "Model")
	writeStringField(of, "exporterName", exporterName)
	writeStringField(of, "exporterVersion", version)
	writeStringField(of, "exporterDate", time.strftime("%Y/%m/%d", now))
	writeStringField(of, "exporterTime", time.strftime("%H:%M:%S", now))
	writeStringField(of, "publisherName", "unknown")
	writeStringField(of, "parsedSystemName", systemName)
	writeStringField(of, "extractionLevel", extractionLevel)
	writeStringField(of, "sourceLanguage", sourceLang)
	writeStringField(of, "sourceDialect", sourceDialect)
	writeRecordPostfix(of)

version = "$Rev: 537 $"
version = version.split(" ")[1]
