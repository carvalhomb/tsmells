#!/bin/bash

USAGE="usage: writeJavaTestStats.sh <rsf> [<xunit.rml> [<crocomem>]]"


if [[ ("$1" == "") || ("$2" == "-h") || ("$2" == "--help") ]]
then
    echo $USAGE
    exit -1
fi

CROCOMEM="100"
XUNITINIT="$TSMELLS/src/initJavaTestEntities.rml"

if [ "$2" != "" ]
then
    XUNITINIT=$2
fi

if [ "$3" != "" ]
then
    CROCOMEM=$3
fi

TMPFILE=$(mktemp)
cat $XUNITINIT $TSMELLS/src/initAuxiliaryTestRelations.rml $TSMELLS/src/writeTestStats.rml > $TMPFILE
#cp $TMPFILE ./concat.rml
cat $1 | crocopat -m $CROCOMEM $TMPFILE
