#!/usr/bin/python

import sys, re

oldRsf = open(sys.argv[1], 'r')
fixRsf = open(sys.argv[1] + "_fix", 'w')
locMeasurement = re.compile('^Measurement\t[1-9].*\t[1-9].*\t\"LOC\"\t[1-9].*$')
for line in oldRsf:
    if line.find('\t"LOC"\t') != -1:
        splitted = line.split('\t')
        loc  = splitted[-1]
        while len(loc) < 10:
            loc = '0' + loc
        splitted[-1] = loc
        fixRsf.write("\t".join(splitted))
    else:
        fixRsf.write(line)

