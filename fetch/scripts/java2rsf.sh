#!/bin/bash
# snavtofamix parses SourceNavigator's database to generate CDIF exchange files
# of the FAMIX source code meta model.
# Copyright (C) 2006 Bart Van Rompaey
#
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
# Copyright 2007  University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>
#

##
# java2rsf.sh is a script that transforms source code into an RSF
# model, abstracting away the underlying steps of
# (i) parsing using Source Navigator
# (ii) dumping SN db to ascii tables
# (iii) snavtofamix to create a FAMIX cdif model from these tables
# (iv) cdif2rsf to transform CDIF into RSF
##

# Include the initalizations of variables
. initialization.sh

NCSS=$FETCH/src/javancss/bin/javancss
VCS=0

$ECHO "Fetch, Copyright (C) 2006-2008 University of Antwerp"
$ECHO "Created by Bart Van Rompaey and Bart Du Bois, Lab On REengineering"
$ECHO "Fetch is Open Source Software/Free Software, licensed under the"
$ECHO "GNU GPL."
$ECHO "This script relies on a robust parser."

# Java specific
LOG_FILE=$SRC_PATH/fetchJava2RSF.err

if [ $XVFB -eq 1 ]
then
	Xvfb $DISPLAY -screen 0 800x600x16 -ac >> $LOG_FILE 2>> $LOG_FILE & # setup X virtual framebuffer
fi

# The number of executed steps
STEPS=12

# SN run
rm -rf $LOG_FILE $SRC_PATH/$PROJ_NAME.proj $SRC_PATH/.snprj
cd $SRC_PATH
$ECHO -n "(01/$STEPS) Running Source Navigator ..."
$ECHO "(01/$STEPS) Running Source Navigator" >> $LOG_FILE
t1=`date '+%s'`
if [ $XVFB -eq 1 ]
then
	DISPLAY=$DISPLAY  $SN --batchmode --create >> $LOG_FILE
else
	$SN --batchmode --create >> $LOG_FILE
fi
if [ $? -ne 0 ]
then
	$ECHO " fail!"
	$ECHO "--> check $LOG_FILE for info"
	exit 69
else
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	$ECHO " pass ("$t "sec.)"
fi

# wait 1 second to let SN finish
activityDescr="	Waiting 1 second to let SN finish"
#$ECHO -n $activityDescr "..."
$ECHO $activityDescr >> $LOG_FILE 2>> $LOG_FILE
sleep 1
activityDescr="[done]"
$ECHO $activityDescr
$ECHO $activityDescr >> $LOG_FILE 2>> $LOG_FILE

if [ $XVFB -eq 1 ]
then
	# kill Xvfb
	process=`cat /tmp/.X$display-lock`
	kill $process
fi
rm -rf dbdump
mkdir dbdump

# dbdump
cd $FETCH/src/snavtofamix
$ECHO -n "(02/$STEPS) Dumping SN database ..."
$ECHO "(02/$STEPS) Dumping SN database" >> $LOG_FILE
t1=`date '+%s'`
bash snav_dbdumps.sh $SRC_PATH $PROJ_NAME $SRC_PATH/dbdump >> $LOG_FILE
if [ $? -ne 0 ]
then
	$ECHO " fail!"
	$ECHO "--> check $LOG_FILE for info"
	exit 69
else
	if [ "1" -eq $VCS ]
	then
		cat $VCS_PATH/$VCS_FILE > $SRC_PATH/dbdump/$PROJ_NAME.vcs
	fi
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	$ECHO " pass ("$t "sec.)"
fi

# insaneref
cd $FETCH/src/snavtofamix
echo -n "(03/$STEPS) Grepping function and method invocations ..."
echo "(03/$STEPS) Grabbing all functions/methods" >> $LOG_FILE
t1=`date '+%s'`
cd $SRC_PATH
#grep -nH "(" `find -L . -name "*.java"` | sed -e 's/\.\///' > 
#$SRC_PATH/dbdump/$PROJ_NAME.insaneref
rm -f $SRC_PATH/dbdump/$PROJ_NAME.insaneref
for i in $(find -L . -name "*.java"); do
	cat $i |\
	$FETCH/scripts/parserExt/removeComments.pl |\
	grep -nH "(" |\
	sed -e "s#^(standard input)#$i#" -e 's#\./##' \
		>> "$SRC_PATH/dbdump/$PROJ_NAME.insaneref"
done

if [ $? -ne 0 ]
then
	$ECHO " fail!"
	$ECHO "--> check $LOG_FILE for info"
	exit 69
else
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	$ECHO " pass ("$t "sec.)" 
fi


# collect package information via separate script
$ECHO -n "(04/$STEPS) Collecting package information ..."
$ECHO "(04/$STEPS) Collecting package information" >> $LOG_FILE
t1=`date '+%s'`
sh $FETCH/scripts/parserExt/getJavaPackageInfo.sh $SRC_PATH > $SRC_PATH/dbdump/$PROJ_NAME.packages
if [ $? -ne 0 ]
then
	$ECHO " fail!"
	$ECHO "--> check $LOG_FILE for info"
	exit 69
else
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	$ECHO " pass ("$t "sec.)"
fi

#fix object
stepString="(05/$STEPS) Adding java.lang.Object inheritance ..."
$ECHO -n $stepString
$ECHO $stepString >> $LOG_FILE
t1=`date '+%s'`
sh $FETCH/scripts/parserExt/fixObjectInheritance.sh $SRC_PATH/dbdump $PROJ_NAME
if [ $? -ne 0 ]
then
	$ECHO " fail!"
	$ECHO "--> check $LOG_FILE for info"
	exit 69
else
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	$ECHO " pass ("$t "sec.)" 
fi

