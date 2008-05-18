#!/usr/bin/python
# This file is part of snavtofamix.
#
# snavtofamix is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# snav2famix is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Anastacia; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
# Copyright 2007 University of Antwerp
# Author(s): Bart Van Rompaey - bart.vanrompaey2@ua.ac.be
#

from log4py 							import Logger

# reads metrics from ascii dumps
import common
from common.Timing import Timer
from common.SourceLocation import SourceLocation

def parseVCSMetrics(vcsDb,fileDict):
	""" reads in file change frequency metrics to FAMIX CDIF"""
	timer = Timer("Parsing VCS measurements")
	timer.start()

	nrOfMeasurements=0
	for line in open(vcsDb, "r"):
		cols = line.split(";")
		fileName = cols[0]
		cf = cols[1].strip()

		#print "VCS", fileName, cf
		if fileName in fileDict.list:
			sLoc = SourceLocation(fileName, "0", "0")
			common.famix.cdifWriter.generateMetrics("CF", cf, fileName, sLoc)
		else:
			log.warn(fileName+" not known to the file dict.")
		nrOfMeasurements += 1

	timer.stop()
	timer.log(nrOfMeasurements)

log = Logger().get_instance()
