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
# Copyright 2007  University of Antwerp
# Author(s): Bart Du Bois <bart.dubois@ua.ac.be>


from cplusplus.data_types.DataRecords import Entity
from cplusplus.data_types.NamespaceUsageEntity import NamespaceUsageEntity
from cplusplus.data_types.NamespaceEntity import NamespaceEntityReference
from common.SourceLocation import SourceLocation

## STATIC
# Verifies whether the given line represents a class usage,
# and can therefore be given to initialize a ClassUsageEntity.
##
def isValidClassUsageLine(line):
	dummyEntity = ClassUsageEntity(line)

	if len(dummyEntity.cols) != 4:
		return False

	if line.strip().split("\t")[0] == "using_namespace":
		return False

	return True

class ClassUsageReference(NamespaceEntityReference):
	def __init__(self):
		NamespaceEntityReference.__init__(self) # reuse constructor
		self.parentNamespaceName = None

	def getSourceLocation(self):
		return self.getSourceLocations()[0]

	def getSourceFile(self):
		return self.getSourceLocation().getSourceFile()

	def getLineNr(self):
		return self.getSourceLocation().getStart()

	##
	# Specify the name of the namespace containing this class usage.
	# In case the parents name is "", it is not contained in a namespace.
	##
	def setParentNamespaceName(self, parentNamespaceName):
		self.parentNamespaceName = parentNamespaceName

	##
	# Verify whether this class usage reference is contained in a namespace.
	##
	def isContainedInANamespace(self):
		return (self.parentNamespaceName != None) and (self.parentNamespaceName != "")

##
# Encapsulation of a class usage entity.
#
# We model a Class Usage Entity as a subclass
# of a Namespace Usage ntity, since both provide similar information
# and behavior. I.e., both define a scope, and both require services
# to identify whether a source location is contained within this scope.
##
class ClassUsageEntity(NamespaceUsageEntity):
	##
	# Initialize an entity using a given line.
	##
	def __init__(self, line):
		NamespaceUsageEntity.__init__(self, line) # reuse constructor

	def getReference(self):
		reference = ClassUsageReference()
		reference.setName(self.getNamespaceName())
		sourceLocation = SourceLocation(self.getDeclaringFileName(), \
									self.getStartLineNumber(), \
									self.getEndLineNumber())
		reference.addSourceLocation(sourceLocation)
		return reference
