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
# Copyright 2006,2007 University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>,
#                     Bart Du Bois <bart.dubois@ua.ac.be>

from log4py import Logger, LOGLEVEL_DEBUG

import common
from common import utils
from common.SourceLocation                     import     SourceLocation

from common.Timing import Timer

# representations of FAMIX entities
from cplusplus.data_types.DataRecords         import     InheritanceEntity
from cplusplus.data_types.AccessibleEntities import AttributeEntity,\
                                            GlobalVariableEntity
from cplusplus.data_types.InvokeableEntities     import     MethodEntity, \
                                            FunctionEntity
from cplusplus.data_types.ClassEntity         import     ClassEntity, \
                                            TypeDefEntity

from cplusplus.data_types.IncludeEntity import IncludeEntity

# dictionaries for FAMIX entities (= smart caches)


from cplusplus.data_types.dictionaries.NamespaceDictionaryBuilder \
                                    import     NamespaceDictionaryBuilder
from cplusplus.data_types.dictionaries.NamespaceUsageDictionaryBuilder \
                                    import     NamespaceUsageDictionaryBuilder
from cplusplus.data_types.dictionaries.ClassUsageDictionaryBuilder import ClassUsageDictionaryBuilder
from cplusplus.accessible_entities             import  AccessEntity
from cplusplus.invokeable_entities             import  InvocationEntity


from cplusplus import EntityCacheFactory
from cplusplus.typed_entities.QualifiedNameHelperFunctions import getParentNamespaceName


# resolvers and rule checkers
from cplusplus import RuleChecker
from common.cpp.ConditionalCompilationBlockResolver \
                                            import     CondBlockResolver,\
                                                     processcppBlock


from cplusplus.accessible_entities.AlternativeAccessResolver import AlternativeAccessResolver
from cplusplus.invokeable_entities.AlternativeInvocationResolver import AlternativeInvocationResolver


#def snav_parseLocalVars(lvDbLoc, methDict, funcDict, classDict):
#    for line in open(lvDbLoc, 'r'):
#        #lv = LocalVarEntity(lvDbLoc,
#        pass

##
# Parse the include directives.
#
# Adjusts the name of the included file as to be relative to the base
# of the SourceNavigator project, and not relative to the including file.
##
#def snav_parseIncludes(includeDbLoc, includeDict, fileDict):
#    dbdumpFile=open(includeDbLoc, "r")
#    for line in dbdumpFile:
#        line = line.strip()
#        cols = line.split(" ")
#
#        includingFile = cols[2].split(";")[0]
#        includedFile = cols[0]
#
#        similarNamedFiles = fileDict.getSimilarNamedFiles(includedFile)
#
#        # Note: we cannot select among multiple similar names
#        if (len(similarNamedFiles) == 1) and (similarNamedFiles[0] != includedFile):
#            includedFile = similarNamedFiles[0]
#
#        notPreviouslyIncluded=includeDict.add(includingFile, includedFile)
#
#        if notPreviouslyIncluded:
#            common.famix.generateIncludeInfo(includingFile, adjustPath(includingFile,includedFile))
#    dbdumpFile.close()

def parseAlternativeIncludes(includeDbLoc, includeDict, fileDict):
    dbdumpFile=open(includeDbLoc, "r")

    for line in dbdumpFile:
        line = line.strip()
        includeEntity = IncludeEntity(line)
        includeEntity.removeRelativePathPart()


        similarNamedFiles = fileDict.getSimilarNamedFiles(includeEntity.getIncludedFile())

        # Note: we cannot select among multiple similar names
        if (len(similarNamedFiles) == 1) and \
            (similarNamedFiles[0] != includeEntity.getIncludedFile()):
            includeEntity.updateIncludedFile(similarNamedFiles[0])

        notPreviouslyIncluded=includeDict.addEntity(includeEntity)

        if notPreviouslyIncluded:
            #common.famix.generateIncludeInfo(includingFile, adjustPath(includingFile,includedFile))
            common.famix.generateIncludeEntity(includeEntity)
    dbdumpFile.close()

def parseMacroDefDirectives(macroDb,maDict):
    sep = ":"
    for line in open(macroDb, "r"):
        line = line.strip()
        sourceFile = line.split(sep)[0]
        start = line.split(sep)[1]
        dataCols = line.split(sep)[2].split(" ")

        name = ""
        value = ""

        # skip scenarios
        if not "#define" in dataCols[0]: continue # only act upon defines now
        if len(dataCols) > 3: continue # don't act upon function macro's
        if 1 >= len(dataCols): continue # don't know what that means ...

        if 2 == len(dataCols): # just a define
            name = dataCols[1]
        elif 3 == len(dataCols): # define with value assignment
            name = dataCols[1]
            value = dataCols[2]

        common.famix.cdifWriter.generateMacroDefinition(\
                            name,value,sourceFile,start)

        sLoc = sourceFile+":"+start
        if not name in maDict:
            maDict[name] = [ sLoc ]
        else:
            maDict[name].append(sLoc)

