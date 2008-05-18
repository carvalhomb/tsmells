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

# This file declares a dictionary with, for any set of accesses a1..an
# appearing at the same line lineNrA in the same file fileA:
# keys: 	fileA x lineNrA
# values:	{TypeClass(a1)..TypeClass(an)}

from log4py import Logger

##
# Encapsulation of the dictionary.
##
class AccessDictionary:
	# initialize an empty dictionary
	dict = {}

	##
	# Initialize a dictionary.
	##
	def __init__(self):
		self.log = Logger().get_instance(self)
		pass

	##
	# Print the dictionary
	##
	def printContent(self):
		self.log.info("Dictionary has", len(self.dict), "elements:")
		for key in self.dict:
			self.log.info( "[",key,",",self.convertToList(self.dict[key]),"]")

	def createKey(self, fileName, lineNr):
		return fileName+":"+lineNr

	def hasKeyFor(self, fileName, lineNr):
		return self.hasKey(self.createKey(fileName,lineNr))

	def hasKey(self, key):
		return self.dict.has_key(key)

	def getValue(self, fileName, lineNr):
		assert self.hasKey(self.createKey(fileName, lineNr))
		return self.dict[self.createKey(fileName,  lineNr)]

	def getValueAsList(self, fileName, lineNr):
		return self.getValue(fileName, lineNr).split(",")

	def convertToList(self, value):
		return value.split(",")

	def add(self, fileName, lineNr, typeClass):
		if typeClass != "" and typeClass != None:
			key = self.createKey(fileName, lineNr)

			value = typeClass
			if self.dict.has_key(key) :
				value = self.dict[key]
				valueAsList=self.convertToList(value)

				if not typeClass in valueAsList:
					value=value+","+typeClass

			self.dict[key] = value

## main
if __name__ == "__main__":
	aDict = AccessDictionary()
	aDict.printContent()

	fileName="a.txt"
	lineNr=220
	key = aDict.createKey(fileName, lineNr)

	fileNameCheck, lineNrCheck = aDict.decomposeKey(key)
	assert fileNameCheck == fileName
	assert lineNrCheck == lineNr

	value = "aClass"

	print "Adding value", value, "for key", key, "to dictionary."
	aDict.add(fileName, lineNr, value)
	print "Value of key",key,"is:",aDict.getValue(fileName,lineNr)
	aDict.printContent()

	print "Dictionary has key",key,"?",aDict.hasKey(fileName,lineNr)
	unexistingFileName="unexisting"
	unexistingKey=aDict.createKey(unexistingFileName, lineNr)
	print "Dictionary has key",unexistingKey,"?",aDict.hasKey(unexistingFileName,lineNr)

	value = "anotherClass"
	print "Adding value", value, "for key", key, "to dictionary."
	aDict.add(fileName, lineNr, value)
	print "Value of key",key,"is:",aDict.getValue(fileName,lineNr)
	aDict.printContent()

	otherLineNr=221
	otherKey=aDict.createKey(fileName,otherLineNr)
	value="newClass"
	print "Adding value", value, "for key", otherKey, "to dictionary."
	aDict.add(fileName, otherLineNr, value)
	aDict.printContent()
