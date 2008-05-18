# !/usr/bin/python

import sys
import utility

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(2)

input_file=sys.argv[1]
output_file="inheritanceWithIDs.txt"

input_file=open(input_file, 'r')
output_file=open(output_file, 'w')

for line in input_file:
	line = line.strip()
	
	if line.startswith("(InheritanceDefinition FM"):
		inhId=line.split("M")[1]
			
		subName = ""
		superName = ""
		sourceFile = ""
		lineNr = ""
		superClassSourceFile = ""
		superClassLineNr = ""
		
		cdifEntity=""
		for inhLine in input_file:
			inhLine = inhLine.strip()

			if inhLine.startswith(")"):
				break
			elif inhLine.startswith("(subclass \""):
				subName=inhLine.split("\"")[1]
			elif inhLine.startswith("(superclass \""):
				superName=inhLine.split("\"")[1]
			elif inhLine.startswith("(sourceAnchor "):
				sourceFile=inhLine.split("\"")[1].strip()
				lineNr = inhLine.split("\"")[2].split(" ")[2]
			elif inhLine.startswith("(superclassSourceAnchor"):
				superClassSourceFile=inhLine.split("\"")[1].strip()
				superClassLineNr = inhLine.split("\"")[2].split(" ")[2]
				
		if ( inhId != "" ):
			subclassId = utility.getClassId(sourceFile,lineNr)
			superclassId = utility.getClassId(superClassSourceFile,superClassLineNr)
			
			if ( subclassId != "" ) and (superclassId != ""):
				inhInfo=inhId + "\t\"" + subName + "->" + superName + "\""
				output_file.write(inhInfo)
				
				inhInfo="\t" + subclassId + "\t" + superclassId + "\n"
				output_file.write(inhInfo)
			else:
				print "subclassId  ("+subName+","+sourceFile+"@"+lineNr+"): ",subclassId
				print "superclassId(" + superName + "," + superClassSourceFile + "@" + superClassLineNr + "): ", superclassId
		else:
			"inhId is empty"
								
input_file.close()
output_file.close()