#!/bin/bash

TEST_SMELL_DIRS="AssertionLess AssertionRoulette DuplicatedCode ForTestersOnly IndentedTest IndirectTest EagerTest GeneralFixture MysteryGuest SensitiveEquality TestEntities"

for i in $TEST_SMELL_DIRS; 
do
    CURR=`pwd`;
    cd $i/java; 
    for j in `ls | grep -v "generic.cmd" | grep -v "blacklist.rml"`;
    do
        CURR2=`pwd`; 
        cd $j;
        cp $j.rsf $j.rsf.`date +%m%d`.backup;
        cd $CURR2;
    done;
    cd $CURR;
done
