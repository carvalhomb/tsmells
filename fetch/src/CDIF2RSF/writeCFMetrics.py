# !/usr/bin/python

import sys

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(64)

input_file=sys.argv[1]
output_file="cfMetricsWithIDs.txt"

fileEntities = {}

# build files dictionary
file_file=open("filesWithIDs.txt", 'r')for line in file_file:
	lineSplittedInTabs=line.split("\t")
	fileId = lineSplittedInTabs[0]
	fileName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")
	
	if ( not(fileName in fileEntities) ):
		fileEntities[fileName] = fileId
		
file_file.close()
# iterate cdif file and prepare CF Metrics
input_file=open(input_file, 'r')
output_file=open(output_file, 'w')
for line in input_file:
	line = line.strip()
	
	if line.startswith("(Measurement FM"):
		cdifId=line.split("M")[2]	

		name = ""
		value = ""
		owner = ""
		
		for measLine in input_file:
			measLine = measLine.strip()
				
			if measLine.startswith(")"):
				break
			elif measLine.startswith("(name \""):
				name=measLine.split("\"")[1]
			elif measLine.startswith("(value \""):
				value=measLine.split("\"")[1]	
			elif measLine.startswith("(belongsTo \""):
				owner=measLine.split("\"")[1]	
				
		# at the file level, we only consider the change frequency metric CF
		if name != "CF": continue
		# do we know all the referenced entities?				
		if ( owner != "" ):
			if ( not (owner in fileEntities) ):
				print "Skipping file metric generation: unknown owner "+owner+" for CDIF-id "+cdifId
				continue
		else:
			print "Skipping file metric generation: empty owner for metric",name,"with CDIF-id",value
			continue
			
		fileId=fileEntities[owner]				
		measInfo=cdifId + "\t" + fileId + "\t\"" + name + "\"\t" + value + "\n"
		output_file.write(measInfo)
input_file.close()
output_file.close()
