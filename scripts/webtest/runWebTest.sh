#!/bin/bash

PYTHONPATH=../outputtest:$PYTHONPATH
export PYTHONPATH

cheetah-compile *.tmpl
python ${TSMELLS}/scripts/webtest/webtest.py
