#!/bin/bash
# This file is part of snavtofamix (Source Navigator to FAMIX).
#
# snavtofamix is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation; either version 2 of the License, or (at your
# option) any later version.
#
# snavtofamix is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
# details.
#
# You should have received a copy of the GNU General Public License along
# with snavtofamix; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
#
# Copyright 2007  University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

##
# This script contains a lightweight, heuristical method to detect namespaces
# in Java source code.
##

ARGS=1         # Script requires 1 argument.

if [ $# -ne "$ARGS" ]
then
  echo "Usage: `basename $0` PROJECT_PATH"
  exit 66
fi


PROJ_PATH=$1
PROJECT_NAME=$2

cd  $PROJ_PATH
for i in `find -L . -name "*.java" -print`; do grep -nH "package " $i | grep -v "Binary file" | sed "s/:/;/g" | sed "s/\.\///g"; done

