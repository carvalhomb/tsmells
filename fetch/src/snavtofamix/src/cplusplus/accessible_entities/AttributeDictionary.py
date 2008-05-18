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

from cplusplus.data_types.AccessibleEntities import AttributeEntity, AccessibleEntityReferenceEntity

##
# Encapsulation of a dictionary of attributes (TODO: and global variables?).
#
# Note: this dictionary is built up similarly to the InvokeableEntityDictionary.
# Accordingly, we can regard attributes as similar to methods, and global variables as similar to functions.
# Thus, this dictionary should actually be called AccessibleEntityDictionary.
#
# The goal of this dictionary is to enable the efficient lookup of attributes. The following design
# decisions are relevant within this context:
#	+decomposition of dictionary
#		The keys of the dictionary need to be chosen such that one needs the least number of steps to
#		lookup an attribute.
#
#		In particular, attribute lookup is performed a tremendous number of times to resolve accesses.
#		I.e., an accesss has a destination attribute. Typical software
# 		systems have thousands of accesses, resulting in thousands of attribute lookups.
#
#		The information available on an attribute from within an access is merely the attribute name.
#			(a) local attribute:      class-name, attribute-name
#			(b) remote attribute:     attribute-name
#
# Accordingly, we use the attribute-name and class-name of an attribute as the main indices within the dictionary.
#
# self.dict[name] = dictionary indexed by the class-name.
#
# self.dict[name][className] = list of entityData, in which entityData
# provides all the available information on that entity.
#
# Currently, entityData consists of the following information:
# entityData = [type, sourceFile, lineNr,attrOwnerTemplateParameters]
#
# TODO: add the resolvedType and the visibility
#
# in which
#	type represents the type of the attribute
#	sourceFile represents the file in which the attribute was declared/implemented
#	lineNr represents the line number at which the attribute was declared
#	attrOwnerTemplateParameters represents the parameters of the attribute owner.
#		An example of such template parameters is "X". This string is obtained from the class declaration,
# 		in which the occurence of "class " or "typename " is replaced with "".
#
# To enable flexible extension of entityData, this data is contained in a list. Each entityData instance
# should contain at least the above elements. All methods of the dictionary should not make any assumptions
# on the total number of data elements contained in entityData, in the sense that the addition of elements
# should not have side-effects on the other methods. Concretely, this means that no method should use
# entityData[someIndex:]
##

from log4py import Logger

class AttributeDictionary:
	##
	# Initialize a dictionary.
	##
	def __init__(self):
		self.dict = {}
		self.log = Logger().get_instance(self)

	##
	# Does the dictionary contain any methods or functions with the given name?
	##
	def hasKey(self, attributeName):
		return (attributeName in self.dict)

	##
	# Retrieve the type of the entity.
	##
	def getType(self, entityData):
		return entityData[0]

	##
	# Retrieve the file in which the entity is declared.
	##
	def getSourceFile(self, entityData):
		return entityData[1]

	##
	# Retrieve the line number at which the entity is declared.
	##
	def getLineNr(self, entityData):
		return entityData[2]

	##
	# Retrieve the template parameters of the attribute owner.
	##
	def getAttrOwnerTemplateParameters(self, entityData):
		return entityData[3]

	##
	# Place attribute named attributeName, owned by class attrOwner in the dictionary. The type of this attribute is
	# attrType, and it is declared in declaringFile.
	#
	# key: attributeName
	# value: attrOwner x [ attrType , declaringFile, attrOwnerTemplateParameters ]
	##
	def add(self, name, owner, attrType, sourceFile, lineNr, attrTemplateParameters):
		added = False

		entityData = [ attrType , sourceFile, lineNr, attrTemplateParameters ]

		if not( name in self.dict ):
			self.dict[name]={}

		if not( owner in self.dict[name] ):
			self.dict[name][owner]=[]

		if not(entityData in self.dict[name][owner]) :
			self.dict[name][owner].append(entityData)
			added = True

		return added

	def retrieveEntityDataList(self, name, owner):
		entityDataList = []

		if name in self.dict:
			if owner in self.dict[name]:
				entityDataList = self.dict[name][owner]

		return entityDataList

	def getOwners(self, attributeName):
		owners=[]

		if ( attributeName in self.dict ):
			for owner in self.dict[attributeName]:
				owners.append(owner)

		return owners

	def getSourceFiles(self, name):
		sourceFiles=[]

		if ( name in self.dict ):
			for owner in self.dict[name]:
				for entityData in self.dict[name][owner]:
					sourceFile = self.getSourceFile(entityData)
					sourceFiles.append(sourceFile)

		return sourceFiles

	def getAttributeType(self, name, owner):
		attrType = None

		if ( (name in self.dict) and (owner in self.dict[name]) ):
			for entityData in self.dict[name][owner]:
				attrType = self.getType(entityData)
				continue

		return attrType

	def getTypes(self, name):
		attrTypes=[]

		if ( name in self.dict ):
			for owner in self.dict[name]:
				for entityData in self.dict[name][owner]:
					if not self.getType(entityData) in attrTypes:
						attrTypes.append(self.getType(entityData))

		return attrTypes


##
# Fills a given attribute dictionary with attributes.
##
class AttributeDictionaryFiller:
	attributeDictionary = None

	##
	# Initialize a dictionary.
	##
	def __init__(self, attributeDictionary):
		self.attributeDictionary = attributeDictionary

	def add(self, attrEntity):
		added = False

		assert self.attributeDictionary != None

		typeReference = attrEntity.getTypeReference()

		if ( attrEntity.name != "" and attrEntity.owner != "" and typeReference.getResolvedName() != None):
			added = self.attributeDictionary.add(attrEntity.name, attrEntity.owner, typeReference.getResolvedName(), attrEntity.sourceFile, attrEntity.start, attrEntity.ownerTemplateParameters)

		return added
