#!/bin/bash
# creates symlinks to header files

SMELLS="/home/nix/JaarProj/Smells"
HEADERS="${SMELLS}/src/headers"
CPPUNIT="${HEADERS}/cpp/cppunit_1.6.2"
JUNIT="${HEADERS}/java/junit"
JUNITNOSTAT="${HEADERS}/java/junitNonStat"
CURR=$(pwd)

TEMP=${SMELLS}/tests/MysteryGuest/cpp
cd ${TEMP} && echo ">> Entered ${TEMP}"
for i in $(ls); do
	cd $i/src && echo "  >> Entered $i/src"
	if [[ $? == 0 ]] ; then
		rm -f "file" &> /dev/null
		ln -s ${HEADERS}/cpp/file 'file'
		rm -f "cppunit" &> /dev/null
		ln -s $CPPUNIT 'cppunit'
		cd ../..
	fi
done
cd $CURR

TEMP=${SMELLS}/tests/MysteryGuest/java
cd ${TEMP} && echo ">> Entered $TEMP"
for i in $(ls); do
	cd $i/src && echo "  >> Entered $i/src"
	if [[ $? == 0 ]] ; then
		rm -f "file" &> /dev/null
		ln -s ${HEADERS}/java/file 'file'
		rm -f "junit" &> /dev/null
		ln -s $JUNIT 'junit'
		cd ../..
	fi
done
cd $CURR

TEMP=${SMELLS}/tests/AssertionRoulette/cpp
cd ${TEMP} && echo ">> Entered ${TEMP}"
for i in $(ls); do
	cd $i/src && echo "  >> Entered $i/src"
	if [[ $? == 0 ]] ; then
		rm -f 'cppunit' &> /dev/null
		ln -s $CPPUNIT 'cppunit'
		cd ../..
	fi
done
cd $CURR
