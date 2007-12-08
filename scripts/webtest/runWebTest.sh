#!/bin/bash

PYTHONPATH=../outputtest:$PYTHONPATH
export PYTHONPATH

python ${TSMELLS}/scripts/webtest/webtest.py
