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

# This file determines what to do with an entity.

import common.cpp
from common import famix
from invokeable_entities import InvocationEntity
from log4py import Logger

##
# Commands for the generator.
# These specify what the generator should do.
##
NIL_CMD 							= 0;										# 0
GENERATE_SRC_METHOD_INFO			= NIL_CMD + 1								# 1
GENERATE_DST_METHOD_INFO			= GENERATE_SRC_METHOD_INFO + 1				# 2
GENERATE_METH_INV_INFO_MI_MI		= GENERATE_DST_METHOD_INFO + 1				# 3
GENERATE_METH_INV_INFO_FU_MI		= GENERATE_METH_INV_INFO_MI_MI + 1			# 4
GENERATE_METH_INV_INFO_MI_FU		= GENERATE_METH_INV_INFO_FU_MI + 1			# 5
GENERATE_METH_INV_INFO_FU_FU		= GENERATE_METH_INV_INFO_MI_FU + 1			# 6
GENERATE_ACCESS_INFO_MI_LOCAL_ATTR 	= GENERATE_METH_INV_INFO_FU_FU + 1			# 7
GENERATE_ACCESS_INFO_MI_REMOTE_ATTR	= GENERATE_ACCESS_INFO_MI_LOCAL_ATTR + 1	# 8
GENERATE_ACCESS_INFO_MI_GLOBAL_VAR	= GENERATE_ACCESS_INFO_MI_REMOTE_ATTR + 1	# 9
GENERATE_ACCESS_INFO_FU_REMOTE_ATTR = GENERATE_ACCESS_INFO_MI_GLOBAL_VAR + 1	# 10
GENERATE_ACCESS_INFO_FU_GLOBAL_VAR	= GENERATE_ACCESS_INFO_FU_REMOTE_ATTR + 1	# 11
GENERATE_ATTR_OWNER_INFO			= GENERATE_ACCESS_INFO_FU_GLOBAL_VAR + 1 	# 12
GENERATE_ATTR_RET_CLASS_INFO		= GENERATE_ATTR_OWNER_INFO + 1				# 13
GENERATE_ATTR_INFO					= GENERATE_ATTR_RET_CLASS_INFO + 1			# 14
GENERATE_METH_OWNER_INFO			= GENERATE_ATTR_INFO + 1					# 15
GENERATE_METH_RETTYPE_INFO			= GENERATE_METH_OWNER_INFO + 1				# 16
GENERATE_METHOD_INFO				= GENERATE_METH_RETTYPE_INFO + 1			# 17
GENERATE_IV_INFO					= GENERATE_METHOD_INFO + 1					# 18

log = Logger().get_instance()


##
# Determine what to do with a method invocation entity. Generates a list of
# commands. Currently, we at least miss the following:
#  -Invocations to destinations whose type is undefined (about 65% of the references)
##
def checkMethInvEntity(methInv):
	aCommandList = []

	if methInv.isSrcMethImpl() and methInv.isDstMethImpl():
		if methInv.formalSrcParams == None or methInv.formalDstParams == None:
			log.warn( "@@TODO@@ T36 no generation since no params. Line |",methInv.line)
			return aCommandList

# @TODO@
#		if not methInv.isSrcKnownMethod():
#			aCommandList.append(GENERATE_SRC_METHOD_INFO)

# @TODO@
#		if not methInv.isDstKnownMethod():
#			aCommandList.append(GENERATE_DST_METHOD_INFO)

		aCommandList.append(GENERATE_METH_INV_INFO_MI_MI)

	elif methInv.isSrcFunction() and methInv.isDstMethImpl():
		aCommandList.append(GENERATE_METH_INV_INFO_FU_MI)

	elif methInv.isSrcMethImpl() and methInv.isDstFunction():
		aCommandList.append(GENERATE_METH_INV_INFO_MI_FU)

	elif methInv.isSrcFunction() and methInv.isDstFunction():
#		if methInv.isSrcKnownFunction() and methInv.isDstKnownFunction():
#			aCommandList.append(GENERATE_METH_INV_INFO_FU_FU)
		aCommandList.append(GENERATE_METH_INV_INFO_FU_FU)

	elif methInv.isSrcMethImpl() and methInv.isDstTypeUndetermined():
		log.warn( "@@TODO@@ T37 Destination type is not determined: ",methInv.dst_class,methInv.dst_name,methInv.src_class,methInv.src_name)

	return aCommandList

