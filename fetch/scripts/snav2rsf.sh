#!/bin/bash
# This file is part of Fetch (the Fact Extraction Tool CHain).
#
# Fetch is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# Fetch is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Anastacia; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
# Copyright 2007 University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

##
# snav2rsf.sh is a script that transforms source code into an RSF
# model, abstracting away the underlying steps of
# (i) parsing using Source Navigator
# (ii) metrics collection using pmccabe (used to be 4c)
# (iii) dumping SN db to ascii tables
# (iv) snavtofamix to create a FAMIX cdif model from these tables
# (v) cdif2rsf to transform CDIF into RSF
##

if [ -z "$SN_HOME" ]
then
	echo "SN_HOME variable should be specified."
	exit 66
fi

if [ -z "$FETCH" ]
then
	echo "FETCH variable should be specified."
	exit 66
fi

if [ -z "$PMC" ]
then
        echo "PMC variable should point to the pmccabe binary in fetch/bin that
suits the host platform. Someday we automate this..."
        exit 66
fi

TARGET=`pwd`
SN=$SN_HOME/snavigator
#PMC=$FETCH/bin/pmccabe-linux-x86
VCS=0
echo "Fetch, Copyright (C) 2006-2007 Bart Du Bois and Bart Van Rompaey"
echo "Fetch is Open Source Software/Free Software, licensed under the"
echo "GNU GPL."
echo "This script relies on a robust parser."

# keep out of the remainder
ARGS=1
SRC_PATH=$(cd "$1" && pwd)
LOG_FILE=$SRC_PATH/fetchCpp2rsf.err

