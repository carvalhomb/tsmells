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

from log4py import Logger

import common
from common									import utils

from common.ResolveableInformation			import TypeReference
from cplusplus.data_types.NamespaceEntity 	import NamespaceEntityReference
from common.entity_references.EntityReferences 	import 	AccessReference,\
														EntityReference,\
														MethodEntityReference
from common.SourceLocation 					import SourceLocation


from java.resolver							import InvocationDestinationResolver
from java.javaLanguage						import isPrimitiveType,\
													isLibraryType,\
													typeToClass

def stringSignature(paramlist):
	if 0 == len(paramlist): return ""
	#signatureString = "("
	signatureString = ""
	for i in paramlist:
		signatureString += i
		signatureString += ","
	signatureString = signatureString[:len(signatureString)-1]
	return signatureString

class PackageEntity:

	##
	# Initialize a package entity given a line from the
	# SourceNavigator dbdump file (.packages).
	##
	def __init__(self, line):
		self.line = line
		cols = line.split(";")
		self.sourceFile = cols[0]
		self.lineNr = cols[1]
		self.name = cols[2].replace("package ", "").replace(".","::").strip()

	def getReference(self):
		reference = NamespaceEntityReference()
		reference.setName(self.name)
		sourceLocation = SourceLocation(self.sourceFile, \
									self.lineNr, \
									self.lineNr)
		reference.addSourceLocation(sourceLocation)
		return reference

def isPackageCandidate(line):
	""" remove non-package declaration lines due to script noise """
	line = line.strip()

	# e.g.: * This method is intented for package ...
	if "*" in line: return False
	if line.strip().startswith("*"): return False
	# e.g.: // ommit root package
	if line.strip().startswith("//"): return False
	# e.g.: /** Generate the name of item with package ...
	if line.startswith("/*"): return False
	if line.split(";") < 3: return False
	return True

class ImportEntity:
	pkgImpIndicator = "*"

	##
	# Initialize a package entity given a line from the
	# SourceNavigator dbdump file (.includes).
	# e.g.
	# java/awt/*;000031.007;gui/AddBuddy.java;31.17;0x0;{};{};{};{}
	##
	def __init__(self, line):
		cols = line.split(";")
		self.name = cols[0]
		self.sourceFile = cols[2]
		self.lineNr = cols[3].split(".")[0]

	def isClassImport(self):
		return not self.pkgImpIndicator in self.name

	def getIdentifier(self):
		""" returns class name for class import, pkg name for package import """
		# some cleaning first
		identifier = self.name
		identifier = identifier.replace(self.pkgImpIndicator, "")
		if identifier.endswith("/"):
			identifier = identifier[0:len(identifier)-1]
		identifier = identifier.replace("/",common.utils.getPkgSeparator())

		return identifier

##
# Encapsulation of an entity.
##
class Entity:
	##
	# Initialize an entity using a given line.
	# Remembers the given line and splits it in pieces.
	##
	def __init__(self, line):
		self.line = line
		self.cols = self.line.split(";")
		for i in range(0,len(self.cols)):
			curCol = self.cols[i].replace("{","").replace("}","")
			self.cols[i] = curCol

	#def getReference(self):
	#	reference = EntityReference()
	#	reference.setSourceFile(self.sourceFile)
	#	reference.setLineNr(self.start)
	#	return reference

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
#
# e.g. # Bar;000001.006;src/AbstractClass.cpp;1.9;0x0;{};{};{};{}
##
class ClassEntity(Entity):
	
	def __init__(self, line):
		"""Initialize a class entity given a line from the SN dbdump cl file"""
		Entity.__init__(self, line)	# entity will initalize self.line and self.cols
		self.name = self.cols[0]
		self.sourceLocation = None
		self.determineSourceLocation()	# will initialize self.soureFile and self.start
		self.template=""
		#self.checkForTemplate() # will initialize self.template
		self.abstract = False
		self.__resolveCharacteristics(self.cols[4])

	def __resolveCharacteristics(self, charCode):
		# TODO: complete
		self.abstract = False
		if "0x404" == charCode:
			self.abstract = True

	##
	# Retrieve the source-location of the entity, consisting of
	# -sourceFile
	# -start-position
	##
	def determineSourceLocation(self):
		sourceFile = self.cols[2]
		start = self.cols[3].split(".")[0]
		
		self.sourceLocation = SourceLocation(sourceFile, start, start)

	##
	# Remembers the template parameters of the class, and stores them in self.template.
	# The format of self.template does not include the brackets, e.g. "X".
	##
	#def checkForTemplate(self):
	#	tplName = self.cols[6].strip()
	#	tplName = tplName.replace("class ", "")
	#	tplName = tplName.replace("typename ", "")
	#	tplName = tplName.replace(" ", "")
	#	if "" != tplName:
	#		self.template = tplName.strip().lstrip("<").rstrip(">")

	def isAbstract(self):
		""" Verify whether the class is abstract. """
		return self.abstract

