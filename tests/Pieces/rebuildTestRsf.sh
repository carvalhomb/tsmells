#!/bin/bash

TEST_SMELL_DIRS="AssertionLess AssertionRoulette DuplicatedCode ForTestersOnly IndentedTest IndirectTest EagerTest GeneralFixture MysteryGuest SensitiveEquality TestEntities"

for i in $TEST_SMELL_DIRS;
do
    TOPDIR=`pwd`
    cd $i/java;
    for j in `ls | grep -v "generic" | grep -v "blacklist.rml"`;
    do
        echo -n "$i->$j ... "
        CURRDIR=`pwd`
        cd $j
        java2rsf.sh src/ > /dev/null && \
        rm -rf src.cdif src/dbdump src/*.proj src/*.err &&\
        mv src.rsf $j.rsf &&\
        echo "done";
        cd $CURRDIR;
    done ;
    cd $TOPDIR
done;
