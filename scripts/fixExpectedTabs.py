#!/usr/bin/python
import sys
import os

f = open(sys.argv[1], 'r')
os.system("cp " + sys.argv[1] + " " + sys.argv[1] + ".old")
lines = []
for line in f:
	spl = line.split(';')
	spl[2] = "\"" + spl[2] + "\""
	spl[4] = "\"" + spl[4] + "\""
	lines.append("\t".join(spl))
f = open(sys.argv[1], "w")
f.write("".join(lines))