##
# Encapsulation of the data on an inheritance link between classes.
#
# Attributes:
# -- initialized in Entity constructor --
# self.line
# self.cols
# -- initialized in constructor --
# self.subclass - The name of the subclass
# self.superclass - The name of the superclass
# self.dst_sourceFile - The name of the file in which the superclass is resolved (if resolved)
# self.dst_lineNr - The line number at which the superclass is declared in the dst_sourceFile
# -- via decomposeVisibility --
# self.accessControlQualifier - the visibility ({public,private,protected}) of the superclass
# -- via determineSourceLocation --
# self.sourceFile - the name of the file at which the inheritance link is declared
# self.start - the line number at which the inheritance link is declared
##
class InheritanceEntity:
 	##
	# Initialize an entity using a given line.
	# Sample Entry:
	#	 Bar;Foo;000007.018;src/Ipublic.cpp;7.22;0x4;{};{};{};{}
	##
	def __init__(self, line):
		self.log = Logger().get_instance(self)
		cols = line.split(";")
		
		# TODO: check number of cols
		self.subclass = cols[0]
		# fix SN messing up fully qualified inheritance [class A extends pkg.B {}]
		self.superclass = cols[1].replace(".","::")
		sourceFile = cols[3]
		start = cols[4].split(".")[0]
		self.sourceLocation = SourceLocation(sourceFile, start, start)
		vis = cols[5].strip()
		self.accessControlQualifier = ""
		self.decomposeVisibility(vis)

		# to be filled in later
		self.package = ""
		self.superSourceLocation= None

	##
	# Determine the visibility of the superclass.
	#
	# Fills in self.accessControlQualifier
	##
	def decomposeVisibility(self,vis):
		if "0x1" == vis:
			self.accessControlQualifier = "private"
		elif "0x2" == vis:
			self.accessControlQualifier = "protected"
		elif "0x4" == vis:
			self.accessControlQualifier = "public"
		else:
			self.log.warn("WARNING: unknown inheritance visibility "+vis)

	def resolvePackage(self,pkgDict):
		self.package = pkgDict.getPackageForFile(self.sourceLocation.sourceFile)

	def resolveSuperClass(self,pkgDict,classDict):
		""" resolves the fully qualified super class """
		if self.superclass.find("::") != -1:
			# qualified SN deficiency
			superSourceFile = self.superclass.replace("::", "/") + ".java"
			superSourceFiles = classDict.getSourceLocations(self.superclass.split("::")[-1])
			for srcLoc in superSourceFiles:
				if srcLoc[0] == superSourceFile:
					self.superSourceLocation = SourceLocation(superSourceFile, srcLoc[1], srcLoc[1])
					return

		superSourceFiles = classDict.getSourceLocations(self.superclass)

		# std. scenario
		if 1 == len(superSourceFiles):
			superSourceFile = superSourceFiles[0][0]
			superStart = superSourceFiles[0][1]
			self.superSourceLocation = \
						SourceLocation(superSourceFile, superStart, superStart)
			pkg = pkgDict.getPackageForFile(superSourceFile)
			if "" != pkg:
				self.superclass = pkg + "::" + self.superclass
		elif 0 == len(superSourceFiles):
			self.log.warn(	"No source locations for superclass "+\
							self.superclass +\
							" of subclass " +\
							self.getQualSubclass())
			self.superSourceLocation = SourceLocation( "", "0", "0")
		else:
			self.log.warn(	"Multiple source locations for superclass "+\
							self.superclass+": ",superSourceFiles,\
							" of subclass " +\
							self.getQualSubclass())
			self.superSourceLocation = SourceLocation( "", "0", "0")
			# TODO check options, such as incorporating import information

	def getQualSubclass(self):
		if "" == self.package:
			return self.subclass
		else:
			return self.package + "::" + self.subclass

