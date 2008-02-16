#!/bin/bash

CLEAN_DIR=${TSMELLS}
rm -i $(find $CLEAN_DIR -name "*~" -o -name "*.err" -o -name "*.proj" -o -name "*.class" -o -name "src.cdif"  -o -name "tmp_duplic_code__" -o -name "*.res")
rm -irf $(find $CLEAN_DIR -name dbdump)

CLEAN_DIR=${TSMELLS}/../Bug
rm -i $(find $CLEAN_DIR -name "*~" -o -name "*.err" -o -name "*.proj" -o -name "*.rsf" -o -name "*.cdif")

