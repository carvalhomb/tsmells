# !/usr/bin/python

#### GLOBAL VARIABLES ####
fileDict = {} # name x id
classDict = {} # name x [id]
invEntDict = {} # name x [id]
classContainmentDict = {} # fileId x {lineNr x classId}
invokableEntityContainmentDict = {} # fileId x {lineNr x entityId}
accessibleEntityContainmentDict = {} # fileId x {lineNr x entityId}

#### GLOBAL VARIABLE INITIALIZERS ####

##
# Initializes the global file dictionary variable
# and returns it.
##
def initializeFileDictionary():
	global fileDict

	if not fileDict:
		files_file=open("filesWithIDs.txt", 'r')

		for line in files_file:
			lineSplittedInTabs=line.split("\t")
			fileId = lineSplittedInTabs[0]
			fileName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")

			if ( not(fileName in fileDict) ):
				fileDict[fileName] = fileId


		files_file.close()

	return fileDict

##
# Initializes the global class dictionary variable
# and returns it.
##
def initializeClassDictionary():
	global classDict

	if not classDict:
		classes_file=open("classesWithIDs.txt", 'r')

		for line in classes_file:
			lineSplittedInTabs=line.split("\t")
			classId = lineSplittedInTabs[0]
			className = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")

			if not(className in classDict) :
				classDict[className] = []

			if not(classId in classDict[className]):
				classDict[className].append(classId)

		classes_file.close()

	return classDict


##
# Initializes the global class containment dictionary variable
# and returns it.
##
def initializeClassContainmentDictionary():
	global classContainmentDict # fileId x {lineNr x classId}

	if not classContainmentDict:
		classContainmentFile=open("classBelongsToFile.txt", 'r')

		for line in classContainmentFile:
			lineSplittedInTabs=line.split("\t")
			classId = lineSplittedInTabs[0]
			fileId = lineSplittedInTabs[1].strip()
			lineNr = lineSplittedInTabs[2].strip()

			if not(fileId in classContainmentDict):
				classContainmentDict[fileId] = {}

			classContainmentDict[fileId][lineNr] = classId

		classContainmentFile.close()

	return classContainmentDict

##
# Initializes the global invokable entity dictionary variable
# and returns it.
##
def initializeInvokableEntityDictionary():
	global invEntDict

	if not invEntDict:
		invEntFile=open("methodsWithIDs.txt", 'r')

		for line in invEntFile:
			lineSplittedInTabs=line.split("\t")
			invEntId = lineSplittedInTabs[0]
			invEntName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")

			if not(invEntName in invEntDict) :
				invEntDict[invEntName] = []

			if not(invEntId in invEntDict[invEntName]):
				invEntDict[invEntName].append(invEntId)

		invEntFile.close()

		invEntFile=open("functionsWithIDs.txt", 'r')

		for line in invEntFile:
			lineSplittedInTabs=line.split("\t")
			invEntId = lineSplittedInTabs[0]
			invEntName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")

			if not(invEntName in invEntDict) :
				invEntDict[invEntName] = []

			if not(invEntId in invEntDict[invEntName]):
				invEntDict[invEntName].append(invEntId)

		invEntFile.close()

	return invEntDict

##
# Initializes the global invokable entity containment dictionary variable
# and returns it.
##
def initializeInvokableEntityContainmentDictionary():
	global invokableEntityContainmentDict # fileId x {lineNr x entityId}

	if not invokableEntityContainmentDict:
		__initializeInvokableEntityContainmentDictionary()

	return invokableEntityContainmentDict

def reInitializeInvokableEntityContainmentDictionary():
	global invokableEntityContainmentDict # fileId x {lineNr x entityId}

	invokableEntityContainmentDict = __initializeInvokableEntityContainmentDictionary()

	return invokableEntityContainmentDict

def __initializeInvokableEntityContainmentDictionary():
	global invokableEntityContainmentDict # fileId x {lineNr x entityId}

	defsWithAssociations = ([], []) # ( [defs], [decl])
	defsWithAssociationsFile = open("defsWithAssociation.txt", 'r')

	for line in defsWithAssociationsFile:
		lineSplittedInTabs = line.split("\t")
		if len(lineSplittedInTabs) != 2:
			if line != "\n": 
				print "defsWithAssociations contains illegal line: " + line[:-1]
			continue
		defId = lineSplittedInTabs[0].strip()
		declId = lineSplittedInTabs[1].strip()
		defsWithAssociations[0].append(defId)
		defsWithAssociations[1].append(declId)

	defsWithAssociationsFile.close()

	invokableEntityContainmentDict = {}
	invokableEntityContainmentFile=open("invokableEntityBelongsToFile.txt", 'r')

	for line in invokableEntityContainmentFile:
		lineSplittedInTabs=line.split("\t")
		entityId = lineSplittedInTabs[0]
		fileId = lineSplittedInTabs[1].strip()
		lineNr = lineSplittedInTabs[2].strip()

		if not(fileId in invokableEntityContainmentDict):
			invokableEntityContainmentDict[fileId] = {}

		if not(lineNr in invokableEntityContainmentDict[fileId]):
			invokableEntityContainmentDict[fileId][lineNr] = {}

		isDefinition = ""
		if entityId in defsWithAssociations[0]:
			isDefinition = True
		elif entityId in defsWithAssociations[1]:
			isDefinition = False
		else:
			isDefinition = False # initial, when defsWithAssoc isnt constructed yet

		invokableEntityContainmentDict[fileId][lineNr][isDefinition] = entityId


	invokableEntityContainmentFile.close()

	return invokableEntityContainmentDict

