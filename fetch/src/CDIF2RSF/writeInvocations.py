# !/usr/bin/python

import sys
import utility

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(64)

input_file=sys.argv[1]

# build files dictionary
methods_file=open("methodsWithIDs.txt", 'r')

methods = {}

for line in methods_file:
	lineSplittedInTabs=line.split("\t")
	methodId = lineSplittedInTabs[0]
	methodName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")
	
	if ( not(methodName in methods) ):
		methods[methodName] = methodId
		
methods_file.close()

functions_file=open("functionsWithIDs.txt", 'r')

functions = {}

for line in functions_file:
	lineSplittedInTabs=line.split("\t")
	functionId = lineSplittedInTabs[0]
	functionName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")
	
	if ( not(functionName in functions) ):
		functions[functionName] = functionId
		
functions_file.close()

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



output_file="invocationsWithIDs.txt"

input_file=open(input_file, 'r')
output_file=open(output_file, 'w')

location_file="invocationLocations.txt"
location_file=open(location_file, 'w')


for line in input_file:
	line = line.strip()
		
	if line.startswith("(Invocation FM"):
		invId=line.split("M")[1]
		invokerUniqueName=""
		invokeeUniqueName=""
		sourceFile=""	# file containing the invocation
		sourceLineNr=""	
		srcSourceFile = ""	# file containing the invoking method/function's declaration
		srcLineNr=""
		destSourceFile=""	# file containing the invoking method/function's declaration
		destLineNr=""
		sourceClassId = ""
		destinationClassId = ""
		
		for invLine in input_file:
			invLine = invLine.strip()
					
			if invLine.startswith(")"):
				break
			elif invLine.startswith("(invokedBy \""):
				invokerUniqueName=invLine.split("\"")[1]
										
			elif invLine.startswith("(invokes \""):
				invokeeUniqueName=invLine.split("\"")[1]
			elif invLine.startswith("(base \""):
				invokeeBase=invLine.split("\"")[1]
				if ( invokeeBase != "" ):
					invokeeUniqueName=invokeeBase+"."+invokeeUniqueName	
			elif invLine.startswith("(sourceAnchor "):
				sourceFile = invLine.split("\"")[1]
				sourceLineNr = invLine.split("\"")[2].split(" ")[2]
			elif invLine.startswith("(sourceSourceAnchor "):
				# line resembles  (sourceSourceAnchor #[file "" start  end |]#)
				srcSourceFile=invLine.split("\"")[1]
				srcLineNr=invLine.split("\"")[2].split(" ")[2]
			elif invLine.startswith("(destinationSourceAnchor "):
				# line resembles  (destinationSourceAnchor #[file "" start  end |]#)
				destSourceFile=invLine.split("\"")[1]
				destLineNr=invLine.split("\"")[2].split(" ")[2]
				
		
		invokerId = utility.getEntityId(srcSourceFile, srcLineNr, needDef = True)
		#invokerId = ""
		#if ( invokerUniqueName in methods ):
		#	invokerId = methods[invokerUniqueName]
		#elif ( invokerUniqueName in functions ):
		#	invokerId = functions[invokerUniqueName]

		#invokeeId = utility.getEntityId(destSourceFile, destLineNr, needDef = True)
		invokeeId = utility.getEntityId(destSourceFile, destLineNr, needDef = False)

		#invokeeId = ""
		#if ( invokeeUniqueName in methods ):
		#	invokeeId = methods[invokeeUniqueName]
		#elif ( invokeeUniqueName in functions ):
		#	invokeeId = functions[invokeeUniqueName]
		
		if not ( (invokerId != "") and (invokeeId != "") ):
			continue
		
		invoInfo=invId + "\t" + invokerId + "\t" + invokeeId + "\n"
		output_file.write(invoInfo)
		
		if sourceLineNr != "" and sourceFile != "":
			fileId = files[sourceFile]
			sourceLineNr = str(int(sourceLineNr)) # remove leading zeroes
			locationInfo = invId + "\t" + fileId + "\t" + sourceLineNr + "\n"
			location_file.write(locationInfo)


input_file.close()
location_file.close()
