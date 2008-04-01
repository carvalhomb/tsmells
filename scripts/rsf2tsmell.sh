#!/bin/bash
# this is supposed to be called from rsf2tsmells.py

RSF=$1
RML=$2
PROJ=$3
MEM=$4
LOG="tsmells/"$PROJ.tsmell

START=$(date +%s)
echo "(00) Starting crocopat ... "
cat $1 | \
crocopat -m $MEM $RML + 2>&1 > "$LOG" |\
grep -Ev \(FALSE\|^Warning\|^used\)


echo  -n "[ AL:`grep -c ^AssertionLess $LOG`" | tee -a $LOG
echo  -n " AR:`grep -c ^AssertionRoulette $LOG`" | tee -a $LOG
echo  -n " DC:`grep -c ^DuplicatedCode $LOG`" | tee -a $LOG
echo  -n " FT:`grep -c ^ForTestersOnly $LOG`" | tee -a $LOG
echo  -n " DI:`grep -c ^IndirectTest $LOG`" | tee -a $LOG
echo  -n " DE:`grep -c ^IndentedTest $LOG`" | tee -a $LOG
echo  -n " MG:`grep -c ^MysteryGuest $LOG`" | tee -a $LOG
echo     " SE:`grep -c ^SensitiveEquality $LOG` ]" | tee -a $LOG

echo "elapsed: $((`date +%s` - $START))s result: $LOG, tsmells/$PROJ.testsuite" | tee -a $LOG
