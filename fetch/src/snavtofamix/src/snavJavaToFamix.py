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
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

#
# famix LEVELS
#
# Level 1:  	- Class OK
#		- InheritanceDefinition OK
#		- BehaviouralEntity (Method, Function) OK
#		+/- Package NOT OK
#
# Level 2:  	- Level 1 OK
#		+/- Attribute OK
#		+/- GlobalVariable NOT AVAILABLE
#
# Level 3:  Level 2 	+/- Access
#			+/- Invocation OK
# Level 4:  Level 3 	+/- Argument
#			+/- FormalParameter OK
#			+/- LocalVariable
#			+/- ImplicitVariable
#

import os
import sys

from log4py 							import	Logger

from common								import	MetricsExtractor
from common.dictionaries				import	FileDictionary,\
												MethodsPerFileDictionary
from common.dictionaries.FileDictionary	import	buildFileDict
from common								import 	DataReqs,\
												CtrlStructGrepReader

# TODO: refactor this away. No more usage of cplusplus code
from cplusplus.typed_entities 			import 	ClassDictionary

from java								import	snavTableReader
from java.javaDictionaries 				import	PackageDictionary,\
												ImportDictionary,\
												MethodDictionary,\
												AttributeDictionary,\
												buildAttrTypeDict,\
												MethodSourceDictionary
from java.metrics import JavaNCSSMetricsExtractor
import common.ResolveableInformation

output_file= ""

################################################################################
# Abbreviations used:
################################################################################
# ann: annotation
# attr: attribute
# cl: class
# db: source navigator dumped database file
# dst: destination
# inf: input file
# inh: inheritance
# imp: import
# meth: method
# pkg: package
# qual: qualified
# ref: reference
# sf: sourcefile
# sign: signature
# src: source
# uniq: unique (name)
# dict: dictionary

################################################################################
# check functions
################################################################################

def ensureFileExists(fileName):
	""" ensures that a file exists by creating it in case it did not exist """
	if not os.path.exists(fileName):
		log.info("database ", fileName," does not exist")
		theFile = open(fileName, 'w')
		theFile.close()
		
def checkFileExists(fileName):
	return os.path.exists(fileName)

def checkfamixL1Reqs(DbLoc):
	ensureFileExists(DbLoc + ".files")
	ensureFileExists(DbLoc + ".packages")
  	ensureFileExists(DbLoc + ".classes")
  	ensureFileExists(DbLoc + ".includes")
  	ensureFileExists(DbLoc + ".inheritance")
  	ensureFileExists(DbLoc + ".methods")

def checkfamixL2Reqs(DbLoc):
	ensureFileExists(DbLoc + ".attributes")
	ensureFileExists(DbLoc + ".attributetypes")
	
def checkfamixL3Reqs(DbLoc):
	ensureFileExists(DbLoc + ".references")

def checkfamixL4Reqs(DbLoc):
	ensureFileExists(DbLoc + ".localvars")

################################################################################
# debug
################################################################################

def debug(aString):
	print aString

class CppDict:
	""" a stub for the C preprocessing dictionary (not considered for Java) """
	def __init__(self):
		pass

	def belongsTocppBlock(self, sourceFile, start):
		# By always returning an empty block string  to queries from cdifWriter,
		# no preprocessing related fields will be created.
		return ""

###########################################################################
# entry point
###########################################################################

project_name = ""
input_file = ""

log = Logger().get_instance()
# right amount of parameters?
if not (len(sys.argv) >= 3):
	print "USAGE: snavJavaToFamix [dumped .snprj db dir path] [project name]"
	sys.exit(2)

# does directory path for dbdump output files exist?
if not os.path.exists(sys.argv[1]):
	print "input file",sys.argv[1],"is not a valid file"
	sys.exit(2)

# project name may not be empty
if sys.argv[2] == "":
	print "empty project name"
	sys.exit(2)

outputFile = sys.argv[3]
outputHandle = open(outputFile, "w")
outputHandle.close()
outputHandle = open(outputFile, "a+")
#common.famix.cdifWriter.set_outputfile(outputFile)
common.famix.cdifWriter.set_outputhandle(outputHandle)
common.famix.cdifWriter.initializeIDCounter()
common.famix.cdifWriter.setCppDict(CppDict())
common.famix.cdifWriter.generateHeader(	True, "snavtofamix", sys.argv[2],\
										 "4", "Java", "")
common.famix.cdifReader.set_cdifhandle(outputHandle)

# Preamble + famix level 1
debug(" Entering famix Level 1")
dbLoc = sys.argv[1] + "/" + sys.argv[2]
fileDbLoc = dbLoc + ".files"
pkgDbLoc = dbLoc + ".packages"
clDbLoc = dbLoc + ".classes"
impDbLoc = dbLoc + ".includes"
miDbLoc = dbLoc + ".methods"
inDbLoc = dbLoc + ".inheritance"
checkfamixL1Reqs(sys.argv[1] + "/" + sys.argv[2])

