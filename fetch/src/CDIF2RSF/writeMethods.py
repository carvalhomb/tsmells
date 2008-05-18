# !/usr/bin/python

import sys
import utility

def parseMethodDeclaration(line):
	cdifId=line.split("M")[2].strip()

	name = ""
	uniqueName = ""
	parentClassName = ""
	parameters = ""
	declaredReturnType = ""
	declaredReturnClass = ""
	sourceFile = ""
	lineNr = ""
	parentSourceFile = ""
	parentLineNr = ""
	typeSourceFile = ""
	typeLineNr = ""
	signature = ""
	visibility = ""

	for methodLine in cdif_file:
		methodLine = methodLine.strip()

		if methodLine.startswith(")"):
			break
		elif methodLine.startswith("(name \""):
			name=methodLine.split("\"")[1]
		elif methodLine.startswith("(belongsTo \""):
			parentClassName=methodLine.split("\"")[1]
		elif methodLine.startswith("(uniqueName \""):
			uniqueName=methodLine.split("\"")[1]
			parameters=""
			uniqueNameBrokenUp=uniqueName.split("(")
			nrOfBrackets=len(uniqueNameBrokenUp)

			bracketIndex=1
			while ( bracketIndex < nrOfBrackets ):
				parameters += uniqueNameBrokenUp[bracketIndex]
				bracketIndex+=1

			parameters = parameters.rstrip(")")

		elif methodLine.startswith("(declaredReturnType \""):
			declaredReturnType=methodLine.split("\"")[1]
		elif methodLine.startswith("(declaredReturnClass \""):
			declaredReturnClass=methodLine.split("\"")[1]
		elif methodLine.startswith("(sourceAnchor "):
			sourceFile = methodLine.split("\"")[1]
			lineNr = methodLine.split("\"")[2].split(" ")[2]
		elif methodLine.startswith("(parentSourceAnchor "):
			parentSourceFile = methodLine.split("\"")[1]
			parentLineNr = methodLine.split("\"")[2].split(" ")[2]
		elif methodLine.startswith("(typeSourceAnchor "):
			typeSourceFile = methodLine.split("\"")[1]
			typeLineNr = methodLine.split("\"")[2].split(" ")[2]
		elif methodLine.startswith("(accessControlQualifier "):
			visibility = methodLine.split("\"")[1]
		elif methodLine.startswith("(signature "):
			signature = methodLine.split("\"")[1]

	if ( parentClassName != "" ):
		methodInfo=cdifId + "\t\"" + uniqueName + "\"\n"
		decl_file.write(methodInfo)

		methodOwnerId = utility.getClassId(parentSourceFile,parentLineNr)

		if ( methodOwnerId != "" ):
			methContainmentInfo=cdifId + "\t" + methodOwnerId + "\n"
			methodContainmentFile.write(methContainmentInfo)

		if ( sourceFile != "" ):
			if ( not (sourceFile in files) ):
				assert False, ("Unknown file "+sourceFile+" function "+cdifId)

			fileId=files[sourceFile]
			invokableEntityContainmentInfo=cdifId + "\t" + fileId + "\t" + lineNr + "\n"
			invokableEntityContainmentFile.write(invokableEntityContainmentInfo)

	if ( declaredReturnClass != "" and typeSourceFile != ""):
		declaredReturnClassId = utility.getClassId(typeSourceFile,typeLineNr)

		if declaredReturnClassId != "":
			methodTypeInfo=cdifId + "\t" + declaredReturnClassId + "\n"
			methodTypeFile.write(methodTypeInfo)

	if visibility != "":
		visibilityInfo = cdifId + "\t" + "\"" + visibility + "\"" + "\n"
		methodVisibilityFile.write(visibilityInfo)

	if signature != "":
		signatureInfo = cdifId + "\t" + "\"" + signature + "\"" + "\n"
		methodSignatureFile.write(signatureInfo)

##
##
# (MethodDefinition FM2284
#        (name "~Underflow")
#        (declaredBy "spExceptions::Underflow.~Underflow()")
#        (uniqueName "spExceptions::Underflow.~Underflow()_1")
#        (sourceAnchor #[file "spreadsheet/spExceptions.cxx" start 13 end 13|]#)
#        (condCompiled "!INC_EXCEPTIONS_H")
#        (declSourceAnchor #[file "spreadsheet/spExceptions.h" start 50 end 50|]#)
#)
##
def parseMethodDefinition(line):
	cdifId=line.split("M")[2].strip()

	name = ""
	declaredBy = ""
	uniqueName = ""
	sourceFile = ""
	lineNr = ""
	declSourceFile = ""
	declSourceLine = ""

	for methodLine in cdif_file:
		methodLine = methodLine.strip()

		if methodLine.startswith(")"):
			break
		elif methodLine.startswith("(name \""):
			name=methodLine.split("\"")[1]
		elif methodLine.startswith("(declaredBy \""):
			declaredBy=methodLine.split("\"")[1]
		elif methodLine.startswith("(uniqueName \""):
			uniqueName=methodLine.split("\"")[1]
		elif methodLine.startswith("(sourceAnchor "):
			sourceFile = methodLine.split("\"")[1]
			lineNr = methodLine.split("\"")[2].split(" ")[2]
		elif methodLine.startswith("(declSourceAnchor "):
			declSourceFile = methodLine.split("\"")[1]
			declSourceLine = methodLine.split("\"")[2].split(" ")[2]

	declId = utility.getEntityId(declSourceFile, declSourceLine)

	if declId != "":
		methodDefInfo=cdifId + "\t" + declId + "\n"
		def_file.write(methodDefInfo)

	if ( sourceFile != "" ):
		if ( not (sourceFile in files) ):
			assert False, ("Unknown file "+sourceFile+" method "+cdifId)

		fileId=files[sourceFile]
		invokableEntityContainmentInfo=cdifId + "\t" + fileId + "\t" + lineNr + "\n"
		invokableEntityContainmentFile.write(invokableEntityContainmentInfo)

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(64)

input_file=sys.argv[1]

decl_file="methodsWithIDs.txt"
def_file="defsWithAssociation.txt"

cdif_file=open(input_file, 'r')
decl_file=open(decl_file, 'w')
def_file=open(def_file, 'w')

methodContainmentFile=open("methodBelongsToClass.txt", 'w')
invokableEntityContainmentFile=open("invokableEntityBelongsToFile.txt", 'a')
methodTypeFile=open("methodHasClassAsReturnType.txt", 'w')
methodVisibilityFile = open("methodVisibility.txt", 'w')
methodSignatureFile = open("methodSignature.txt", 'w')

files = utility.initializeFileDictionary()

for line in cdif_file:
	line = line.strip()

	if line.startswith("(Method FM"):
		parseMethodDeclaration(line)
cdif_file.close()
invokableEntityContainmentFile.close()

utility.reInitializeInvokableEntityContainmentDictionary()

cdif_file=open(input_file, 'r')
invokableEntityContainmentFile=open("invokableEntityBelongsToFile.txt", 'a')

for line in cdif_file:
	if line.startswith("(MethodDefinition FM"):
		parseMethodDefinition(line)

invokableEntityContainmentFile.close()
utility.reInitializeInvokableEntityContainmentDictionary()

cdif_file.close()
decl_file.close()
def_file.close()
methodContainmentFile.close()
methodVisibilityFile.close()
methodSignatureFile.close()
