# !/usr/bin/python

import sys
import utility

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(2)

input_file=sys.argv[1]
output_file="typedefsWithIDs.txt"

input_file=open(input_file, 'r')
output_file=open(output_file, 'w')

for line in input_file:
	line = line.strip()
	
	if line.startswith("(TypeDef FM"):
		typeDefId=line.split("M")[1]
				
		aliasName = ""
		baseName = ""
		sourceFile = ""
		destSourceFile = ""
		destLineNr = ""
		
		cdifEntity=""
		for typedefLine in input_file:
			typedefLine = typedefLine.strip()

			if typedefLine.startswith(")"):
				break
			elif typedefLine.startswith("(declaredReturnType \""):
				aliasName=typedefLine.split("\"")[1]
			elif typedefLine.startswith("(baseReturnType \""):
				baseName=typedefLine.split("\"")[1]
			elif typedefLine.startswith("(sourceAnchor "):
				sourceFile=typedefLine.split("\"")[1].strip()
			elif typedefLine.startswith("(destinationSourceAnchor"):
				destSourceFile=typedefLine.split("\"")[1].strip()
				destLineNr = typedefLine.split("\"")[2].split(" ")[2]	
		
		if ( typeDefId != "" ):
			baseNameWithoutTemplates = baseName.split("<")[0]
			print "utility.getClassId("+destSourceFile+","+destLineNr+")"
			baseId = utility.getClassId(destSourceFile,destLineNr)
			
			if ( baseId != "" ):
				typeDefInfo=typeDefId + "\t\"" + aliasName + "\"\t\"" + baseName + "\""
				output_file.write(typeDefInfo)
				
				typeDefInfo="\t" + baseId + "\t\n"
				output_file.write(typeDefInfo)
								
input_file.close()
output_file.close()