def parseConditionalCompilationDirectives(ccDb,compCondDict):
    cbR = CondBlockResolver("")
    for line in open(ccDb, "r"):
        cols = line.split(":")
        sourceFile = cols[0]
        if cbR.sourceFile != sourceFile:
            cbR.nextSourceFile(sourceFile)
        if cbR.lock:
            continue
        try:
            cbR = processcppBlock(compCondDict,cbR,cols[1],cols[2])
        except Exception:
            pass

    for block in compCondDict.dict:
        common.famix.cdifWriter.generateCompilationCondition(block,compCondDict.getFamixSourceAnchorListForBlock(block))
        pass

def parseNamespaces(namespaceFileName):
    dictBuilder = NamespaceDictionaryBuilder(namespaceFileName)
    dictBuilder.build()
    namespaceDict = dictBuilder.getNamespaceDictionary()

    # write all namespace references to CDIF
    for namespaceRef in namespaceDict.getAllReferences():
        common.famix.cdifWriter.generatePackageInfo(namespaceRef)

    return namespaceDict

def parseNamespaceUsages(namespaceFileName):
    dictBuilder = NamespaceUsageDictionaryBuilder(namespaceFileName)
    dictBuilder.build()
    namespaceUsageDict = dictBuilder.getNamespaceDictionary()

    return namespaceUsageDict

def parseClassUsages(namespaceFileName, namespaceContainmentChecker):
    dictBuilder = ClassUsageDictionaryBuilder(namespaceFileName)
    dictBuilder.build()
    dictBuilder.refineDictWithNamespaceInfo(namespaceContainmentChecker)
    classUsageDict = dictBuilder.getNamespaceDictionary()
    return classUsageDict

##
# Parse classes from the given class-database-dump (.classes file).
# The given method-definition and method-implementation database-dump are used
# to find out whether the class is abstract.
# There are faster ways to check this, e.g. by using codes similar to the ones
# used by methods for modifiers (virtual, static, private...)
##
def parseClasses(clDb, classDictionary, namespaceContainmentChecker):
    for line in open(clDb, 'r'):
        cl = ClassEntity(line)

        namespaceName = \
            namespaceContainmentChecker.getSurroundingNamespaceName(cl.sourceLocation)

        classNameIsCPPkeyword = (cl.name in common.cpp.CPP_KEYWORDS)

        classContainedInDictionary = classDictionary.containsClass(\
                                            cl.name, \
                                            cl.sourceLocation.sourceFile, \
                                            namespaceName)

        sourceFile = cl.sourceLocation.sourceFile
        start = cl.sourceLocation.start
        if not( classContainedInDictionary or classNameIsCPPkeyword ):
            common.famix.cdifWriter.generateClassInfo(cl.name, # class name
                                    namespaceName,  # namespace
                                    #snav_isAbstractClass(cl.name, mdDb, miDb),
                                    cl.isAbstract(), # should use this method, which currently returns False!
                                    sourceFile, start, start # sourceAnchor info
                                    )
            ##
            # TODO: Find out how we can recognize nested classes.
            # E.g., Group 000458.008 spreadsheet/spreadsheetApp.h;458.13 0x0 {} {} {} {}
            # We should find out that it is actually an inner class of SpreadsheetApp
            # And thus its name should be SpreadsheetApp::Group.
            ##

            # put any data you want to remember about this class in classData
            classData = [ cl.template, namespaceName ]

            classDictionary.add(cl.name, sourceFile, start, classData)
        else:
            log.warn("Ignoring duplicate class entity.",cl.line)

def parseTypedefs(typedefDbLoc, typedefDict, classSelector, namespaceContainmentChecker):
    input_file=open(typedefDbLoc, "r")
    for line in input_file:
        typeDef = TypeDefEntity(line)
        typeDefRef = typeDef.getReference()

        if typeDefRef.isMacroRelatedTypeDef():
            continue

        sourceLoc = typeDefRef.getSourceLocation()
        namespaceName = namespaceContainmentChecker.getSurroundingNamespaceName(sourceLoc)

        typeDefRef.setNamespaceName(namespaceName)

        if typeDefRef.getBaseClass() != None:
            originalTypeWithoutTemplates=typeDefRef.getBaseClass().split("<")[0]

            potentialTargets = classSelector.selectClasses(originalTypeWithoutTemplates, \
                                                        sourceLoc.sourceFile, \
                                                        sourceLoc.start)
            nrOfTargets = len(potentialTargets)

            if ( nrOfTargets > 1 ):
                pass
                #log.warn("TypeDef-base-type-problem: ",nrOfTargets," classes with name \"" + originalTypeWithoutTemplates + ".")
            elif ( nrOfTargets == 0 ):
                pass
                #log.warn("TypeDef-base-type-problem: no class with name \"" + originalTypeWithoutTemplates     + "\" known from file ",sourceLoc.sourceFile,".")
            # Happens regularly, e.g., in case of library classes
            else:
                # get the data on the sole left-over method-owner
                classReference = potentialTargets[0]

                baseSourceLoc = SourceLocation(classReference.getSourceFile(),\
                                                classReference.getLineNr(),\
                                                classReference.getLineNr())
                typeDefRef.setBaseSourceLocation(baseSourceLoc) # note: does not change typeDef

        common.famix.generateTypeDefInfo(typeDefRef)
        typedefDict.add(typeDefRef)
    input_file.close()

