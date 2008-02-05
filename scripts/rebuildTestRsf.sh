#!/bin/bash

cd $TSMELLS/tests

for i in `ls | grep -v "RmComments" | grep -v "LazyTest" | grep -v "GenCtrlStructInfo"`; do
    TOPDIR=`pwd`
	cd $i/java; 
	for j in `ls | grep -v "generic" | grep -v "blacklist.rml"`; do
		echo -n "$i->$j ... "
        CURRDIR=`pwd`
		cd $j
		java2rsf.sh src/ > /dev/null && \
        rm -rf src.cdif src/dbdump src/*.proj src/*.err &&\
		mv src.rsf rsf/$j.rsf &&\
		echo "done";
		cd $CURRDIR;
	done ;
    cd $TOPDIR
done;