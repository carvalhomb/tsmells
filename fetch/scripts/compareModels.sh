#!/bin/bash
# This file is part of Fetch (the Fact Extraction Tool CHain).
#
# Fetch is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# Fetch is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Anastacia; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
# Copyright 2007  University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

##
# This script takes to source directories and compares how they differ
# (typically to be used for two versions of a same system). First, RSF
# models are generated for both source directories (if the RSF models
# do not yet exist at the expected place), next unique names lists for
# model entitites are extracted and compared.
# The results are reported in a diff -u file and in two report files
# that describe which entities have been deleted and added between these
# two versions.
#
##

CROCOPAT=../bin/crocopat-linux-ppc
ARGS=2

if [ $# -ne "$ARGS" ]
then
  echo "Usage: `basename $0` SRC_ROOT1 SRC_ROOT2"
  exit 64
fi

PROJ1_NAME=`basename $1`
PROJ2_NAME=`basename $2`

# if .rsf files don't exist yet, use cpp2rsf script to create them from
source code via the tool chain
if [ ! -f $PROJ1_NAME.rsf ]
then
	./cpp2rsf.sh $1
fi

if [ ! -f $PROJ2_NAME.rsf ]
then
	./cpp2rsf.sh $2
fi

# extract unique names for both structural as well as behavioral entities using crocopat
$CROCOPAT rml/reportStructuralUniqueNames.rml < $PROJ1_NAME.rsf > $PROJ1_NAME-StructuralUniqueNames.txt
$CROCOPAT rml/reportBehavioralUniqueNames.rml < $PROJ1_NAME.rsf > $PROJ1_NAME-BehavioralUniqueNames.txt
$CROCOPAT rml/reportStructuralUniqueNames.rml < $PROJ2_NAME.rsf > $PROJ2_NAME-StructuralUniqueNames.txt
$CROCOPAT rml/reportBehavioralUniqueNames.rml < $PROJ2_NAME.rsf > $PROJ2_NAME-BehavioralUniqueNames.txt
cat $PROJ1_NAME-StructuralUniqueNames.txt | sort > $PROJ1_NAME-structural.txt
cat $PROJ1_NAME-BehavioralUniqueNames.txt | sort > $PROJ1_NAME-behavioral.txt
cat $PROJ2_NAME-StructuralUniqueNames.txt | sort > $PROJ2_NAME-structural.txt
cat $PROJ2_NAME-BehavioralUniqueNames.txt | sort > $PROJ2_NAME-behavioral.txt
rm $PROJ1_NAME-StructuralUniqueNames.txt $PROJ2_NAME-StructuralUniqueNames.txt
rm $PROJ1_NAME-BehavioralUniqueNames.txt $PROJ2_NAME-BehavioralUniqueNames.txt

# create diff and comm reports
diff -u $PROJ1_NAME-structural.txt $PROJ2_NAME-structural.txt > $PROJ1_NAME-$PROJ2_NAME-structural.diff
diff -u $PROJ1_NAME-behavioral.txt $PROJ2_NAME-behavioral.txt > $PROJ1_NAME-$PROJ2_NAME-behavioral.diff
comm -3 -2 $PROJ1_NAME-structural.txt $PROJ2_NAME-structural.txt > $PROJ1_NAME-$PROJ2_NAME-structural-REMOVED.txt
comm -3 -2 $PROJ1_NAME-behavioral.txt $PROJ2_NAME-behavioral.txt > $PROJ1_NAME-$PROJ2_NAME-behavioral-REMOVED.txt
comm -3 -1 $PROJ1_NAME-structural.txt $PROJ2_NAME-structural.txt > $PROJ1_NAME-$PROJ2_NAME-structural-ADDED.txt
comm -3 -1 $PROJ1_NAME-behavioral.txt $PROJ2_NAME-behavioral.txt > $PROJ1_NAME-$PROJ2_NAME-behavioral-ADDED.txt
exit 0

