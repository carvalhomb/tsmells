#!/usr/bin/python
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
# Copyright 2006  University of Antwerp
# Author(s):Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>,
#


# Cache System storing strings

class Cache:

	def __init__(self):
		self.fCache = []

	def cache_containsEntry(self, uniqName):
		return uniqName in self.fCache

	def entriesStartingWith(self, prefix):
		results = []
		for entry in self.fCache:
			if entry.startswith(prefix):
				results.append(entry)
		return results

	def cache_addEntry(self, uniqName):
		if not uniqName in self.fCache:
			self.fCache.append(uniqName)

	def debug(self):
		print self.fCache
		print len(self.fCache)


