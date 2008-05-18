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

from cplusplus.data_types.NamespaceEntity import NamespaceEntity, \
												NamespaceEntityReference
from cplusplus.data_types.NamespaceUsageEntity import NamespaceUsageEntityReference



##
# Dictionary containing NamespaceEntityReference instances.
##
class NamespaceDictionary:
	def __init__(self):
		self.references = []
		self.fileInDictionaryCache = {} # fileName x boolean
		self.referenceCache = {} # fileName x [ reference ]

	##
	# Add a reference to the given entity.
	##
	def add(self, namespaceEntity):
		reference = namespaceEntity.getReference()
		return self.addReference(reference)

	def addReference(self, reference):
		added = False

		if reference == None:
			return False

		parentRef = self.getParent(reference)
		childRefs = self.getChildren(reference) # must be queried before adding

		# NOTE: we do not wish to nest namespace usages
		if (parentRef == None) | (isinstance(reference, NamespaceUsageEntityReference)):
			# (a) non-nested namespace
			if self.containsNamespaceNamed(reference.getName()):
				# (a.1) extension of an existing namespace
				referenceToBeExtended = self.getNamespaceNamed(reference.getName())
				added = referenceToBeExtended.extendWith(reference) # might have no effect
			else:
				# (a.2) new unrelated namespace
				self.references.append(reference)
				added = True

		else:
			# (b) nested namespace
			reference.nestNameWithin(parentRef)
			self.references.append(reference)
			added = True

		# if we need to adapt the children too
		if added and not(not childRefs):
			# nest the name of each child
			for childRef in childRefs:
				childRef.nestNameWithin(reference)
		
		if added:
			for srcFile in reference.getSourceFiles():
				self.fileInDictionaryCache[srcFile] = True
				if not self.referenceCache.has_key(srcFile): self.referenceCache[srcFile] = []
				self.referenceCache[srcFile].append(reference)

		return added

	def getParent(self, reference):
		parentRef = None

		for curReference in self.references:
			if curReference.surrounds(reference):
				if (parentRef == None) or parentRef.surrounds(curReference):
					parentRef = curReference

		return parentRef

	def getChildren(self, reference):
		childRefs = []

		for curReference in self.references:
			if reference.surrounds(curReference):
				childRefs.append(curReference)

		return childRefs

	##
	# Return the NamespaceEntityReference that is the most deeply
	# nested namespace that surrounds the given location
	##
	def getDeepestSurroundingNamespace(self, sourceLocation):
		deepestSurroundingNamespace = None

		for curReference in self.references:
			if curReference.surroundsLocation(sourceLocation):
				if deepestSurroundingNamespace == None:
					deepestSurroundingNamespace = curReference
				elif deepestSurroundingNamespace.surrounds(curReference):
					deepestSurroundingNamespace = curReference

		return deepestSurroundingNamespace

	def containsNamespaceNamed(self, name):
		return (self.getNamespaceNamed(name) != None)

	def getNamespaceNamed(self, name):
		reference = None

		for curReference in self.references:
			if curReference.getName() == name:
				reference = curReference
				break

		return reference

	def fileInDictionary(self, fileName):
		try:
			return self.fileInDictionaryCache[fileName]
			
		except KeyError:
			inDictionary = False

			for curReference in self.references:
				if curReference.involves(fileName):
					inDictionary = True
					break
			self.fileInDictionaryCache[fileName] = inDictionary
			
			return inDictionary

	def getReferences(self, fileName):
		try:
			return self.referenceCache[fileName]
			
		except KeyError:
			references = []
			
			for reference in self.references:
				if reference.involves(fileName):
					references.append(reference)
			self.referenceCache[fileName] = references
			
			return references

	def getAllReferences(self):
		return self.references

	def getNumberOfEntities(self):
		return len(self.references)
		
