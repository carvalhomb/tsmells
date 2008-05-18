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

from log4py import Logger, LOGLEVEL_DEBUG

##
# Base class for reports on the resolution of entities.
##
class ResolutionReport:
	##
	# Initialize the generic data.
	##
	def __init__(self, line):
		self.line = line.strip()
		self.entityType = "Undefined"
		self.logger = Logger().get_instance(self)
		self.logger.set_loglevel(LOGLEVEL_DEBUG)

	##
	# Return a string representation of the report.
	##
	def __repr__(self):
		assert False, "Should be implemented by subclass."

	##
	# Verify whether the resolution succeeded.
	##
	def resolutionSucceeded(self):
		assert False, "Should be implemented by subclass."


	def log(self):
		self.logger.debug("Failed to resolve " + self.entityType + ": " + self.line)
		self.logger.debug(self.__repr__())

	def logIfFlawed(self):
		if self.resolutionFeasible() and (not self.resolutionSucceeded()):
			self.log()

##
# Resolution report for references (invocations or accesses).
##
class ReferenceResolutionReport(ResolutionReport):
	def __init__(self, line):
		ResolutionReport.__init__(self, line)
		self.surroundingDefFound = False
		self.defLocFound = False
		self.destinationNameKnown = False
		self.nrOfFoundDestinations = 0
		self.nrOfIncludedDestinations = 0

	##
	# Was resolution feasible?
	# E.g., if no method/function is known with the given destination name,
	# then the resolution of an invocation is infeasible.
	##
	def resolutionFeasible(self):
		return self.destinationNameKnown

	##
	# Verify whether the resolution succeeded.
	##
	def resolutionSucceeded(self):
		success = self.surroundingDefFound and self.defLocFound
		success = success and self.destinationNameKnown
		success = success and (self.nrOfFoundDestinations == 1)
		success = success and (self.nrOfIncludedDestinations == 1)

	##
	# Return a string representation of the report.
	##
	def __repr__(self):
		repr = ""

		# Order messages by order in resolution algorithm.

		if not(self.surroundingDefFound):
			repr += "Parent entity not found."
		elif not(self.defLocFound):
			repr += "Source location of parent entity not found."
		elif not(self.destinationNameKnown):
			repr += "No entity known with destination name."
		elif self.nrOfFoundDestinations != 1:
			repr += "Found " + `self.nrOfFoundDestinations` + " potential destinations."
		elif self.nrOfIncludedDestinations != 1:
			repr += "Found " + `self.nrOfIncludedDestinations` + " included potential destinations."

		return repr

	##
	# Indicate that the method definition surrounding the source of the reference
	# is found.
	##
	def setSurroundingDefinitionFound(self):
		self.surroundingDefFound = True

	##
	# Indicate that the location of the definition surrounding the source of the
	# reference is found.
	##
	def setDefinitionLocationFound(self):
		self.defLocFound = True

	##
	# Indicate that no entity was found for the destination name.
	##
	def setDestinationNameKnown(self):
		self.destinationNameKnown = True

	##
	# Indicate that number entities were found corresponding
	# to the characterization of the destination.
	##
	def setNumberOfFoundDestinations(self, number):
		self.nrOfFoundDestinations = number

	##
	# Indicate that there are number entities included.
	# One tuple indicates a single declaration with potentially multiple definitions.
	##
	def setNumberOfIncludedDestinations(self, number):
		self.nrOfIncludedDestinations = number
