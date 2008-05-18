# !/usr/bin/python
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
# Copyright 2007 University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>
#

import sys
import os

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"sn-table-file"
  sys.exit(64)

input_file=sys.argv[1]
input_file=open(input_file, 'r')

output_file=sys.argv[1]+".lower"
output_file=open(output_file, 'w')

for aLine in input_file:
	if not ";" in aLine:
		output_file.close()
		os.system("rm "+sys.argv[1]+".lower")
		sys.exit(64)
	cols = aLine.split(";")
	left = cols[0]
	right = cols[1].rstrip()

	space = cols[0].rfind(" ")
	filePath = cols[0][space+1:]
	print left[:space]+" "+filePath.lower()+";"+right+"\n",
	#output_file.write(left[:space]+" "+filePath.lower()+";"+right+"\n")
output_file.close()
input_file.close()

