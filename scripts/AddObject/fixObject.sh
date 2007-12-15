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

if [ -z "$1" ]; then
	echo "Provide dbdump directory"
	exit -1
fi

DUMPDIR=`echo $1 | sed -e 's/\/$//'`

python $TSMELLS/scripts/AddObject/fixObjectInheritance.py $DUMPDIR

OBJDECL="Object 000001.001 java/lang/Object.java;1.00 0xc {} {} {} {}"
OBJMETHODS="Object clone 000002.001 java/lang/Object.java;02.00 0xc {java.lang.Object} {} {} {}
Object equals 000003.001 java/lang/Object.java;03.00 0xc {boolean} {java.lang.Object} {obj} {}
Object finalize 000004.001 java/lang/Object.java;04.00 0xc {void} {} {} {}
Object getClass 000005.001 java/lang/Object.java;05.00 0xc {java.lang.Class} {} {} {}
Object hasCode 000006.001 java/lang/Object.java;06.00 0xc {int} {} {} {}
Object notify 000007.001 java/lang/Object.java;07.00 0xc {void} {} {} {}
Object notifyAll 000008.001 java/lang/Object.java;08.00 0xc {void} {} {} {}
Object toString 000009.001 java/lang/Object.java;09.00 0xc {java.lang.String} {} {} {}
Object wait 000010.001 java/lang/Object.java;10.00 0xc {void} {} {} {}
Object wait 000011.001 java/lang/Object.java;11.00 0xc {void} {long} {timeout} {}
Object wait 000012.001 java/lang/Object.java;12.00 0xc {void} {long,int} {timeout,nanos} {}"
OBJPKG="java/lang/Object.java;1;package java.lang;"
OBJFILE="java/lang/Object.java;java 1"

echo "$OBJDECL"    >> $DUMPDIR/*.classes
echo "$OBJMETHODS" >> $DUMPDIR/*.methods
echo "$OBJMETHODS" >> $DUMPDIR/*.methoddefs
echo "$OBJPKG"     >> $DUMPDIR/*.packages
echo "$OBJFILE"    >> $DUMPDIR/*.files