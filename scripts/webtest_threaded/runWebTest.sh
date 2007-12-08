#!/bin/bash

PYTHONPATH=/home/nix/JaarProj/outputtest:$PYTHONPATH
export PYTHONPATH

python ${TSMELLS}/src/webtest/webtest.py
