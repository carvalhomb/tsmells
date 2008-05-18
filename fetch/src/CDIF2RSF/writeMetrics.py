# !/usr/bin/python

import sys

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(64)

# add additional metrics to this list
# - pmccabe gives us LOC, CC and NOS
# - javancss gives us LOC, CC and COM
CONSIDERED_METRICS= [ "LOC", "CC", "NOS", "COM", "COND", "LOOP" ]

input_file=sys.argv[1]

output_file="metricsWithIDs.txt"

invEntities = {}

# build files dictionary
method_file=open("methodsWithIDs.txt", 'r')
function_file=open("functionsWithIDs.txt", 'r')

for line in method_file:
	lineSplittedInTabs=line.split("\t")
	methodId = lineSplittedInTabs[0]
	methodName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")
	
	if ( not(methodName in invEntities) ):
		invEntities[methodName] = methodId
		
method_file.close()

for line in function_file:
	lineSplittedInTabs=line.split("\t")
	functionId = lineSplittedInTabs[0]
	functionName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")
	
	if ( not(functionName in invEntities) ):
		invEntities[functionName] = functionId	
function_file.close()

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
		
		if ( owner != "" ):
			# considered metrics should have valid owners
			if ( (name in CONSIDERED_METRICS) and not (owner in invEntities) ):
				# should be robust...
				#assert False, ("Unknown owner "+owner+" method/function "+cdifId)
				print "Unknown owner "+owner+" method/function "+cdifId
				continue
		else:
			assert False;
		
		if name in CONSIDERED_METRICS:
			invId=invEntities[owner]				
			measInfo=cdifId + "\t" + invId + "\t\"" + name + "\"\t" + value + "\n"
			output_file.write(measInfo)

input_file.close()
output_file.close()
