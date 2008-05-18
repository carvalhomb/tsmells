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

from cplusplus.data_types.DataRecords import Entity
from cplusplus.accessible_entities.AccessResolutionReport import AccessResolutionReport
from common.utils import returnParamLists

##
# Encapsulation of an attribute reference entity.
##
class AccessEntity(Entity):
	## ATTRIBUTES ##
	# -- initialized in constructor --
	# cols
	# accessDatabase
	# methodDefinitionDatabase
	# src_sourceFile
	# src_start
	# dst_sourceFile
	# dst_start
	# formalSrcParams The formal parameters of the source method (previously src_params)
	# -- initialized in decomposeData
	# dst_owner The class owning the referenced attribute
	# dst_class The class containing the referenced attribute
	# dst_name The name of the referenced attribute
	# dst_type The type of the referenced attribute
	# src_class The class containing the referencing method/function
	# src_name The name of the referencing method/function
	# src_type The type of the referencer (method or function)
	# OLD: src_param The actual parameters of the source method
	# NEW: actualSrcParams The actual parameters of the source method (previously src_param)

	# OLD: dst_param
	# NEW: actualDstParams
	# acc_type
	# -- initialized in determineSourceLocation
	# sourceFile
	# start

	##
	# Initializes a method invocation entity instance with a given
	# line from the class database and databases for method definitions
	# and implementations.
	##
	def __init__(self, accessLine):
		Entity.__init__(self, accessLine)
		self.cols = self.line.split(";")
		self.formalSrcParams = None
		self.src_sourceFile = ""
		self.src_start = ""
		self.dst_sourceFile = ""
		self.dst_start = ""
		self.report = AccessResolutionReport(accessLine)
		self.src_namespace = ""
		self.actualSrcParams = ""
		self.actualDstParams = ""
		
	def getReport(self):
		return self.report

	##
	# Decomposes the entity line.
	#
	# @requires: self.cols to be initialized
	# @ensures: initialization of attributes from cols
	##
	def decomposeData(self):
		self.dst_owner = self.cols[0].replace("#", "")
		self.dst_name = self.cols[1]
		self.dst_type = self.cols[2]
		self.src_class = self.cols[3].replace("#", "")
		self.src_name = self.cols[4]
		self.src_type = self.cols[5]
		
		paramCols = ''.join(self.cols[9:])
		self.actualSrcParams, self.actualDstParams = returnParamLists(paramCols)

		self.actualSrcParams=self.actualSrcParams.replace("const ","").replace(" *","*").replace(" []","[]")
		self.actualDstParams=self.actualDstParams.replace("const ","").replace(" *","*").replace(" []","[]")

		self.acc_type = self.cols[6]
		self.dst_class = ""

		self.determineSourceLocation()

	def isResolved(self):
		return not self.isDstClassUnknown()

	def isSrcTypeMethodImpl(self):
		return self.src_type == "mi"

	def isSrcTypeFunction(self):
		return self.src_type == "fu"

	def isDstTypeUndefined(self):
		return self.dst_type == "ud"

	def isDstClassUnknown(self):
		return self.dst_owner == ""

	def getSourceSignature(self):
		if self.src_class != "":
			return self.src_class+"."+self.src_name+"("+self.actualSrcParams+")"
		else:
			return self.src_name+"("+self.actualSrcParams+")"

	def getSrcUniqueName(self):
		uniqueName = ""


		if self.src_class != "":
			uniqueName = self.src_class+"."
		elif self.src_namespace != "":
			uniqueName = self.src_namespace + "::"

		uniqueName += self.src_name+"("+self.formalSrcParams+")"

		return uniqueName

	##
	# @requires: src type to be a method implementation or a function
	##
	def srcHasNoParams(self):
		assert self.isSrcTypeMethodImpl() or self.isSrcTypeFunction()
		return self.formalSrcParams == None
