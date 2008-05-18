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
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

################################################################################
# Abbreviations used:
################################################################################
# attr: attribute
# cl: class
# db: source navigator dumped database file
# dst: destination
# inf: input file
# inh: inheritance
# imp: import
# mtd: method
# pkg: package
# qual: qualified
# ref: reference
# src: source
# uniq: unique (name)
# dict: dictionary

from log4py 							import Logger

import common
from common 							import famix

from java.javaEntities 					import 	PackageEntity, \
												ImportEntity, \
												InheritanceEntity, \
												ClassEntity, \
												MethodEntity, \
												AttributeEntity, \
												MethodInvocationEntity, \
												AttributeReferenceEntity, \
												AnnotationEntity,\
												isPackageCandidate, \
												stringSignature

from java.resolver.InsaneInvocationResolver import \
										InsaneInvocationResolver

################################################################################
# famix entity parsing functions
################################################################################

# parse namespace table and generate correct famix.
def parsePackages(pkgDb, pkgDict):
	inf = open(pkgDb, 'r')
	pkgCache=[]
	for line in inf:
		pkg = PackageEntity(line)
		if not isPackageCandidate(line): continue
		pkgDict.add(pkg.sourceFile, pkg.name)
		if pkg.name in pkgCache: continue
		common.famix.cdifWriter.generatePackageInfo(pkg.getReference())
		pkgCache.append(pkg.name)
	inf.close()

# parse class table from snavigator db and generate correct famix.
def parseClasses(clDb, pkgDict, classDict):
	for line in open(clDb, 'r'):
		cl = ClassEntity(line)
		namespName = pkgDict.getPackageForFile(cl.sourceLocation.sourceFile)
		sourceFile = cl.sourceLocation.sourceFile
		start = cl.sourceLocation.start
		famix.cdifWriter.generateClassInfo(cl.name, # class name
					namespName, # package
					cl.isAbstract(),
					sourceFile, start, start
										)
		classData = [ None, namespName ]
		classDict.add(	cl.name, cl.sourceLocation.sourceFile, \
						cl.sourceLocation.start, classData)

# introduces stub class
def appendClassInfo(clName, sourceFile, start, classDict):
	common.famix.cdifWriter.generateClassInfo(clName, # class name
					"", # package
					False,
					"", 0, 0 # sourceFile, start, end
										)
	classData = [ "stub", "" ]
	classDict.add(clName, sourceFile, start, classData)

# builds up a cache of imports, for type resolution later on
def parseImports(impDb, impDict):
	for line in open(impDb, 'r'):
		imp = ImportEntity(line)
		if imp.isClassImport():
			impDict.addClass(imp.sourceFile, imp.getIdentifier())
		else:
			impDict.addPackage(imp.sourceFile, imp.getIdentifier())

# parse inheritance table from snavigator db and generate correct famix
def parseInheritance(inhDb, pkgDict, inhDict, classDict):
	for line in open(inhDb, 'r'):
		inh = InheritanceEntity(line)
		inh.resolveSuperClass(pkgDict, classDict)

		# if superclass and/or subclass don't exist, create them as stubs
		# update: no, let's ignore them. If you want TestCase, then make a stub
		# for it.
		if not classDict.hasKey(inh.subclass):
			#snav_appendClassInfo(	inh.subclass, inh.sourceLocation.sourceFile, \
			#						inh.sourceLocation.start, classDict)
			pass
		else:
			inh.resolvePackage(pkgDict)
			#nameSp = pkgDict.getPackageForFile(inh.sourceLocation.sourceFile)

		if not classDict.hasKey(inh.superclass):
			#snav_appendClassInfo(	inh.superclass, inh.sourceLocation.sourceFile, \
			#						inh.sourceLocation.start, classDict)
			pass

		# std scenario
		common.famix.cdifWriter.generateInheritanceInfo(	inh.subclass, \
											inh.superclass, \
											inh.package, \
											inh.accessControlQualifier, \
											inh.sourceLocation.sourceFile, \
											inh.sourceLocation.start, \
											inh.sourceLocation.start, \
											inh.superSourceLocation.sourceFile, \
											inh.superSourceLocation.start)
		#print inh.subclass, inh.getQualSubclass()
		if inhDict.has_key(inh.getQualSubclass()):
			inhDict[inh.getQualSubclass()].append(inh.superclass)
		else:
			inhDict[inh.getQualSubclass()] = [ inh.superclass ]

def parseMethods(miDb, pkgDict, classDict, methDict, methSrcDict):
	""" parse method table from snavigator db and generate correct famix """
	# Prot addCont 000253.022 mess/Prot.java;253.32 0x14
	#						{void} {String,String} {login,nick} {}
	for line in open(miDb, 'r'):
		mtd = MethodEntity(line)
		mtd.package = pkgDict.getPackageForFile(mtd.sourceLocation.sourceFile)
		if methDict.add(mtd.package, mtd.owner, mtd.name):
			mtd.resolveReturnType()
			mtd.resolveParentType(classDict)
			methSrcDict.add(mtd.getQualifiedName(), mtd.getSourceLocation())

			mtdRef = mtd.getReference()
			common.famix.cdifWriter.generateMethodDeclarationInfo(\
					mtd.sourceLocation, mtdRef, \
					mtdRef.parentReference, mtdRef.typeReference)
			common.famix.cdifWriter.generateMethodDefinitionInfo\
					(mtd.sourceLocation, mtd.sourceLocation, mtdRef, \
					mtdRef.parentReference, mtdRef.typeReference, 1)

			for i in range(len(mtd.parameters)):
				common.famix.cdifWriter.generateFormalParInfo(	\
											mtd.parameterNames[i], \
											mtd.parameters[i], \
											mtd.package+"::"+mtd.name, i+1)
		else:
			# if meth.add returns True it means the method already exists
			log.warn(	"method "+\
						mtd.package+"::"+mtd.owner+"."+mtd.name+\
						" already exists.")
			continue

