#!/bin/bash

XUNITRML=$TSMELLS/src/initCppTestEntities.rml

if [ "$2" != "" ]
then
    XUNITRML=$TSMELLS/src/$2
fi

CROCOMEM="50"

if [ "$3" != "" ]
then
    CROCOMEM=$3
fi

TMPFILE=$(mktemp)
cat $XUNITRML $TSMELLS/src/initAuxRel.rml  $TSMELLS/src/writeTestStats.rml > $TMPFILE
#cp $TMPFILE ./concat.rml
cat $1 | crocopat -m $CROCOMEM $TMPFILE
