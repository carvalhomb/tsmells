#!/bin/bash

CROCOMEM="50"

if [ "$2" != "" ]
then
    CROCOMEM=$2
fi

TMPFILE=$(mktemp)
cat $TSMELLS/src/initJavaTestEntities.rml $TSMELLS/src/writeJavaTestStats.rml > $TMPFILE
cat $1 | crocopat -m $CROCOMEM $TMPFILE