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

from cplusplus.data_types.dictionaries.NamespaceUsageDictionary import NamespaceUsageDictionary

##
# Dictionary for Class Usage Entities.
##
class ClassUsageDictionary(NamespaceUsageDictionary):
	def addReference(self, reference):
		added = False

		if reference == None:
			return False

		# (a) non-nested namespace
		if self.containsNamespaceNamed(reference.getName()):
			# (a.1) extension of an existing namespace
			referenceToBeExtended = self.getNamespaceNamed(reference.getName())
			added = referenceToBeExtended.extendWith(reference) # might have no effect

		else:
			# (a.2) new unrelated namespace
			self.references.append(reference)
			added = True

		if added:
			for srcFile in reference.getSourceFiles():
				self.fileInDictionaryCache[srcFile] = True
				if not self.referenceCache.has_key(srcFile): self.referenceCache[srcFile] = []
				self.referenceCache[srcFile].append(reference)

		return added
