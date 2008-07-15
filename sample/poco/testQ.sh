#!/bin/bash

export TSMELLS=$(cd ../.. && pwd)
export FETCH=$TSMELLS/fetch
export PATH=$TSMELLS/scripts:$TSMELLS/fetch/scripts:$TSMELLS/guess:$PATH
export GUESS_HOME=$TSMELLS/guess

#cpp2rsf.sh poco
#rsf2tsmell.py poco.rsf cppunit_poco 512
#tsmells2gdf.py poco
tsmellsviz.sh poco

rm -f DUPLI_TMP tmp_duplic_code__ tsmells.log DUMP_TEST_METRICS DUPLI_DONE
