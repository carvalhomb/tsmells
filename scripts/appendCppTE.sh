#!/bin/bash

WRITECPPUNITRML="${TSMELLS}/src/writeCppTestEntities.rml"
CPPUNITASSERTS="${TSMELLS}/src/cppUnitAsserts.rsf"
cat $1 $CPPUNITASSERTS | crocopat $WRITECPPUNITRML | cat $1 -
