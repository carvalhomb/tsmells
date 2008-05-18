# !/usr/bin/python

import sys

if len(sys.argv) < 2:
  print "Usage:",sys.argv[0],"rsf-output-file-name"
  sys.exit(64)

output_file_name=sys.argv[1]

output_file=open(output_file_name, 'w')

input_file=open("modulesWithIDs.txt", 'r')
for line in input_file:
	output_file.write("Module\t" + line)
input_file.close()

input_file=open("moduleBelongsToModule.txt", 'r')
for line in input_file:
	output_file.write("ModuleBelongsToModule\t" + line)
input_file.close()

input_file=open("filesWithIDs.txt", 'r')
for line in input_file:
	output_file.write("File\t" + line)
input_file.close()

input_file=open("fileBelongsToModule.txt", 'r')
for line in input_file:
	output_file.write("FileBelongsToModule\t" + line)
input_file.close()

input_file=open("includeBelongsToFile.txt", 'r')
for line in input_file:
	output_file.write("Include\t" + line)
input_file.close()

input_file=open("conditionalCompilationBlocks.txt", 'r')
for line in input_file:
	output_file.write("ConditionalCompilation\t" + line)
input_file.close()

input_file=open("classesWithIDs.txt", 'r')
for line in input_file:
	output_file.write("Class\t" + line)
input_file.close()

#input_file=open("typedefsWithIDs.txt", 'r')
#for line in input_file:
#	output_file.write("TypeDef\t" + line)
#input_file.close()

input_file=open("classBelongsToFile.txt", 'r')
for line in input_file:
	output_file.write("ClassBelongsToFile\t" + line)
input_file.close()

input_file=open("inheritanceWithIDs.txt", 'r')
for line in input_file:
	output_file.write("InheritsFrom\t" + line)
input_file.close()

input_file=open("methodsWithIDs.txt", 'r')
for line in input_file:
	output_file.write("Method\t" + line)
input_file.close()

input_file=open("methodBelongsToClass.txt", 'r')
for line in input_file:
	output_file.write("MethodBelongsToClass\t" + line)
input_file.close()

input_file = open("methodVisibility.txt", 'r')
for line in input_file:
	output_file.write("Visibility\t" + line)
input_file.close()

input_file = open("methodSignature.txt", 'r')
for line in input_file:
	output_file.write("Signature\t" + line)
input_file.close()

input_file=open("methodHasClassAsReturnType.txt", 'r')
for line in input_file:
	output_file.write("HasType\t" + line)
input_file.close()

input_file=open("attributesWithIDs.txt", 'r')
for line in input_file:
	output_file.write("Attribute\t" + line)
input_file.close()

input_file=open("attributeBelongsToClass.txt", 'r')
for line in input_file:
	output_file.write("AttributeBelongsToClass\t" + line)
input_file.close()

input_file=open("attributeHasClassAsType.txt", 'r')
for line in input_file:
	output_file.write("HasType\t" + line)
input_file.close()

input_file=open("functionsWithIDs.txt", 'r')
for line in input_file:
	output_file.write("Function\t" + line)
input_file.close()

input_file=open("invokableEntityBelongsToFile.txt", 'r')
for line in input_file:
	output_file.write("InvokableEntityBelongsToFile\t" + line)
input_file.close()

input_file=open("functionHasClassAsReturnType.txt", 'r')
for line in input_file:
	output_file.write("HasType\t" + line)
input_file.close()

input_file=open("defsWithAssociation.txt", 'r')
for line in input_file:
	output_file.write("DefinitionForDeclaration\t" + line)
input_file.close()

input_file=open("globalVarsWithIDs.txt", 'r')
for line in input_file:
	output_file.write("GlobalVar\t" + line)
input_file.close()

input_file=open("accessibleEntityBelongsToFile.txt", 'r')
for line in input_file:
	output_file.write("AccessibleEntityBelongsToFile\t" + line)
input_file.close()

input_file=open("globalVarHasClassAsType.txt", 'r')
for line in input_file:
	output_file.write("HasType\t" + line)
input_file.close()

input_file=open("invocationsWithIDs.txt", 'r')
for line in input_file:
	output_file.write("Invokes\t" + line)
input_file.close()

input_file=open("invocationLocations.txt", 'r')
for line in input_file:
	output_file.write("LineNo\t" + line)
input_file.close()

input_file=open("accessesWithIDs.txt", 'r')
for line in input_file:
	output_file.write("Accesses\t" + line)
input_file.close()

input_file=open("accessesLocations.txt", 'r')
for line in input_file:
	output_file.write("LineNo\t" + line)
input_file.close()

input_file=open("leftValueAccesses.txt", 'r')
for line in input_file:
	output_file.write("LeftValueAccess\t" + line)
input_file.close()

input_file=open("entityBelongsToBlock.txt", 'r')
for line in input_file:
	output_file.write("entityBelongsToBlock\t" + line)
input_file.close()

input_file=open("annotations.txt", 'r')
for line in input_file:
	output_file.write("Annotation\t" + line)
input_file.close()

input_file=open("annotationBelongsToEntity.txt", 'r')
for line in input_file:
	output_file.write("AnnotationBelongsToEntity\t" + line)
input_file.close()

input_file=open("metricsWithIDs.txt", 'r')
for line in input_file:
	output_file.write("Measurement\t" + line)
input_file.close()

input_file=open("cfMetricsWithIDs.txt", 'r')
for line in input_file:
	output_file.write("Measurement\t" + line)
input_file.close()


output_file.close()
