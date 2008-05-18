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
# Author(s): Bart Du Bois <bart.dubois@ua.ac.be>

def getParentNamespaceName(qualifiedClassName):
	parentNamespaceName = ""

	if "::" in qualifiedClassName:
		parentList = qualifiedClassName.split("::")
		nrOfParts = len(parentList)
		parentNamespaceName = "::".join(parentList[0:nrOfParts-1])

	return parentNamespaceName

def getNonQualifiedName(qualifiedClassName):
	className = qualifiedClassName

	if "::" in qualifiedClassName:
		parentList = qualifiedClassName.split("::")
		nrOfParts = len(parentList)
		className = parentList[nrOfParts-1]

	return className
