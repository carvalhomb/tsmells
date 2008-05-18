# !/usr/bin/python

import sys
import utility

def parseFunctionDeclaration(line):
	cdifId=line.split("M")[1]

	name = ""
	uniqueName = ""
	parameters = ""
	declaredReturnType = ""
	declaredReturnClass = ""
	sourceFile = ""
	start = ""
	typeSourceFile = ""
	typeLineNr = ""

	for functionLine in input_file:
		functionLine = functionLine.strip()

		if functionLine.startswith(")"):
			break
		elif functionLine.startswith("(name \""):
			name=functionLine.split("\"")[1]
		elif functionLine.startswith("(uniqueName \""):
			uniqueName=functionLine.split("\"")[1]
			parameters=""
			uniqueNameBrokenUp=uniqueName.split("(")
			nrOfBrackets=len(uniqueNameBrokenUp)

			bracketIndex=1
			while ( bracketIndex < nrOfBrackets ):
				parameters += uniqueNameBrokenUp[bracketIndex]
				bracketIndex+=1

			parameters = parameters.rstrip(")")

		elif functionLine.startswith("(declaredReturnType \""):
			declaredReturnType=functionLine.split("\"")[1]
		elif functionLine.startswith("(declaredReturnClass \""):
			declaredReturnClass=functionLine.split("\"")[1]
		elif functionLine.startswith("(sourceAnchor "):
			sourceFile = functionLine.split("\"")[1]
			start = functionLine.split("\"")[2].split(" ")[2]
		elif functionLine.startswith("(typeSourceAnchor "):
			typeSourceFile = functionLine.split("\"")[1]
			typeLineNr = functionLine.split("\"")[2].split(" ")[2]

	functionInfo=cdifId + "\t\"" + uniqueName + "\"\n"
	output_file.write(functionInfo)

	if ( sourceFile != "" ):
		if ( not (sourceFile in files) ):
			print sourceFile, files
			assert False, ("Unknown file "+sourceFile+" function "+cdifId)

		fileId=files[sourceFile]
		functionContainmentInfo=cdifId + "\t" + fileId + "\t" + start + "\n"
		invokableEntityContainmentFile.write(functionContainmentInfo)

	if ( declaredReturnClass != "" and typeSourceFile != ""):
		declaredReturnClassId = utility.getClassId(typeSourceFile,typeLineNr)

		if declaredReturnClassId != "":
			functionTypeInfo=cdifId + "\t" + declaredReturnClassId + "\n"
			functionTypeFile.write(functionTypeInfo)

##
#(FunctionDefinition FM2047
#        (name "coltolong")
#        (declaredBy "coltolong(string)")
#        (uniqueName "coltolong(string)_1")
#        (sourceAnchor #[file "spreadsheet/util.cxx" start 69 end 69|]#)
#        (declSourceAnchor #[file "spreadsheet/util.h" start 90 end 90|]#)
#)
##
def parseFunctionDefinition(line):
	cdifId=line.split("M")[1].strip()

	name = ""
	declaredBy = ""
	uniqueName = ""
	sourceFile = ""
	lineNr = ""
	declSourceFile = ""
	declSourceLine = ""

	for functionLine in input_file:
		functionLine = functionLine.strip()

		if functionLine.startswith(")"):
			break
		elif functionLine.startswith("(name \""):
			name=functionLine.split("\"")[1]
		elif functionLine.startswith("(declaredBy \""):
			declaredBy=functionLine.split("\"")[1]
		elif functionLine.startswith("(uniqueName \""):
			uniqueName=functionLine.split("\"")[1]
		elif functionLine.startswith("(sourceAnchor "):
			sourceFile = functionLine.split("\"")[1]
			lineNr = functionLine.split("\"")[2].split(" ")[2]
		elif functionLine.startswith("(declSourceAnchor "):
			declSourceFile = functionLine.split("\"")[1]
			declSourceLine = functionLine.split("\"")[2].split(" ")[2]

	declId = utility.getEntityId(declSourceFile, declSourceLine)

	if declId != "":
		functionDefInfo=cdifId + "\t" + declId + "\n"
		def_file.write(functionDefInfo)

	if ( sourceFile != "" ):
		if ( not (sourceFile in files) ):
			assert False, ("Unknown file "+sourceFile+" function "+cdifId)

		fileId=files[sourceFile]
		invokableEntityContainmentInfo=cdifId + "\t" + fileId + "\t" + lineNr + "\n"
		invokableEntityContainmentFile.write(invokableEntityContainmentInfo)

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(64)

cdif_file=sys.argv[1]

output_file="functionsWithIDs.txt"

files={}

# build files dictionary
files_file=open("filesWithIDs.txt", 'r')

for line in files_file:
	lineSplittedInTabs=line.split("\t")
	fileId = lineSplittedInTabs[0]
	fileName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")

	if ( not(fileName in files) ):
		files[fileName] = fileId

files_file.close()

input_file=open(cdif_file, 'r')
output_file=open(output_file, 'w')
invokableEntityContainmentFile=open("invokableEntityBelongsToFile.txt", 'a')
functionTypeFile=open("functionHasClassAsReturnType.txt", 'w')

for line in input_file:
	line = line.strip()

	if line.startswith("(Function FM"):
		parseFunctionDeclaration(line)

input_file.close()
invokableEntityContainmentFile.close()

utility.reInitializeInvokableEntityContainmentDictionary()

input_file=open(cdif_file, 'r')
invokableEntityContainmentFile=open("invokableEntityBelongsToFile.txt", 'a')

def_file_name="defsWithAssociation.txt"
def_file=open(def_file_name, 'a')

for line in input_file:
	line = line.strip()

	if line.startswith("(FunctionDefinition FM"):
		parseFunctionDefinition(line)

invokableEntityContainmentFile.close()
utility.reInitializeInvokableEntityContainmentDictionary()

input_file.close()
output_file.close()
def_file.close()
