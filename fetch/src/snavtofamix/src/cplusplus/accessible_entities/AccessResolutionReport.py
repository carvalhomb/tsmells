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

from cplusplus.generic import ReferenceResolutionReport

class AccessResolutionReport(ReferenceResolutionReport):
	def __init__(self, line):
		ReferenceResolutionReport.__init__(self, line)
		self.entityType = "Access"
		self.validDestinationName = False
		self.destinationClassKnown = False
		self.validDestinationType = False

	##
	# Return a string representation of the report.
	##
	def __repr__(self):
		repr = ReferenceResolutionReport.__repr__(self)

		# Order messages by order in resolution algorithm

		if not self.validDestinationName :
			repr += "Destination name is invalid."
		elif self.destinationClassKnown and not(self.validDestinationType):
			repr += "Destination type is invalid."

		return repr

	def setValidDestinationName(self):
		self.validDestinationName = True

	def setDestinationClassKnown(self):
		self.destinationClassKnown = True

	def setValidDestinationType(self):
		self.validDestinationType = True
