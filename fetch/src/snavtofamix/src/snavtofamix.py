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
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>,
#                     Bart Du Bois <bart.dubois@ua.ac.be>

#######################################################################
# abbreviations used
#######################################################################
#
# dict 	- dictionary
# ent	- entity
# snav  - Source Navigator
# db	- database
# cl	- class
# ma	- macro

#######################################################################
# imports
#######################################################################

# python std + external libs
import sys
import os

from log4py import Logger, LOGLEVEL_DEBUG

# reading and writing

import common.cpp
from common import DataReqs
from common import MetricsExtractor
from common.dictionaries.FileDictionary import FileDictionary, \
													buildFileDict

# dictionaries for FAMIX entities (= smart caches)
from cplusplus.data_types.dictionaries.NamespaceDictionary \
									import 	NamespaceDictionary
from cplusplus.data_types.dictionaries.NamespaceUsageDictionary \
									import 	NamespaceUsageDictionary
from cplusplus.data_types.dictionaries.ClassUsageChecker import ClassUsageChecker
from cplusplus.data_types.dictionaries.ClassUsageDictionary import ClassUsageDictionary
from cplusplus.InheritanceDictionary 			import 	InheritanceDictionary, \
											TransitiveInheritanceDictionary
from cplusplus.typed_entities.ClassDictionary 		import 	ClassDictionary
from cplusplus.accessible_entities 			import 	AttributeDictionary, \
											AttributeDictionaryFiller, \
											AccessDictionary
from cplusplus.invokeable_entities 			import 	InvokeableEntityDictionary

from cplusplus.data_types.dictionaries.includeDict \
											import 	IncludeDictionary, \
													TransitiveIncludeDictionary

# resolvers and rule checkers
from cplusplus.typed_entities 				import 	ClassDictionary, \
													ClassSelector, \
													TypedefDictionary
from cplusplus.data_types.NamespaceContainmentChecker \
											import 	NamespaceContainmentChecker
from common.cpp.CppDictionary 				import 	CppDictionary


from cplusplus.typed_entities.Scope 		import Scope

from cplusplus import snavTableReader
from common.Timing import Timer

from java.javaDictionaries 				import	MethodSourceDictionary #hmm
from common.dictionaries 				import	CtrlStructCountDictionary,\
												MethodsPerFileDictionary
from common								import	CtrlStructGrepReader

###############################################################################
# global variables
###############################################################################

output_file = ""
output_handle = ""

###############################################################################
# entry point
###############################################################################

project_name = ""
input_file = ""

log = Logger().get_instance()
log.set_loglevel(LOGLEVEL_DEBUG)

# right amount of parameters?
if not (len(sys.argv) >= 4):
	print "USAGE: python snavtofamix dbdumpPath projectName cdifOutputFile"
	print "\tdbdumpPath -- path of the directory in which the Source Navigator project's database was dumped"
	print "\tprojectName -- the name of the Source Navigator project file, without the .proj extension"
	print "\tcdifOutputFile -- the path and file name of the resulting cdif output file"
	sys.exit(2)

# does directory path for dbdump output files exist?
if not os.path.exists(sys.argv[1]):
	print "input file",sys.argv[1],"is not a valid file"
	sys.exit(2)

# project name must not be empty
if sys.argv[2] == "":
	print "empty project name"
	sys.exit(2)

# Configuration & initialization of output
common.famix.cdifWriter.writeMooseCompliantCdif()
output_file = sys.argv[3]
output_handle = open(output_file, "w")
output_handle.close()
output_handle = open(output_file, "a+")
#common.famix.cdifWriter.set_outputfile(output_file)
common.famix.cdifWriter.set_outputhandle(output_handle)
common.famix.cdifWriter.initializeIDCounter()
common.famix.cdifWriter.generateHeader(\
    True, "snavtofamix", sys.argv[2], "4", "C++", "ISO98")

common.famix.cdifReader.set_cdifhandle(output_handle)

dbLoc = sys.argv[1] + "/" + sys.argv[2]
dbdumpPath = sys.argv[1]

DataReqs.checkfamixCppReqs(sys.argv[1] + "/" + sys.argv[2])

log.info("	Parsing files ...")
fileDict = FileDictionary()
fileDbLoc = dbLoc + ".files"
buildFileDict(fileDbLoc, fileDict)
common.famix.cdifWriter.generateFileInfo(fileDict)

#log.info("	Parsing includes ...")
#includeDict = IncludeDictionary()
#includeDbLoc = dbLoc + ".includes"
#snav_parseIncludes(includeDbLoc, includeDict, fileDict)

