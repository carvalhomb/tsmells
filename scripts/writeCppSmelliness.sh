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

#
# Some pieces tests still use this script but its terribly
# deprecated. use rsf2tsmell.py instead
#
#$ grep -R "writeCppSmelliness.sh" *
#tests/MysteryGuest/cpp/FileAccessTestCommand/FileAccessTestCommand.cmd
#tests/AssertionLess/cpp/MultiTCNoAssert/MultiTCNoAssert.cmd
#tests/AssertionLess/cpp/SingleTCDefDeclNoAssert/SingleTCDefDeclNoAssert.cmd
#tests/AssertionLess/cpp/SingleTCDefDeclAssert/SingleTCDefDeclAssert.cmd
#tests/AssertionLess/cpp/SingleTCAssert/SingleTCAssert.cmd
#tests/AssertionLess/cpp/SingleTCNoAssert/SingleTCNoAssert.cmd
#

SMELL=$1
RSF=$2

if [ "$SMELL" == "MysteryGuest" ]
then
    RSF="$RSF $TSMELLS/src/dump/rml/MysteryBlacklistCpp.rsf"
fi

TMPFILE=$(mktemp) && \
cat $TSMELLS/src/dump/rml/initCppTestEntities.rml $TSMELLS/src/dump/rml/initAuxiliary.rml $TSMELLS/src/dump/rml/compute$SMELL.rml > $TMPFILE && \
cat $RSF | crocopat $TMPFILE && \
rm -rf $TMPFILE &> /dev/null;
