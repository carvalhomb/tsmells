#!/bin/bash

WRITEJUNITRML="${TSMELLS}/src/writeJavaTestEntities.rml"
JUNITASSERTS="${TSMELLS}/src/jUnitAsserts.rsf"
cat $1 $JUNITASSERTS | crocopat $WRITEJUNITRML | cat $1 -