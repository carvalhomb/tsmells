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
# Copyright 2006,2007  University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>,
#                     Bart Du Bois <bart.dubois@ua.ac.be>

import time, sys

from log4py import Logger

##
# Enables time measurement.
#
# Typical usage:
#
# timer = Timer("Parsing method declarations")
# timer.start()
# nrOfMethods = parseMethods(...)
# timer.stop()
# timer.log(nrOfMethods)
##
class Timer:
	def __init__(self, activityDescription):
		if sys.platform == "win32":
			self.timer = time.clock
		else:
			self.timer = time.time

		self.begin = self.end = 0
		self.activityDescription = activityDescription
		self.logger = Logger().get_instance(self)

	##
	# Starts the timer.
	##
	def start(self):
		self.begin = self.timer()
	##
	# Stop the timer.
	##
	def stop(self):
		self.end = self.timer()

	def log(self, nrOfWorkItems):
		self.logger.info(self.activityDescription + " took "\
		 + `self.milli()` + " ms for " + `nrOfWorkItems` + " work items, or " \
		 + `self.getMillisPerItem(nrOfWorkItems)` + " ms per item.")

	def seconds(self):
		return self.milli() / 1000

	def getMillisPerItem(self, nrOfItems):
		millisPerItem = 0

		if nrOfItems != 0:
			millisPerItem = float(self.milli()) / float(nrOfItems)

		return millisPerItem

	def milli(self):
		return self.micro() / 1000

	def micro(self):
		return int((self.end - self.begin) * 1000000)
