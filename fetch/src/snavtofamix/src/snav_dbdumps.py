# !/usr/bin/python
# snavtofamix parses SourceNavigator's database to generate CDIF exchange files
# of the FAMIX source code meta model.
# Copyright (C) 2006 Bart Van Rompaey and Bart Du Bois
#
# This file is part of snavtofamix.
#
# snavtofamix is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# snavtofamix is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with snavtofamix; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
# Copyright 2007 University of Antwerp
# Author(s): Bart Van Rompaey - bart.vanrompaey2@ua.ac.be
#					Bart Du Bois - bart.dubois@ua.ac.be
#
# snav_dbdumps.py dumps the SourceNavigator project databases to text files
# to be processed by snavtofamix.py.

# Another joyfull Van Rompaey & Du Bois creation

import os
import sys

ARGS=3         # Script requires 3 arguments.
PROJ_PATH=""
DEST_PATH=""
PROJECT_NAME=""

def check_snav_paths():
	print "* SourceNavigator project path check"

	if not os.path.isdir(PROJ_PATH):
		print "	- SourceNavigator project path",PROJ_PATH,"doesn't exist ... abort!"
		sys.exit(1)

	print "	- SourceNavigator project path",PROJ_PATH,"exists ... OK"

	if not os.path.isfile(PROJ_PATH + "/" + PROJECT_NAME + ".proj"):
		print "	- SourceNavigator",PROJ_PATH,"/"+PROJECT_NAME +".proj project file doesn't exist ... abort!"
		sys.exit(1)

	print "	- SourceNavigator",PROJ_PATH + "/" + PROJECT_NAME + ".proj project file exists ... OK"

	if not os.path.isdir(PROJ_PATH + "/.snprj"):
		print "	- .SourceNavigator db .snprj dir", PROJ_PATH + "/.snprj doesn't exist ... abort!"
		sys.exit(1)

	print "	- SourceNavigator .snprj dir",PROJ_PATH + "/.snprj exists ... OK"

	if not os.path.isdir(DEST_PATH):
		print "	- dbdump destination dir doesn't exist ... creating",DEST_PATH
		os.system("mkdir -p " + DEST_PATH)

		if not os.path.isdir(DEST_PATH):
			print "	- failed creating",DEST_PATH,"... abort!"
			sys.exit(1)

	print "	- dbdump destination dir",DEST_PATH,"exists ... OK"

	if not os.path.isfile(DBDUMP_PATH):
		print "	- dbdump utilities' path",DBDUMP_PATH,"doesn't exist ... abort!"
		sys.exit(1)

	print "	- dbdump utilities' path",DBDUMP_PATH,"exists ... OK "

	print"Paths checked."

def dump_db(databaseExtension, asciiFileExtension):
	if not os.path.isfile(PROJ_PATH + "/.snprj/" + PROJECT_NAME + "." + databaseExtension):
		os.system("touch " + DEST_PATH + "/" + PROJECT_NAME + "." + asciiFileExtension)
	else:
		dump_command = DBDUMP_PATH + " " + "\"" + PROJ_PATH + "/.snprj/" + PROJECT_NAME + "." + databaseExtension + "\""
		dump_command += " > " + "\"" + DEST_PATH + "/" + PROJECT_NAME + "." + asciiFileExtension + "\""
		return_code = os.system(dump_command)

		if return_code != 0:
			print "[failed dumping", asciiFileExtension, ": code",return_code,"]"
			print "dump_command was:",dump_command
			sys.exit(1)

def dump_dbs() :
	print "* Dumping SourceNavigator's", PROJECT_NAME, "database ...",

	dump_db("cl", "classes")
	dump_db("in", "inheritance")
	dump_db("mi", "methods")
	dump_db("md", "methoddefs")
	dump_db("fu", "functions")
	dump_db("iv", "attributes")
	dump_db("gv", "globalvar")
	dump_db("by", "references")
	dump_db("to", "localvars")
	dump_db("t", "typedef")
	dump_db("iu", "includes")

	print "[done]"

def createTemp(asciiFileExtension):
	copyCommand = "cp " + "\"" + DEST_PATH + "/" + PROJECT_NAME + "." + asciiFileExtension + "\"" + " "
        tmpFilePath = "\"" + DEST_PATH + "/" + PROJECT_NAME + "." + asciiFileExtension + ".tmp" + "\""
	copyCommand += tmpFilePath
	result_code = os.system(copyCommand)

	if result_code != 0:
		print "Failed copying file", DEST_PATH + "/" + PROJECT_NAME + "." + asciiFileExtension
		sys.exit(1)

        chmodCommand = "chmod 777 " + tmpFilePath
        result_code = os.system(chmodCommand)

	if result_code != 0:
		print "Failed chmodding file", tmpFilePath
		sys.exit(1)


def sanitizeFile(asciiFileExtension):
	fileName = DEST_PATH + "/" + PROJECT_NAME + "." + asciiFileExtension
	filePath = fileName
	tempFileName = fileName + ".tmp"

	input_file = open(fileName, 'r')
	output_file = open(tempFileName, 'w')

	for line in input_file:
		line = line.replace("<const ","<")
		line = line.replace("<unsigned long int>","<long>")
		line = line.replace("<signed long int>","<long>")
		line = line.replace("<signed ","<")
		line = line.replace("<unsigned ","<")
		output_file.write(line)

	input_file.close()
	output_file.close()

	result_code = os.system("mv " + "\"" + tempFileName + "\"" + " " + "\"" + fileName + "\"")
	if result_code != 0:
		print "Failed moving file", "\"" + tempFileName + "\"" + " to","\"" + fileName + "\""
		sys.exit(1)

# Fixes issues with the field separator in the SN dumped tables (being the white space)
# and the possibility of this separator occurring within fields (e.g. vector<const Foo>)
def sanitize():
	print "* Sanitize dumped tables ...",

	createTemp("classes")
	createTemp("inheritance")
	createTemp("methods")
	createTemp("methoddefs")
	createTemp("references")

	sanitizeFile("classes")
	sanitizeFile("inheritance")
	sanitizeFile("methods")
	sanitizeFile("methoddefs")
	sanitizeFile("references")

	print " Done"


if len(sys.argv) < ARGS:
	print "Usage: python", sys.argv[0] + " project-path project-name dest-path"
	sys.exit(1)

if os.environ['DBDUMP_PATH']:
        DBDUMP_PATH = os.environ['DBDUMP_PATH']
        print "Retrieved DBDUMP_PATH from environment: \"" + DBDUMP_PATH + "\""

PROJ_PATH=sys.argv[1]
PROJECT_NAME=sys.argv[2]
DEST_PATH=sys.argv[3]

check_snav_paths()
dump_dbs()
sanitize()
