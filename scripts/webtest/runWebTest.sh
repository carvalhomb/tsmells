#!/bin/bash

PYTHONPATH=$TSMELLS/lib/Cheetah:$TSMELLS/lib/CherryPy:$PYTHONPATH
export PYTHONPATH

python $TSMELLS/lib/Cheetah/bin/cheetah-compile *.tmpl
python ${TSMELLS}/scripts/webtest/webtest.py
