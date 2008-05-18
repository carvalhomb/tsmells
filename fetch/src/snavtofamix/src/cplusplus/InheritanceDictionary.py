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

import sys
from log4py import Logger

##
# Encapsulation of an inheritance dictionary.
#
# Key: subclass
# Value: [superClass1,...,superClassN]
##
class InheritanceDictionary:
	##
	# Initialize a dictionary.
	##
	def __init__(self):
		# initialize an empty dictionary
		self.dict = {}
		self.log = Logger().get_instance(self)


	##
	# Does subclass have any superclasses?
	##
	def hasKey(self, subclassName):
		return (subclassName in self.dict )

	##
	# Retrieve the list of superclasses of the given className
	##
	def getSuperclasses(self, className):
		superclasses = []

		if ( className in self.dict ):
			superclasses = self.dict[className]

		return superclasses

	##
	# Retrieve the list of transitive superclasses of the given className
	##
	def getTransitiveSuperclasses(self, className):
		superclasses = []
		self.getTransitiveSuperclassesInternal(className, superclasses)
		return superclasses

	def getTransitiveSuperclassesInternal(self, className, resultSet):
		additionalResultSet = []
		if (className in self.dict):
			for superclassName in self.getSuperclasses(className):
				if not ((superclassName in resultSet) or (superclassName in additionalResultSet)):
					resultSet.append(superclassName)
					additionalResultSet.append(superclassName)

		for superclassName in additionalResultSet:
			self.getTransitiveSuperclassesInternal(superclassName, resultSet)

	##
	# Appends includedFile to the list of files included by includingFile.
	##
	def add(self, subclassName, superclassName):
		if not(subclassName in self.dict) :
			self.dict[subclassName]=[]

		if not (superclassName in self.dict[subclassName]):
			self.dict[subclassName].append(superclassName)

	##
	# Print the contents of the dictionary.
	##
	def printContent(self):
		self.log.info( "Dictionary has", len(self.dict), "elements:")
		for key in self.dict:
			self.log.info( "[",key,",",self.getSuperclasses(key),"]")

##
# Encapsulation of an inheritance dictionary.
# Copies the content of a given include dictionary in such a way that
# requests for transitive superclasses no longer require a calculation.
#
# Key: subclassName
# Value: [transSuperclass1,...,transSuperclassN]
##
class TransitiveInheritanceDictionary:
	##
	# Initialize a transitive inheritance dictionary given an include dictionary.
	##
	def __init__(self, inheritanceDictionary):
		# initialize an empty dictionary
		self.dict = {}
		self.inhDict = inheritanceDictionary

		self.calculateTransitiveSuperclasses()

		self.inhDict = None
		self.log = Logger().get_instance(self)


	##
	# Add the transitive included files for each including file.
	##
	def calculateTransitiveSuperclasses(self):
		for subclass in self.inhDict.dict:
			transitiveSuperclasses=self.inhDict.getTransitiveSuperclasses(subclass)
			self.dict[subclass] = transitiveSuperclasses

	##
	# Is the dictionary aware of any superclasses of the given className
	##
	def hasKey(self, className):
		return (className in self.dict )

	##
	# Retrieve a list of names of transitive superclasses of the given class-name.
	##
	def getTransitiveSuperclasses(self, className):
		superclasses = []

		if ( className in self.dict ):
			superclasses = self.dict[className]

		return superclasses

	##
	# Verify whether the given superclass-name is a known (transitive) superclass of the given subclass-name.
	##
	def isSubclassOf(self, subclassName, superclassName):
		return (superclassName in self.getTransitiveSuperclasses(subclassName))

	##
	# Print the contents of the dictionary.
	##
	def printContent(self):
		self.log.info( "Dictionary has", len(self.dict), "elements:")
		for key in self.dict:
			self.log.info( "[",key,",",self.getTransitiveSuperclasses(key),"]")

## main
if __name__ == "__main__":
	inhDict = InheritanceDictionary()
	subclassName="AsubClass"
	superclassName="AsuperClass"
	inhDict.add(subclassName, superclassName)
	assert len(inhDict.getSuperclasses(subclassName)) == 1, "Set of superclasses not as expected."
	assert inhDict.getSuperclasses(subclassName)[0] == superclassName, "Superclass not as expected."
	assert (inhDict.getTransitiveSuperclasses(subclassName) == inhDict.getSuperclasses(subclassName) ), "Set of transitive superclasses not as expected."

	prevSubclassName=subclassName
	prevSuperclassName=superclassName
	subclassName=superclassName
	superclassName="AsuperSuperClass"
	inhDict.add(subclassName, superclassName)
	assert len(inhDict.getSuperclasses(subclassName)) == 1, "Set of superclasses not as expected."
	assert inhDict.getSuperclasses(subclassName)[0] == superclassName, "Superclass not as expected."
	assert (inhDict.getTransitiveSuperclasses(subclassName) == inhDict.getSuperclasses(subclassName) ), "Set of transitive superclasses not as expected."
	assert (prevSuperclassName in inhDict.getTransitiveSuperclasses(prevSubclassName))
	assert (superclassName in inhDict.getTransitiveSuperclasses(prevSubclassName))

#	inhDict.printContent()

	oldinhDict = inhDict.dict.copy()
	transInhDictionary = TransitiveInheritanceDictionary(inhDict)
	assert (inhDict.dict == oldinhDict), "Construction of transitive inheritance dictionary corrupted inheritance dictionary."

	assert (len(transInhDictionary.getTransitiveSuperclasses(prevSubclassName)) == 2), "Number of transitive superclasses incorrect."
	assert ( transInhDictionary.getTransitiveSuperclasses(prevSubclassName)[0] == prevSuperclassName), "Transitive superclass incorrect."
	assert ( transInhDictionary.getTransitiveSuperclasses(prevSubclassName)[1] == superclassName), "Transitive superclass incorrect."

	assert ( transInhDictionary.isSubclassOf(subclassName, superclassName) )
	assert ( transInhDictionary.isSubclassOf(prevSubclassName, subclassName) )
	assert ( transInhDictionary.isSubclassOf(prevSubclassName, superclassName) )

#	transInhDictionary.printContent()

	print "Test OK."
