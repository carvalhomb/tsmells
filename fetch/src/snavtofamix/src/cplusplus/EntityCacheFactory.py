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
# Copyright 2007   University of Antwerp
# Author(s): Bart Du Bois <bart.dubois@ua.ac.be>

from data_types.DataRecords import DepricatedClassReferenceEntity

# builds and returns a dictionary of local variables
# found by Source Navigator and reported in the
# references table of its database.
def buildAndReturnLocalVarCacheFromDb(lvDb):
	localVarCache = {}
	inf=open(lvDb, 'r')
	for line in inf:
		if "cl" in line.split(";")[2]:
			clRef = DepricatedClassReferenceEntity(line)
			signature = clRef.sourceSignature()
			#print signature
			if signature in localVarCache:
				localVarCache[signature].append(clRef.dst_class)
			else:
				localVarCache[signature] = [ clRef.dst_class ]
	inf.close()
	return localVarCache

