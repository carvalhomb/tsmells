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

# This file declares a dictionary for typedefs
#	+key: 	typedef-name (alias-name)
#	+value: resolved name (struct or class)
#
#
##

from log4py import Logger

from cplusplus.data_types.ClassEntity import TypeDefEntityReference

##
# Encapsulation of the dictionary.
#
# TODO: the dictionary should also maintain entityData, in which e.g., the
# source file in which the typedef was declared is mentioned. This would
# enable to verify whether the typedef is transitively included from a
# location in the source code.
##
class TypedefDictionary:
	# initialize an empty dictionary
#	dict = {}

	##
	# Initialize a dictionary.
	##
	def __init__(self):
		self.references = []
		self.log = Logger().get_instance(self)

	##
	# TODO: also verify whether the given alias is not like SomeClass<T>.
	# In that case we have to look for SomeClass
	##
	def getBaseType(self, qualifiedAlias):
		baseType = None

		reference = self.getReference(qualifiedAlias)

		if reference != None:
			baseType = reference.getBaseType()

		return baseType

	def containsAlias(self, qualifiedAlias):
		return (self.getReference(qualifiedAlias) != None)

	def getReference(self, qualifiedAlias):
		reference = None

		for curRef in self.references:
			if curRef.getQualifiedAliasName() == qualifiedAlias:
				reference = curRef
				break

		return reference

	def containsAliasForBaseType(self, qualifiedBaseType):
		return (self.getTypeDefsForBaseType(qualifiedBaseType) != [])

	def getTypeDefsForBaseType(self, qualifiedBaseType):
		references = []

		for curRef in self.references:
			if curRef.getBaseType() == qualifiedBaseType:
				references.append(curRef)

		return references

	def add(self, typeDefRef):
		added = False

		if typeDefRef.isValid() and not(typeDefRef.isMacroRelatedTypeDef()):
			if not(self.containsAlias(typeDefRef.getName())):
				if self.containsAlias(typeDefRef.getBaseType()):
					typeDefRef.setBaseType(self.getBaseType(typeDefRef.getBaseType()))

				self.references.append(typeDefRef)
				added = True

				# if there are any base types that now turn out to be aliases
				for ref in self.getTypeDefsForBaseType(typeDefRef.getName()):
					ref.setBaseType(typeDefRef.getBaseType())

		return added