##
# TODO: keep information on sourceSourceAnchor and destSourceAnchor.
# We currently don't bother since we don't generate duplicate classes...
#
# TODO2: truly find out whether classes are abstract or not!
##
def parseInheritance(inhDb, clDict, inhDict, classSelector):
    for line in open(inhDb, 'r'):
        inh = InheritanceEntity(line)

        # search for subclass
        potentialTargets = classSelector.selectClasses(inh.subclass, inh.sourceFile, inh.start)
        nrOfTargets = len(potentialTargets)
        if ( nrOfTargets > 1 ):
            log.warn( "Subclass-problem: ",nrOfTargets," classes with name \""
                        + inh.subclass + ".")
            log.warn( "	- ",potentialTargets[0].sourceFile,potentialTargets[0].lineNr)
            log.warn( "	- ",potentialTargets[1].sourceFile,potentialTargets[0].lineNr)
            continue # ignore inheritance link since we can't pinpoint the target. Hasn't happen yet, though.
        elif ( nrOfTargets == 0 ):
            log.warn("Sub-problem: no class with name \"" +
                        inh.subclass     + "\" known from file ",inh.sourceFile)
            # Happens regularly, e.g., in case of library classes or for qualified class names
            continue # ignore inheritance link since we can't find the declaration of the superclass.

        classReference = potentialTargets[0]
        inh.subclass = classReference.getUniqueName()

        # search for superclass
        potentialTargets = classSelector.selectClasses(inh.superclass, inh.sourceFile, inh.start)
        nrOfTargets = len(potentialTargets)

        if ( nrOfTargets > 1 ):
            log.warn( "Superclass-problem: ",nrOfTargets," reachable classes with name \""
                        + inh.superclass + ".")
            continue # ignore inheritance link since we can't pinpoint the target. Hasn't happen yet, though.
        elif ( nrOfTargets == 0 ):
            log.warn("Superclass-problem: no reachable class with name \"" +
                        inh.superclass     + "\" known from file ",inh.sourceFile)
            # Happens regularly, e.g., in case of library classes or for qualified class names
            continue # ignore inheritance link since we can't find the declaration of the superclass.

        classReference = potentialTargets[0]
        inh.superclass = classReference.getUniqueName()

        # The classReference contains the source location of the class usage
        # declaration. We still need to find out the source location of the
        # class itself.
        clSourceList = clDict.getClassesByNamespace(classReference.className,classReference.namespaceName)
        nrOfTargets = len(clSourceList)
        if (nrOfTargets > 1):
            log.warn("Superclass-problem: ", nrOfTargets, " source locations for class with name \""
                        + inh.superclass + "\":" + inh.sourceFile + " @" + inh.start)
            continue # ignore inheritance link since we can't pinpoint the target. Hasn't happen yet, though.
        elif (nrOfTargets == 0):
            log.warn("Superclass-problem: no source location for class with name \"" +
                        inh.superclass)
            # Happens regularly, e.g., in case of library classes or for qualified class names
            continue # ignore inheritance link since we can't find the declaration of the superclass.

        classReference = clSourceList[0]
        inh.dst_sourceFile = classReference.getSourceFile()
        inh.dst_lineNr = classReference.getLineNr()

        # Note: we have three cases here:
        #  (a) We found a single class corresponding to the superclass.
        #      In that case, the dst_sourceFile and dst_lineNr of the superclass are filled in.
        #
        #  (b) We found more than a single class corresponding to the superclass.
        #  (c) We did not found any class corresponding to the superclass.
        #      In both cases (b and c), the dst_sourceFile and dst_lineNr will be empty.
        common.famix.cdifWriter.generateInheritanceInfo(inh.subclass, inh.superclass, "", inh.accessControlQualifier, inh.sourceFile, inh.start, inh.start, inh.dst_sourceFile, inh.dst_lineNr)

        # TODO: also include sourceFile and lineNr for both subclass and superclass, as [className, sourceLine, lineNr] forms a unique identifier for a class.
        inhDict.add(inh.subclass, inh.superclass)