class MethodEntity:
	""" Initialize a mthd entity given a line from the SN methods table """

	##
	# Prot;addCont;000253.022;mess/Prot.java;253.32;0x14;
	#						{void};{String,String};{login,nick};{}
	##
	def __init__(self, line):
		self.log = Logger().get_instance(self)
		self.cols = line.split(";")
		self.owner = self.cols[0]
		self.name = self.cols[1]+"("+utils.cleanCol(self.cols[7])+")"

		sourceFile = self.cols[3]
		lineNr = self.cols[4].split(".")[0]
		self.sourceLocation = SourceLocation(sourceFile, lineNr, lineNr)

		self.isConstructor = False
		# TODO: parse visibility codes
		self.accessControlQualifier = "public"
		# TODO: static?
		self.hasClassScope = False
		# TODO: abstract? Interface?
		self.isAbstract = False
		params = self.cols[7]
		if "{}" == params:
			self.parameters = []
			self.parameterNames = []
		else:
			self.parameters = utils.cleanCol(params).split(",")
			self.parameterNames = utils.cleanCol(self.cols[8]).split(",")

		# to be resolved later
		self.package = None
		self.retType = None
		self.parentType = None
		# scope, visibility, abstract
		self.__resolveCharacteristics(self.cols[5])

	def __resolveCharacteristics(self, charCode):
		# todo: complete possibilities
		if "0x1" == charCode:
			self.accessControlQualifier = "private"
			self.hasClassScope = False
			self.isAbstract = False
		if "0xc" == charCode:
			self.accessControlQualifier = "public"
			self.hasClassScope = True
			self.isAbstract = False
		if "0x14" == charCode:
			self.accessControlQualifier = "public"
			self.hasClassScope = False
			self.isAbstract = True

	def resolveReturnType(self):
		self.retType = TypeReference()
		self.retType.resolvedName = utils.cleanCol(self.cols[6])
		self.retType.referencedName = self.retType.resolvedName

	def resolveParentType(self,classDict):
		parentType = TypeReference()
		parentType.resolvedName = self.owner

		if "" == self.package:
			parentType.referencedName = self.owner
		else:
			parentType.referencedName = self.package+"::"+self.owner
		srcLocs = classDict.getSourceLocations(self.owner)
		if 1 == len(srcLocs):
			parentType.sourceFile = srcLocs[0][0]
			parentType.lineNr = srcLocs[0][1]
		else:
			self.log.warn("multiple source locations for class",\
					parentType.sourceFile)
		self.parentType = parentType

	def baseName(self):
		""" returns unqualified method name without parameters """
		return self.name.split("(")[0]

	def getSignature(self):
		""" returns method signature """
		return stringSignature(self.parameters)

	def getQualifiedName(self):
		name = self.owner+"."+self.name
		if "" == self.package:
			return name
		else:
			return self.package+"::"+name

	def getSourceLocation(self):
		return self.sourceLocation.sourceFile+":"+self.sourceLocation.start

	def getReference(self):
		""" creates and returns a language independent method reference """
		mtdRef = MethodEntityReference()
		mtdRef.setName(self.baseName())
		mtdRef.setFormalParameters("("+self.getSignature()+")")
		mtdRef.parentReference = self.parentType
		mtdRef.typeReference = self.retType
		mtdRef.setModifiers(self.accessControlQualifier)
		mtdRef.setIsAbstract(self.isAbstract)
		mtdRef.setClassScope(self.hasClassScope)
		return mtdRef

