#!/bin/bash
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

##
# snav_dbdumps.sh dumps the SourceNavigator project databases to text files
# to be processed by snavtofamix.py.
##

if [ -z "$SN_HOME" ]
then
	echo "SN_HOME variable should be specified."
	exit 66
fi

DBDUMP_PATH=$SN_HOME/dbdump
ARGS=3         # Script requires 3 arguments.
PROJ_PATH=""
DEST_PATH=""
PROJECT_NAME=""

function check_snav_paths() {
	echo "* Source Navigator project path check"

	if [ ! -d $PROJ_PATH ]
	then
		echo "	- Source Navigator project path $PROJ_PATH doesn't exist ... abort!"
		exit 1;
	fi
	echo "	- Source Navigator project path $PROJ_PATH exists ... OK"

	if [ ! -f $PROJ_PATH/$PROJECT_NAME.proj ]
	then
		echo "	- Source Navigator $PROJ_PATH/$PROJECT_NAME.proj project file doesn't exist ... abort!"
		exit 1;
	fi
	echo "	- Source Navigator $PROJ_PATH/$PROJECT_NAME.proj project file exists ... OK"

	if [ ! -d $PROJ_PATH/.snprj ] # Source Navigator
	then
		if [ -d $PROJ_PATH/SNDB4 ] # Source Navigator NG
		then
			mv $PROJ_PATH/SNDB4 $PROJ_PATH/.snprj
		else
			echo "	- .Source Navigator db .snprj dir $PROJ_PATH/.snprj doesn't exist ... abort!"
			exit 1;
		fi
	fi
	echo "	- Source Navigator .snprj dir $PROJ_PATH/.snprj exists ... OK"

	if [ ! -d $DEST_PATH ]
	then
		echo "	- dbdump destination dir doesn't exist ... creating $DEST_PATH"
		mkdir -p $DEST_PATH;
	fi

	if [ ! -d $DEST_PATH ]
	then
		echo "	- failed creating $DEST_PATH ... abort!"
		exit 1;
	fi
	echo "	- dbdump destination dir $DEST_PATH exists ... OK"

	if [ ! -f $DBDUMP_PATH ]
	then
		echo "	- dbdump utilities' path $DBDUMP_PATH doesn't exist ... abort!"
		exit 1;
	fi
	echo "	- dbdump utilities' path $DBDUMP_PATH exists ... OK "
	echo "	DONE"
}

##
# Dump a SN database with the given extension $1 to a file with the given extension $2
##
function dump_db() {
	if [ ! -f "$PROJ_PATH/.snprj/${PROJECT_NAME}.$1" ]
	then
		touch $DEST_PATH/${PROJECT_NAME}.$2
	else
		$DBDUMP_PATH -c ";" ${PROJECT_NAME}.$1 > $DEST_PATH/${PROJECT_NAME}.$2
	fi
}

function dump_dbs() {
	echo -n "* Dumping SourceNavigator's $PROJECT_NAME database ..."

	cd $PROJ_PATH/.snprj

	dump_db f files
	dump_db cl classes
	dump_db in inheritance
	dump_db mi methods
	dump_db md methoddefs
	dump_db fu functions
	dump_db fd functiondefs
	dump_db iv attributes
	dump_db gv globalvar
	dump_db con constants
	dump_db by references
	dump_db t typedef
	dump_db iu includes

	cd -

	echo " Done"
}

# Fixes issues with the field separator in the SN dumped tables (being the white space)
# and the possibility of this separator occurring within fields (e.g. vector<const Foo>)
function sanitize() {
	echo -n "* Sanitize dumped tables ..."

	cp $DEST_PATH/${PROJECT_NAME}.classes $DEST_PATH/${PROJECT_NAME}.classes.tmp
	cp $DEST_PATH/${PROJECT_NAME}.inheritance $DEST_PATH/${PROJECT_NAME}.inheritance.tmp
	cp $DEST_PATH/${PROJECT_NAME}.methods $DEST_PATH/${PROJECT_NAME}.methods.tmp
	cp $DEST_PATH/${PROJECT_NAME}.functions $DEST_PATH/${PROJECT_NAME}.functions.tmp
	cp $DEST_PATH/${PROJECT_NAME}.functiondefs $DEST_PATH/${PROJECT_NAME}.functiondefs.tmp
	cp $DEST_PATH/${PROJECT_NAME}.methoddefs $DEST_PATH/${PROJECT_NAME}.methoddefs.tmp
	cp $DEST_PATH/${PROJECT_NAME}.references $DEST_PATH/${PROJECT_NAME}.references.tmp

	cat $DEST_PATH/${PROJECT_NAME}.classes.tmp | sed "s/<const /</g;s/<unsigned long int>/<long>/g;s/<signed long int>/<long>/g;s/<signed /</g;s/<unsigned /</g" > $DEST_PATH/${PROJECT_NAME}.classes

	cat $DEST_PATH/${PROJECT_NAME}.inheritance.tmp | sed "s/<const /</g;s/<unsigned long int>/<long>/g;s/<signed long int>/<long>/g;s/<signed /</g;s/<unsigned /</g" > $DEST_PATH/${PROJECT_NAME}.inheritance

	cat $DEST_PATH/${PROJECT_NAME}.methods.tmp | sed "s/<const /</g;s/<unsigned long int>/<long>/g;s/<signed long int>/<long>/g;s/<signed /</g;s/<unsigned /</g" > $DEST_PATH/${PROJECT_NAME}.methods

	cat $DEST_PATH/${PROJECT_NAME}.methoddefs.tmp | sed "s/<const /</g;s/<unsigned long int>/<long>/g;s/<signed long int>/<long>/g;s/<signed /</g;s/<unsigned /</g" > $DEST_PATH/${PROJECT_NAME}.methoddefs

	cat $DEST_PATH/${PROJECT_NAME}.functions.tmp | sed "s/<const /</g;s/<unsigned long int>/<long>/g;s/<signed long int>/<long>/g;s/<signed /</g;s/<unsigned /</g" > $DEST_PATH/${PROJECT_NAME}.functions

	cat $DEST_PATH/${PROJECT_NAME}.functiondefs.tmp | sed "s/<const /</g;s/<unsigned long int>/<long>/g;s/<signed long int>/<long>/g;s/<signed /</g;s/<unsigned /</g" > $DEST_PATH/${PROJECT_NAME}.functiondefs

	cat $DEST_PATH/${PROJECT_NAME}.references.tmp | sed "s/<const /</g;s/<unsigned long int>/<long>/g;s/<signed long int>/<long>/g;s/<signed /</g;s/<unsigned /</g" > $DEST_PATH/${PROJECT_NAME}.references
	rm $DEST_PATH/*.tmp
	echo " Done"
}

if [ $# -ne "$ARGS" ]
then
  echo "Usage: `basename $0` old-pattern new-pattern filename"
  exit -1
fi

PROJECT_NAME=$2
PROJ_PATH=$1
DEST_PATH=$3

# are all required paths existing?
check_snav_paths
# then dump the SN database to ascii tables
dump_dbs
# do some cleanup because of the lack of separator
sanitize