##
# Parse method implementations from a given database-dump file. Two files are currently envisioned,
# being either:
# (a) the method implementation database-dump file (.methods)
#    In that case, parameter isImplementation will be False.
#
# (b) the method definition database-dump file (.methoddefs)
#    In that case, parameter isImplementation will be True.
#
# TODO: verify whether the abstractness of a method is calculated correctly within MethodEntity.
#
# @dbdumpFile - the name of the method implementation or definition database-dump file
# @isImplementation - whether the given dbdumpFile is the method implementation database dump file (False) or the
# method definition databasse dump file (True).
# @param classSelector - the class selector to be used for selecting method owners
# @param classDict - the class dictionary
##
def parseMethods(dbdumpFile, isImplementation,\
				classSelector, classDict, namespaceContainmentChecker, \
				typedefDict,invokeableEntityDict, methodSourceDict = None):
    nrOfLinesProcessed = 0

    for line in dbdumpFile:
    	
        nrOfLinesProcessed += 1

        isAbstract = False
        method = MethodEntity(isAbstract, isImplementation, line)

        method.postProcessParameters(typedefDict)

        methRef = method.getReference()

        sourceLoc = SourceLocation(methRef.getSourceFile(),\
                                        methRef.getLineNr(),\
                                        methRef.getLineNr())
        namespaceName = namespaceContainmentChecker.getSurroundingNamespaceName(sourceLoc)

        method.setNamespaceName(namespaceName)
        methRef = method.getReference()
		
        parentReference = method.getParentReference()
        potentialTargets = classSelector.selectClasses(parentReference.getReferencedName(),\
                                                 methRef.getSourceFile(), methRef.getLineNr())
        nrOfTargets = len(potentialTargets)

        if ( nrOfTargets > 1 ):
            log.warn("Method-owner-problem: ",nrOfTargets," classes with name \""\
                     + parentReference.getReferencedName() + ".")
            continue # ignore containment link since we can't pinpoint the target.
        elif ( nrOfTargets == 0 ):
        	if parentReference.getCleanName() == "AsamBuilder":
        		references = classDict.getClassesByName(parentReference.getCleanName())

        		assert False

        	log.warn("Method-owner-problem: no class with name \"" \
                    + parentReference.getCleanName() + "\" known from file ",\
                    methRef.getSourceFile(),"@",methRef.getLineNr(),".")
        	# seems to happen for inner-classes, e.g. owner named SpreadsheetApp::Group
        	continue # ignore containment link since we can't find the declaration of the owner.
        else:
            # get the data on the sole left-over method-owner
            classReference = potentialTargets[0]

            parentReference.setSourceFile(classReference.getSourceFile())
            parentReference.setLineNr(classReference.getLineNr())

            # fill in the method owner template parameters
            method.ownerTemplateParameters=classReference.getTemplateParameters()

            if method.ownerTemplateParameters == None:
                method.ownerTemplateParameters=classReference.getTemplateParameters()

            if (methRef.getNamespaceName() == "") and ("::" in methRef.ownerName):
                method.namespaceName = getParentNamespaceName(methRef.ownerName)
                methRef = method.getReference()

        typeReference = method.getTypeReference()

        if not(typeReference.isPrimitive()):
            potentialTargets = classSelector.selectClasses(typeReference.getCleanName(),\
                                                     methRef.getSourceFile(), methRef.getLineNr())
            nrOfTargets = len(potentialTargets)

            if ( nrOfTargets > 1 ):
                pass
                #log.warn("Method-type-problem: ",nrOfTargets," classes with name \""\
                #         + typeReference.getCleanName() + ".")
            elif ( nrOfTargets == 0 ):
                pass
                #log.warn("Method-type-problem: no class with name \"" \
                #        + typeReference.getCleanName() + "\" known from file ",\
                #        methRef.getSourceFile(),"@",methRef.getLineNr(),".")
            else:
                # get the data on the sole left-over method-type
                classReference = potentialTargets[0]

                typeReference.setResolvedName(classReference.getUniqueName())
                typeReference.setSourceFile(classReference.getSourceFile())
                typeReference.setLineNr(classReference.getLineNr())
        else:
            typeReference.setResolvedName(typeReference.getCleanName())

            # TODO: fill in the method type template parameters

        #didNotExistYet = invokeableEntityDictFiller.add(method.getReference())
        invokeableEntityDict.addMultiLocReference(method.getReference())
        if methodSourceDict:
            # this is a definition, add it to methodSrcDict.
            # if you want declarations build another dict
            sLoc = method.getReference().getSourceFile() + ":" + str(method.getReference().getLineNr())
            methodSourceDict.add(method.getReference().getUniqueName(), sLoc)
        invokeableEntityDict.addMultiLocReference(method.getReference())

        #if didNotExistYet:
        #    aCommandList = RuleChecker.checkMethodEntity(method)
        #    RuleChecker.generateMethodInfo(aCommandList, method)

    return nrOfLinesProcessed


