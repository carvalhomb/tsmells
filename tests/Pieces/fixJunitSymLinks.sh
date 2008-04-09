#!/bin/bash

TEST_SMELL_DIRS="AssertionLess AssertionRoulette DuplicatedCode ForTestersOnly IndentedTest IndirectTest EagerTest GeneralFixture MysteryGuest SensitiveEquality TestEntities"


for i in $TEST_SMELL_DIRS;
do
    TOPDIR=`pwd`
    cd $i/java;
    for j in `ls | grep -v "generic" | grep -v "blacklist.rml"`;
    do
        CURRDIR=`pwd`
        cd $j/src
        #svn rm org #&&\
        ln -s ../../../../../../stubs/java/org/
        cd $CURRDIR;
    done ;
    cd $TOPDIR
done
