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
# Author(s): Bart Du Bois <bart.dubois@ua.ac.be>


from common.entity_references.EntityReferences import InvokableEntityReference
from cplusplus.typed_entities.TypedefDictionary import TypedefDictionary
from cplusplus.InheritanceDictionary import TransitiveInheritanceDictionary
from cplusplus.invokeable_entities.TypeTranslator import TypeTranslator
from cplusplus.typed_entities.QualifiedNameHelperFunctions import getNonQualifiedName

class AlternativeParameterTypeMatcher:
	def __init__(self, transInhDict, typedefDict, actualParams):
		self.transInhDict = transInhDict
		self.typedefDict = typedefDict
		self.actualParams = actualParams.lstrip("(").rstrip(")")
		self.typeTranslator = TypeTranslator(typedefDict)

	def matches(self, invEntityRef):
		return self.isIdenticalMatch(invEntityRef) or self.isNonIdenticalMatch(invEntityRef)

	def isIdenticalMatch(self, invEntityRef):
		bracketlessFormalPars = invEntityRef.getBracketLessFormalParameters()
		isIdenticalMatch = (bracketlessFormalPars == self.actualParams)
		return isIdenticalMatch

	def isNonIdenticalMatch(self, invEntityRef):
		matches = False

		methodOwnerTemplateParameters = []

		if invEntityRef.getOwnerTemplateParameters() != "":
			methodOwnerTemplateParameters =  invEntityRef.getOwnerTemplateParameters().strip().split(",")

		actualParamList = self.actualParams.split(",")
		formalParamList = invEntityRef.getBracketLessFormalParameters().split(",")

		parIndex = 0
		nrOfActualParams = len(actualParamList)
		nrOfFormalParams = len(formalParamList)

		for actualParam in actualParamList:
			if parIndex >= nrOfFormalParams:
				break

			formalParam = formalParamList[parIndex]

			isLastFormalParameter = ( (parIndex == (nrOfFormalParams - 1)) and (formalParam == "...") )
			isLastActualParameter = ( (parIndex == (nrOfActualParams - 1)) and (actualParam == "...") )
			if isLastFormalParameter or isLastActualParameter :
				# ellipses case reached => success
				break
			elif parIndex < nrOfFormalParams:
				if not(self.typesMatch(actualParam, formalParam, methodOwnerTemplateParameters)):
					return False

			parIndex += 1

		return True

	def typesMatch(self, actualParam, formalParam, methodOwnerTemplateParameters):
		matches = False

		if actualParam == "unknown":
			return True

		actualParamType = self.typeTranslator.translateTypeName(actualParam)
		formalParamType = self.typeTranslator.translateTypeName(formalParam)

		# List all your parameter matching attempts here
		# Ensure that the loop continues if the current parameter
		# is still a match.
		# In case the current parameter is no match,
		# put matches to False and return.
		if (formalParamType == actualParamType):
			matches = True
		elif (formalParamType in methodOwnerTemplateParameters):
			# => treat similar as unknown: ignore current actual param
			matches = True # formal param-type = method owner template parameter
		elif (formalParamType in self.transInhDict.getTransitiveSuperclasses(actualParamType)):
			matches = True # actual parameter type is a subclass of the formal parameter type
		elif self.isTemplateVariant(actualParamType, formalParamType):
			matches = True
		elif self.isNamespaceVariant(actualParam, formalParam):
			matches = True
		elif self.areCompatibleTypes(actualParamType, formalParamType):
			matches = True

		return matches

	def areCompatibleTypes(self, actualParamType, formalParamType):
		actualParam = getNonQualifiedName(actualParamType)
		formalParam = getNonQualifiedName(formalParamType)

		if actualParam == "int":
			if formalParam in ["long","float","double"]:
				return True
		elif (actualParam == "float") and (formalParam == "double"):
			return True
		elif (actualParam == "char*") and (formalParam == "string"):
			return True
		elif (actualParam == "string") and (formalParam == "char*"):
			return True

		return False

	##
	# Verify whether the formal parameter type is equal to the actual parameter
	# type, but then prefixed with a namespace name.
	#
	# Is needed for resolving the signatures provided by CCCC.
	#
	# TODO: limit this to the namespaces used at that point
	##
	def isNamespaceVariant(self, actualParam, formalParam):
		undecoratedActualParam = self.typeTranslator.undecorate(actualParam)
		undecoratedFormalParam = self.typeTranslator.undecorate(formalParam)

		if "::" in undecoratedFormalParam:
			typeParts = undecoratedFormalParam.split("::")
			formalParamTypeWithoutNamespacePrefix = typeParts[len(typeParts)-1]
		else:
			formalParamTypeWithoutNamespacePrefix = undecoratedFormalParam

		isVariant = (undecoratedActualParam == formalParamTypeWithoutNamespacePrefix)

		return isVariant

	##
	# Verify whether the formal parameter type is a template instantiation
	# of the actual parameter tpye, e.g. vector and vector<Y>.
	#
	# Is needed for resolving the signatures provided by CCCC.
	##
	def isTemplateVariant(self, actualParamType, formalParamType):
		if "<" in formalParamType:
			formalParamTypeWithoutTemplatePart = formalParamType[0:formalParamType.index("<")]
			if " " in formalParamType:
				formalParamTypeWithoutTemplatePart += formalParamType[formalParamType.index(" "):]
		else:
			formalParamTypeWithoutTemplatePart = formalParamType

		isVariant = (actualParamType == formalParamTypeWithoutTemplatePart)

		return isVariant