log.info("  Parsing ALTERNATIVE includes...")
includeDict = IncludeDictionary()
includeDbLoc = dbLoc + ".includes2"
snavTableReader.parseAlternativeIncludes(includeDbLoc, includeDict, fileDict)

# Create a cache of transitive included files
transIncludeDict = TransitiveIncludeDictionary(includeDict)

log.info("	Parsing conditional compilation directives ...")
dbLoc = sys.argv[1] + "/" + sys.argv[2]
condCompDbLoc = dbLoc + ".condcomp"
DataReqs.checkfamixPPReqs(sys.argv[1] + "/" + sys.argv[2])
condCompDict = CppDictionary()
snavTableReader.parseConditionalCompilationDirectives(condCompDbLoc,condCompDict)
common.famix.cdifWriter.setCppDict(condCompDict)

log.info("	Parsing macro definition directives ...")
maDict = {}
snavTableReader.parseMacroDefDirectives(condCompDbLoc,maDict)

# Preamble + famix level 1
log.info("Entering famix Level 1")
namespaceDbLoc = dbLoc + ".namespaces"
clDbLoc = dbLoc + ".classes"
miDbLoc = dbLoc + ".methods"
mdDbLoc = dbLoc + ".methoddefs"
inDbLoc = dbLoc + ".inheritance"
fuDbLoc = dbLoc + ".functions"
fdDbLoc = dbLoc + ".functiondefs"
DataReqs.checkfamixL1Reqs(sys.argv[1] + "/" + sys.argv[2])

# default initialization
namespaceContainmentChecker = NamespaceContainmentChecker(NamespaceDictionary())
namespaceUsageContainmentChecker = NamespaceContainmentChecker(NamespaceUsageDictionary())
classUsageContainmentChecker = ClassUsageChecker(ClassUsageDictionary())

if os.path.exists(namespaceDbLoc):
	log.info("	Parsing namespaces...")
	namespaceDict = snavTableReader.parseNamespaces(namespaceDbLoc)
	namespaceContainmentChecker = NamespaceContainmentChecker(namespaceDict)
	log.info("Parsed " + `namespaceDict.getNumberOfEntities()` + " namespaces")

	log.info("	Parsing namespace usages...")
	namespaceUsageDict = snavTableReader.parseNamespaceUsages(namespaceDbLoc)
	namespaceUsageContainmentChecker = NamespaceContainmentChecker(namespaceUsageDict)
	log.info("Parsed " + `namespaceUsageDict.getNumberOfEntities()` + " namespace usages")

	log.info("	Parsing class usages...")
	classUsageDict = snavTableReader.parseClassUsages(namespaceDbLoc, namespaceContainmentChecker)
	classUsageContainmentChecker = ClassUsageChecker(classUsageDict)
	log.info("Parsed " + `classUsageDict.getNumberOfEntities()` + " class usages")

log.info("	Parsing classes ...")
classDict = ClassDictionary()
snavTableReader.parseClasses(clDbLoc, classDict, namespaceContainmentChecker)

unionDbLoc = dbLoc + ".unions"

if os.path.exists(unionDbLoc):
	log.info("	Parsing unions...")
	snavTableReader.parseClasses(unionDbLoc, classDict, namespaceContainmentChecker)

typedefDbLoc = dbLoc + ".typedef"
log.info("	Parsing typedefs ...")
typedefDict = TypedefDictionary()

scope = Scope(includeDict, namespaceContainmentChecker, namespaceUsageContainmentChecker,classUsageContainmentChecker)
classSelector = ClassSelector(classDict, scope, typedefDict)
snavTableReader.parseTypedefs(typedefDbLoc, typedefDict, classSelector, namespaceContainmentChecker)

log.info("	Parsing inheritance relations ...")
inhDict = InheritanceDictionary()
snavTableReader.parseInheritance(inDbLoc, classDict, inhDict, classSelector)

# Create a cache of transitive superclasses
transInhDict=TransitiveInheritanceDictionary(inhDict)

scope.setTransitiveInheritanceDict(transInhDict)

methodSourceDict = MethodSourceDictionary() # TODO this is a java dictionary, make this general

log.info("	Parsing methods ...")
invokeableEntityDict = InvokeableEntityDictionary()
snavTableReader.parseMethodTables(miDbLoc, mdDbLoc, classSelector, classDict, namespaceContainmentChecker,typedefDict,invokeableEntityDict, methodSourceDict)

log.info("	Parsing functions ...")
snavTableReader.parseFunctionTables(fuDbLoc, fdDbLoc, typedefDict, namespaceContainmentChecker, classSelector, invokeableEntityDict, methodSourceDict)

