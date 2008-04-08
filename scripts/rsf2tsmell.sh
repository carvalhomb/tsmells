#!/bin/bash
# this is supposed to be called from rsf2tsmells.py

RSF=$1
RML=$2
PROJ=$3
MEM=$4
RES="tsmells/"$PROJ.tsmell
LOG="tsmells.log"

echo "" >> $LOG
echo '#--------------------------------------------------------------------->>' >> $LOG
echo '#-- Running rsf2tsmells' >> $LOG
echo "#-- $PROJ  " >> $LOG
echo '#--' $(date) >> $LOG
echo '#--------------------------------------------------------------------->>' >> $LOG
echo "" >> $LOG

START=$(date +%s)
echo "(00) Starting crocopat ... "
cat $1 | \
crocopat -m $MEM $RML 2>&1 > "$RES" |\
grep -Ev \(FALSE\|^Warning\|^used\)

echo "" >> $LOG
echo  -n "[ AL:`grep -c ^AssertionLess $RES`"      | tee -a $LOG
echo  -n " AR:`grep -c ^AssertionRoulette $RES`"   | tee -a $LOG
echo  -n " DC:`grep -c ^DuplicatedCode $RES`"      | tee -a $LOG
echo  -n " ET:`grep -c ^EagerTest $RES`"           | tee -a $LOG
echo  -n " FT:`grep -c ^ForTestersOnly $RES`"      | tee -a $LOG
echo  -n " GF:`grep -c ^GeneralFixture $RES`"      | tee -a $LOG
echo  -n " DI:`grep -c ^IndirectTest $RES`"        | tee -a $LOG
echo  -n " DE:`grep -c ^IndentedTest $RES`"        | tee -a $LOG
echo  -n " MG:`grep -c ^MysteryGuest $RES`"        | tee -a $LOG
echo     " SE:`grep -c ^SensitiveEquality $RES` ]" | tee -a $LOG

echo "elapsed: $((`date +%s` - $START))s result: $RES, tsmells/$PROJ.testsuite" | tee -a $LOG
