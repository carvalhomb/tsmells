# !/usr/bin/python

import sys
import utility

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"cdif-input-file"
  sys.exit(64)

fileName=sys.argv[1]

annotations = {}
classDict = utility.initializeClassDictionary()
invEntDict = utility.initializeInvokableEntityDictionary()
#methDict = 

##
# a two-phase approach:
##

# First, read all annotations and cache them
cdifFile=open(fileName, "r")
annotationFile=open("annotations.txt", 'w')
annBelongsToEntityFile=open("annotationBelongsToEntity.txt", 'w')
for line in cdifFile:
	line = line.strip()
	ownerId = ""
	if line.startswith("(Annotation FM"):
		annId=line.split("M")[1]
		
		cdifEntity=""
		for annLine in cdifFile:
			annLine = annLine.strip()

			if annLine.startswith(")"):
				break
			elif annLine.startswith("(name \""):
				ann=annLine.split("\"")[1]
			elif annLine.startswith("(belongsTo \""):
				owner=annLine.split("\"")[1]

		if annId != "" and (owner in classDict or owner in invEntDict):
			annotations[ann] = annId
			annotationInfo=annId + "\t\"" + ann + "\"\n"
			annotationFile.write(annotationInfo)
			if owner in classDict:
				ownerId = classDict[owner][0] # assume only one id for class name
			elif owner in invEntDict:
				ownerId = invEntDict[owner][0] 
			annotationOwnerInfo=annId + "\t" + ownerId + "\n"
			annBelongsToEntityFile.write(annotationOwnerInfo)
			
cdifFile.close()
annotationFile.close()
annBelongsToEntityFile.close()
