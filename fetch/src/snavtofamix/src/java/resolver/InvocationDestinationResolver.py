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
from common									import SNDeficiencies
from java									import javaLanguage
import java

class InvocationDestinationResolver:

	def __init__(self,mtdSrcDict,classDict,pkgDict,impDict,mtdInv):
		self.mtdSrcDict = mtdSrcDict
		self.classDict = classDict
		self.pkgDict = pkgDict
		self.impDict = impDict
		self.mtdInv = mtdInv
		self.log = Logger().get_instance(self)

	def resolveInvocationDstType(self):
		if self.mtdInv.dst_class in self.mtdInv.uknDstTypes: return
		isTypeResolved = False

		srcLocs = self.classDict.getSourceLocations(self.mtdInv.dst_class)
		if 1 == len(srcLocs): # best case
			dstTypeLoc = srcLocs[0][0]
			self.__resolveSingleType(dstTypeLoc)
			isTypeResolved = True
		elif 0 == len(srcLocs): # worst case, no solution
			self.__resolveNoType()
			isTypeResolved = False
		else: # digg deeper (e.g. imports)
			isTypeResolved = self.__resolveMultipleTypes(srcLocs)
		return isTypeResolved # look, I wrote a method with a single exit point!

	def __resolveSingleType(self, dstTypeLoc):
		# resolved as there is only one possible location
		pkgName = self.pkgDict.getPackageForFile(dstTypeLoc)
		self.mtdInv.dst_package = pkgName

	def __resolveNoType(self):
		# can't resolve, there is not a single possible location
		self.log.warn("URMI (MITYP): Can't find srcLoc for dst type: "+self.mtdInv.dst_class)
		self.mtdInv.uknDstTypes.append(self.mtdInv.dst_class)

	def __resolveMultipleTypes(self, srcLocs):
		# There are multiple locations, we should take packaging and
		# imports into account now
		for dstLoc in srcLocs:
			# we traverse the list of possible destination source files
			i = dstLoc[0]
			pkgName = self.pkgDict.getPackageForFile(i)
			uniqName = pkgName + common.utils.getPkgSeparator() + self.mtdInv.dst_class

			# We assume that there are no two suitable imports, and that
			# as soon as we find a matching solution, we take it, either
			# because the classes of source and destination match,
			# because the packages of the source file and the possible
			# destination file match or because there is a matching import
			# in the source file with the possible destination.
			#
			# Threats:
			# - we can wrongly conclude that matching class names means that
			#   source and destination are the same
			if self.mtdInv.src_class == self.mtdInv.dst_class:
				# STR1: source and destination class have same name
				self.mtdInv.dst_package = self.mtdInv.package
				return True
			if self.mtdInv.package == pkgName:
				# STR2: source and destination reside in the same package
				self.mtdInv.dst_package = self.mtdInv.package
				return True
			if not self.impDict.hasKey(self.mtdInv.srcLoc):
				# can't go to STR3 if info not present in impDict
				continue
			if self.impDict.hasImportFor(self.mtdInv.srcLoc, uniqName):
				# STR3: we found a destination source file with matching
				# import in the source source file.
				self.mtdInv.dst_package = pkgName
				return True
			# no solution yet, proceed to the next
		# none of the resolution strategies worked ...
		self.log.warn(	"URMI (MITYP): none of multiple locations for dst class matched: "+\
							self.mtdInv.dst_class+":", \
							srcLocs	)
		return False

	def resolveDstMtd(self,mtdDict,inhDict):
		""" checks whether the dst method is known to the model. If not,\
			try a couple of resolution strategies before giving up """
		exists = False
		dstSign = self.mtdInv.getDstSignature()
		dstMeth = self.mtdInv.getDstMethUniqName()
		if "assertEquals" in dstMeth:
			return False # let insane handle this, does a better job then SN

		# we already went through the resolution strategies. Give up
		if dstMeth in self.mtdInv.uknDstMethods: return False

		if not mtdDict.dictHasMethodInPackage(self.mtdInv.dst_package,self.mtdInv.dst_class,dstSign):
			# Uh oh ... dst method is not known to the model...
			# Therefore, we try a couple of resolution strategies.
			signature = java.javaEntities.stringSignature(self.mtdInv.dst_param)
			# 1) is it a library method? won't not exist, nor do we report
			if self.__invokesLibraryMethod(): pass
			# 2) does it qualify for the SN lost parameter syndrome?
			elif self.__resolvedDstViaSNDeficiencyResolution(mtdDict,signature):
				exists = True
			# 3) Does a superclass have the method? -> call on inherited method
			elif self.superClassIsMethodOwner(inhDict, mtdDict, self.mtdInv.getDstBase()):
				exists = True
			# 4) is there a unique method with the same name (not parameters)
			elif self.isUniqueMethodName(mtdDict,dstSign):
				# TODO : marge with step 2?
				# fails for byteToHex because there is byteToHex(byte) and byteToHex(byte[])
				exists = True
			# 5) elif superclass AND SN Deficiency
			elif self.superClassIsMethodOwnerWithSNDef(inhDict, mtdDict, self.mtdInv.getDstBase(),signature):
				exists = True
			# e.g. URMI: No sourceLocation for dst method messenger::Yahoo::YahooCLI_0015Packet.setBody(String)
			# setBody belongs to YahooPackage (superclass of) AND needs a String parameter
			else:
				# We don't know ... . Put in ignore list
				self.log.warn(	"URMI (MIMTD): Unknown Destination Method "+\
							dstMeth)
				self.mtdInv.uknDstMethods.append(dstMeth)
				exists = False
		else:
			exists = True
		return exists

	def __invokesLibraryMethod(self):
		return javaLanguage.isJDKMethod(self.mtdInv.getClassQualMeth())

	def __resolvedDstViaSNDeficiencyResolution(self, methDict, signature):
		# precondition for this resolution strategy
		if not SNDeficiencies.qualifiesForLostParameterDeficiency(signature):
			return False

		exists = False
		combs = []
		emptySign = ""
		maxLength = 4
		# will build list of all combinations of String and Int up to length 4
		signCombinations = SNDeficiencies.generateIntStringCombinations(combs,emptySign,0,maxLength)
		for sign in signCombinations:
			signature = self.mtdInv.dst_name + "(" + sign + ")"

			if methDict.dictHasMethodInPackage(	self.mtdInv.dst_package, \
												self.mtdInv.dst_class, \
												signature):
				self.mtdInv.dst_param = sign.split(",")	# rectifying data record
				exists = True
				return exists # as soon as we find a match we return
		return exists

	def superClassIsMethodOwner(self, inhDict, methDict, superClass):
		""" checks, recursively, whether one of the super classes is the actual method owner """
		pkgName = common.utils.splicePackage(superClass, "::")
		className = common.utils.spliceClass(superClass, "::")

		if methDict.dictHasMethodInPackage(pkgName,\
										className,self.mtdInv.getDstSignature()):
			self.mtdInv.dst_package = pkgName
			self.mtdInv.dst_class = className
			return True
		elif inhDict.has_key(superClass):
			for entry in inhDict[superClass]:
				# recursive ...
				if self.superClassIsMethodOwner(inhDict,methDict,entry):
					return True
			return False
		else:
			return False

	def superClassIsMethodOwnerWithSNDef(self,inhDict,methDict,superClass,signature):
		""" Checks, recursively, whether one of the super classes is the
			actual method owner of a method that got wrongly parametrized
			by SN."""
		pkgName = common.utils.splicePackage(superClass, "::")
		className = common.utils.spliceClass(superClass, "::")
		tmpPkg = self.mtdInv.dst_package
		tmpCls = self.mtdInv.dst_class
		self.mtdInv.dst_package = pkgName
		self.mtdInv.dst_class = className
		if self.__resolvedDstViaSNDeficiencyResolution(methDict,signature):
			self.mtdInv.dst_package = pkgName
			self.mtdInv.dst_class = className
			return True
		elif inhDict.has_key(superClass):
			for entry in inhDict[superClass]:
				# recursive ...
				if self.superClassIsMethodOwnerWithSNDef(inhDict,methDict,entry,signature):
					return True
			return False
		else:
			self.mtdInv.dst_package = tmpPkg
			self.mtdInv.dst_class = tmpCls
			return False

	def isUniqueMethodName(self, mtdDict, dstSign):
		dstSign = dstSign.split("(")[0] # remove (empty) parameter list
		matchingSignatures = mtdDict.getSignaturesWithMethodName(dstSign)
		if 1 == len(matchingSignatures):
			params = matchingSignatures[0].split("(")[1] # cut mtd name part
			params = params[:len(params)-1] # remove last )
			self.mtdInv.dst_param = params.split(",")
			#self.log.warn("isUniqueMethodName? ",dstSign," ",str(matchingSignatures))
			#self.log.warn(str(self.mtdInv.dst_param))
			return True
		else:
			return False

	def resolveDstMtdLoc(self, dstMtdUniqName):
		#dstMtdUniqName = self.getDstMethUniqName()
		if dstMtdUniqName in self.mtdInv.uknDstMethLoc: return
		dstMtdSourceFiles = self.mtdSrcDict.getSourceLocFor(dstMtdUniqName)
		if 1 == len(dstMtdSourceFiles):
			dstSourceFile = dstMtdSourceFiles[0].split(":")[0]
			dstStart = dstMtdSourceFiles[0].split(":")[1]

			self.mtdInv.dstLoc = dstSourceFile
			self.mtdInv.dstLineNr = dstStart
			return True
		elif 0 == len(dstMtdSourceFiles):
			self.log.warn(	"URMI (MILOC): No sourceLocation for dst method "+\
							dstMtdUniqName)
			self.mtdInv.uknDstMethLoc.append(dstMtdUniqName)
			return False
		else:
			self.log.warn(	"URMI (MILOC): Multiple sourceLocations for dst method "+\
							dstMtdUniqName+": ",dstMtdSourceFiles)
			return False

