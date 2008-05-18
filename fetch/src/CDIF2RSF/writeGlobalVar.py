# !/usr/bin/python

import sys
import utility

def parseGlobalVariableDeclaration(line):
	cdifId=line.split("M")[1]	
	
	name = ""
	uniqueName = ""
	declaredType = ""
	declaredClass = ""
	sourceFile = ""
	start = ""
	typeSourceFile = ""
	typeLineNr = ""
		
	for globalVarLine in input_file:
		globalVarLine = globalVarLine.strip()
		
		if globalVarLine.startswith(")"):
			break
		elif globalVarLine.startswith("(name \""):
			name=globalVarLine.split("\"")[1]
		elif globalVarLine.startswith("(uniqueName \""):
			uniqueName=globalVarLine.split("\"")[1]
		elif globalVarLine.startswith("(declaredType \""):
			declaredType=globalVarLine.split("\"")[1]
		elif globalVarLine.startswith("(declaredClass \""):
			declaredClass=globalVarLine.split("\"")[1]
		elif globalVarLine.startswith("(sourceAnchor "):
			sourceFile = globalVarLine.split("\"")[1]
			start = globalVarLine.split("\"")[2].split(" ")[2]
		elif globalVarLine.startswith("(typeSourceAnchor "):
			typeSourceFile = globalVarLine.split("\"")[1]
			typeLineNr = globalVarLine.split("\"")[2].split(" ")[2]	
		
	globalVarInfo=cdifId + "\t\"" + uniqueName + "\"\n"
	output_file.write(globalVarInfo)

	if ( sourceFile != "" ):
		if ( not (sourceFile in utility.fileDict) ):
			assert False, ("Unknown file "+sourceFile+" globalVar "+cdifId)

		fileId=utility.fileDict[sourceFile]				
		globalVarContainmentInfo=cdifId + "\t" + fileId + "\t" + start + "\n"
		accessibleEntityContainmentFile.write(globalVarContainmentInfo)

	if ( declaredClass != "" and typeSourceFile != ""):
		declaredClassId = utility.getClassId(typeSourceFile,typeLineNr)

		if declaredClassId != "":
			globalVarTypeInfo=cdifId + "\t" + declaredClassId + "\n"
			globalVarTypeFile.write(globalVarTypeInfo)


## main
if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(64)

cdif_file=sys.argv[1]

output_file="globalVarsWithIDs.txt"

utility.initializeFileDictionary()

input_file=open(cdif_file, 'r')
output_file=open(output_file, 'w')
accessibleEntityContainmentFile=open("accessibleEntityBelongsToFile.txt", 'a')
globalVarTypeFile=open("globalVarHasClassAsType.txt", 'w')

for line in input_file:
	line = line.strip()
	
	if line.startswith("(GlobalVariable FM"):
		parseGlobalVariableDeclaration(line)

input_file.close()
accessibleEntityContainmentFile.close()