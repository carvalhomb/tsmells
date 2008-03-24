#!/bin/bash

export TSMELLS_GDF="$1"
export TSMELLS_SRCPICKLE="$2"
export TSMELLS_METRICPICKLE="$3"

guess --nowarn $TSMELLS/src/viz/tsmellsviz.py
