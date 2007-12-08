#!/bin/bash

PYTHONPATH=/home/nix/JaarProj/outputtest:$PYTHONPATH
export PYTHONPATH

python ${TSMELLS}/scripts/runTests.py ${TSMELLS}/tests/MysteryGuest ${TSMELLS}/tests/AssertionRoulette ${TSMELLS}/tests/AssertionLess
