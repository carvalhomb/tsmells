# !/usr/bin/python

import sys
import utility

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(64)

input_file=sys.argv[1]

# build files dictionary
files_file=open("filesWithIDs.txt", 'r')
files = {}

for line in files_file:
	lineSplittedInTabs=line.split("\t")
	fileId = lineSplittedInTabs[0]
	fileName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")

	if ( not(fileName in files) ):
		files[fileName] = fileId

files_file.close()

methods_file=open("methodsWithIDs.txt", 'r')

methods = {} # name x id

for line in methods_file:
	lineSplittedInTabs=line.split("\t")
	methodId = lineSplittedInTabs[0]
	methodName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")

	if ( not(methodName in methods) ):
		methods[methodName] = methodId

methods_file.close()

functions_file=open("functionsWithIDs.txt", 'r')

functions = {} # name x id

for line in functions_file:
	lineSplittedInTabs=line.split("\t")
	functionId = lineSplittedInTabs[0]
	functionName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")

	if ( not(functionName in functions) ):
		functions[functionName] = functionId

functions_file.close()


attributes_file=open("attributesWithIDs.txt", 'r')

attributes = {} # name x id

for line in attributes_file:
	lineSplittedInTabs=line.split("\t")
	attrId = lineSplittedInTabs[0]
	attrName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")

	if ( not(attrName in attributes) ):
		attributes[attrName] = attrId

attributes_file.close()

globalVars_file = open("globalVarsWithIDs.txt", 'r')

globalVars = {} # name x id

for line in globalVars_file:
	lineSplittedInTabs=line.split("\t")
	globalVarId = lineSplittedInTabs[0]
	globalVarName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")

	if ( not(globalVarName in globalVars) ):
		globalVars[globalVarName] = globalVarId

globalVars_file.close()

output_file="accessesWithIDs.txt"

access_type_file="leftValueAccesses.txt"

input_file=open(input_file, 'r')
output_file=open(output_file, 'w')
access_type_file=open(access_type_file,'w')
location_file="accessesLocations.txt"
location_file=open(location_file, 'w')

for line in input_file:
	line = line.strip()

	if line.startswith("(Access FM"):
		accessId=line.split("M")[1]

		accessorUniqueName = ""
		accesseeUniqueName = ""
		sourceFile=""	# file containing the reference
		sourceLineNr=""
		srcSourceFile = ""	# file containing the invoking method/function's declaration
		srcLineNr=""
		destSourceFile=""	# file containing the invoking method/function's declaration
		destLineNr=""
		isAccessLValue=False

		for accessLine in input_file:
			accessLine = accessLine.strip()

			if accessLine.startswith(")"):
				break
			elif accessLine.startswith("(accessedIn \""):
				accessorUniqueName=accessLine.split("\"")[1]
			elif accessLine.startswith("(accesses \""):
				accesseeUniqueName=accessLine.split("\"")[1]
			elif accessLine.startswith("(sourceSourceAnchor "):
				# line resembles  (sourceSourceAnchor #[file "" start  end |]#)
				srcSourceFile=accessLine.split("\"")[1]
				srcLineNr=accessLine.split("\"")[2].split(" ")[2]
			elif accessLine.startswith("(destinationSourceAnchor "):
				# line resembles  (destinationSourceAnchor #[file "" start  end |]#)
				destSourceFile=accessLine.split("\"")[1]
				destLineNr=accessLine.split("\"")[2].split(" ")[2]
			elif accessLine.startswith("(sourceAnchor "):
				# line resembles (sourceAnchor #[file "Acquisition/Api/Src/AcqApi/AcqApiCommandHandler.cpp" start 000673 end 000673|]#)
				sourceFile = accessLine.split("\"")[1]
				sourceLineNr = accessLine.split("\"")[2].split(" ")[2]
			elif accessLine.startswith("(isAccessLValue "):
				# line resembles (isAccessLValue -TRUE-)
				value = accessLine.split("-")[1]
				isAccessLValue = (value == "TRUE")

		accessorId = ""
		accessorId = utility.getEntityId(srcSourceFile, srcLineNr, needDef = True)

		accesseeId = ""
		if ( accesseeUniqueName in attributes ):
			accesseeId = attributes[accesseeUniqueName]
		elif ( accesseeUniqueName in globalVars ):
			accesseeId = globalVars[accesseeUniqueName]

		if not ( (accessorId != "") and (accesseeId != "") ):
			continue

		accessInfo=accessId + "\t" + accessorId + "\t" + accesseeId + "\n"
		output_file.write(accessInfo)

		if ( isAccessLValue ):
			access_type_file.write(accessId + "\n")

		if sourceLineNr != "" and sourceFile != "":
			fileId = files[sourceFile]
			sourceLineNr = str(int(sourceLineNr)) # strip leading zero's
			locationInfo = accessId + "\t" + fileId + "\t" + sourceLineNr + "\n"
			location_file.write(locationInfo)


input_file.close()
output_file.close()
access_type_file.close()
location_file.close()

