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
# Copyright 2006,2007 University of Antwerp
# Author(s): Bart Du Bois <bart.dubois@ua.ac.be>

from common.utils import normalizeFileName


def buildFileDict(fileDb, fileDict):
	""" Reads files from SN's file table into a dictionary """
	fileDbFile = open(fileDb, "r")

	for line in fileDbFile:
		fileName = line.strip().split(";")[0]
		fileDict.add(fileName)

	fileDbFile.close()

##
# Dictionary serving the purpose of matching filenames.
# Purpose:
# Sometimes include directives refer to a non-nested file name,
# while in fact the file is nested.
# By  using the getSimilarNamedFiles method, one can find out
# which files actually correspond.
##
class FileDictionary:
	def __init__(self):
		self.list = []

	def add(self, absoluteFilePath):
		normalizedFilePath = normalizeFileName(absoluteFilePath)
		if not normalizedFilePath in self.list:
			self.list.append(normalizedFilePath)
			return True

		return False

	##
	# Return a list of absolute file paths of files which
	# have the same fileName as the given absolute file path.
	#
	# E.g.: if the dictionary content is {a/b.h, b.h, c.h}
	# then the call getSimilarNamedFiles("d/b.h")
	# will return ["a/b.h", "b.h"]
	##
	def getSimilarNamedFiles(self, fileName):
		similarNamedFiles = []

		for absFileName in self.list:
			if absFileName == fileName:
				if not absFileName in similarNamedFiles:
					similarNamedFiles.append(absFileName)
			elif absFileName.endswith("/" + fileName):
				if not absFileName in similarNamedFiles:
					similarNamedFiles.append(absFileName)

		return similarNamedFiles