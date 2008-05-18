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
from common.utils import normalizeFileName

##
# Selects sourceFiles that are qualified by their absolute path (from the project
# root from a list of source files extracted from preprocessor #include directives
# where files are typically qualified by a relative path.
##
#def selectSourceFileMatches(sourceFile,includeList):
#	print sourceFile, includeList


##
# Adjusts the given relative file name "referencedFileName" according to the given "currentFileName".
#
# E.g., if referencedFileName="../file.h" and currentFileName="MyDir/MySubDir/file.cpp" then
# the result will be "MyDir/file.h".
#
# Does not adjust the path in case:
# 	(a) referencedFileName starts with "/"
#	(b) referencedFileName does not start with "../" or with "./", yet does contain a "/"
##
def adjustPath(currentFileName, referencedFileName):
	currentFileName = normalizeFileName(currentFileName)
	referencedFileName = normalizeFileName(referencedFileName)

	adjustedPath=""

	shouldLeaveAsAbsolute = referencedFileName.startswith("/")
	shouldLeaveAsAbsolute = referencedFileName.startswith("/") or (("/" in referencedFileName) and not(referencedFileName.startswith("../")))

	if ( shouldLeaveAsAbsolute ):
		adjustedPath=referencedFileName
	else:
		currentPath=currentFileName.split("/")
		currentPath=currentPath[0:len(currentPath)-1]

		referencedFilePath=referencedFileName.split("/")
		mereReferencedFileName=referencedFilePath[len(referencedFilePath)-1]
		referencedFilePath=referencedFilePath[0:len(referencedFilePath)-1]

		for pathElement in referencedFilePath:
			if ( pathElement == ".." ):
				# if we can still peal of one directory from the currentPath
				if ( not(not currentPath) ):
					currentPath=currentPath[0:len(currentPath)-1]
				else:
					currentPath.append("..")
			else:
				currentPath.append(pathElement)

		for pathElement in currentPath:
			adjustedPath+=pathElement+"/"

		adjustedPath+=mereReferencedFileName

	return adjustedPath

##
# Encapsulation of an include dictionary.
# All file names are normalized, as to maximally remove .. and . path elements.
# Also, filenames are converted to an "absolute path".
# E.g., if a file Src/A/a.cpp includes a file ../../Inc/A/A.h, the value for
# key "Src/A/A.cpp" will be "Inc/A/A.h".
#
# Key: includingFile
# Value: [includedFile1,...,includedFile]
##
class IncludeDictionary:
	##
	# Initialize a dictionary.
	##
	def __init__(self):
		# initialize an empty dictionary
		self.dict = {}
		self.log = Logger().get_instance(self)

	##
	# Does filename include other files?
	##
	def hasKey(self, fileName):
		normalizedFileName=normalizeFileName(fileName)
		return (normalizedFileName in self.dict )

	##
	# Retrieve a list of files included directly by the given file
	##
	def getIncludedFiles(self, includingFile):
		includedFiles = []

		normalizedIncludingFile=normalizeFileName(includingFile)

		if ( normalizedIncludingFile in self.dict ):
			includedFiles = self.dict[normalizedIncludingFile]

		return includedFiles

	##
	# Retrieve a list of files included directly and indirectly by the given file.
	##
	def getTransitiveIncludedFiles(self, includingFile):
		includedFiles = []
		normalizedIncludingFile=normalizeFileName(includingFile)
		self.getTransitiveIncludedFilesInternal(normalizedIncludingFile, includedFiles)
		return includedFiles

	def getTransitiveIncludedFilesInternal(self, includingFile, resultSet):
		additionalResultSet = []
		if self.hasKey(includingFile):
			for includedFile in self.getIncludedFiles(includingFile):
				if not ((includedFile in resultSet) or (includedFile in additionalResultSet)):
					resultSet.append(includedFile)
					additionalResultSet.append(includedFile)

		for includedFile in additionalResultSet:
			self.getTransitiveIncludedFilesInternal(includedFile, resultSet)

	def addEntity(self, includeEntity):
		return self.add(includeEntity.getIncludingFile(), \
					includeEntity.getIncludedFile())

	##
	# Appends includedFile to the list of files included by includingFile.
	#
	# @returns True/False indicating whether the includedFile was added
	##
	def add(self, includingFile, includedFile):
		if ( includingFile == None ) or ( includedFile == None ):
			self.log.error("Preconditions violated: ",[includingFile,includedFile])
			return False

		added = False

		normalizedIncludingFile=normalizeFileName(includingFile)
		adjustedIncludedFile=adjustPath(normalizedIncludingFile,includedFile)

		if not(normalizedIncludingFile in self.dict) :
			self.dict[normalizedIncludingFile]=[]

		if not (adjustedIncludedFile in self.dict[normalizedIncludingFile]):
			self.dict[normalizedIncludingFile].append(adjustedIncludedFile)
			added = True

		return added

	##
	# Print the contents of the dictionary.
	##
	def printContent(self):
		self.log.info( "Dictionary has", len(self.dict), "elements:")
		for key in self.dict:
			self.log.info( "[",key,",",self.getIncludedFiles(key),"]")

