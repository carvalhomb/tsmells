# !/bin/sh
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
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>,
#                     Bart Du Bois <bart.dubois@ua.ac.be>

# $Rev: 320 $:     Revision of last commit
# $Author: bdubois $:  Author of last commit
# $Date: 2007-05-09 15:43:44 +0200 (Wed, 09 May 2007) $:    Date of last commit
#

# modify these
DBDUMP_PATH=/opt/local/bin/dbdump
SNAV_PATH=/opt/local/bin/snavigator
PMCCABE_PATH=/usr/bin/pmccabe

PROJ_PATH=""
DEST_PATH=""
SRC_PATH=""
SOURCE_FILES="sources.txt"

CPPFILES='\( -name "*.cpp" -o -name "*.c" -o -name "*.cc" -o -name "*.cxx" -o -name "*.h" -o -name "*.hpp" -o -name "*.hh" -o -name "*.hxx" \)'

function do_cleaning() {
	rm -rf $PROJECT_NAME
	rm -rf .snprj
	rm -rf $SOURCE_FILES
}

function check_snav_paths() {
	echo "* SourceNavigator project path check"

	if [ ! -d $PROJ_PATH ]
	then
		echo "	- SourceNavigator project path $PROJ_PATH doesn't exist ... abort!"
		exit 1;
	fi
	echo "	- SourceNavigator project path $PROJ_PATH exists ... OK"

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

	if [ ! -f $SNAV_PATH ]
	then
		echo "	- snavigator binary path $SNAV_PATH doesn't exist ... abort!"
		exit 1;
	fi
	echo "	- dbdump utilities' path $SNAV_PATH exists ... OK "
	echo "	DONE"

	if [ ! -f $DBDUMP_PATH ]
	then
		echo "	- dbdump utilities' path $DBDUMP_PATH doesn't exist ... abort!"
		exit 1;
	fi
	echo "	- dbdump utilities' path $DBDUMP_PATH exists ... OK "
	echo "	DONE"
}

function dump_dbs() {
	echo -n "* Dumping SourceNavigator's $PROJECT_NAME database ..."
	if [ ! -f $PROJ_PATH/.snprj/${PROJECT_NAME}.cl ]
	then
		touch $DEST_PATH/${PROJECT_NAME}.classes
	else
		$DBDUMP_PATH $PROJ_PATH/.snprj/${PROJECT_NAME}.cl > $DEST_PATH/${PROJECT_NAME}.classes
	fi

	if [ ! -f $PROJ_PATH/.snprj/${PROJECT_NAME}.in ]
	then
		touch $DEST_PATH/${PROJECT_NAME}.inheritance
	else
		$DBDUMP_PATH $PROJ_PATH/.snprj/${PROJECT_NAME}.in > $DEST_PATH/${PROJECT_NAME}.inheritance
	fi

	if [ ! -f $PROJ_PATH/.snprj/${PROJECT_NAME}.mi ]
	then
		touch $DEST_PATH/${PROJECT_NAME}.methods
	else
		$DBDUMP_PATH $PROJ_PATH/.snprj/${PROJECT_NAME}.mi > $DEST_PATH/${PROJECT_NAME}.methods
	fi

	if [ ! -f $PROJ_PATH/.snprj/${PROJECT_NAME}.md ]
	then
		touch $DEST_PATH/${PROJECT_NAME}.methoddefs
	else
		$DBDUMP_PATH $PROJ_PATH/.snprj/${PROJECT_NAME}.md > $DEST_PATH/${PROJECT_NAME}.methoddefs
	fi

	if [ ! -f $PROJ_PATH/.snprj/${PROJECT_NAME}.fu ]
	then
		touch $DEST_PATH/${PROJECT_NAME}.functions
	else
		$DBDUMP_PATH $PROJ_PATH/.snprj/${PROJECT_NAME}.fu > $DEST_PATH/${PROJECT_NAME}.functions
	fi

	if [ ! -f $PROJ_PATH/.snprj/${PROJECT_NAME}.iv ]
	then
		touch $DEST_PATH/${PROJECT_NAME}.attributes
	else
		$DBDUMP_PATH $PROJ_PATH/.snprj/${PROJECT_NAME}.iv > $DEST_PATH/${PROJECT_NAME}.attributes
	fi

	if [ ! -f $PROJ_PATH/.snprj/${PROJECT_NAME}.gv ]
	then
		touch $DEST_PATH/${PROJECT_NAME}.globalvar
	else
		$DBDUMP_PATH $PROJ_PATH/.snprj/${PROJECT_NAME}.gv > $DEST_PATH/${PROJECT_NAME}.globalvar
	fi

	if [ ! -f $PROJ_PATH/.snprj/${PROJECT_NAME}.by ]
	then
		touch $DEST_PATH/${PROJECT_NAME}.references
	else
		$DBDUMP_PATH $PROJ_PATH/.snprj/${PROJECT_NAME}.by > $DEST_PATH/${PROJECT_NAME}.references
	fi

	if [ ! -f $PROJ_PATH/.snprj/${PROJECT_NAME}.to ]
	then
		touch $DEST_PATH/${PROJECT_NAME}.localvars
	else
		$DBDUMP_PATH $PROJ_PATH/.snprj/${PROJECT_NAME}.to > $DEST_PATH/${PROJECT_NAME}.localvars
	fi


	if [ ! -f $PROJ_PATH/.snprj/${PROJECT_NAME}.t ]
	then
		touch $DEST_PATH/${PROJECT_NAME}.typedef
	else
		$DBDUMP_PATH $PROJ_PATH/.snprj/${PROJECT_NAME}.t > $DEST_PATH/${PROJECT_NAME}.typedef
	fi

	if [ ! -f $PROJ_PATH/.snprj/${PROJECT_NAME}.iu ]
	then
		touch $DEST_PATH/${PROJECT_NAME}.includes
	else
		$DBDUMP_PATH $PROJ_PATH/.snprj/${PROJECT_NAME}.iu > $DEST_PATH/${PROJECT_NAME}.includes
	fi
	echo " Done"
}

