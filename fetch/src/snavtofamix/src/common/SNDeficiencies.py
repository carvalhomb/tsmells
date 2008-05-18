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
# Copyright 2007  University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

def isEmptySignature(signature):
    return "" == signature

def qualifiesForLostParameterDeficiency(signature):
	""" If method parameterless then qualifies for SN deficiency """
	return isEmptySignature(signature)

def generateIntStringCombinations(combs,sign,i,size):
	combs = []
	stringExt = ""
	intExt = ""

	if i < size:
		if "" == sign:
			stringExt = addStringParameter("")
			intExt = addIntParameter("")
			combs.append(stringExt)
			combs.append(intExt)
		else:
			stringExt = sign + "," + addStringParameter("")
			intExt = sign + "," + addIntParameter("")
			combs.append(stringExt)
			combs.append(intExt)

		combs.extend(generateIntStringCombinations(combs,stringExt,i+1,size))
		combs.extend(generateIntStringCombinations(combs,intExt,i+1,size))
		return combs
	return combs


def addStringParameter(signature):
	""" Add a string to the param list and check existance again """
	signature = "String"
	return signature

def addIntParameter(signature):
	""" Add an int to the param list and check existance again """
	signature = "int"
	return signature
