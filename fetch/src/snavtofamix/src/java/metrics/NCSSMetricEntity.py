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

from log4py import Logger

class NCSSMetricEntity:
	""" represents a measurement for an invokable entity reported by ncss """
	sep = "."  # separator for pkg, class and method
	pkgSep = "::" # CDIF pkg separator

	# ID     LOC CC COM namespace.class.method(params)
	#--------------------------------------------------
	# 224    4   1    1 messenger.ICQ.ICQCLI_GOODBYEPacket.ICQCLI_GOODBYEPacket(Vector)
	# 225    3   1    1 messenger.ICQ.ICQCLI_GOODBYEPacket.updatePacket()
	# 226    9   1    1 messenger.ICQ.ICQCLI_IDENTPacket.ICQCLI_IDENTPacket(Vector)
	# 227    3   1    1 messenger.ICQ.ICQCLI_IDENTPacket.updatePacket()
	# 228    4   1    0 messenger.ICQ.ICQCLI_KEEPALIVEPacket.ICQCLI_KEEPALIVEPacket(Vector)
	# 229    3   1    1 messenger.ICQ.ICQCLI_KEEPALIVEPacket.updatePacket()
	# 230    4   1    1 messenger.ICQ.ICQCLI_RATESREQUESTPacket.ICQCLI_RATESREQUESTPacket(Vector)
	def __init__(self, line):
		line = line.strip()

		self.metricLOC, self.metricCC, self.metricCOM, \
		self.pkgName, self.className, self.methodName = \
			self.__decomposeCols(line)

		self.invEntRef = None # ref to invokable entity; to be resolved later
		self.log = Logger().get_instance(self)

	def __decomposeCols(self, line):
		# TODO: refactor this bogus implementation
		# ... and unit testit
		LOC=0
		CC=0
		COM=0
		uniqName = ""

		# grmbl! f... format
		i = 0

		# skip initial white spaces as long as id doesn't match max
		# number of possible id digits.
		while " " == line[i]:
			i = i + 1

		# skip id, doesn't interest us
		while " " != line[i]:
			i = i + 1

		# skip next batch of white spaces
		while " " == line[i]:
			i = i + 1

		while " " != line[i]:
			LOC = int(line[i]) + 10*LOC
			i = i + 1

		# skip next batch of white spaces
		while " " == line[i]:
			i = i + 1

		while " " != line[i]:
			CC = int(line[i]) + 10*CC
			i = i + 1

		# skip next batch of white spaces
		while " " == line[i]:
			i = i + 1

		while " " != line[i]:
			COM = int(line[i]) + 10*COM
			i = i + 1

		# skip next batch of white spaces
		while " " == line[i]:
			i = i + 1

		while i < len(line):
			uniqName = uniqName + line[i]
			i = i + 1

		# Abusing the utils function in this context where there is no
		# distinction between package, class and method separators.
		nrSeps = uniqName.count(self.sep)
		if nrSeps > 1: # there is a package
			pkgName = self.__extractPackage(uniqName)
			mtdName = uniqName[uniqName.rfind(self.sep)+1:]
			if "" == mtdName:
				# won't become a known entity
				return "","","","","",""
			classOwner = uniqName.split(pkgName)[1].split(mtdName)[0].replace(self.sep,"")
		elif 1 == nrSeps: # there is no package
			classOwner = uniqName.split(self.sep)[0]
			mtdName = uniqName.split(self.sep)[1]	
			pkgName = ""


		return str(LOC),str(CC),str(COM),pkgName,classOwner,mtdName

	def __extractPackage(self,uniqName):
		qClsName = uniqName[:uniqName.rfind(self.sep)]
		pkgName = qClsName[:qClsName.rfind(self.sep)]
		return pkgName

	def isKnownEntity(self, invEntDict):
		if not invEntDict.dictHasMethod(self.className,self.methodName):
			return False
		invEntWMatchingNames = \
					invEntDict.getMethodInClass(self.className, self.methodName)
		if 1 == len(invEntWMatchingNames):
			return True
		else:
			self.log.warn("multiple matches: ",invEntWMatchingNames)
			return False

	def composeUniqName(self):
		pkgName = self.pkgName.replace(self.sep,self.pkgSep)
		if "" == pkgName:
			return self.className+self.sep+self.methodName
		else:
			return pkgName+self.pkgSep+self.className+self.sep+self.methodName