##
# Parse methods from the method definition and method implementation
# database-dump-files.
#
# @param methodImplementationDBD -     the name of the method implementation
#                                    database-dump file (.methods)
# @param methodDefinitionDBD -     the name of the method definition database-dump
#                                file (.methoddefs)
# @param invokeableEntityDictFiller -     the filler to be used for filling an
#                                        invokeable entity dictionary.
# @param classSelector -     the class selector to be used for selecting method
#                            owners
# @param classDict - the class dictionary
##
def parseMethodTables(    methodImplementationDBD, methodDefinitionDBD, \
                        classSelector, classDict, namespaceContainmentChecker,\
                        typedefDict,invokeableEntityDict, methodSourceDict):
    # build up a method cache
    # these caches are used by the RuleChecker to find out
    # whether the method should be generated.
    # TODO: use the method-dictionary to filter out generation requests
    # so that there is no longer a need for the low-level caches used below.

    dbdumpFile=open(methodDefinitionDBD, 'r')
    isImpl = False

    timer = Timer("Parsing method declarations")
    timer.start()
    nrOfItems = parseMethods(    dbdumpFile, isImpl, \
                    classSelector, classDict, namespaceContainmentChecker,typedefDict,invokeableEntityDict)
    timer.stop()
    timer.log(nrOfItems)

    dbdumpFile.close()

    # check for methods that are immediately implemented
    dbdumpFile=open(methodImplementationDBD, 'r')
    isImpl = True

    timer = Timer("Parsing method definitions")
    timer.start()
    nrOfItems = parseMethods(    dbdumpFile, isImpl, \
                    classSelector, classDict, namespaceContainmentChecker,\
                    typedefDict,invokeableEntityDict, methodSourceDict)
    timer.stop()
    timer.log(nrOfItems)

    dbdumpFile.close()

def parseFunctions(dbdumpFile, isImpl, typedefDict, namespaceContainmentChecker, classSelector, invokeableEntityDict, methodSourceDict = None):
    nrOfLinesProcessed = 0

    for line in dbdumpFile:
        nrOfLinesProcessed += 1
        line = line.strip()

        function = FunctionEntity(isImpl, line)
        function.postProcessParameters(typedefDict)

        functionRef = function.getReference()
        sourceLoc = SourceLocation(functionRef.getSourceFile(),\
                                        functionRef.getLineNr(),\
                                        functionRef.getLineNr())

        namespaceName = namespaceContainmentChecker.getSurroundingNamespaceName(sourceLoc)
        function.namespaceName = namespaceName

        functionRef = function.getReference()

        typeReference = function.getTypeReference()

        if not(typeReference.isPrimitive()):
            potentialTargets = classSelector.selectClasses(typeReference.getCleanName(),\
                                                     functionRef.getSourceFile(), functionRef.getLineNr())
            nrOfTargets = len(potentialTargets)

            if ( nrOfTargets > 1 ):
                pass
                #log.warn("Function-type-problem: ",nrOfTargets," classes with name \""\
                #         + typeReference.getCleanName() + ".")
            elif ( nrOfTargets == 0 ):
                pass
                #log.warn("Function-type-problem: no class with name \"" \
                #        + typeReference.getCleanName() + "\" known from file ",\
                #        functionRef.getSourceFile(),"@",functionRef.getLineNr(),".")
            else:
                # get the data on the sole left-over method-type
                classReference = potentialTargets[0]

                typeReference.setResolvedName(classReference.getUniqueName())
                typeReference.setSourceFile(classReference.getSourceFile())
                typeReference.setLineNr(classReference.getLineNr())

                # TODO: fill in the return type template parameters
        else:
            typeReference.setResolvedName(typeReference.getCleanName())

        if methodSourceDict:
            # this is a definition, add it to methodSrcDict.
            # if you want declarations build another di        if methodSourceDict:
            # this is a definition, add it to methodSrcDict.
            # if you want declarations build another dict
            sLoc = functionRef.getSourceFile() + ":" + str(functionRef.getLineNr())
            methodSourceDict.add(functionRef.getUniqueName(), sLoc)

            sLoc = functionRef.getSourceFile() + ":" + str(functionRef.getLineNr())
            methodSourceDict.add(functionRef.getUniqueName(), sLoc)

        invokeableEntityDict.addMultiLocReference(functionRef)

    return nrOfLinesProcessed

