#!/bin/bash

CLEAN_DIR=${TSMELLS}
rm -i $(find $CLEAN_DIR -name "*~" -o -name "*.err" -o -name "*.proj" -o -name "*.class")
rm -irf $(find $CLEAN_DIR -name dbdump)

CLEAN_DIR=${TSMELLS}/../Bug
rm -i $(find $CLEAN_DIR -name "*~" -o -name "*.err" -o -name "*.proj" -o -name "*.rsf" -o -name "*.cdif")

