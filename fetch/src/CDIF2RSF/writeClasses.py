# !/usr/bin/python

import sys

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(64)

input_file=sys.argv[1]

output_file="classesWithIDs.txt"

files_file=open("filesWithIDs.txt", 'r')

files = {}

for line in files_file:
	lineSplittedInTabs=line.split("\t")
	fileId = lineSplittedInTabs[0]
	fileName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")
	
	if ( not(fileName in files) ):
		files[fileName] = fileId

input_file=open(input_file, 'r')
output_file=open(output_file, 'w')
classContainmentFile=open("classBelongsToFile.txt",'w')
	
for line in input_file:
	line = line.strip()
		
	if line.startswith("(Class FM"):
		classId=line.split("M")[1]
			
		className = ""
		uniqueClassName = ""
		isAbstract = ""
		sourceFile = ""
		lineNr = ""
		
		cdifEntity=""
		for classLine in input_file:
			classLine = classLine.strip()

			if classLine.startswith(")"):
				break
			elif classLine.startswith("(name \""):
				className=classLine.split("\"")[1]
			elif classLine.startswith("(uniqueName \""):
				uniqueClassName=classLine.split("\"")[1]
			elif classLine.startswith("(isAbstract -"):
				isAbstract = classLine.split("-")[1]
			elif classLine.startswith("(sourceAnchor "):
				sourceFile=classLine.split("\"")[1].strip()
				lineNr=classLine.split("\"")[2].split(" ")[2]
			
		if ( classId != "" ):
			classInfo=classId + "\t\"" + uniqueClassName + "\"\n"
			output_file.write(classInfo)
			
			if ( sourceFile != "" ):
				fileId = files[sourceFile]
				#print classId," ",uniqueClassName," belongs to ",fileId," ",sourceFile
				classContainmentInfo=classId + "\t" + fileId + "\t" + lineNr + "\n"
				classContainmentFile.write(classContainmentInfo)
							
input_file.close()
output_file.close()	
classContainmentFile.close()
