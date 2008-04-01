#!/bin/bash

if [[ "$1" == "" ]]
then
    echo "usage: $0 <source root>"
fi

if [[ "$2" == "" ]]
then
    export TSMELLS_GDF="tsmells/$1.gdf"
    export TSMELLS_SRCPICKLE="tsmells/$1.srcloc"
    export TSMELLS_METRICPICKLE="tsmells/$1.metrics"
else
    export TSMELLS_GDF="$1"
    export TSMELLS_SRCPICKLE="$2"
    export TSMELLS_METRICPICKLE="$3"
fi

guess --nowarn $TSMELLS/src/viz/tsmellsviz.py