##
# Acts upon a commandList for a given method invocation entity.
##
def generateMethodInvocationInfo(aCommandList, methInv):
	for aCommand in aCommandList:
		if ( aCommand == GENERATE_METH_INV_INFO_MI_MI ) :
			common.famix.cdifWriter.generateCplusPlusInvocationInfoFromEntity(methInv)
		elif ( aCommand == GENERATE_METH_INV_INFO_FU_MI ) :
			methInv.src_class = ""
			common.famix.cdifWriter.generateCplusPlusInvocationInfoFromEntity(methInv)
		elif ( aCommand == GENERATE_METH_INV_INFO_MI_FU ) :
			methInv.dst_class = ""
			common.famix.cdifWriter.generateCplusPlusInvocationInfoFromEntity(methInv)
		elif ( aCommand == GENERATE_METH_INV_INFO_FU_FU ) :
			methInv.src_class = ""
			methInv.dst_class = ""
			common.famix.cdifWriter.generateCplusPlusInvocationInfoFromEntity(methInv)

### ACCESS

##
# Determine what to do with an access entity. Generates a list of commands
#
# Currently, we at least miss the following:
#  -References to destinations whose class is unknown (about 90% of the references)
##
def checkAccessEntity(access):
	aCommandList = []

	if access.isDstTypeUndefined():

		if access.isSrcTypeMethodImpl():
			if access.srcHasNoParams():
				# some inner classes not parsed, soem library types
				pass

			#TODO: verify whether the access-entity already indicates
			# an owner: if so, do not prefer the source class!
			elif common.famix.cdifReader.isAttribute(access.src_class, access.dst_name):
				aCommandList.append(GENERATE_ACCESS_INFO_MI_LOCAL_ATTR)

			elif not access.isDstClassUnknown() and common.famix.cdifReader.isAttribute(access.dst_owner, access.dst_name):
				aCommandList.append(GENERATE_ACCESS_INFO_MI_REMOTE_ATTR)

			elif common.famix.cdifReader.isGlobalVar(access.dst_name):
				aCommandList.append(GENERATE_ACCESS_INFO_MI_GLOBAL_VAR)
		elif access.isSrcTypeFunction():
			if access.srcHasNoParams():
				# some inner classes not parsed, some library types
				pass
			if not access.isDstClassUnknown() and common.famix.cdifReader.isAttribute(access.dst_owner, access.dst_name):
				aCommandList.append(GENERATE_ACCESS_INFO_FU_REMOTE_ATTR)

			elif common.famix.cdifReader.isGlobalVar(access.dst_name):
				aCommandList.append(GENERATE_ACCESS_INFO_FU_GLOBAL_VAR)
	else:
		if access.src_type == "mi" and access.dst_type == "gv":
			aCommandList.append(GENERATE_ACCESS_INFO_MI_GLOBAL_VAR)
		elif access.src_type == "fu" and access.dst_type == "gv":
			aCommandList.append(GENERATE_ACCESS_INFO_FU_GLOBAL_VAR)
		elif access.src_type == "mi" and access.dst_type == "iv":
			aCommandList.append(GENERATE_IV_INFO)
		elif access.dst_type == "ec" or access.dst_type == "e":
			pass
	return aCommandList