##
# Parse functions from the given database-dump-file and add them to the dictionary.
#
# @param functionDBD - the name of the function database-dump file (.functions)
##
def parseFunctionTables(functionDBD, functionDefDBD, \
                        typedefDict, namespaceContainmentChecker, classSelector, \
                        invokeableEntityDict, methodSourceDict):
    dbdumpFile=open(functionDBD, 'r')
    isImpl = True
    timer = Timer("Parsing function declarations")
    timer.start()
    nrOfItems = parseFunctions(dbdumpFile, isImpl, typedefDict, namespaceContainmentChecker, classSelector, invokeableEntityDict, methodSourceDict) # temporarily also add to declarations to methodSourceDict
    timer.stop()
    timer.log(nrOfItems)
    dbdumpFile.close()

    dbdumpFile=open(functionDefDBD, 'r')
    isImpl = False
    timer = Timer("Parsing function definitions")
    timer.start()
    nrOfItems = parseFunctions(dbdumpFile, isImpl, typedefDict, namespaceContainmentChecker, classSelector, invokeableEntityDict, methodSourceDict)
    timer.stop()
    timer.log(nrOfItems)
    dbdumpFile.close()

##
# Parse attributes from the given database-dump-file and add them to the dictionary.
#
# @param attributeDBD - the name of the attribute database-dump file (.attributes)
# @param classSelector - the class selector to be used for selecting method owners
# @param attributeDictFiller - the filler to be used for filling an attribute dictionary.
##
def parseAttributes(attributeDBD, classSelector, attributeDictFiller, namespaceContainmentChecker):

    for line in open(attributeDBD, 'r'):
        attribute = AttributeEntity(line)
        attrRef = attribute.getReference()

        sourceLoc = SourceLocation(attrRef.getSourceFile(),\
                                        attrRef.getLineNr(),\
                                        attrRef.getLineNr())
        namespaceName = namespaceContainmentChecker.getSurroundingNamespaceName(sourceLoc)

        attribute.setNamespaceName(namespaceName)
        attrRef = attribute.getReference()

        parentRef = attribute.getParentReference()
        potentialTargets = classSelector.selectClasses(parentRef.getReferencedName(), attrRef.getSourceFile(), attrRef.getLineNr())
        nrOfTargets = len(potentialTargets)

        if ( nrOfTargets > 1 ):
            log.warn("Attribute-owner-problem: ",nrOfTargets," classes with name \"" + parentRef.getReferencedName() + ".")
            continue # ignore containment link since we can't pinpoint the target.
        elif ( nrOfTargets == 0 ):
            log.warn("Attribute-owner-problem: no class with name \"" + parentRef.getReferencedName() + "\" known from file ",attrRef.getSourceFile(),"@",attrRef.getLineNr(),".")
            # seems to happen for inner-classes, e.g. owner named SpreadsheetApp::Group
            continue # ignore containment link since we can't find the declaration of the owner.
        else:
            # get the data on the sole left-over attribute-owner
            classReference = potentialTargets[0]
            parentRef.setSourceFile(classReference.getSourceFile())
            parentRef.setLineNr(classReference.getLineNr())
            attribute.ownerTemplateParameters=classReference.getTemplateParameters()

        # resolve the attribute type
        typeRef = attribute.getTypeReference()

        if not(typeRef.isPrimitive()):
            potentialTargets = classSelector.selectClasses(typeRef.getCleanName(),\
                                                     attrRef.getSourceFile(), attrRef.getLineNr())
            nrOfTargets = len(potentialTargets)

            if ( nrOfTargets > 1 ):
                pass
                #log.warn("Attribute-type-problem: ",nrOfTargets," classes with name \""\
                #         + typeRef.getCleanName() + ".")
                typeRef.setResolvedName(typeRef.getCleanName())
            elif ( nrOfTargets == 0 ):
                pass
                #log.warn("Attribute-type-problem: no class with name \"" \
                #        + typeRef.getCleanName() + "\" known from file ",\
                #        attrRef.getSourceFile(),"@",attrRef.getLineNr(),".")
                typeRef.setResolvedName(typeRef.getCleanName())
            else:
                # get the data on the sole left-over method-type
                classReference = potentialTargets[0]

                typeRef.setResolvedName(classReference.getUniqueName())
                typeRef.setSourceFile(classReference.getSourceFile())
                typeRef.setLineNr(classReference.getLineNr())

                # TODO: fill in the attribute type template parameters
        else:
            typeRef.setResolvedName(typeRef.getCleanName())
        
        attrRef = attribute.getReference()

        # TODO: also put the attribute type in the attribute dictionary, so that it can be passed
        # on to the access-dictionary when accesses are resolved. That way, the resolution of
        # further accesses invocations

        aCommandList = RuleChecker.checkAttributeEntity(attribute)
        RuleChecker.generateAttributeInfo(aCommandList, attribute)
        attributeDictFiller.add(attribute)
        #attrDict = attributeDictFiller.attributeDictionary.addReference(attrRef)