class AttributeEntity:

	##
	#
	# e.g. AContact;fLogin;000040.016;messenger/AContact.java;40.22;0x1;{};{};{};{}
	##
	def __init__(self, line):
		""" Initialize a method entity given a line from the SourceNavigator \
			dbdump file (.methods). """
		self.log = Logger().get_instance(self)
		cols = line.split(";")
		self.owner = cols[0]
		self.name = cols[1]
		self.sourceFile = cols[3]
		self.lineNr = cols[4].split(".")[0]
		self.visibility = "public"
		# to be configured with additional information
		self.package = None
		self.attrType = None
		self.parentType = None

	def resolvePackage(self,pkgDict):
		self.package = pkgDict.getPackageForFile(self.sourceFile)

	def __locateAttributeType(self, pkgDict, classDict, attrType):
		""" identifies the source location of the attributes' type def."""
		if isPrimitiveType(attrType.resolvedName):
			# it is a primitive type, no source location for them
			attrType.resolvedName = ""
			attrType.sourceFile = ""
			attrType.lineNr = "0"
			self.attrType = attrType
			return

		# it is a class, but does the class dictionary know it?
		srcLocs = classDict.getSourceLocations(attrType.resolvedName)
		if 1 == len(srcLocs):
			dstLoc = srcLocs[0][0]
			dstLineNr = srcLocs[0][1]
			pkgName = pkgDict.getPackageForFile(dstLoc)
			attrType.resolvedName = \
					utils.composeQualifiedName(pkgName,attrType.resolvedName)
			attrType.sourceFile = dstLoc
			attrType.lineNr = dstLineNr
		elif 0 == len(srcLocs):
			# is it a library type? don't log about them
			if not isLibraryType(attrType.resolvedName):
				self.log.warn(	"no source locations for attribute type: "+\
								attrType.resolvedName+" of attribute "+\
								self.owner+"."+self.name)
			attrType.sourceFile = ""
			attrType.lineNr = "0"
		else:
			self.log.warn(	"multiple source locations (",srcLocs,\
							") for attribute type: "+\
							attrType.resolvedName+" of attribute "+self.owner+\
							"."+self.name)
			attrType.sourceFile = ""
			attrType.lineNr = "0"
		self.attrType = attrType

	def resolveAttrType(self, pkgDict, classDict, attrTypeDict):
		""" resolves attribute type information such as
			qualified name and source location """
		attrType = TypeReference()

		qualAttrName = self.getQualAttrName()
		if attrTypeDict.has_key(qualAttrName):
			attrType.referencedName = attrTypeDict[qualAttrName]
		else:
			attrType.referencedName = ""
		attrType.resolvedName = typeToClass(attrType.referencedName)
		self.__locateAttributeType(pkgDict, classDict, attrType)

	def resolveParentType(self,classDict):
		parentType = TypeReference()
		parentType.resolvedName = self.owner
		parentType.referencedName = \
						utils.composeQualifiedName(self.package,self.owner)
		self.parentType = parentType

		# TODO package!
		className = utils.spliceClass(	self.parentType.referencedName, \
										utils.qualSeparator)
		srcLocs = classDict.getSourceLocations(className)
		if 1 == len(srcLocs):
			dstLoc = srcLocs[0][0]
			dstLineNr = srcLocs[0][1]
			self.parentType.sourceFile = dstLoc
			self.parentType.lineNr = dstLineNr
			#pkgName = pkgDict.getPackageForFile(dstLoc)
			#attrType.resolvedName = \
			#		utils.composeQualifiedName(pkgName,attrType.resolvedName)
			#attrType.sourceFile = dstLoc
			#attrType.lineNr = dstLineNr
		elif 0 == len(srcLocs):
			self.log.warn(	"no source locations for attribute owner: "+\
							self.parentType.referencedName+" of attribute "+\
							self.owner+"."+self.name)
			self.parentType.sourceFile = ""
			self.parentType.lineNr = "0"
		else:
			self.log.warn(	"multiple source locations for attribute owner: "+\
							self.parentType.referencedName+" of attribute "+\
							self.owner+"."+self.name)
			self.parentType.sourceFile = ""
			self.parentType.lineNr = "0"

	def getQualAttrName(self):
		return utils.composeQualifiedMethodName(self.package, self.owner,self.name)