##
# Initializes the accessible entity containment dictionary variable
# and returns it.
##
def initializeAccessibleEntityContainmentDictionary():
	global accessibleEntityContainmentDict # fileId x {lineNr x entityId}

	if not accessibleEntityContainmentDict:
		__initializeAccessibleEntityContainmentDictionary()

	return accessibleEntityContainmentDict


def reInitializeAccessibleEntityContainmentDictionary():
	global accessibleEntityContainmentDict # fileId x {lineNr x entityId}

	accessibleEntityContainmentDict = __initializeAccessibleEntityContainmentDictionary()

	return accessibleEntityContainmentDict

def __initializeAccessibleEntityContainmentDictionary():
	global accessibleEntityContainmentDict # fileId x {lineNr x entityId}

	accessibleEntityContainmentDict = {}
	accessibleEntityContainmentFile=open("accessibleEntityBelongsToFile.txt", 'r')

	for line in accessibleEntityContainmentFile:
		lineSplittedInTabs=line.split("\t")
		entityId = lineSplittedInTabs[0]
		fileId = lineSplittedInTabs[1].strip()
		lineNr = lineSplittedInTabs[2].strip()

		if not(fileId in accessibleEntityContainmentDict):
			accessibleEntityContainmentDict[fileId] = {}

		accessibleEntityContainmentDict[fileId][lineNr] = entityId

	accessibleEntityContainmentFile.close()

	return accessibleEntityContainmentDict

#
# @classContainmentDict: fileId x {lineNr x classId}
#
# @returns - the id of the class named className in file fileName.
##
def getClassId(fileName, lineNr):
	fileDict = initializeFileDictionary()
	classContainmentDict = initializeClassContainmentDictionary()

	fileId = ""
	classId = ""

	if ( fileName in fileDict ):
		fileId = fileDict[fileName]

	if ( fileId != "" ) and ( fileId in classContainmentDict ):
		if lineNr in classContainmentDict[fileId]:
			classId = classContainmentDict[fileId][lineNr]
		# try +2/+1/-1/-2
		elif `(int(lineNr) - 1)` in classContainmentDict[fileId]:
			classId = classContainmentDict[fileId][`(int(lineNr) - 1)`]
		elif `(int(lineNr) + 1)` in classContainmentDict[fileId]:
			classId = classContainmentDict[fileId][`(int(lineNr) + 1)`]
		elif `(int(lineNr) - 2)` in classContainmentDict[fileId]:
			classId = classContainmentDict[fileId][`(int(lineNr) - 2)`]
		elif `(int(lineNr) + 2)` in classContainmentDict[fileId]:
			classId = classContainmentDict[fileId][`(int(lineNr) + 2)`]


	return classId

##
# @invokableEntityContainmentDict - fileId x {lineNr x entityId}
#
# @returns - the id of the class named className in file fileName.
##
def getEntityId(fileName, lineNr, needDef = False):
	# default to getting a declaration id
	fileDict = initializeFileDictionary()
	invokableEntityContainmentDict = initializeInvokableEntityContainmentDictionary()

	fileId = ""
	entityId = ""

	if ( fileName in fileDict ):
		fileId = fileDict[fileName]

	errMsg = "Failed to find entity in " + fileName + " (id " + fileId + ") @" + lineNr
	if  fileId == "" or not( fileId in invokableEntityContainmentDict ):
		print errMsg, "[unkown file]"
	elif not (lineNr in invokableEntityContainmentDict[fileId]):
		print errMsg + " [no entity on linenumber]"
	elif not (needDef in invokableEntityContainmentDict[fileId][lineNr]):
		# Doesn't seem to be necessary, makes duplicate DefinitionForDeclaration entries
		entityId = checkAddDefinition(needDef, fileId, lineNr, invokableEntityContainmentDict, errMsg)
		#pass
	else:
		entityId = invokableEntityContainmentDict[fileId][lineNr][needDef]

	return entityId

def checkAddDefinition(needDef, fileId, lineNr, invokableEntityContainmentDict, errMsg):
	entityId = ""
	if needDef and ( (not needDef) in invokableEntityContainmentDict[fileId][lineNr]):
		# This is a request for a definition where only a declaration exists.
		# It means SN deficiency of not logging the definition.
		# eg the 3 main functions in TeamSpirit
		# => add the definition to the model here ...
		addDefinition(fileId, lineNr, invokableEntityContainmentDict)
		entityId = invokableEntityContainmentDict[fileId][lineNr][False]
	else:
		if needDef: t = "def"
		else: t = "decl"
		print errMsg + " [type " + t + " not found]"
	return entityId

def addDefinition(fileId, lineNr, invokableEntityContainmentDict):
	""" Fix broken SN by adding a defintion to the model where only
		a declaration existed """
	declId = invokableEntityContainmentDict[fileId][lineNr][False]
	print "Warning: adding a definition for " + declId
	invokableEntityContainmentDict[fileId][lineNr][True] = declId
	defsWithAssociationsFile = open("defsWithAssociation.txt", 'a')
	defDeclInfo = declId + "\t" + declId + "\n"# identical IDs, which is bad.
	defsWithAssociationsFile.write(defDeclInfo)
	defsWithAssociationsFile.close()