def parseGlobalVars(globalVarDBD, namespaceContainmentChecker, classSelector):
	#global namespaceContainmentChecker
	for line in open(globalVarDBD, 'r'):
		gv = GlobalVariableEntity(line)
		gvRef = gv.getReference()
		
		sourceLoc = SourceLocation(gvRef.getSourceFile(),\
                                        gvRef.getLineNr(),\
                                        gvRef.getLineNr())
		namespaceName = namespaceContainmentChecker.getSurroundingNamespaceName(sourceLoc)
		
		gv.namespaceName = namespaceName
		gvRef = gv.getReference() # TODO: refactor reference as to be a true updateable instance,
                                  # in that case it would be sufficient to state
                                  # gv.setNamespaceName(namespaceName)


		# resolve the global variable type
		typeRef = gv.getTypeReference()
		
		if not(typeRef.isPrimitive()):
			potentialTargets = classSelector.selectClasses(typeRef.getCleanName(),\
                                                     gvRef.getSourceFile(), gvRef.getLineNr())
			nrOfTargets = len(potentialTargets)
			
			if ( nrOfTargets > 1 ):
				pass
				#log.warn("GlobalVariable-type-problem: ",nrOfTargets," classes with name \""\
				#         + typeRef.getReferencedName() + ".")
				typeRef.setResolvedName(typeRef.getCleanName())
			elif ( nrOfTargets == 0 ):
				pass
				#log.warn("GlobalVariable-type-problem: no class with name \"" \
				#        + typeRef.getReferencedName() + "\" known from file ",\
				#        gvRef.getSourceFile(),"@",gvRef.getLineNr(),".")
				typeRef.setResolvedName(typeRef.getCleanName())
			else:
				# get the data on the sole left-over method-type
				classReference = potentialTargets[0]
				
				typeRef.setResolvedName(classReference.getUniqueName())
				typeRef.setSourceFile(classReference.getSourceFile())
				typeRef.setLineNr(classReference.getLineNr())

				# TODO: fill in the global variable type template parameters
		else:
			typeRef.setResolvedName(typeRef.getCleanName())
		
		# extract this in a processing method.
		if typeRef.getReferencedName() != "":
			common.famix.cdifWriter.generateGlobalVarInfo(gv)
            

def parseAccess(acDb, accesDict, invEntityDict, attrDict, typedefDict, transInhDict, scope, referenceLinesRecognizedAsAccesses, output_file):
    # TODO this really, really needs to be refactored
    common.famix.cdifReader.buildAttributeCache(output_file)
    common.famix.cdifReader.buildGlobalVarCache(output_file)

    # build local var cache
    localVarCache = EntityCacheFactory.buildAndReturnLocalVarCacheFromDb(acDb)

    accessResolver = AlternativeAccessResolver(scope, transInhDict, typedefDict, invEntityDict, attrDict)

    lineNr = 0
    for line in open(acDb, "r"):
        if not isPotentialAccess(line):
            lineNr += 1
            continue

        access = AccessEntity(line)
        access.decomposeData()

        if accessResolver.resolve(access):
            accesDict.add(access.sourceFile, access.start, access.dst_class)
            aCommandList = RuleChecker.checkAccessEntity(access)

            if not(not aCommandList):
                RuleChecker.generateAccessInfo(aCommandList, access)
                referenceLinesRecognizedAsAccesses.append(lineNr)
        else:
            access.getReport().logIfFlawed()

        lineNr += 1

    return lineNr

## THIS CURRENTLY IS NOT WORTH THE PERFORMANCE COST
# Early differentiation between an access and an invocation
# Give-aways (is certainly not an access when):
#  -dst_type = line.split(" ")[2] == mi or cl (very rare):
#
# Illustrating example:
# # m_10MinRCMessage ud CAcqApplFluoTimeGuard CAcqApplFluoTimeGuard mi p 000104 AcqApplFluoTimeGuard.cpp;{} {CAcqApplGenBuzzerProxy &,CAcqCIControlItemManager &}
##
def isPotentialAccess(line):
    isPotentialAccess = True

    dst_type = line.split(";")[2]

    ##
    # ma = macro
    # t = type
    # e = constant
    # ec = enumeration
    # gv = globalVar
    # cl = class
    # con = ? but no method so it seems
    ##
    # ud and gv are potential accesses
    if dst_type in ['mi','fu','fd','ma','t','cl']:
        isPotentialAccess = False

    destParams = line.split("{")[1].split("}")[0]
    if destParams != "":
        isPotentialAccess = False

    return isPotentialAccess