##
# Acts upon a commandList for a given access entity.
##
def generateAccessInfo(aCommandList, access):
	for aCommand in aCommandList:
		if ( GENERATE_ACCESS_INFO_MI_LOCAL_ATTR == aCommand ) :
			access.dst_class = access.src_class
			common.famix.cdifWriter.generateAccess(access)
			#common.famix.cdifWriter.generateAccessInfo(access.src_class, access.src_name, access.formalSrcParams, access.src_class, access.dst_name, access.acc_type, access.sourceFile, access.start, access.start, access.src_sourceFile, access.src_start, access.dst_sourceFile, access.dst_start)
		elif ( GENERATE_ACCESS_INFO_MI_REMOTE_ATTR == aCommand ) :
			common.famix.cdifWriter.generateAccess(access)
			#common.famix.cdifWriter.generateAccessInfo(access.src_class, access.src_name, access.formalSrcParams, access.dst_owner, access.dst_name, access.acc_type, access.sourceFile, access.start, access.start,access.src_sourceFile, access.src_start,  access.dst_sourceFile, access.dst_start)
		elif ( GENERATE_ACCESS_INFO_MI_GLOBAL_VAR == aCommand ) :
			access.dst_owner = ""
			access.formalSrcParams = access.actualSrcParams # REMOVE WITH NEW IMPLEMENTATION
			common.famix.cdifWriter.generateAccess(access)
			#common.famix.cdifWriter.generateAccessInfo(access.src_class, access.src_name, access.actualSrcParams, "", access.dst_name, access.acc_type, access.sourceFile, access.start, access.start, access.src_sourceFile, access.src_start, access.dst_sourceFile, access.dst_start)
		elif ( GENERATE_ACCESS_INFO_FU_REMOTE_ATTR == aCommand ) :
			common.famix.cdifWriter.generateAccess(access)
			#common.famix.cdifWriter.generateAccessInfo(access.src_class,access.src_name, access.formalSrcParams, access.dst_owner, access.dst_name, access.acc_type, access.sourceFile, access.start, access.start, access.src_sourceFile, access.src_start, access.dst_sourceFile, access.dst_start)
		elif ( GENERATE_ACCESS_INFO_FU_GLOBAL_VAR == aCommand ) :
			access.dst_owner = ""
			access.formalSrcParams = access.actualSrcParams # REMOVE WITH NEW IMPLEMENTATION
			common.famix.cdifWriter.generateAccess(access)
			#common.famix.cdifWriter.generateAccessInfo("", access.src_name, access.actualSrcParams, "", access.dst_name, access.acc_type, access.sourceFile, access.start, access.start, access.src_sourceFile, access.src_start, access.dst_sourceFile, access.dst_start)
		elif ( GENERATE_IV_INFO == aCommand ):
			common.famix.cdifWriter.generateAccess(access)
			#common.famix.cdifWriter.generateAccessInfo(access.src_class, access.src_name, access.formalSrcParams, access.dst_owner, access.dst_name, access.acc_type, access.sourceFile, access.start, access.start, access.src_sourceFile, access.src_start, access.dst_sourceFile, access.dst_start)


### ATTRIBUTES

##
# Determine what to do with an attribute entity. Generates a list of commands
##
def checkAttributeEntity(attr):
	aCommandList = []

	# unsure whether this can happen
	if attr.owner in common.cpp.CPP_KEYWORDS:
		return []

	aCommandList.append(GENERATE_ATTR_INFO);

	return aCommandList

##
# Acts upon a commandList for a given attribute entity.
##
def generateAttributeInfo(aCommandList, attr):
	for aCommand in aCommandList:
		parentRef = attr.getParentReference()
		typeRef = attr.getTypeReference()
		if ( aCommand == GENERATE_ATTR_INFO ):
			common.famix.cdifWriter.generateFieldInfo(
				attr.name,
				parentRef,
				typeRef,
				attr.visibility,
				attr.hasClassScope,
				attr.sourceFile,
				attr.start, attr.end
				)
		# will no longer happen since we no longer push these commands on the list
		# in case it well be re-instated later: add the following 2 lines respectively
		# before and after the parseAttributes method in snavtofamix.py
		#	famix.cdifReader.buildClassCache(output_file)
		#	famix.cdifReader.flushClassCache()
		elif ( aCommand == GENERATE_ATTR_OWNER_INFO ):
			common.famix.cdifWriter.generateClassInfo(parentRef.getReferencedName(), "", False, "", "", "")
			common.famix.cdifReader.addClassToCache(attr.owner)
		elif ( aCommand == GENERATE_ATTR_RET_CLASS_INFO ):
			#TODO: we don't want to use the low level caches!!!! Use dictionaries instead!!!!
			common.famix.cdifWriter.generateClassInfo(typeRef.getResolvedName(), "", False, "", "", "")
			common.famix.cdifReader.addClassToCache(typeRef.getResolvedName())

### METHODS

##
# Determine what to do with an method entity. Generates a list of commands
##
def checkMethodEntity(meth):
	aCommandList = []

	# unsure whether this can ever happen
	if meth.owner in common.cpp.CPP_KEYWORDS:
		return []

	aCommandList.append(GENERATE_METHOD_INFO)

	return aCommandList

##
# Acts upon a commandList for a given method entity.
##
def generateMethodInfo(aCommandList, meth):
	for aCommand in aCommandList:
		# TODO: store parentRef and typeRef in InvokableEntityReference
		parentRef = meth.getParentReference()
		typeRef = meth.getTypeReference()

		if ( aCommand == GENERATE_METHOD_INFO):
			common.famix.cdifWriter.generateMethodInfoFromReference(meth.getReference())
			common.famix.cdifReader.addMethodToCache(meth.getUniqueName())
