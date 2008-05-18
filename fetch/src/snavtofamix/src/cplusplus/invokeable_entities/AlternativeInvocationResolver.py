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

from cplusplus.invokeable_entities.AlternativeInvokableEntityResolver import AlternativeInvokeableEntityResolver
from common.SourceLocation import SourceLocation
from cplusplus.invokeable_entities.ParentInvokeableEntityResolver import ParentInvokableEntityResolver
from cplusplus.accessible_entities.AccessDictionary import AccessDictionary
from log4py import Logger

##
# Resolver for the source and destination of an invocation.
# Resolves the following information for both the source and destination:
# 	invocation.dst_class
#	invocation.formalDstParams
#	invocation.dst_sourceFile
#	invocation.dst_start
#
# Note: No logging should occur during the resolution. Instead, the
# InvocationResolutionReport contained as an attribute report in the given
# invocation should be used for reporting.
##
class AlternativeInvocationResolver(ParentInvokableEntityResolver):
	def __init__(self, scope, transInhDict, typedefDict, invEntityDict, accessDict = None):
		ParentInvokableEntityResolver.__init__(self,scope, transInhDict, typedefDict, invEntityDict, accessDict)
		self.invEntityDict = invEntityDict

	##
	# Resolve the source and destination of the invocation.
	##
	def resolve(self, invocation):
		if self.resolveSource(invocation):
			return self.resolveDestination(invocation)

		return False

	##
	# Resolves the destination of the invocation to a declaration (potentially
	# immediately defining the declaration.
	#
	# Returns whether the resolution succeeded.
	##
	def resolveDestination(self, invocation):
		resolutionReport = invocation.getReport()

		uniqueName = invocation.dst_name

		# don't spend time looking for methods or functions
		# that are totally unknown
		if not self.invEntityDict.hasMultiLocKey(uniqueName):
			return False

		resolutionReport.setDestinationNameKnown()

		if invocation.dst_class != "":
			uniqueName = invocation.dst_class + "." + invocation.dst_name

		refLoc = SourceLocation(invocation.sourceFile, invocation.start, invocation.start)

		# ensure that # is not regarded as a class name
		if ( invocation.dst_class == "#" ):
			invocation.dst_class = ""

		selection = self.resolver.select(invocation.dst_class, invocation.dst_name, invocation.actualDstParams, refLoc, invocation.src_class)

		if not selection:
			return False
		elif len(selection) > 1:
			# TODO: indien references file gesorteerd op lijn en karakter
			# dan kan je alsnog een resolution aanbieden indien er slechts 1
			# access was en geen andere invocaties op die lijn verwerkt werden
			# de dst_class is dan het type van het attribuut
			resolutionReport.setNumberOfFoundDestinations(len(selection))
			return False

		resolutionReport.setNumberOfFoundDestinations(1)

		# merely one invokable entity
		invokableEntityRef = selection[0]
		multiLoc = invokableEntityRef.getLocation()

		declarationDefinitionTuples = multiLoc.getIncludedDeclarationDefinitionTuples(self.scope, refLoc.getSourceFile())
		if not declarationDefinitionTuples:
			return False
		elif len(declarationDefinitionTuples) > 1:
			resolutionReport.setNumberOfIncludedDestinations(len(declarationDefinitionTuples))
			return False

		resolutionReport.setNumberOfIncludedDestinations(1)

		# merely one declaration-definition tuple
		declLoc = declarationDefinitionTuples[0][0] # retrieve first location from single tuple
		self.fillInInvocationDestination(invocation, invokableEntityRef, declLoc)
		return True

	def fillInInvocationDestination(self, invocation, invokableEntityRef, dstLoc):
		invocation.dst_class = invokableEntityRef.getOwnerName()
		if invocation.dst_class == "":
			invocation.dst_type = "fu"
		else:
			invocation.dst_type = "mi"
		invocation.formalDstParams = invokableEntityRef.getFormalParameters().lstrip("(").rstrip(")")
		invocation.dst_sourceFile = dstLoc.getSourceFile()
		invocation.dst_start = dstLoc.getStart()
		invocation.dst_namespace = invokableEntityRef.getNamespaceName()
