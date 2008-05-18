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
# Copyright 2006,2007 University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>,
#                     Bart Du Bois <bart.dubois@ua.ac.be>


from cplusplus.data_types.dictionaries.includeDict import adjustPath
from cplusplus.data_types.DataRecords import Entity
from common.SourceLocation import SourceLocation

##
# Encapsulation of an entity.
##
class IncludeEntity(Entity):
	def __init__(self, includeLine):
		Entity.__init__(self, includeLine)
		self.cols = self.line.split(":")

		self.includingFile = self.cols[0]
		self.includeLineNr = self.cols[1]
		self.includedFile = self.cols[2]

		if "<" in self.includedFile:
			# library case, e.g.: #include <string>
			self.includedFile = self.includedFile.split("<")[1].split(">")[0]
		elif "\"" in self.includedFile:
			# normal case, e.g.: #include "util.h"
			self.includedFile = self.includedFile.split("\"")[1]
			self.includedFile = adjustPath(self.includingFile, self.includedFile)
		# in case this happens:
		# gtk+/gdk/linux-fb/gdkprivate-fb.h:42:#include FT_FREETYPE_H
		# glib/gthread/gthread_impl.c:46:#include G_THREAD_SOURCE
		else:
			pass

	def getIncludingFile(self):
		return self.includingFile

	def getSourceLocation(self):
		return SourceLocation(self.includingFile, self.includeLineNr, \
							self.includeLineNr)

	def getIncludedFile(self):
		return self.includedFile

	def updateIncludedFile(self, includedFile):
		self.includedFile = includedFile

	def removeRelativePathPart(self):
		tmp = self.includedFile
		while tmp.startswith("."):
			tmp = tmp[1:]
		self.includedFile = tmp
