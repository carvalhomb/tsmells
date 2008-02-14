#!/bin/bash

CROCOMEM="50"

if [ "$2" != "" ]
then
    CROCOMEM=$2
fi

TMPFILE=$(mktemp)
cat $TSMELLS/src/initCppTestEntities.rml $TSMELLS/src/writeTestStats.rml > $TMPFILE
#cp $TMPFILE ./concat.rml
cat $1 | crocopat -m $CROCOMEM $TMPFILE