if [ $# -lt "$ARGS" ]
then
  echo "Usage: `basename $0` SRC_ROOT [VCS file]"
  exit 64
elif [ $# -eq "2" ]
then
  VCS=1
  VCS_PATH=$(cd `dirname "$2"` && pwd)
  VCS_FILE=`basename $2`
fi

if [ ! -d $SRC_PATH ]
then
  echo "$SRC_PATH is not an existing path"
  echo "Exiting ..."
  exit 66
fi

PROJ_NAME=`basename $SRC_PATH`

# SN run
#rm -rf $LOG_FILE $SRC_PATH/$PROJ_NAME.proj $SRC_PATH/.snprj
cd $SRC_PATH
#activityDescr="(1/9) Running Source Navigator"
#echo -n $activityDescr "..."
#echo $activityDescr >> $LOG_FILE 2>> $LOG_FILE
#t1=`date '+%s'`
#$SN --batchmode --create >> $LOG_FILE
#if [ $? -ne 0 ]
#then
#	echo " fail!"
#	echo "--> check $LOG_FILE for info"
#	exit 69
#else
#	t2=`date '+%s'`
#	t=`expr $t2 - $t1`
#	echo " pass ("$t "sec.)"
#fi
rm -rf dbdump
mkdir dbdump

# dbdump
cd $FETCH/src/snavtofamix
activityDescr="(2/9) Dumping SN database"
echo -n $activityDescr "..."
echo $activityDescr >> $LOG_FILE
t1=`date '+%s'`
./snav_dbdumps.sh $SRC_PATH $PROJ_NAME $SRC_PATH/dbdump >> $LOG_FILE
if [ $? -ne 0 ]
then
	echo " fail!"
	echo "--> check $LOG_FILE for info"
	exit 69
else
	if [ "1" -eq $VCS ]
	then
		cat $VCS_PATH/$VCS_FILE > $SRC_PATH/dbdump/$PROJ_NAME.vcs
	fi
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	echo " pass ("$t "sec.)"
fi

# preprocessor conditional compilation (see http://www.cppreference.com/preprocessor/preprocessor_if.html)
cd $SRC_PATH
activityDescr="(3/9) Collecting conditional compilation and macro definition directives"
echo -n $activityDescr "..."
echo $activityDescr >> $LOG_FILE
t1=`date '+%s'`
allFiles=`find . \( -name "*.cpp" -o -name "*.c" -o -name "*.cc" -o -name "*.cxx" -o -name "*.hpp" -o -name "*.h" -o -name "*.hh" -o -name "*.hxx" \)`
for file in $allFiles; do
	perl $FETCH/scripts/parserExt/preprocDirectives.pl "$file" | sed "s/\.\///g" >> ./dbdump/$PROJ_NAME.condcomp
done
t2=`date '+%s'`
t=`expr $t2 - $t1`
echo " pass ("$t "sec.)"

# alternative includes
cd $SRC_PATH
activityDescr="(4/9) Collecting include directives"
echo -n $activityDescr "..."
echo $activityDescr >> $LOG_FILE
t1=`date '+%s'`
grep "#include" ./dbdump/$PROJ_NAME.condcomp > ./dbdump/$PROJ_NAME.includes2
t2=`date '+%s'`
t=`expr $t2 - $t1`
echo " pass ("$t "sec.)"

activityDescr="(4/9) Collecting namespace declarations and uses"
echo -n $activityDescr "..."
echo $activityDescr >> $LOG_FILE
cd $SRC_PATH
t1=`date '+%s'`
perl $FETCH/scripts/parserExt/namespaceScript/getNamespaces.pl -s . c cc cpp cxx h hh hpp hxx > ./dbdump/$PROJ_NAME.namespaces 2>> $LOG_FILE
if [ $? -ne 0 ]
then
	echo " fail!"
	echo "--> check $LOG_FILE for info"
	exit 69
else
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	echo " pass ("$t "sec.)"
fi

activityDescr="(5/9) Calculating metrics via Pmccabe"
echo -n $activityDescr "..."
echo $activityDescr >> $LOG_FILE
cd $SRC_PATH
t1=`date '+%s'`
# pmccabe can't cope with method implementations inside a class declaration scope. so we don't parse headers (with possible implementations) for now.
#find . \( -name "*.cpp" -o -name "*.c" -o -name "*.cc" -o -name "*.cxx" -o -name "*.hpp" -o -name "*.h" -o -name "*.hh" -o -name "*.hxx" \) | xargs $HERE/../bin/$PMC >> ./dbdump/$PROJ_NAME.pmcmetrics 2>> $LOG_FILE
find . \( -name "*.cpp" -o -name "*.c" -o -name "*.cc" -o -name "*.cxx" \) | xargs $PMC >> ./dbdump/$PROJ_NAME.pmcmetrics 2>> $LOG_FILE
t2=`date '+%s'`
t=`expr $t2 - $t1`
echo " pass ("$t "sec.)"

# some cleanup, these files eat hard disks!
activityDescr="(6/9) Cleaning temporary files"
echo -n $activityDescr "..."
echo $activityDescr >> $LOG_FILE
t1=`date '+%s'`
#rm -rf $SRC_PATH/$PROJ_PATH.proj $SRC_PATH/.snprj $SRC_PATH/dbdump/*.tmp
if [ $? -ne 0 ]
then
	echo " fail!"
	exit 69
else
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	echo " pass ("$t "sec.)"
fi

# snavtofamix
activityDescr="(7/9) Running snavtofamix"
echo -n $activityDescr "..."
echo $activityDescr >> $LOG_FILE
cd $FETCH/src/snavtofamix/src
rm -rf ../snavtofamix.log
t1=`date '+%s'`
python snavtofamix.py $SRC_PATH/dbdump $PROJ_NAME $PROJ_NAME.cdif >> $LOG_FILE
if [ $? -ne 0 ]
then
	echo " fail!"
	exit 69
else
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	echo " pass ("$t "sec.)"
fi

if [ -f ../snavtofamix.log ]
then
	cat ../snavtofamix.log >> $LOG_FILE
fi
mv $PROJ_NAME.cdif $TARGET

#cdif2rsf
activityDescr="(8/9) Running CDIF2RSF"
echo -n $activityDescr "..."
echo $activityDescr >> $LOG_FILE
cd $FETCH/src/CDIF2RSF/
t1=`date '+%s'`
python cdif2rsf.py $TARGET/$PROJ_NAME.cdif $TARGET/$PROJ_NAME.rsf >> $LOG_FILE
if [ $? -ne 0 ]
then
	echo " fail to generate RSF Model!"
	echo "--> check $LOG_FILE for info"
	echo "--> CDIF written to $TARGET/$PROJ_NAME.cdif"
	exit 69
else
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	echo " pass ("$t "sec.)"
	echo "--> see details in log file $LOG_FILE"
	echo "--> CDIF written to $TARGET/$PROJ_NAME.cdif"
	echo "--> RSF written to $TARGET/$PROJ_NAME.rsf"
fi
cd $TARGET
echo -n "MO:" `grep -c "^Module	" $TARGET/$PROJ_NAME.rsf`
echo -n "FI:" `grep -c "^File	" $TARGET/$PROJ_NAME.rsf`
echo -n "CL:" `grep -c "^Class	" $TARGET/$PROJ_NAME.rsf`
echo -n "GV:" `grep -c "^GlobalVariable	" $TARGET/$PROJ_NAME.rsf`
echo -n "ME:" `grep -c "^Method	" $TARGET/$PROJ_NAME.rsf`
echo -n "FU:" `grep -c "^Function	" $TARGET/$PROJ_NAME.rsf`
echo -n "IN:" `grep -c "^Invokes	" $TARGET/$PROJ_NAME.rsf`
echo -n "AC:" `grep -c "^Accesses	" $TARGET/$PROJ_NAME.rsf`
echo -n "MT:" `grep -c "^Measurement	" $TARGET/$PROJ_NAME.rsf`

exit 0

