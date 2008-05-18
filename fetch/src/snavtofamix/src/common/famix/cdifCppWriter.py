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
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>
#

#
# Write FAMIX for C++ extensions

from common.famix import cdifWriter

def generateTypeDefInfo(typeDefRef):
	of = cdifWriter.get_outputhandle()
	cdifWriter.writeRecordPrefix(of, "TypeDef")
	cdifWriter.writeStringField(of, "belongsToContext", typeDefRef.getNamespaceName())
	cdifWriter.writeStringField(of, "declaredReturnType", typeDefRef.getName())
	cdifWriter.writeStringField(of, "baseReturnType", typeDefRef.getBaseType())

	if typeDefRef.getBaseClass() != None:
		cdifWriter.writeStringField(of, "baseReturnClass", typeDefRef.getBaseClass())

	if typeDefRef.getSourceLocation() != None:
		srcLoc = typeDefRef.getSourceLocation()
		cdifWriter.writeSourceAnchor(of, srcLoc.sourceFile,\
										srcLoc.start,\
										srcLoc.end)

	if typeDefRef.getBaseSourceLocation() != None:
		baseSrcLoc = typeDefRef.getBaseSourceLocation()
		if baseSrcLoc.sourceFile != "":
			cdifWriter.writeTypeSourceAnchor(of, baseSrcLoc.sourceFile,\
												 baseSrcLoc.start)

	cdifWriter.writeRecordPostfix(of)

def writeDestinationSourceAnchor(of, dst_sourcefile, dst_start):
	# write destination source anchor
	of.write("\n\t(")
	of.write("destinationSourceAnchor #[file \"")
	of.write(dst_sourcefile)
	of.write("\" start ")
	of.write(dst_start)
	of.write(" end ")
	of.write(dst_start)
	of.write("|]#)")

def generateIncludeInfo(includingFile, includedFile):
	of = cdifWriter.get_outputhandle()
	cdifWriter.writeRecordPrefix(of, "Include")
	cdifWriter.writeStringField(of, "includingFile", includingFile)
	cdifWriter.writeStringField(of, "includedFile", includedFile)
	cdifWriter.writeRecordPostfix(of)

def generateIncludeEntity(includeEntity):
	of = cdifWriter.get_outputhandle()
	cdifWriter.writeRecordPrefix(of, "Include")
	cdifWriter.writeStringField(of, "includingFile", includeEntity.getIncludingFile())
	cdifWriter.writeStringField(of, "includedFile", includeEntity.getIncludedFile())
	cdifWriter.writeSourceAnchors(of, [includeEntity.getSourceLocation()])
	cdifWriter.writeRecordPostfix(of)
