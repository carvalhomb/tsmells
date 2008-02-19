#!/bin/bash
# this is supposed to be called from rsf2tsmells.py

RSF=$1
RML=$2
PROJ=$3
LOG=$PROJ.tsmell

START=$(date +%s)
echo "(00) Starting crocopat ... "
cat $1 | \
crocopat $RML + 2>&1 > "$LOG" |\
grep -Ev \(FALSE\|^Warning\|^used\)


echo  -n "[ AL:`grep -c ^AssertionLess $LOG`"
echo  -n " AR:`grep -c ^AssertionRoulette $LOG`"
echo  -n " DC:`grep -c ^DuplicatedCode $LOG`"
echo  -n " FT:`grep -c ^ForTestersOnly $LOG`"
echo  -n " DI:`grep -c ^IndirectTest $LOG`"
echo  -n " DE:`grep -c ^IndentedTest $LOG`"
echo  -n " MG:`grep -c ^MysteryGuest $LOG`"
echo     " SE:`grep -c ^SensitiveEquality $LOG` ]"

echo "elapsed: $((`date +%s` - $START))s result: $LOG"
