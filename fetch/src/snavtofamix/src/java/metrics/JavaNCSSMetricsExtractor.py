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
# Copyright 2007   University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

from log4py 							import	Logger

from common.Timing import Timer
from common.SourceLocation import SourceLocation
from common.famix.cdifWriter import generateMetrics

from java.metrics.NCSSMetricEntity import NCSSMetricEntity

log = Logger().get_instance()

def parseMetrics(ncssDb, invEntDict):
	timer = Timer("Parsing ncss measurements")
	timer.start()

	nrOfMeasurements=0
	for line in open(ncssDb, "r"):
		if "Nr. " in line: continue
		if line.startswith("Average"): break

		ncssEnt = NCSSMetricEntity(line)
		
		if not ncssEnt.isKnownEntity(invEntDict):
			log.warn(	"Unknown invokable entity "+\
							ncssEnt.composeUniqName())
			log.warn("line: "+line)
			continue
		
		uniqName = ncssEnt.composeUniqName()
		sLoc = SourceLocation("", "0", "0")
		generateMetrics("CC", ncssEnt.metricCC, uniqName, sLoc)
		generateMetrics("COM", ncssEnt.metricCOM, uniqName, sLoc)
		generateMetrics("LOC", ncssEnt.metricLOC, uniqName, sLoc)
		nrOfMeasurements += 1

	timer.stop()
	timer.log(nrOfMeasurements)

