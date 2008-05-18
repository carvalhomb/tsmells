# !/usr/bin/python

import sys

directorySeparationCharacter="/"
maxDirectoryDepth=99

##
# Extract a module name from a given file name.
#
# Calculates the name of the directory to which the given file name belongs.
# restricted to a directory nesting depth of maxDirectoryDepth.
#
# E.g.: 
#	extractModuleNameFromFileName("a/b/c/d.h", i) = "a/b/c" for all i in [3,infinity]
# 	extractModuleNameFromFileName("a/b/c/d.h", 2) = "a/b"
# 	extractModuleNameFromFileName("a/b/c/d.h", 1) = "a"
#
#	extractModuleNameFromFileName("a.h", i) = "/" for all i

#
# Assumptions: 
#	+Directory separation is provided by the "/" character.
#	 
#	+The root level of the directory structure is "/"
#	+The final part of a given file name (as separated in directories)
#	 is considered to be a file when it contains a "."
#
#	 E.g.:
#		/a/b.[?]* is considered to be a file b.[?]* within a directory a within /
#
# @maxDirectoryDepth - allows to configure the extraction of module names
#	Has no effect when smaller then 1.
#
# @returns The name of the module to which the file belongs.
##
def extractModuleName(fileName):
	global directorySeparationCharacter
	global maxDirectoryDepth
	
	substringIndicatingNameAsFile="."
		
	# by default every file is contained
	# in the root module
	moduleName=directorySeparationCharacter
	
	# if the file belongs to a directory
	if ( "/" in fileName):
		fileNameParts = fileName.split(directorySeparationCharacter)
		
		# by default a file in a directory
		# belongs to the root directory module
		moduleName=fileNameParts[0]		

		# if the file is located at least
		# one directory deep
		if (len(fileNameParts) > 1 ):
			lastDirectoryIndex=min(len(fileNameParts)-1,maxDirectoryDepth-1)
			
			if lastDirectoryIndex > 0:
				index=1
				while ( index < lastDirectoryIndex ):
					moduleName += directorySeparationCharacter + fileNameParts[index]
					index += 1
				
				lastFileNamePart = fileNameParts[lastDirectoryIndex]
				moduleNameIsFileName = (substringIndicatingNameAsFile in lastFileNamePart)
				#if not(moduleNameIsFileName):
				#	moduleName += directorySeparationCharacter + lastFileNamePart
		
	return moduleName

##
# Adds the parents of the given module name to the modules list, in case these
# are not contained yet. E.g.,:
# addParentModules("/a/b/c",[]) will result in the addition of (in order):
#	+"/"
#	+"/a"
#	+"/a/b"
#
# @moduleName - string representing a module name. Should not be a file name.
#  I.e., every part of the module name should be a valid module.
#  moduleName should start with a directorySeparationCharacter
##
def addParentModules(moduleName, modules, moduleBelongsToModule):
	if moduleName == "":
		return
		
	# first add the root module
	if not(directorySeparationCharacter in modules):
		parentModuleName = directorySeparationCharacter
		modules.append(parentModuleName)
	print moduleName
	childModules = moduleName.rstrip(directorySeparationCharacter).split(directorySeparationCharacter)
	# it always holds that childModules[0] = 
	print childModules
	# e.g. /X
	if len(childModules) == 2:
		# ensure that the module will have the root as
		# its parent
		childModuleId = modules.index(moduleName)+1
		# retrieve the module id of the root
		parentModuleId = modules.index(directorySeparationCharacter)+1
		if not(childModuleId in moduleBelongsToModule):
			moduleBelongsToModule[childModuleId] = parentModuleId

	# e.g. /X/Y/Z
	elif len(childModules) > 2:
		childModules = childModules[1:len(childModules)] # ['X','Y']
	
		childModulePath=directorySeparationCharacter

		for childModule in childModules:
			parentModulePath = childModulePath # initially /
		
			if ( childModulePath != directorySeparationCharacter ):
				childModulePath += directorySeparationCharacter


			childModulePath += childModule # e.g., /X, /X/Y

			if not(childModulePath in modules):
				modules.append(childModulePath)

			childModuleId = modules.index(childModulePath)+1
			if not(childModuleId in moduleBelongsToModule):
				parentModuleId = modules.index(parentModulePath)+1
				moduleBelongsToModule[childModuleId] = parentModuleId

def addFile(line, files, modules, fileBelongsToModule, moduleBelongsToModule):
	sourceFile = line.split("\"")[1]

	if ( not(sourceFile in files) ):
		files.append(sourceFile)
		fileId = files.index(sourceFile)+1

		moduleName = extractModuleName(sourceFile)
		
		if not(moduleName.startswith(directorySeparationCharacter)):
			moduleName = directorySeparationCharacter + moduleName

		if not(moduleName in modules):
			modules.append(moduleName)

		moduleId = modules.index(moduleName)+1

		fileBelongsToModule[fileId] = moduleId
		addParentModules(moduleName, modules, moduleBelongsToModule)

## main
if __name__ == "__main__":
	if len(sys.argv) < 2:
	  print "Usage:",sys.argv[0],"cdif-input-file"
	  sys.exit(64)
	
	input_file=sys.argv[1]
	input_file=open(input_file, 'r')
	
	# first build up the relations
	files = [] 			# sourceFileName (fileId = index(sourceFileName)+1)
	modules=[]			# moduleName	(moduleId = index(moduleName)+1)
	fileBelongsToModule={} 		# fileId x moduleId
	moduleBelongsToModule={} 	# childModuleId x parentModuleId
	
	for line in input_file:
		line = line.strip()
		
		# if the line is the start of an entity
		if line.startswith("(SourceFile FM"):
			for line in input_file:
				line = line.strip()

				if line.startswith("(uniqueName "):
					addFile(line, files, modules, fileBelongsToModule, moduleBelongsToModule)
				elif line.startswith(")"):
					break
	
	input_file.close()

	# then write everything to file
	modules_file=open("modulesWithIDs.txt", 'w')	
	for moduleName in modules:
		moduleId = `modules.index(moduleName)+1`
		moduleInfo = moduleId + "\t\"" + moduleName + "\"\n";
		modules_file.write(moduleInfo)
	modules_file.close()

	moduleBelongsToModule_file=open("moduleBelongsToModule.txt", 'w')
	moduleIndices = moduleBelongsToModule.keys()
	moduleIndices.sort()
	for childModuleId in moduleIndices:
		parentModuleId = moduleBelongsToModule[childModuleId]
		moduleBelongsToModuleInfo = `childModuleId` + "\t" + `parentModuleId` + "\n"
		moduleBelongsToModule_file.write(moduleBelongsToModuleInfo)
	moduleBelongsToModule_file.close()

	files_file=open("filesWithIDs.txt", 'w')
	for fileName in files:
		fileId = `files.index(fileName)+1`
		fileInfo = fileId + "\t\"" + fileName + "\"\n";
		files_file.write(fileInfo)
	files_file.close()
	
	fileBelongsToModule_file=open("fileBelongsToModule.txt", 'w')
	fileIndices = fileBelongsToModule.keys()
	fileIndices.sort()
	for fileId in fileIndices:
		parentModuleId = fileBelongsToModule[fileId]
		fileBelongsToModuleInfo = `fileId` + "\t" + `parentModuleId` + "\n"
		fileBelongsToModule_file.write(fileBelongsToModuleInfo)
	fileBelongsToModule_file.close()
