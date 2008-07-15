#!/bin/bash

export TSMELLS=$(cd ../.. && pwd)
export FETCH=$TSMELLS/fetch
export PATH=$TSMELLS/scripts:$TSMELLS/fetch/scripts:$TSMELLS/guess:$PATH
export GUESS_HOME=$TSMELLS/guess

#cpp2rsf.sh argouml
#rsf2tsmell.py argouml.rsf junit-3.8_nostub 512
#tsmells2gdf.py argouml
tsmellsviz.sh argouml

rm -f DUPLI_TMP tmp_duplic_code__ tsmells.log DUMP_TEST_METRICS DUPLI_DONE