class MethodInvocationEntity(Entity):
	uknDstMethods = []
	uknDstMethLoc = []
	uknDstTypes = []

	##
	# MSNPacket;getContent;mi;MSNProtocol;sendPacket;mi;p;000183;
	#								messenger/MSN/MSNProtocol.java;{};{IPacket}
	##
	def __init__(self, line):
		Entity.__init__(self, line)	# entity will initalize self.line and self.cols
		self.log = Logger().get_instance(self)
		self.cols = line.split(";")
		self.dst_class = self.cols[0]
		self.dst_name = self.cols[1]
		self.src_class = self.cols[3]
		self.src_name = self.cols[4]
		self.sourceFile = self.cols[8]
		self.lineNr = utils.removeUpfrontZeroes(self.cols[7])
		self.visibility = "public"
		self.src_param = utils.cleanCol(self.cols[10].strip()).split(",")
		self.dst_param = utils.cleanCol(self.cols[9]).split(",")
		# to be resolved later
		self.package = ""
		self.dst_package = ""
		self.srcLoc = ""
		self.srcLineNr = "0"
		self.dstLoc = ""
		self.dstLineNr = "0"
		#self.log.warn(self.cols[3]+"."+self.cols[4]+" -> "+self.cols[0]+"."+self.cols[1])

	def resolvePackage(self,pkgDict):
		self.package = pkgDict.getPackageForFile(self.sourceFile)
		return True

	def resolveOwner(self,mtdSrcDict):
		# for source method
		srcMtdSourceFiles = mtdSrcDict.getSourceLocFor(self.getSrcMethUniqName())
		# std. scenario
		if 1 == len(srcMtdSourceFiles):
			srcSourceFile = srcMtdSourceFiles[0].split(":")[0]
			srcStart = srcMtdSourceFiles[0].split(":")[1]

			self.srcLoc = srcSourceFile
			self.srcLineNr = srcStart
			return True
		elif 0 == len(srcMtdSourceFiles):
			self.log.warn(	"No sourceLocation for owner of method "+\
							self.getSrcMethUniqName())
			return False
		else:
			self.log.warn(	"Multiple sourceLocations for owner of method "+\
							self.getSrcMethUniqName()+": ",srcMtdSourceFiles)
			return False

	def resolveDestination(self,mtdDict,mtdSrcDict,classDict,pkgDict,impDict,inhDict):
		#self.log.warn("------------------------------------")
		#self.log.warn(self.cols[3]+"."+self.cols[4]+" -> "+self.cols[0]+"."+self.cols[1])

		dtr = InvocationDestinationResolver(mtdSrcDict,classDict,pkgDict,impDict,self)
		# destination type, destination method and destination location must
		# be known uniquely
		return 	dtr.resolveInvocationDstType() and \
				dtr.resolveDstMtd(mtdDict,inhDict) and \
				dtr.resolveDstMtdLoc(self.getDstMethUniqName())


	#def checkMethodsExist(self,inhDict, methDict):
	#	""" checks whether both src and dst methods are known to the model """
	#	return self.srcMethodExists(methDict) and self.dstMethodExists(inhDict, methDict)

	#def srcMethodExists(self, methDict):
	#	""" checks whether the src method is known to the model. \
	#		Haven't seen this failing up till now """
	#	srcSign = self.getSrcSignature()
	#	if not methDict.dictHasMethodInPackage(self.package,self.src_class,srcSign):
	#		srcMeth = self.getSrcMethUniqName()
	#		# library methods do not exist in the model, we don't report
	#		if self.__invokesLibraryMethod(): pass
	#		else: self.log.warn("Source method "+srcMeth+" not known")
	#		return False
	#	return True

	# Destination method resolution strategy methods





	def isMethodInvocation(self):
		if "mi" in self.cols[2] and "mi" in self.cols[5]: return True
		return False

	# all kind of getters

	def getOwnerUniqueName(self):
		if "" == self.package:
			return self.src_class
		else:
			return self.package + "::" + self.src_class

	def getSrcMethUniqName(self):
		srcMeth = ""
		if "" == self.package:
			srcMeth = self.getOwnerUniqueName()+"."+self.getSrcSignature()
		else:
			srcMeth = 	self.getOwnerUniqueName()+"."+\
						self.getSrcSignature()
		return srcMeth

	def getClassQualMeth(self):
		""" get method only qualified by its class name (not by package)  """
		classQualMeth = self.dst_class+"."+self.dst_name+\
						"("+stringSignature(self.dst_param)+")"
		return classQualMeth

	def getDstMethUniqName(self):
		classQualMeth = self.getClassQualMeth()
		if "" == self.dst_package:
			return classQualMeth
		else:
			return self.dst_package+"::"+classQualMeth

	def getDstBase(self):
		if "" == self.dst_package:
			return self.dst_class
		else:
			return self.dst_package+"::"+self.dst_class

	def getSrcSignature(self):
		return self.src_name + "(" + stringSignature(self.src_param) + ")"

	def getDstSignature(self):
		return self.dst_name + "(" + stringSignature(self.dst_param) + ")"

