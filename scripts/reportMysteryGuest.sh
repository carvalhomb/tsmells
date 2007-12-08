#!/bin/bash

if [ -z $1 ]; then
	echo "Provide an RSF"
	exit -1
fi

appendCppTE.sh $1 | cat - ${TSMELLS}/src/MysteryBlacklistCpp.rsf | crocopat ${TSMELLS}/src/MysteryGuest.rml