log.info("	Parsing files ...")
fileDict = FileDictionary.FileDictionary()
fileDbLoc = dbLoc + ".files"
buildFileDict(fileDbLoc, fileDict)
common.famix.cdifWriter.generateFileInfo(fileDict)

debug("  - parsing packages ...")
pkgDict = PackageDictionary()
snavTableReader.parsePackages(pkgDbLoc, pkgDict)

debug("  - parsing classes ...")
# we use the generic ClassDictionary, shared with C++
classDict = ClassDictionary()
snavTableReader.parseClasses(clDbLoc, pkgDict, classDict)

debug("  - parsing imports ...")
impDict = ImportDictionary()
snavTableReader.parseImports(impDbLoc,impDict)

debug("  - parsing inheritance relations ...")
inhDict = {}
snavTableReader.parseInheritance(inDbLoc,pkgDict,inhDict,classDict)

# famix level 2
debug(" Entering famix Level 2")
debug("  - parsing methods ...")
methDict = MethodDictionary()
mtdSrcDict = MethodSourceDictionary()
snavTableReader.parseMethods(miDbLoc,pkgDict,classDict,methDict,mtdSrcDict)

attrDbLoc = dbLoc + ".attributes"
attrTypeDbLoc = dbLoc + ".attributetypes"
checkfamixL2Reqs(sys.argv[1] + "/" + sys.argv[2])
debug("  - parsing attributes ...")
attrDict = AttributeDictionary()
attrTypeDict = buildAttrTypeDict(attrTypeDbLoc)
snavTableReader.parseAttributes(attrDbLoc,attrDict,attrTypeDict,classDict,pkgDict)

# famix level 3
debug(" Entering famix Level 3")
referencesDbLoc = dbLoc + ".references"
checkfamixL3Reqs(sys.argv[1] + "/" + sys.argv[2])
debug("  - parsing invocations ...")
miList = snavTableReader.parseMethodInvocations(	referencesDbLoc,pkgDict,impDict,\
										inhDict, classDict,methDict,mtdSrcDict)

# transform methodsource dictionary into something useable for ctrlstruct info
mtdpfDict = MethodsPerFileDictionary.MethodsPerFileDictionary(mtdSrcDict)

# part of insane additional invocation resolver
iDb = dbLoc + ".insaneref"
if DataReqs.checkFileExists(iDb):
	snavTableReader.\
	parseAdditionalMethodInvocations(iDb,pkgDict,impDict,inhDict,classDict, \
									 methDict,mtdSrcDict,miList,mtdpfDict)


debug("  - parsing accesses ...")
snavTableReader.parseAttributeReferences(referencesDbLoc,pkgDict,classDict, \
										 mtdpfDict)

annDbLoc = dbLoc + ".annotations"
if checkFileExists(annDbLoc):
	debug("  - parsing annotations ...")
	snavTableReader.parseAnnotations(annDbLoc, pkgDict, classDict, mtdpfDict, attrDict)

#print impDict.clsImportDict
#print impDict.pkgImportDict
# famix level 4
#debug(" Entering famix Level 4")
#lvDbLoc = dbLoc + ".localvar"
#checkfamixL4Reqs(sys.argv[1] + "/" + sys.argv[2])
#debug("  - parsing formal parameters ...")
#snav_parseFormalParameters(miDbLoc)

# famix level 4+

# metrics calculated by javaNCSS
if DataReqs.checkJavaNCSSReqsSatisfied(sys.argv[1] + "/" + sys.argv[2]):
	log.info("Parsing javancss metrics ...")
	ncssDbLoc = dbLoc + ".ncssmetrics"
	JavaNCSSMetricsExtractor.parseMetrics(ncssDbLoc,methDict)

# file change frequency metrics, provided by either CVS or SVN
if DataReqs.checkVCSMetricsReqsSatisfied(sys.argv[1] + "/" + sys.argv[2]):
	log.info("Parsing change frequency metric ...")
	vcsDbLoc = dbLoc + ".vcs"
	MetricsExtractor.parseVCSMetrics(vcsDbLoc,fileDict)

# conditional stats
cDb = dbLoc + ".conditionals"
if DataReqs.checkConditionalsReqsSatisfied(dbLoc):
	log.info("Parsing conditionals info")
	CtrlStructGrepReader.parse(cDb, mtdSrcDict, mtdpfDict, CtrlStructGrepReader.COND)

#loop stats
lDb = dbLoc + ".loops"
if DataReqs.checkLoopsReqsSatisfied(dbLoc):
	log.info("Parsing loops info")
	CtrlStructGrepReader.parse(lDb, mtdSrcDict, mtdpfDict, CtrlStructGrepReader.LOOP)

# Postamble
common.famix.cdifWriter.generatePostamble()
outputHandle.close()

