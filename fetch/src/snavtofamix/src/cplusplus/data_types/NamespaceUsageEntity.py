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
# Copyright 2007   University of Antwerp
# Author(s): Bart Du Bois <bart.dubois@ua.ac.be>


from cplusplus.data_types.DataRecords import Entity
from cplusplus.data_types.NamespaceEntity import NamespaceEntity, \
											NamespaceEntityReference

## STATIC
# Verifies whether the given line represents a namespace usage,
# and can therefore be given to initialize a NamespaceUsageEntity.
##
def isValidNamespaceUsageLine(line):
    isValid = True

    dummyEntity = NamespaceUsageEntity(line)

    if len(dummyEntity.cols) != 4:
        isValid = False

    if line.strip().split("\t")[0] != "using_namespace":
    	isValid = False

    return isValid

##
# Encapsulation of a namespace usage entity.
#
# We model a Namespace Usage Entity as a subclass
# of a Namespace Entity, since both provide similar information
# and behavior. I.e., both define a scope, and both require services
# to identify whether a source location is contained within this scope.
##
class NamespaceUsageEntity(NamespaceEntity):
	##
	# Initialize an entity using a given line.
	##
	def __init__(self, line):
		Entity.__init__(self, line) # does not accurately decompose line into cols
		self.cols=self.line.split("\t")[1:] # first column == "using_namespace"

	def createReference(self):
		return NamespaceUsageEntityReference()

class NamespaceUsageEntityReference(NamespaceEntityReference):
	pass