class AttributeReferenceEntity:
	##
	# ABytePacket;fContent;iv;ABytePacket;getContentByte;mi;r;000054;
	#						messenger/ABytePacket.java;{};{}
	##

	def __init__(self, line):
		self.log = Logger().get_instance(self)
		self.cols = line.split(";")
		self.dst_class = self.cols[0]
		self.dst_name = self.cols[1]
		self.src_class = self.cols[3]
		self.src_name = self.cols[4]
		self.acc_type = self.cols[5]
		self.sourceFile = self.cols[8]
		self.lineNr = utils.removeUpfrontZeroes(self.cols[7])
		self.src_param = utils.cleanCol(self.cols[10].strip()).split(",")
		self.package = ""
		self.srcSourceLocation = SourceLocation("","0","0")
		self.dstSourceLocation = SourceLocation("","0","0")
		
	def resolvePackage(self,pkgDict,classDict, mtdpfDict):	
		#srcSourceFiles = classDict.getSourceLocations(self.src_class)
		# std. scenario
		#if 1 == len(srcSourceFiles):
		#	srcSourceFile = srcSourceFiles[0][0]
		#	srcStart = srcSourceFiles[0][1]
		#	self.srcSourceLocation = SourceLocation(srcSourceFile, srcStart, srcStart)
		#	pkg = pkgDict.getPackageForFile(self.sourceFile)
		#	if pkg: self.src_class = pkg + "::" + self.src_class
		#elif 0 == len(srcSourceFiles):
		#	self.log.warn(	"No sourceLocation for attribute owner: "+\
		#					self.src_class)
		#else:
		#	self.log.warn("Multiple sourceLocations for attribute owner: "+\
		#					self.src_class+": ",srcSourceFiles)
		
		resolvMtd, srcStart = mtdpfDict.getEnclosing(self.sourceFile, self.lineNr)
		if not resolvMtd:
			self.log.warn("Could not find the method containing "+self.lineNr+" in "+self.sourceFile)
			return
		
		
		if not self.src_name in resolvMtd:
			# might use class location for this
			self.log.warn("Resolved enclosing method differs SN dump. [SN:" +\
						  self.src_name + "<> em:" + resolvMtd + "]")
		elif not self.src_class in resolvMtd: # fully qualified
			self.log.warn("Enclosed class differs from SN dump [SN:" +\
						  self.src_class + "<> ec:" + resolvMtd + "]")
		else:
			self.srcSourceLocation = SourceLocation(self.sourceFile, str(srcStart), str(srcStart))
			pkg = pkgDict.getPackageForFile(self.sourceFile)
			if pkg: 
				self.src_class = pkg + "::" + self.src_class
		
		# for destination
		dstSrcFiles = classDict.getSourceLocations(self.dst_class)
		if 1 == len(dstSrcFiles):
			# std. scenario
			dstSourceFile = dstSrcFiles[0][0]
			dstStart = dstSrcFiles[0][1]
			self.dstSourceLocation = SourceLocation(dstSourceFile, dstStart, dstStart)
			pkg = pkgDict.getPackageForFile(dstSourceFile)
			if pkg: self.dst_class =  pkg + "::" + self.dst_class
			
		else:
			# todo check options, such as incorporating import information
			self.log.warn("Could not resolve destination:" + self.dst_class)
			#print "dstSrcFiles: ",self.dst_class,len(dstSrcFiles), dstSrcFiles

	def isAttributeReference(self):
		arDstType = self.cols[2]
		return "iv" in arDstType

	def getReference(self):
		aR = AccessReference()
		aR.src_class = self.src_class
		aR.src_name = self.src_name
		aR.formalSrcParams = stringSignature(self.src_param)
		aR.dst_owner = self.dst_class
		aR.dst_name = self.dst_name
		aR.dst_class = self.dst_class
		aR.acc_type = self.acc_type
		aR.sourceFile = self.sourceFile
		aR.start = self.lineNr
		aR.src_sourceFile = self.srcSourceLocation.sourceFile
		aR.src_start = self.srcSourceLocation.start
		aR.dst_sourceFile = self.dstSourceLocation.sourceFile
		aR.dst_start = self.dstSourceLocation.start
		aR.dst_type = "" # compatibility with C++, where there is Gv access as well
		return aR

