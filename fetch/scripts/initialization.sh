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
# Copyright 2007-2008  University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

##
# initialization.sh is a script that encapsulates the initialization
# of script variables for both cpp2rsf.sh and java2rsf.sh
# 
# In case you would like to reuse the variables initialized here,
# include this shell script by using the following line:
# . initialization.sh
##

# The builtin echo command ignores the -n command
# Accordingly, we lookup the true echo command.
ECHO=`which echo`

if [ -z "$SN_HOME" ]
then
	$ECHO "SN_HOME variable should be specified."
	exit 66
fi

if [ -z "$FETCH" ]
then
	$ECHO "FETCH variable should be specified."
	exit 66
fi

XVFB=0 # 0 => don't use a Virtual Xserver
TARGET=`pwd`
SN=snavigator
VCS=0

# keep out of the remainder
ARGS=1
SRC_PATH=$(cd "$1" && pwd)

if [ $# -lt "$ARGS" ]
then
  $ECHO "Usage: `basename $0` SRC_ROOT [VCS file]"
  exit 64
elif [ $# -eq "2" ]
then
  VCS=1
  VCS_PATH=$(cd `dirname "$2"` && pwd)
  VCS_FILE=`basename $2`
fi

if [ ! -d $SRC_PATH ]
then
  $ECHO -N "$SRC_PATH is not an existing path"
  $ECHO "[exiting]"
  exit 66
fi

PROJ_NAME=`basename $SRC_PATH`

if [ $XVFB -eq 1 ]
then
	# Virtual X, we don't need an X server to be running for Source Navigator
	# http://en.wikipedia.org/wiki/Xvfb
	# http://www.neowin.net/forum/index.php?showtopic=449055
	# search free slot for Xserver
	for i in `seq 3000 4000`
	do
	        if [ ! -f /tmp/.X$i-lock ]
	                then
	                        display=$i
	                        break
	        fi
	done
	DISPLAY=:$display
fi