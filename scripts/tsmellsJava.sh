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

PROJ=$1
PARAMS=( $@ )
M4SCRIPT=$TSMELLS/scripts/tsmellJava.m4
MACRO="-DANAST_JUNIT -DASSERTIONLESS -DDUPLICATED_CODE -DINDIRECT_TEST -DINDENTED_TEST -DMYSTERY_GUEST -DASSERTION_ROULETTE -DSENSITIVE_EQUALITY -DFOR_TESTERS_ONLY -DINDIRECT_TEST_TRESHOLD=4 -DDUPLICATED_CODE_PYGEN=$TSMELLS/src/DuplicatedCode.py -DDUPLICATED_CODE_TRESHOLD=4 -DMYSTERY_GUEST_BLACKLIST=$TSMELLS/tests/MysteryGuest/java/blacklist.rml"
RSF=$PROJ.rsf

START=`date +%s`
LOG="$PROJ.`date +%y-%m-%d@%H:%M`.tsmells"

java2rsf.sh $PROJ &&\
RML=$(mktemp) &&\
m4  $MACRO $M4SCRIPT > $RML &&\
cat $RML > rml.tmp &&\
echo "running croco" &&\
#cat $RSF | crocopat $RML 2> /dev/null &&\
cat $RSF | crocopat $RML | tee -a $LOG && \
rm -rf $RML &> /dev/null;

echo "--"
echo  -n "#AL:`grep ^AssertionLess $LOG | wc -l`" | tee -a $LOG
echo  -n "#AR:`grep ^AssertionRoulette $LOG | wc -l`" | -a tee $LOG
echo  -n "#DC:`grep ^DuplicatedCode $LOG | wc -l`" | -a tee $LOG
echo  -n "#DI:`grep ^IndirectTest $LOG | wc -l`" | -a tee $LOG
echo  -n "#DE:`grep ^IndentedTest $LOG | wc -l`" | -a tee $LOG
echo  -n "#MG:`grep ^MysteryGuest $LOG | wc -l`" | -a tee $LOG
echo  -n "#SE:`grep ^SensitiveEquality $LOG | wc -l`" | -a tee $LOG
echo  -n "#FT:`grep ^ForTestersOnly $LOG | wc -l`" | -a tee $LOG

echo ">> Took $((`date +%s` - $START)) seconds" | tee -a $LOG

