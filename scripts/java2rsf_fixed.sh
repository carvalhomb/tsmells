#!/bin/bash

#
# Wrapper script for java2rsf.sh 
# includes some fixes for SN deficiencies
#

TEMP=$1_tmp
mkdir $TEMP
cp -r $1/* $TEMP
fixJavaAsserts.sh $TEMP
java2rsf.sh $TEMP
rm -rf $TEMP