function create_snproject() {
	echo -n "* Creating SourceNavigator project " $PROJECT_NAME " ... "
	touch "$SOURCE_FILES"
	for i in `find $SRC_PATH \( -name "*.cpp" -o -name "*.c" -o -name "*.cc" -o -name "*.cxx" -o -name "*.h" -o -name "*.hpp" -o -name "*.hh" -o -name "*.hxx" \) -print`; do echo $i >> $SOURCE_FILES; done
	$SNAV_PATH -b --debug -p $PROJECT_NAME -i $SOURCE_FILES >> $PROJECT_NAME"-SNParse.log"
	echo "Done (check echo $PROJECT_NAME-SNParse.log)"
}

function check_pmccabe() {
	echo "* PMCCabe path check"
	if [ ! -f $PMCCABE_PATH ]
	then
		echo "	- pmccabe path $PMCCABE_PATH doesn't exist ... abort!"
		exit 1;
	fi
	echo "	- pmccabe path $PMCCABE_PATH exists ... OK "

	if [ ! -d $SRC_PATH ]
	then
		echo "	- project $PROJECT_NAME source code path $SRC_PATH doesn't exist ... abort!"
		exit 1;
	fi
	echo "	- project $PROJECT_NAME source code path $SRC_PATH exists ... OK"
	echo "	DONE"
}

function run_pmccabe() {
	echo -n "* running pmccabe ... "
	touch $DEST_PATH/${PROJECT_NAME}.metrics
	for i in `find $SRC_PATH \( -name "*.cpp" -o -name "*.c" -o -name "*.cc" -o -name "*.cxx" -o -name "*.h" -o -name "*.hpp" -o -name "*.hh" -o -name "*.hxx" \) -print`; do pmccabe $i >> $DEST_PATH/${PROJECT_NAME}.metrics 2>> $PROJECT_NAME"-PMCCABEParse.log"; done
	echo "Done"
}

function run_snavtofamix() {
	echo "* running snavtofamix.py ... "
	echo "python snavtofamix.py" $DEST_PATH/ $PROJECT_NAME $PROJECT_NAME.cdif
		PYTHONPATH=src:$PYTHONPATH python src/snavtofamix.py $DEST_PATH/ $PROJECT_NAME $PROJECT_NAME.cdif
}

#if [ ! $# -eq 3 ]
#then
#	echo "Usage: ./snavtofamix.sh SRC_PATH PROJECT_NAME DEST_PATH"
#	exit 0;
#fi

#echo $*
SRC_PATH=$1
PROJECT_NAME=$2
DEST_PATH=$3
PROJ_PATH=./

do_cleaning
check_snav_paths
create_snproject
dump_dbs

check_pmccabe
run_pmccabe

run_snavtofamix
