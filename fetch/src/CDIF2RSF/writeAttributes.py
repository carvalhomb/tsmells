# !/usr/bin/python

import sys
import utility


if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(64)

input_file=sys.argv[1]
input_file=open(input_file, 'r')

output_file="attributesWithIDs.txt"
output_file=open(output_file, 'w')

attributeContainmentFile=open("attributeBelongsToClass.txt", 'w')
attributeTypeFile=open("attributeHasClassAsType.txt", 'w')
attributeVisibilityFile = open("attributeVisibility.txt", 'w')
attributeSignatureFile = open("attributeSignature.txt", 'w')

for line in input_file:
	line = line.strip()
		
	if line.startswith("(Attribute FM"):
		cdifId=line.split("M")[1]	
					
		name = ""
		uniqueName=""
		parentClassName = ""
		declaredType = ""
		accessControlQualifier = ""
		declaredClass = ""
		sourceFile = ""
		lineNr = ""
		parentSourceFile = ""
		parentLineNr = ""
		typeSourceFile = ""
		typeLineNr = ""
			
		for attributeLine in input_file:
			attributeLine = attributeLine.strip()
				
			if attributeLine.startswith(")"):
				break
			elif attributeLine.startswith("(name \""):
				name = attributeLine.split("\"")[1]
				uniqueName = name
			elif attributeLine.startswith("(belongsTo \""):
				parentClassName = attributeLine.split("\"")[1]
			elif attributeLine.startswith("(declaredType \""):
				declaredType = attributeLine.split("\"")[1]
			elif attributeLine.startswith("(declaredClass \""):
				declaredClass = attributeLine.split("\"")[1]
			elif attributeLine.startswith("(accessControlQualifier \""):
				accessControlQualifier = attributeLine.split("\"")[1]
			elif attributeLine.startswith("(sourceAnchor "):
				sourceFile = attributeLine.split("\"")[1]
				lineNr = attributeLine.split("\"")[2].split(" ")[2]	
			elif attributeLine.startswith("(typeSourceAnchor "):
				typeSourceFile = attributeLine.split("\"")[1]
				typeLineNr = attributeLine.split("\"")[2].split(" ")[2]	
			elif attributeLine.startswith("(parentSourceAnchor "):				
				parentSourceFile = attributeLine.split("\"")[1]
				parentLineNr = attributeLine.split("\"")[2].split(" ")[2]	

		if ( parentClassName != "" ):
			uniqueName=parentClassName+"."+name
			attrInfo=cdifId + "\t\"" + uniqueName + "\"\n"
			output_file.write(attrInfo)

			attributeOwnerId = utility.getClassId(parentSourceFile,parentLineNr)
			
			if attributeOwnerId != "":
				attrContainmentInfo=cdifId + "\t" + attributeOwnerId + "\n"
				attributeContainmentFile.write(attrContainmentInfo)
		
		if ( declaredClass != "" and typeSourceFile != ""):
			declaredClassId = utility.getClassId(typeSourceFile,typeLineNr)
			
			if declaredClassId != "":
				attrTypeInfo=cdifId + "\t" + declaredClassId + "\n"
				attributeTypeFile.write(attrTypeInfo)
	
input_file.close()
output_file.close()
attributeContainmentFile.close()
attributeTypeFile.close()
attributeVisibilityFile.close()
attributeSignatureFile.close()