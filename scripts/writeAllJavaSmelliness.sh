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

CROCOMEM=50
if [ ! "$2" == "" ] 
then
    CROCOMEM=$2
fi

CONCATRML="$TSMELLS/src/initJavaTestEntities.rml $TSMELLS/src/MysteryGuest.rml $TSMELLS/src/AssertionRoulette.rml $TSMELLS/src/AssertionLess.rml $TSMELLS/src/SensitiveEquality.rml"

RSF="$RSF $TSMELLS/src/MysteryBlacklistJava.rsf"

cat $CONCATRML > concat.rml

TMPFILE=$(mktemp) && \
cat  $CONCATRML > $TMPFILE && \
cat $RSF | crocopat -m $CROCOMEM $TMPFILE && \
rm -rf $TMPFILE &> /dev/null;