def parseAttributes(atDb, attrDict, attrTypeDict, classDict, pkgDict):
	""" parse attribute table from snavigator db and generate correct famix """
	for line in open(atDb, 'r'):
		attr = AttributeEntity(line)
		attr.resolvePackage(pkgDict)
		attr.resolveAttrType(pkgDict, classDict, attrTypeDict)
		attr.resolveParentType(classDict)

		# std scenario
		if attrDict.add(attr.name, attr.owner):
			common.famix.cdifWriter.generateFieldInfo(attr.name, attr.parentType, \
										attr.attrType, attr.visibility, \
										False, attr.sourceFile, \
										attr.lineNr, attr.lineNr)
		else:
			# if attrDict.add returns True it means the method already exists
			log.warn("attribute "+attr.package+"::"+attr.owner+"."+attr.name+\
						" already exists.")
			continue

def parseAttributeReferences(acDb, pkgDict, classDict, mtdpfDict):
	""" generates famix accesses to attributes from SN ref db """
	for line in open(acDb, 'r'):
		ar = AttributeReferenceEntity(line)
		if not ar.isAttributeReference(): continue
		ar.resolvePackage(pkgDict, classDict, mtdpfDict)
		ac = ar.getReference()
		common.famix.cdifWriter.generateAccess(ac)

def parseMethodInvocations(miDb, pkgDict, impDict, inhDict, classDict, methDict, mtdSrcDict):
	""" generates famix method invocation entities from SN ref db """

	# invocation cache for the insane additional method invocation resolver
	miList = []
	for line in open(miDb, 'r'):
		inv = MethodInvocationEntity(line)
		# let's rule out all reasons not to generate an entity
		if not inv.isMethodInvocation(): continue
		if not inv.resolvePackage(pkgDict): continue
		if not inv.resolveOwner(mtdSrcDict): continue
		if not inv.resolveDestination(methDict, mtdSrcDict, classDict, pkgDict, impDict, inhDict): continue

		# we log in inv if src or dst method does not exist
		#if inv.dstMethodExists(inhDict, methDict):
		common.famix.cdifWriter.generateJavaInvocationInfo(\
					inv.getOwnerUniqueName(), inv.src_name, \
					stringSignature(inv.src_param), \
					inv.getDstBase(), inv.dst_name, \
					stringSignature(inv.dst_param), \
					inv.sourceFile, inv.lineNr, inv.lineNr, \
					inv.srcLoc, inv.srcLineNr, \
					inv.dstLoc, inv.dstLineNr)

		# invocation cache for the insane additional method invocation resolver
		miList.append([[inv.getOwnerUniqueName(), inv.src_name, \
					stringSignature(inv.src_param), \
					inv.getDstBase(), inv.dst_name, \
					stringSignature(inv.dst_param), \
					inv.sourceFile, inv.lineNr, \
					inv.srcLoc, inv.srcLineNr, \
					inv.dstLoc, inv.dstLineNr]])
	return miList

# Highly insane additional invocation resolver
def parseAdditionalMethodInvocations(iDb, pkgDict, impDict, inhDict, \
									 classDict, methDict, mtdSrcDict, miList, mtdpfDict):
	""" generates famix method invocation entities from grep on '(' :) """
	for line in open(iDb, 'r'):
		sourceFile = line.split(":")[0].replace("./", "")
		lineNr = line.split(":")[1]
		content = line.split(":")[2].strip()
		# multiple invocations per line possible
		for i in content.split("("):
			#print sourceFile+":"+lineNr+" "+content
			inv = InsaneInvocationResolver(pkgDict, impDict, inhDict, \
					classDict, methDict, mtdSrcDict, miList, mtdpfDict, \
					sourceFile, lineNr, i, content)
			if inv.explore():
				#print inv.getOwnerUniqueName(), inv.src_name, \
				#	inv.getDstBase(), inv.dst_name, \
				#	inv.dst_param, \
				#	inv.sourceFile, inv.lineNr, inv.lineNr, \
				#	inv.srcLoc, inv.srcLineNr, \
				#	inv.dstLoc, inv.dstLineNr

				common.famix.cdifWriter.generateJavaInvocationInfo(\
					inv.getOwnerUniqueName(), inv.src_name, \
					inv.src_param, \
					inv.getDstBase(), inv.dst_name, \
					inv.dst_param, \
					inv.sourceFile, inv.lineNr, inv.lineNr, \
					inv.srcLoc, inv.srcLineNr, \
					inv.dstLoc, inv.dstLineNr)

def parseAnnotations(annDbLoc,pkgDict,classDict,methDict,attrDict):
	for line in open(annDbLoc, 'r'):
		
		annEnt = AnnotationEntity(line)
		
		if not annEnt.isValidAnnotation(): continue
		
		if annEnt.findOwner(pkgDict,classDict,methDict,attrDict):
			common.famix.cdifWriter.generateAnnotation(\
					annEnt.owner,\
					annEnt.content,\
					annEnt.sourceFile,\
					annEnt.lineNr)							   
		else:
			sLoc = annEnt.sourceFile+"@"+annEnt.lineNr
			log.warn("Can not find owner for annotation:",annEnt.content,"@",sLoc)
		


log = Logger().get_instance()
