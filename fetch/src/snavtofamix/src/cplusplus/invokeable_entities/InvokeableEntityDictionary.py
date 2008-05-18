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



from common.entity_references.EntityReferences import \
                InvokableEntityReference, \
                MethodEntityReference
from log4py import Logger
from common.entity_references.MultipleLocationEntity import MultipleLocationEntity
from common.utils import intersection, union, setdifference

##
# Encapsulation of a dictionary of functions and or methods.
#
# The goal of this dictionary is to enable the efficient lookup of methods
# and functions. The following design decisions are relevant within this
# context:
#	+decomposition of dictionary
#		The keys of the dictionary need to be chosen such that one needs the least number of steps to
#		lookup a method/function.
#
#		In particular, method/function lookup is performed a tremendous number of times to resolve invocations.
#		I.e., an invocation has a source method/function and a destination method/function. Typical software
# 		systems have thousands of invocations, resulting in thousands of method lookups.
#
#		The information available on a method/function from within an invocation differs between the source
#		and the destination:
#			(a) source:      class-name (or "" for a function), method/function-name, actual parameters
#			(b) destination: merely method-name and actual parameters
#
#		Since the lookup of the source of a method/function can be kept in a cache (to be used for subsequent invocations
#		in the same method/function, the dictionary needs to be optimized for looking up the destination of
#		the invocation, for which only the method-name and actual parameters are known.
#
# Accordingly, we use the method-name and formal parameters of a method as the main indices within the dictionary.
# (The general idea is that the indices are composed of the signature of an entity, and the entityData consists
# of all the data that does not belong to the signature).
#
# self.objectDict[nameOfMethodOrFunction] = dictionary indexed by the formal parameters in string-format, termed
# formalParameterDescription as of now.
#
# self.dict[nameOfMethodOrFunction][formalParameterDescription] = list of InvokableEntityReference instances
# (or subclasses thereof)
#
# TODO: incorporate visibility information!
##
class InvokeableEntityDictionary:
	##
	# Initialize a dictionary.
	##
	def __init__(self):
		self.dict = {} # dictionary of references having multiple locations
		self.log = Logger().get_instance(self)

	##
	# Does the dictionary contain any methods or functions with the given name?
	##
	def hasMultiLocKey(self, nameOfMethodOrFunction):
		return self.dict.has_key(nameOfMethodOrFunction)


	def retrieveMultiLocReferencesFor(self, classNameOrEmpty, nameOfMethodOrFunction):
		references = []

		if self.hasMultiLocKey(nameOfMethodOrFunction):
			for formalPars in self.dict[nameOfMethodOrFunction]:
				for reference in self.dict[nameOfMethodOrFunction][formalPars]:
					if reference.getOwnerName() == classNameOrEmpty:
						references.append(reference)

		return references

	def retrieveAllMultiLocReferences(self):
		allReferences = []

		for name in self.dict:
			for formalParameters in self.dict[name]:
				for reference in self.dict[name][formalParameters]:
					allReferences.append(reference)

		return allReferences

	def retrieveMultiLocParameterDescriptions(self, nameOfMethodOrFunction):
		formalParameters = []

		if nameOfMethodOrFunction in self.dict:
			formalParameters = self.dict[nameOfMethodOrFunction].keys()

		return formalParameters


	##
	# Retrieve a list of references to entities satisfying the following name.
	# In case className is not empty (method case):
	#		namespaceName::className::name(formalParDescr)
	# Else:
	#		namespaceName::name(formalParDescr)
	#
	# Note: there can only be one such reference
	##
	def retrieveMultiLocReferenceToQualifiedEntity(self, namespaceName, className, name, formalParDescr):
		theReference = None

		if name in self.dict:
			if formalParDescr in self.dict[name]:
				for reference in self.dict[name][formalParDescr]:
					inNamespace = (reference.getNamespaceName() == namespaceName)
					inClass = (reference.getOwnerName() == className)
					if inClass and inNamespace:
						theReference = reference
						break

		return theReference

	##
	# Retrieve a list of references to invokeable entities
	# with the given name.
	##
	def retrieveMultiLocReferencesForName(self, nameOfMethodOrFunction):
		references = []

		if self.hasMultiLocKey(nameOfMethodOrFunction):
			for formalParameter in self.dict[nameOfMethodOrFunction]:
				for reference in self.dict[nameOfMethodOrFunction][formalParameter]:
					references.append(reference)

		return references

	##
	# Classes used should also incorporate superclasses.
	##
	def retrieveMultiLocReferencesForQualifiedName(self, allowedNamespaceNames, nameOfMethodOrFunction, classesUsed=[]):
		references = []

		namedReferences = self.retrieveMultiLocReferencesForName(nameOfMethodOrFunction)
		for reference in namedReferences:
			if reference.getNamespaceName() in allowedNamespaceNames:
				references.append(reference)
			elif reference.getNamespaceName() != "":
				if reference.getOwnerName() in classesUsed:
					references.append(reference)

		return references

	def retrieveMultiLocReferencesForQualifiedNameAndClass(self, allowedNamespaceNames, classNameOrEmpty, nameOfMethodOrFunction):
		selectedReferences = []

		references = self.retrieveMultiLocReferencesForQualifiedName(allowedNamespaceNames, nameOfMethodOrFunction)

		if classNameOrEmpty == "":
			return references

		# else: filter using className
		for reference in references:
			if reference.getOwnerName() == classNameOrEmpty:
				selectedReferences.append(reference)

		return selectedReferences

	def addMultiLocReference(self, reference):
		if reference.getName() == "":
			return False

		added = False

		if not(self.hasMultiLocKey(reference.getName())):
			self.dict[reference.getName()] = {}

		oldReferenceList = []

		# if the dictionary does not contain any methods or functions with the given name and parameters yet
		if not(reference.getFormalParameters() in self.dict[reference.getName()]):
			self.dict[reference.getName()][reference.getFormalParameters()] = []
		else:
			oldReferenceList = self.dict[reference.getName()][reference.getFormalParameters()]
		newMultiLoc = reference.getLocation()

		isNewEntity = True
		for oldRef in oldReferenceList:
			#if oldRef.getUniqueName() == reference.getUniqueName():
			if oldRef == reference:
				isNewEntity = False

				# append the new locations
				oldMultiLoc = oldRef.getLocation()
				oldDefs = oldMultiLoc.getDefinitionLocations()
				oldDecls = oldMultiLoc.getDeclarationLocations()
				newDecls = newMultiLoc.getDeclarationLocations()
				setDiff = setdifference(newDecls, oldDecls)

				if not(not setDiff):
					for location in setDiff:
						oldMultiLoc.addDeclaration(location)

					added = True

				oldDefs = oldMultiLoc.getDefinitionLocations()
				newDefs = newMultiLoc.getDefinitionLocations()
				setDiff = setdifference(newDefs, oldDefs)

				if not(not setDiff):
					for location in setDiff:
						oldMultiLoc.addDefinition(location)

					added = True

				# since no other instances with this unique name can be
				# present, we leave the loop
				break

		if isNewEntity:
			self.dict[reference.getName()][reference.getFormalParameters()].append(reference)
			added=True
		return added