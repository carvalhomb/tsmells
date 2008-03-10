#!/bin/bash

XUNITRML=initCppTestEntities.rml

if [ "$2" != "" ]
then
    XUNITRML=$2
fi

CROCOMEM="50"

if [ "$3" != "" ]
then
    CROCOMEM=$3
fi

TMPFILE=$(mktemp)
cat $TSMELLS/src/$XUNITRML $TSMELLS/src/initAuxiliaryTestRelations.rml  $TSMELLS/src/writeTestStats.rml > $TMPFILE
#cp $TMPFILE ./concat.rml
cat $1 | crocopat -m $CROCOMEM $TMPFILE
