# !/usr/bin/python

import sys

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(64)

fileName=sys.argv[1]

conditions = {}

##
# a two-phase approach:
##

# First, read all conditional compilation blocks and cache them
cdifFile=open(fileName, "r")
condCompFile=open("conditionalCompilationBlocks.txt", 'w')
	
for line in cdifFile:
	line = line.strip()
	cond = ""	
	if line.startswith("(CompilationCondition FM"):
		ccId=line.split("M")[1]
		
		cdifEntity=""
		for ccLine in cdifFile:
			ccLine = ccLine.strip()

			if ccLine.startswith(")"):
				break
			elif ccLine.startswith("(condition \""):
				cond=ccLine.split("\"")[1]

		if ccId != "":
			conditions[cond] = ccId
			condCompilationInfo=ccId + "\t\"" + cond + "\"\n"
			condCompFile.write(condCompilationInfo)
		
cdifFile.close()
condCompFile.close()

belongsToCondCompFile=open("entityBelongsToBlock.txt", 'w')
# Secondly, write entityBelongsToBlock file
cdifFile=open(fileName, "r")
for line in cdifFile:
	line = line.strip()
	
	# every entity potentially belongs to a conditional compilation block
	if " FM" in line:
		eId = line.split("FM")[1]
		
		cdifEntity = ""
		for entLine in cdifFile:
			entLine = entLine.strip()
			
			if entLine.startswith(")"):
				break
			elif entLine.startswith("(condCompiled \""):
				cond=entLine.split("\"")[1]
			
		if cond in conditions:
			condCompilationInfo=eId + "\t" + conditions[cond] + "\n"
			belongsToCondCompFile.write(condCompilationInfo)
			cond = ""
				
cdifFile.close()
belongsToCondCompFile.close()
