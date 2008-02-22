#!/bin/bash

CLEAN_DIR=${TSMELLS}
rm -i $(find $CLEAN_DIR -name "*~" -o -name "*.err" -o -name "*.proj" -o -name "*.class" -o -name "src.cdif"  -o -name "tmp_duplic_code__" -o -name "*.res" -o -name "*.exp.old" -o -name "*.py.bak" -o -name "*.pyc")
rm -irf $(find $CLEAN_DIR -name dbdump)
