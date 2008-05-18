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
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>,
#                     Bart Du Bois <bart.dubois@ua.ac.be>

from common.SourceLocation import SourceLocation
from cplusplus.invokeable_entities.ParentInvokeableEntityResolver import ParentInvokableEntityResolver
from cplusplus.accessible_entities.AttributeDictionary import AttributeDictionary
from common.famix import cdifReader
from log4py import Logger

##
# Resolver for the source and destination of an access.
#
# Resolves
#	access.dst_owner
#	access.dst_class
##
class AlternativeAccessResolver(ParentInvokableEntityResolver):
	def __init__(self, scope, transInhDict, typedefDict, invEntityDict, attrDict):
		ParentInvokableEntityResolver.__init__(self,scope, transInhDict, typedefDict, invEntityDict)
		self.attrDict = attrDict

	def resolve(self, access):		
		if not access.isDstTypeUndefined() and not access.dst_type == "gv":
			# Ok, the RuleChecker will find out and generate the necessary Famix entities.
			return True # generate an access
		elif access.dst_name == "":
			return False

		access.getReport().setValidDestinationName()

		if self.resolveSource(access):
			return self.resolveDestination(access)

		return False

	##
	# Resolves the destination of the access.
	#
	#
	# Returns whether the resolution succeeded.
	##
	def resolveDestination(self, access):
		resolved = False

		if access.dst_type == "gv":
			return self.resolveAccessAsGlobalVarRef(access)
		elif self.resolveAccessAsAttributeRef(access):
			return True # case closed

		return resolved

	def filterOutNonIncludedOwners(self, attributeOwners, access):
		# TODO: we need to incorporate namespace info here!!
		transInhDict = self.scope.getTransitiveIncludeDict()
		transIncludedFiles = transInhDict.getTransitiveIncludedFiles(access.sourceFile)

		for attrOwner in attributeOwners:
			entityDataList = self.attrDict.retrieveEntityDataList(access.dst_name, attrOwner)
			isIncluded = False
			for entityData in entityDataList:
				srcFile = self.attrDict.getSourceFile(entityData)
				if srcFile in transIncludedFiles:
					isIncluded = True
					break

			# if we haven't found an entityData for (name,attrOwner)
			# which is included, ignore it
			if not(isIncluded):
				attributeOwners.remove(attrOwner)

		return attributeOwners
	##
	# Try to resolve the access as an attribute reference
	##
	def resolveAccessAsAttributeRef(self,access):
		resolutionReport = access.getReport()

		if not( self.attrDict.hasKey(access.dst_name) ):
			return False # attempt failed

		resolutionReport.setDestinationNameKnown()

		# if we don't know which class contains the referenced attribute
		if access.dst_owner == "": # main scenario
			attributeOwners = self.attrDict.getOwners(access.dst_name)

			resolutionReport.setNumberOfFoundDestinations(len(attributeOwners))

			# TODO: we need to incorporate namespace info here!!
			attributeOwners = self.filterOutNonIncludedOwners(attributeOwners, access)

			# @@TODO@@ reduce the amount of possibilities by checking visibility of possible matches
			if len(attributeOwners) == 0:
				return False ## attempt failed
			elif len(attributeOwners) > 1 :
				# the attribute dictionary contains multiple (owner, attrType) pairs
				# => find out the right possible attribute type
				if self.resolveAttrRefWithMultiplePotentialTargets(access):
					attrType = self.attrDict.getAttributeType(access.dst_name, access.dst_owner)
					self.fillInAccessDestination(access, access.dst_owner, attrType)
					return True
				return False # failed to resolve access as an attribute reference

			resolutionReport.setNumberOfIncludedDestinations(1)

			# the attribute dictionary contains a single (owner,attrType) pair
			# => attribute reference resolved
			attrOwner = attributeOwners[0]
			attrType = self.attrDict.getAttributeType(access.dst_name, attrOwner)
			self.fillInAccessDestination(access, attrOwner, attrType)
			return True ## case closed

		else:
			resolutionReport.setDestinationClassKnown()
			# we know which class contains the referenced attribute. e.g. fully qualified
			# accesses such as to static fields.
			# thus lookup the type of this attribute

			# TODO: we need to incorporate namespace info here!!
			# TODO: does this dict also contains global variables?
			attrType = self.attrDict.getAttributeType(access.dst_name, access.dst_owner)

			if attrType == "" :
				return False # attempt failed

			resolutionReport.setValidDestinationType()

			# case closed!
			self.fillInAccessDestination(access, access.dst_owner, attrType)
			return True # case closed

	##
	# Try to resolve the access as a global variable reference
	# - @@ TODO @@ Need to take into account reachability (static global var, file)
	##
	def resolveAccessAsGlobalVarRef(self,access):
		# TODO: There should be a GlobalVariableDictionary available here!
		
		if cdifReader.isGlobalVar(access.dst_name):
			access.dst_type = "gv"
			access.dst_class = "@@TODO@@"
			return True # case closed

		return False

	##
	# There are multiple attributes with a name equal to the one accessed.
	##
	def resolveAttrRefWithMultiplePotentialTargets(self, access):
		attributeOwners = self.attrDict.getOwners(access.dst_name)

		attributeOwners = self.filterOutNonIncludedOwners(attributeOwners, access)

		# is it a local attribute?
		if access.src_class in attributeOwners:
			# then consider the referenced type
			attr_type = self.attrDict.getAttributeType(access.dst_name, access.src_class)
			self.fillInAccessDestination(access, access.src_class, attr_type)
			return True # case closed
		# the class referring to the attribute does not contain an attribute with that name
		else:
			attributeOwners = self.attrDict.getOwners(access.dst_name)
			attributeOwners = self.filterOutNonIncludedOwners(attributeOwners, access)

			# The positive scenario: the include resolution strategy finds one possible owner.
			if 1 == len(attributeOwners):
				attrOwner = attributeOwners[0]
				attrType = self.attrDict.getAttributeType(access.dst_name, attrOwner)
				self.fillInAccessDestination(access, attrOwner,attrType)
				return True # case closed
			elif 0 == len(attributeOwners):
				# attempt to resolve the access as a reference to an attribute of the superclasses of the referencing class
				if ( self.resolveAttrRefUsingSuperclasses(access, attributeOwners) ):
					return True # case closed

		return False

	##
	# Attempt to resolve an access as a reference to an attribute of the superclasses of the referencing class.
	##
	def resolveAttrRefUsingSuperclasses(self,access, attributeOwners):
		superClasses=[]
		superClasses = cdifReader.getSuperClassesOf(access.src_class, superClasses);

		## Added by BDB on 02/01/2007: keep only those superclasses who contain the attribute
		superClassesContainingAttribute = []
		for superClass in superClasses:
			if superClass in attributeOwners:
				superClassesContainingAttribute.append(superClass)
			elif typedefDict.containsAlias(superClass):
				originalSuperclassName = typedefDict.getBaseType(superClass)
				if originalSuperclassName in attributeOwners:
					superClassesContainingAttribute.append(originalSuperclassName)

		# Positive scenario, we resolved the reference to the single superclass containing the attribute
		if 1 == len(superClassesContainingAttribute):
			# We can set the resolved attribute owner IF the attribute is accessible.
			# This depends on the access modifier of the attribute as well as of the inheritance type
			attrOwner = superClassesContainingAttribute[0]
			attrType = attrDict.getAttributeType(access.dst_name, attrOwner)
			self.fillInAccessDestination(access, attrOwner, attrType)
			return True # case closed
		# More than one option ...
		elif len(superClassesContainingAttribute) > 1:
			log.warn( "@@TODO@@ T16b2a still a problem for attribute name", access.dst_name, "in ",access.src_class+"."+access.src_name,"file",access.sourceFile,access.start,"finds more than one superclass containing attribute",access.dst_name,":", superClasses, ". Need other resolution strategies")
			return False # attempt failed

	##
	# Actual resolution: update the access with resolved info.
	# @accessOwner: the class owning the attribute
	# @accessType: the type of the attribute
	#
	# Fills in:
	# 	dst_owner
	#	dst_class
	#
	# These should already be filled in:
	#	dst_name
	#	acc_type
	##
	def fillInAccessDestination(self, access, accessOwner, accessType):
		if accessOwner != "" and access.isDstTypeUndefined():
			# owner
			access.dst_owner = accessOwner
			# type
			access.dst_class = accessType

			entityDataList = self.attrDict.retrieveEntityDataList(access.dst_name, access.dst_owner)
			if not(not entityDataList):
				entityData = entityDataList[0]
				access.dst_sourceFile = self.attrDict.getSourceFile(entityData)
				access.dst_start = self.attrDict.getLineNr(entityData)

		else:
			log.warn( "@@TODO@@ T??? unexpected location within fillInAccess with accessOwner = ",accessOwner,"and isDstTypeUndefined",access.isDstTypeUndefined())
