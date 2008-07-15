#!/bin/bash

export TSMELLS=$(cd ../.. && pwd)
export FETCH=$TSMELLS/fetch
export PATH=$TSMELLS/scripts:$TSMELLS/fetch/scripts:$TSMELLS/guess:$PATH
export GUESS_HOME=$TSMELLS/guess

java2rsf.sh anastacia
rsf2tsmell.py anastacia.rsf junit-3.x_ana
tsmells2gdf.py anastacia
tsmellsviz.sh anastacia

rm -f DUPLI_TMP tmp_duplic_code__ tsmells.log DUMP_TEST_METRICS DUPLI_DONE
