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


from sets import Set
from cplusplus.data_types.DataRecords import Entity
from common.utils import returnParamLists
from common.famix import cdifReader
import common.utils
from cplusplus.invokeable_entities.InvocationResolutionReport import InvocationResolutionReport

##
# Encapsulation of an entity.
##
class InvocationEntity(Entity):
	## ATTRIBUTES ##
	# -- initialized in constructor --
	# cols
	# invokeableEntityDict	# dictionary for methods or functions
	# OLD: src_params	# the parameters of the source method as confirmed by the method/function dictionary
	# NEW: formalSrcParams # the formal parameters of the source method (previously src_params)

	# dst_params	# the parameters of the target method as confirmed by the method/function dictionary
	# NEW: formalDstParams # the formal parameters of the destination method (previously dst_params)

	# src_sourceFile	# the source file of the source method
	# src_start			# the source line nr of the source method
	# dst_sourceFile	# the source file of the target method
	# dst_start			# the source line nr of the target method
	# -- initialized in decomposeData
	# dst_class	# the name of the class owning the invoked method
	# dst_name 	# the name of the invoked method
	# dst_type 	# the type of the referenced entity: e.g., a method implementation
	# src_class	# the name of the class owning the method that invoked the method
	# src_name	# the name of the method invoking the other method
	# src_type	# the type of the referencing entity: e.g., a method implementation
	# OLD: sourceParams	# the parameters of the source method as found in the references file
	# NEW: actualSrcParams # the actual parameters of the source method (previously sourceParams)

	# destParams # the parameters of the destination method as found in the references file
	# NEW: actualDstParams # the actual parameters of the destination method (previously destParams)

	# sourceFile, start	# source file and line number of the invocation

	#################################### INITIALIZATION METHODS ####################################

	##
	# Initializes an invocation entity instance with a given
	# line from the reference database.
	##
	def __init__(self, invLine, invokeableEntityDictionary):
		Entity.__init__(self, invLine)
		
		self.cols = self.line.split(";")
		self.invokeableEntityDict = invokeableEntityDictionary

		self.formalSrcParams = None
		self.formalDstParams = None
		
		self.sourceFile = ""
		self.start = ""

		self.src_sourceFile = ""
		self.src_start = ""

		self.dst_sourceFile = ""
		self.dst_start = ""
		self.report = InvocationResolutionReport(invLine)

		self.src_namespace = ""
		self.dst_namespace = ""

	def getReport(self):
		return self.report

	##
	# Decomposes the reference line.
	#
	# @requires: self.cols to be initialized
	# @ensures: class, name and type of both source and destination to be initialized
	##
	def decomposeData(self):
		self.dst_class = self.cols[0]	# the name of the class owning the invoked method
		self.dst_name = self.cols[1]	# the name of the invoked method

		# remove template parameters from destination method or function name
		# unsure whether this code is ever used
		if ( self.dst_name.startswith("<") and (">" in self.dst_name) ):
			# cut of the part until and including the first > character
			self.dst_name=self.dst_name[self.dst_name.index(">")+1:]

		self.dst_type = self.cols[2]	# the type of the referenced entity: e.g., a method implementation
		self.src_class = self.cols[3]	# the name of the class owning the method that invoked the method
		self.src_name = self.cols[4]	# the name of the method invoking the other method

		# remove template parameters from source method or function name
		# unsure whether this code is ever used
		if ( self.src_name.startswith("<") and (">" in self.src_name) ):
			# cut of the part until and including the first > character
			self.src_name=self.src_name[self.src_name.index(">")+1:]

		self.src_type = self.cols[5]	# the type of the referencing entity: e.g., a method implementation
		paramCols = ''.join(self.cols[9:])
		self.actualSrcParams, self.actualDstParams = returnParamLists(paramCols)	# the parameters as found in the references file
		
		self.actualSrcParams = self.actualSrcParams.replace(" *","*")
		self.actualDstParams = self.actualDstParams.replace(" *","*")
		self.sourceFile = self.cols[8]
		self.__cleanLineNumber(self.cols[7])
		
		
		#self.determineSourceLocation()
		
	def __cleanLineNumber(self, lineNo):
		lineNr = ""
		i = 0
		# found cases where lineNo was 000000
		while '0' == lineNo[i] and i < len(lineNo)-1:
			i = i + 1 # skip the zeroes
		lineNr = lineNo[i:]
		self.start = lineNr
		
	#################################### STATUS CHECKING METHODS ####################################

	##
	# Verifies whether the source entity is a method implementation.
	##
	def isSrcMethImpl(self):
		return self.src_type == "mi"

	##
	# Verifies whether the source entity is a function.
	##
	def isSrcFunction(self):
		return self.src_type == "fu"

	##
	# Verifies whether the type of source entity is unknown.
	##
	def isSrcTypeDetermined(self):
		return self.src_type != "ud"

	##
	# Verifies whether the target entity is a method implementation.
	##
	def isDstMethImpl(self):
		return self.dst_type == "mi"

	##
	# Verifies whether the target entity is a function.
	##
	def isDstFunction(self):
		return self.dst_type == "fu"

	##
	# Verifies whether the type of target entity is unknown.
	##
	def isDstTypeDetermined(self):
		return self.dst_type != "ud"

	##
	# Verifies whether the class owning the source entity is known.
	##
	def isSrcClassDetermined(self):
		return (self.src_class != "#" and self.src_class != "")

	##
	# Verifies whether the class owning the target entity is known.
	##
	def isDstClassDetermined(self):
		return (self.dst_class != "#" and self.dst_class != "")

	##
	# Verifies whether the parameters of the source entity are resolved.
	##
	def areSrcParametersDetermined(self):
		return self.formalSrcParams != None

	##
	# Verifies whether the parameters of the target entity are resolved.
	##
	def areDstParametersDetermined(self):
		return self.formalDstParams != None

	##
	# The source of an invocation is complete when:
	#	# the src_type is determined
	#	# the src_params is determined
	#	# in case the src is a method implementation
	#	#	the src_class should be determined
	##
	def isSrcCompletelyResolved(self):
		isCompletelyResolved = self.isSrcTypeDetermined() and self.areSrcParametersDetermined()

		if ( isCompletelyResolved ):
			if ( self.isSrcMethImpl() ):
				isCompletelyResolved = isCompletelyResolved and self.isSrcClassDetermined()

		return isCompletelyResolved

	def getSrcUniqueName(self):
		uniqueName = ""


		if self.src_class != "":
			uniqueName = self.src_class+"."
		elif self.src_namespace != "":
			uniqueName = self.src_namespace + "::"

		uniqueName += self.src_name+"("+self.formalSrcParams+")"

		return uniqueName

	##
	# Returns namespaceName::functionName(pars)
	# or methodName(pars).
	##
	def getTrgName(self):
		trgName = ""

		if (self.dst_class == "") and (self.dst_namespace != ""):
			trgName = self.dst_namespace + "::"

		trgName += self.dst_name+"("+self.formalDstParams+")"

		return trgName

	##
	# The target of an invocation is complete when:
	#	# the dst_type is determined
	#	# the dst_params is determined
	#	# in case the dst is a method implementation
	#	#	the dst_class should be determined
	##
	def isDstCompletelyResolved(self):
		isCompletelyResolved = self.isDstTypeDetermined() and self.areDstParametersDetermined()

		if ( isCompletelyResolved ):
			if ( self.isDstMethImpl() ):
				isCompletelyResolved = isCompletelyResolved and self.isDstClassDetermined()

		return isCompletelyResolved

	##
	# An invocation is complete when:
	#	# the src_type and dst_type are both determined
	#	# the src_params and dst_params are determined
	#	# in case the src is a method implementation
	#	#	the src_class should be determined
	#	# in case the dst is a method implementation
	#	# 	the dst_class should be determined
	##
	def isCompletelyResolved(self):
		isCompletelyResolved = self.isSrcCompletelyResolved() and self.isDstCompletelyResolved()
		return isCompletelyResolved

	#################################### RESOLUTION METHODS ####################################