log.info("	Generating invokable entities ...")
for reference in invokeableEntityDict.retrieveAllMultiLocReferences():
	common.famix.cdifWriter.generateInvocationEntityReference(reference,scope)

# famix level 2
log.info("Entering famix Level 2")
attrDbLoc = dbLoc + ".attributes"
gvDbLoc = dbLoc + ".globalvar"
conDbLoc = dbLoc + ".constants"
DataReqs.checkfamixL2Reqs(sys.argv[1] + "/" + sys.argv[2])

log.info("Parsing attributes ...")
attrDict = AttributeDictionary()
attrDictFiller = AttributeDictionaryFiller(attrDict)
snavTableReader.parseAttributes(attrDbLoc,classSelector,attrDictFiller, namespaceContainmentChecker)

log.info("Parsing global variables ...")
snavTableReader.parseGlobalVars(gvDbLoc, namespaceContainmentChecker, classSelector)
snavTableReader.parseGlobalVars(conDbLoc, namespaceContainmentChecker, classSelector)

# famix level 3
log.info("Entering famix Level 3")
referencesDbLoc = dbLoc + ".references"
DataReqs.checkfamixL3Reqs(sys.argv[1] + "/" + sys.argv[2])

#  list of line numbers of the referencesDbLoc which are already
# processed as accesses
referenceLinesRecognizedAsAccesses = []

log.info("Parsing accesses ...")
accessDict = AccessDictionary()
timer = Timer("Parsing accesses")
timer.start()
nrOfItems = snavTableReader.parseAccess(referencesDbLoc, accessDict, invokeableEntityDict, attrDict, typedefDict, transInhDict, scope, referenceLinesRecognizedAsAccesses, output_file)
timer.stop()
timer.log(nrOfItems)

log.info("Parsing invocations ...")
timer = Timer("Parsing invocations")
timer.start()
nrOfItems = snavTableReader.parseInvocations(referencesDbLoc, accessDict, \
								invokeableEntityDict, typedefDict, \
								transIncludeDict, transInhDict, scope, \
								referenceLinesRecognizedAsAccesses)
timer.stop()
timer.log(nrOfItems)

log.info("Parsing macro uses ...")
timer = Timer("Parsing macro uses")
timer.start()
nrOfItems = snavTableReader.parseMacroUses(referencesDbLoc,maDict)
timer.stop()
timer.log(nrOfItems)

# famix level 4
log.info("Entering famix Level 4")
lvDbLoc = dbLoc + ".localvar"
DataReqs.checkfamixL4Reqs(sys.argv[1] + "/" + sys.argv[2])
log.info("Parsing formal parameters ...")
snavTableReader.parseFormalParameters(miDbLoc)
#debug("  - parsing local variables ...")
#snav_parseLocalVariables(lvDbLoc)

log.info("Entering famix Level 4+")
# metrics calculated by pmccabe, to be replacing the less robust cccc metrics
DataReqs.checkPmcmetricReqsSatisfied(sys.argv[1] + "/" + sys.argv[2])
log.info("Parsing pmccabe metrics ...")
pmcDbLoc = dbLoc + ".pmcmetrics"
snavTableReader.parsePmcmetrics(pmcDbLoc,invokeableEntityDict)

# transform methodsource dictionary into something useable for ctrlstruct info
methodsPerFileDict = MethodsPerFileDictionary.MethodsPerFileDictionary(methodSourceDict)

# conditional stats
cDb = dbLoc + ".conditionals"
if DataReqs.checkConditionalsReqsSatisfied(dbLoc):
	log.info("Parsing conditionals info")
	CtrlStructGrepReader.parse(cDb, methodSourceDict, methodsPerFileDict, CtrlStructGrepReader.COND)

# loop stats
lDb = dbLoc + ".loops"
if DataReqs.checkLoopsReqsSatisfied(dbLoc):
	log.info("Parsing loops info")
	CtrlStructGrepReader.parse(lDb, methodSourceDict, methodsPerFileDict, CtrlStructGrepReader.LOOP)
	
# file change frequency metrics, provided by either CVS or SVN
if DataReqs.checkVCSMetricsReqsSatisfied(sys.argv[1] + "/" + sys.argv[2]):
	log.info("Parsing change frequency metric ...")
	vcsDbLoc = dbLoc + ".vcs"
	MetricsExtractor.parseVCSMetrics(vcsDbLoc,fileDict)

# Postamble
common.famix.cdifWriter.generatePostamble()
output_handle.close()

