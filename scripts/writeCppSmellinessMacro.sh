#!/bin/bash
# This file is part of Tsmells
#
# Tsmells is free software; you can redistribute it and/or modify it 
# under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 2 of the License, or (at your 
# option) any later version.
#
# Tsmells is distributed in the hope that it will be useful, but WITHOUT 
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more 
# details.
#
# You should have received a copy of the GNU General Public License along 
# with outputtest; if not, write to the Free Software Foundation, Inc., 
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA 
#
# Copyright 2007 Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>
#

RSF=$1

PARAMS=( $@ )
M4SCRIPT=$TSMELLS/scripts/tsmellsCpp.m4

MACRO="-DXUNIT_INIT=$TSMELLS/src/initCppTestEntities.rml"
for i in $(seq 1 $(( $# - 1 )))
do
    MACRO="$MACRO ${PARAMS[i]}"
done

RML=$(mktemp) && \
m4  $MACRO $M4SCRIPT > $RML &&\
#cat $RML &&\
cat $RSF | crocopat $RML 2> /dev/null && \
#cat $RSF | crocopat $RML && \
rm -rf $RML &> /dev/null;