#get control structure information
$ECHO -n "(06/$STEPS) Grepping loop and conditionals ..."
t1=`date '+%s'`
sh $FETCH/scripts/parserExt/generateCtrlStructInfo.sh $SRC_PATH $PROJ_NAME "JCOND"
sh $FETCH/scripts/parserExt/generateCtrlStructInfo.sh $SRC_PATH $PROJ_NAME "JLOOP"
if [ $? -ne 0 ]
then
	$ECHO " fail!"
	$ECHO "--> check $LOG_FILE for info"
	exit 69
else
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	$ECHO " pass ("$t "sec.)" 
fi

# collect attribute types via separate script
stepString="(07/$STEPS) Collecting attribute type information ..."
$ECHO -n $stepString
$ECHO $stepString >> $LOG_FILE
t1=`date '+%s'`
$FETCH/scripts/parserExt/getJavaFields.pl $SRC_PATH > $SRC_PATH/dbdump/$PROJ_NAME.attributetypes
if [ $? -ne 0 ]
then
	$ECHO " fail!"
	$ECHO "--> check $LOG_FILE for info"
	exit 69
else
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	$ECHO " pass ("$t "sec.)"
fi

stepString="(08/$STEPS) Collecting size and complexity metrics ..."
$ECHO -n $stepString
$ECHO $stepString >> $LOG_FILE
t1=`date '+%s'`
find $SRC_PATH -name "*.java" | xargs sh $NCSS -function > $SRC_PATH/dbdump/$PROJ_NAME.ncssmetrics 2>> $LOG_FILE
# don't check, otherwise not robust anymore ...
t2=`date '+%s'`
t=`expr $t2 - $t1`
$ECHO " pass ("$t "sec.)"

stepString="(09/$STEPS) Collecting annotations ..."
$ECHO -n $stepString
$ECHO $stepString >> $LOG_FILE
t1=`date '+%s'`
cd $SRC_PATH
find . -name "*.java" | xargs grep -nH "@" > $SRC_PATH/dbdump/$PROJ_NAME.annotations 2>> $LOG_FILE
# don't check, otherwise not robust anymore ...
t2=`date '+%s'`
t=`expr $t2 - $t1`
$ECHO " pass ("$t "sec.)"

# some cleanup, these files eat hard disk
stepString="(10/$STEPS) Cleaning temporary files ..."
$ECHO -n $stepString
$ECHO $stepString >> $LOG_FILE
t1=`date '+%s'`
rm -rf $SRC_PATH/$PROJ_PATH.proj $SRC_PATH/.snprj $SRC_PATH/dbdump/*.tmp
if [ $? -ne 0 ]
then
	$ECHO " fail!"
	$ECHO "--> check $LOG_FILE for info"
	exit 69
else
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	$ECHO " pass ("$t "sec.)"
fi

# snavtofamix
stepString="(11/$STEPS) Running snavtofamix ..."
$ECHO -n $stepString
$ECHO $stepString >> $LOG_FILE
cd $FETCH/src/snavtofamix/src
rm -rf ../snavtofamix.log
t1=`date '+%s'`
python snavJavaToFamix.py $SRC_PATH/dbdump $PROJ_NAME $PROJ_NAME.cdif >> $LOG_FILE
if [ $? -ne 0 ]
then
	$ECHO " fail!"
	$ECHO "--> check $LOG_FILE for info"
	exit 69
else
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	$ECHO " pass ("$t "sec.)"
fi
if [ -f ../snavtofamix.log ]
then
	cat ../snavtofamix.log >> $LOG_FILE
fi
mv $PROJ_NAME.cdif $TARGET/

#cdif2rsf
stepString="(12/$STEPS) Running CDIF2RSF ..."
$ECHO -n $stepString
$ECHO $stepString >> $LOG_FILE
cd $FETCH/src/CDIF2RSF/
t1=`date '+%s'`
python cdif2rsf.py $TARGET/$PROJ_NAME.cdif $TARGET/$PROJ_NAME.rsf >> $LOG_FILE
if [ $? -ne 0 ]
then
	$ECHO " fail to generate RSF Model!"
	$ECHO "--> check $LOG_FILE for info"
	$ECHO "--> CDIF written to $TARGET/$PROJ_NAME.cdif"
	exit 69
else
	t2=`date '+%s'`
	t=`expr $t2 - $t1`
	$ECHO " pass ("$t "sec.)"
	$ECHO "--> CDIF written to $TARGET/$PROJ_NAME.cdif"
	$ECHO "--> RSF written to $TARGET/$PROJ_NAME.rsf"
	$ECHO "--> You might want to check out $LOG_FILE"
fi
cd $HERE
$ECHO -n "MO:" `grep -c "^Module	" $TARGET/$PROJ_NAME.rsf` " "
$ECHO -n "FI:" `grep -c "^File	" $TARGET/$PROJ_NAME.rsf` " "
$ECHO -n "CL:" `grep -c "^Class	" $TARGET/$PROJ_NAME.rsf` " "
$ECHO -n "GV:" `grep -c "^GlobalVariable	" $TARGET/$PROJ_NAME.rsf` " "
$ECHO -n "ME:" `grep -c "^Method	" $TARGET/$PROJ_NAME.rsf` " "
$ECHO -n "FU:" `grep -c "^Function	" $TARGET/$PROJ_NAME.rsf` " "
$ECHO -n "IN:" `grep -c "^Invokes	" $TARGET/$PROJ_NAME.rsf` " "
$ECHO -n "AC:" `grep -c "^Accesses	" $TARGET/$PROJ_NAME.rsf` " "
$ECHO "MT:" `grep -c "^Measurement	" $TARGET/$PROJ_NAME.rsf` " "

exit 0