##
# Parse invocations. These can be:
#     (a) function-to-function
#    (b) function-to-method
#    (c) method-to-function
#    (d) method-to-method
##
def parseInvocations(referencesFileName, accessDict, invokeableEntityDict, \
                        typedefDict, transIncludeDict, transInhDict,scope, \
                        referenceLinesRecognizedAsAccesses):
    resolver = AlternativeInvocationResolver(scope, transInhDict, typedefDict, \
                                            invokeableEntityDict, accessDict)
    lineNr = 0

    nrOfLinesProcessed = 0
    for line in open(referencesFileName, "r"):
        if not(lineNr in referenceLinesRecognizedAsAccesses) and isPotentialInvocation(line):
            nrOfLinesProcessed += 1
            inv = InvocationEntity(line, invokeableEntityDict)
            inv.decomposeData()
            #print "------"
            #print line,
            #print inv.dst_class+"|"+inv.dst_name+"|"+inv.dst_type+"|"+inv.actualDstParams+"|"+inv.sourceFile+"|"+inv.start
            #print inv.src_class+"|"+inv.src_name+"|"+inv.src_type+"|"+inv.actualSrcParams+"|"+inv.sourceFile+"|"+inv.start
            #print "-------"
            resolver.resolve(inv)

            if ( inv.isCompletelyResolved() ):
                aCommandList = RuleChecker.checkMethInvEntity(inv)
                RuleChecker.generateMethodInvocationInfo(aCommandList, inv)
            else:
                inv.getReport().logIfFlawed()

        lineNr += 1

    return nrOfLinesProcessed

## THIS CURRENTLY IS NOT WORTH THE PERFORMANCE COST
# Early differentiation between an invocation and an access
# Give-aways (is certainly not an invocation when):
#  -parameter or rhs-indication: cols[6] = w (SN sometimes wrongfully interprets a
#  method invocation as a reference passed as a parameter...
#  -coding conventions?!
#
# Illustrating example:
# # ACQAPPLTIMEGUARDSTATUS_INACTIVE ud CAcqApplFluoTimeGuard HandleEvent mi p 000259 AcqApplFluoTim
##
def isPotentialInvocation(line):
    isPotentialInvocation = True

    cols = line.split(";")
    src_type = cols[6]
    if src_type in ['w','ma']:
        isPotentialInvocation = False

    dst_type = cols[2]

    ##
    # ma = macro
    # t = type
    # e = constant
    # ec = enumeration
    # gv = globalVar
    # cl = class
    # con = ? but no method so it seems => perhaps new restriction
    ##
    if dst_type in ['ma','t','e','ec','gv','con','cl'] :
        isPotentialInvocation = False

    return isPotentialInvocation

def parseFormalParameters(miDb):
    input_file=open(miDb, 'r')
    for line in input_file:
        line = line.strip()
        cols = line.split(";")
        par_types, par_names = utils.retrieveFormalParameters(cols[6:])
        signature = utils.retrieveSignature(cols[6:])
        behEntName = ""

        # remove template parameters from method or function name
        if ( cols[1].startswith("<") and (">" in cols[1]) ):
            # cut of the part until and including the first > character
            cols[1]=cols[1][cols[1].index(">")+1:]

        if cols[0] != "#":
            behEntName = cols[0] + "." + cols[1] + signature[1]
        else : behEntName = cols[1] + signature[2]

        i = 0
        while i < len(par_names):
            common.famix.cdifWriter.generateFormalParInfo(par_names[i], par_types[i], behEntName, i+1)
            i = i + 1

    input_file.close()

def parseMacroUses(acDb,maDict):
    lineNr = 0

    nrOfLinesProcessed = 0
    for line in open(acDb, "r"):
        cols = line.split(";")
        src_type = cols[6]
        dst_type = cols[2]

        if "ma" == dst_type:
            nrOfLinesProcessed += 1
            #inv = InvocationEntity(line, invokeableEntityDict)
            #inv.decomposeData()
        lineNr += 1
    return nrOfLinesProcessed



# metrics and co

def parsePmcmetrics(pmcDb, invEntDict):
	from common.metrics  import	PmcMetricEntity
	
	timer = Timer("Parsing measurements")
	timer.start()
	nrOfMeasurements=0
	for line in open(pmcDb, "r"):
		pmcEnt = PmcMetricEntity(line)
		if not pmcEnt.isKnownEntity(invEntDict):
			continue
		
		uniqName = pmcEnt.invEntRef.getUniqueName()
		
		sLoc = SourceLocation(pmcEnt.filename, pmcEnt.lineNr, pmcEnt.lineNr)
		common.famix.cdifWriter.generateMetrics("CC", pmcEnt.metricCC, uniqName, sLoc)
		common.famix.cdifWriter.generateMetrics("NOS", pmcEnt.metricNOS, uniqName, sLoc)
		#common.famix.cdifWriter.generateMetrics("LOC", pmcEnt.metricLOC, uniqName, sLoc)
		common.famix.cdifWriter.generateMetrics("LOC", pmcEnt.metricNOS, uniqName, sLoc)
		nrOfMeasurements += 1

	timer.stop()
	timer.log(nrOfMeasurements)

log = Logger().get_instance()
log.set_loglevel(LOGLEVEL_DEBUG)