class AnnotationEntity:
	"""Parses annotation db line and tries to associate with 
		class or method in the neighbourhood"""
	##
	# /tmp/trunk/src/test/pacman/PacmanTest.java:30:	@Before public void setup() {
	##
	def __init__(self, line):
		self.log = Logger().get_instance(self)
		cols = line.split(":")
		self.sourceFile = cols[0].replace("./", "")
		self.lineNr = cols[1]
		self.content = cols[2].lstrip().split(" ")[0].strip() # typically some code behind it
		self.owner = ""
		
	def isValidAnnotation(self):
		if self.content.startswith("*") or \
			self.content.startswith("//") or \
			self.content.startswith("/*"): 
			return False
		return True
	
	def __getLocation(self):
		return self.sourceFile+":"+self.lineNr
			
	def findOwner(self, pkgDict, classDict, mtdpfDict, attrDict):
		#uniqueName = ""
		if self.__findClassOwner(pkgDict, classDict): 
			#print "classowner: ",self.owner
			return True
		elif self.__findMethodOwner(mtdpfDict): 
			#print "methodowner: ",self.owner
			return True
		# TODO annotations for attributes
		else:
			self.log.warn("Can not find owner for annotation: "+self.content+\
					 " on location: "+self.__getLocation())
		return False
	
	def __findClassOwner(self,pkgDict,classDict):
		resolved = False
		locClDict = classDict.createLocationBasedDictionary()
		
		loc = self.__getLocation()
		prevLoc = self.sourceFile+":"+str(int(self.lineNr)+1)
		
		# annotation typically resides on the line of the class itself
		# or above
		if loc in locClDict:
			self.owner = locClDict[loc][0]
			resolved = True
		elif prevLoc in locClDict:
			self.owner = locClDict[prevLoc][0]
			resolved = True
		
		if resolved:
			pkg = pkgDict.getPackageForFile(self.sourceFile)
			if pkg: 
				self.owner = pkg + "::" + self.owner
			return True
		else:
			return False
	
	def __findMethodOwner(self,mtdpfDict):
		#print mtdpfDict.dict.keys()
		#print self.sourceFile
		if not mtdpfDict.hasKey(self.sourceFile): return False
		
		mtdInSf = mtdpfDict.dict[self.sourceFile]
		for name, mtdLineNr in mtdInSf:
			annLineNr = int(self.lineNr)
			if 	mtdLineNr == annLineNr or \
				mtdLineNr == annLineNr+1:
				self.owner = name
				return True
		return False
	