##
# Encapsulation of an include dictionary.
# Copies the content of a given include dictionary in such a way that
# requests for transitive included files no longer require a calculation.
#
# Key: includingFile
# Value: [transIncludedFile1,...,transIncludedFileN]
##
class TransitiveIncludeDictionary:
	##
	# Initialize a transitive include dictionary given an include dictionary.
	##
	def __init__(self, includeDictionary):
		# initialize an empty dictionary
		self.dict = {}
		self.includeDict = includeDictionary

		self.calculateTransitiveIncludes()

		self.includeDict = None
		self.log = Logger().get_instance(self)


	##
	# Add the transitive included files for each including file.
	# Includes the including file as its own transitive included file.
	##
	def calculateTransitiveIncludes(self):

		for includingFile in self.includeDict.dict:
			transitiveIncludedFiles=self.includeDict.getTransitiveIncludedFiles(includingFile)
			if not(includingFile in transitiveIncludedFiles):
				transitiveIncludedFiles.append(includingFile)

			self.dict[includingFile] = transitiveIncludedFiles

	##
	# Does filename include other files?
	##
	def hasKey(self, fileName):
		normalizedFileName=normalizeFileName(fileName)
		return (normalizedFileName in self.dict )

	##
	# Retrieve a list of files included directly and indirectly by the given file.
	# The returned list will at least include the given includingFile.
	##
	def getTransitiveIncludedFiles(self, includingFile):
		includedFiles = []
		normalizedIncludingFile=normalizeFileName(includingFile)

		if ( normalizedIncludingFile in self.dict ):
			includedFiles = self.dict[normalizedIncludingFile]

		if not( includingFile in includedFiles ):
			includedFiles.append(normalizedIncludingFile)


		return includedFiles

	def foundConfidentSymlinkMatch(self,sourceFile,transIncludeDict):
		min_conf = 2
		if self.trySymlinkPath(sourceFile, transIncludeDict) >= min_conf:
			return True
		else:
			return False

	##
	# A header file can be included with a shorter path, or a seemingly
	# different path that is actuall shortened or changed by the use of symlinks.
	# The build system is responsible for defining the right include directives.
	#
	#
	##
	def trySymlinkPath(self,sourceFile,transIncludeDict):
		""" tries to resolve file path with presumed symlink by comparing \
			to a list of absolute paths, indicating the number of path part
			matches by a confidence number """
		conf = 0 # number of matching directories for best match

		for i in transIncludeDict:
			tmpConf = self.computePathSimilarity(sourceFile,i)

			if tmpConf > conf:
				conf = tmpConf

		return conf

	def computePathSimilarity(self,path1,path2):
		""" computes number of similar entries (file name, directories) in two\
			paths, starting from the back"""

		#print path1, path2
		sep = "/"
		path1Cols = path1.split(sep)
		path2Cols = path2.split(sep)
		path1Len = len(path1Cols) - 1
		path2Len = len(path2Cols) - 1
		path1Name = ""
		path2Name = ""
		conf = 0
		while path1Len >= 0 and path2Len >= 0:
				path1Name = path1Cols[path1Len] + sep + path1Name
				path2Name = path2Cols[path2Len] + sep + path2Name
				#print "  ",path1Name,path2Name
				if path1Name == path2Name:
					conf = conf + 1
				path1Len = path1Len - 1
				path2Len = path2Len - 1
		return conf
