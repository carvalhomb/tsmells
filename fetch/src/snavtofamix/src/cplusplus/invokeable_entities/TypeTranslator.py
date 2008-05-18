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

class TypeTranslator:
	def __init__(self, typedefDict):
		self.typedefDict = typedefDict

	##
	# Translate the given type name.
	##
	def translateTypeName(self, typeName):
		translatedTypeName = self.undecorate(typeName)

		if " " in translatedTypeName:
			rawTypeName=translatedTypeName[0:translatedTypeName.index(" ")]
			afterParts=translatedTypeName[translatedTypeName.index(" "):]
			translatedRawTypeName = self.normalizeAsTypedef(rawTypeName)
			translatedTypeName=translatedRawTypeName+afterParts
		else:
			translatedTypeName = self.normalizeAsTypedef(translatedTypeName)
		translatedTypeName = self.undecorate(translatedTypeName)

		return translatedTypeName
	##
	# Remove const or & occurences from the type.
	##
	def undecorate(self, parameterType):
		undecoratedType = parameterType.replace("const ", "")
		undecoratedType = undecoratedType.replace(" &","")
		undecoratedType = undecoratedType.replace("unsigned *", "unsigned int *")
		undecoratedType = undecoratedType.replace("unsigned [", "unsigned int [")
		undecoratedType = undecoratedType.replace("unsigned ", "")
		undecoratedType = undecoratedType.replace("unsigned", "int")
		return undecoratedType

	##
	# Retrieve the original name of the given alias, or itself in case
	# there is no such typedef.
	##
	def normalizeAsTypedef(self, parameterType):
		normalizedType = parameterType

		if self.typedefDict.containsAlias(parameterType):
			 normalizedType = self.typedefDict.getBaseType(parameterType)

		return normalizedType
