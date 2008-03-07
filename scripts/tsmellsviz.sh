#!/bin/bash

export TSMELLS_GDF="$1"
export TSMELLS_SRCPICKLE="$2"

guess $TSMELLS/src/viz/tsmellsviz.py
