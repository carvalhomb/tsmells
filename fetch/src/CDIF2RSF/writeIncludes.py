# !/usr/bin/python

import sys

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(64)

input_file=sys.argv[1]

files_file=open("filesWithIDs.txt", 'r')

files = {}

for line in files_file:
	lineSplittedInTabs=line.split("\t")
	fileId = lineSplittedInTabs[0]
	fileName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")
	
	if ( not(fileName in files) ):
		files[fileName] = fileId

input_file=open(input_file, 'r')
includeContainmentFile=open("includeBelongsToFile.txt",'w')
	
for line in input_file:
	line = line.strip()
		
	if line.startswith("(Include FM"):
		incId=line.split("M")[1]
			
		incingFile = ""
		incedFile = ""
		
		cdifEntity=""
		for incLine in input_file:
			incLine = incLine.strip()

			if incLine.startswith(")"):
				break
			elif incLine.startswith("(includingFile \""):
				incingFile=incLine.split("\"")[1]
			elif incLine.startswith("(includedFile \""):
				incedFile=incLine.split("\"")[1]
			
		if ( incId != "" and files.has_key(incingFile) and files.has_key(incedFile)):
			incingId = files[incingFile]
			incedId = files[incedFile]
			includeContainmentInfo=incId + "\t" + incingId + "\t" + incedId + "\n"
			includeContainmentFile.write(includeContainmentInfo)
			
input_file.close()
includeContainmentFile.close()
