#!/bin/bash
# This file is part of Tsmells
#
# Tsmells is free software; you can redistribute it and/or modify it 
# under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 2 of the License, or (at your 
# option) any later version.
#
# Tsmells is distributed in the hope that it will be useful, but WITHOUT 
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more 
# details.
#
# You should have received a copy of the GNU General Public License along 
# with outputtest; if not, write to the Free Software Foundation, Inc., 
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA 
#
# Copyright 2007 Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>
#

ARGS=3

USAGE="Usage: `basename $0` PROJECT_PATH PROJECT_NAME TYPE\n\t with type any of [JCOND|JLOOP|CCOND|CLOOP]"

if [ $# -ne "$ARGS" ]
then
	echo -e $USAGE
	exit -1
fi

PROJ_PATH=$1
PROJ_NAME=$2
TYPE=$3

function jcond {
	for i in $(find -L . -name "*.java"); do
		cat $i |\
		$TSMELLS/scripts/removeComments.pl |\
		grep -noHE "(( |	|^)(if|switch)( |	|\(|$))" |\
		sed -e "s#^(standard input)#$i#" -e 's#\./##' \
			>> "$PROJ_PATH/dbdump/$PROJ_NAME.conditionals"
	done
}

function jloop {
	for i in $(find -L . -name "*.java"); do
		cat $i |\
		$TSMELLS/scripts/removeComments.pl |\
		grep -noHE "(( |	|^)(for|while|do)( |	|\(|$))" |\
		sed -e "s#^(standard input)#$i#" -e 's#\./##' \
			>> "$PROJ_PATH/dbdump/$PROJ_NAME.loops"
	done
}

function ccond {
	for i in $(find -L . -name "*.cxx" -o -name "*.cpp" -o -name "*.c" -o -name "*.h" -o -name "*.hh" -o -name "*.hpp"); do
		cat $i |\
		$TSMELLS/scripts/removeComments.pl |\
		grep -noHE "(( |	|^)(if|switch)( |	|\(|$))" |\
		sed -e "s#^(standard input)#$i#" -e 's#\./##' \
			>> "$PROJ_PATH/dbdump/$PROJ_NAME.conditionals"
	done
}

function cloop {
	for i in $(find -L . -name "*.cxx" -o -name "*.cpp" -o -name "*.c" -o -name "*.h" -o -name "*.hh" -o -name "*.hpp"); do
		cat $i |\
		$TSMELLS/scripts/removeComments.pl |\
		grep -noHE "(( |	|^)(for|while|do)( |	|\(|$))" |\
		sed -e "s#^(standard input)#$i#" -e 's#\./##' \
			>> "$PROJ_PATH/dbdump/$PROJ_NAME.loops"
	done
}

case "$TYPE" in
	'JCOND')
		jcond
		;;
	'JLOOP')
		jloop
		;;
	'CCOND')
		ccond
		;;
	'CLOOP')
		cloop
		;;
	*)
		echo -e $USAGE;
		;;
esac

