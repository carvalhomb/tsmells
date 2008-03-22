#!/usr/bin/python
#
# This file is part of TSmells
#
# TSmells is free software; you can redistribute it and/or modify it 
# under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 2 of the License, or (at your 
# option) any later version.
#
# TSmells is distributed in the hope that it will be useful, but WITHOUT 
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more 
# details.
#
# You should have received a copy of the GNU General Public License along 
# with TSmells; if not, write to the Free Software Foundation, Inc., 
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA 
#
# Copyright 2007-2008 Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>
#

def isSourceLine(toCheck):
	""" verify if toCheck is a legal source line number
		line numbers should be integers and strict positive """
	try:
		int(toCheck)
		return int(toCheck) > 0

	except:
		return False

def isCount(toCheck):
	""" verify if toCheck is a legal count
		counts should be integers and positive """
	try:
		int(toCheck)
		return int(toCheck) >= 0

	except:
		return False

def filterComma(toFilter):
	return toFilter.replace(',', ';')

class SourceEntityz(object):
	''' Abstract root entity for smells & test entities '''
	def __init__(self):
		self.dict = dict() # { qualName x [(file, line), ... ] }

	def hasKey(self, qualifiedName):
		return self.dict.has_key(qualifiedName)

	def getLocation(self, qualifiedName):
		"""
		pre:
			self.hasKey(qualifiedName)
		"""
		return self.dict[qualifiedName][0]

	def appendMetricInfo(self, metricDict):
		pass
