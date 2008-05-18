# !/usr/bin/python

import os
from os import EX_OK, EX_USAGE, EX_UNAVAILABLE, EX_IOERR
import sys

##
# $Rev: 1155 $:     Revision of last commit
# $Author: bdubois $:  Author of last commit
# $Date: 2007-05-03 07:51:55 +0200 (Thu, 03 May 2007) $:    Date of last commit
##

def convertToRsf(cdif_input_file_name, rsf_output_file_name):
	print "Reading from file", cdif_input_file_name + "..."
	
	print "\tFiles...",
	return_code = os.system("python ./writeFiles.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
	
	print "\tIncludes...",
	return_code = os.system("python ./writeIncludes.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
		
	print "\tConditional Compilation...",
	return_code = os.system("python ./writeCondComp.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
	
	print "\tClasses...",
	return_code = os.system("python ./writeClasses.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
	
	#print "\tTypeDefs...",
	#return_code = os.system("python ./writeTypedefs.py " + cdif_input_file_name)
	#if return_code == EX_OK:
	#	print "[ok]"
	#else:
		print "[failed]"
		return EX_UNAVAILABLE
	
	print "\tInheritance...",
	return_code = os.system("python ./writeInheritance.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
	
	print "\tMethods...",
	return_code = os.system("python ./writeMethods.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
	
	print "\tAttributes...",
	return_code = os.system("python ./writeAttributes.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
	
	print "\tFunctions...",
	return_code = os.system("python ./writeFunctions.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
	
	print "\tGlobalVariables...",
	return_code = os.system("python ./writeGlobalVar.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
	
	print "\tAccesses...",
	return_code = os.system("python ./writeAccesses.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
	
	print "\tInvocations...",
	return_code = os.system("python ./writeInvocations.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
		
	print "\tAnnotations...",
	return_code = os.system("python ./writeAnnotations.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
	
	print "\tSize and Complexity Metrics...",
	return_code = os.system("python ./writeMetrics.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
		
	print "\tChange Frequency Metrics...",
	return_code = os.system("python ./writeCFMetrics.py " + cdif_input_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
	
	print "Merging output...",
	return_code = os.system("python ./makeRsf.py " + rsf_output_file_name)
	if return_code == EX_OK:
		print "[ok]"
	else:
		print "[failed]"
		return EX_UNAVAILABLE
	
	remove_command = "rm -f 	accessesWithIDs.txt attributeBelongsToClass.txt \
								attributeHasClassAsType.txt attributesWithIDs.txt \
								classBelongsToFile.txt classesWithIDs.txt \
								conditionalCompilationBlocks.txt typedefsWithIDs.txt\
								fileBelongsToModule.txt filesWithIDs.txt \
								invokableEntityBelongsToFile.txt functionsWithIDs.txt \
								globalVarsWithIDs.txt globalVarHasClassAsType.txt \
								accessibleEntityBelongsToFile.txt \
								inheritanceWithIDs.txt invocationsWithIDs.txt \
								methodBelongsToClass.txt methodsWithIDs.txt \
								modulesWithIDs.txt metricsWithIDs.txt \
								moduleBelongsToModule.txt includeBelongsToFile.txt \
								methodHasClassAsReturnType.txt functionHasClassAsReturnType.txt \
								entityBelongsToBlock.txt methodVisibility.txt \
								methodSignature.txt attributeSignature.txt attributeVisibility.txt\
								accessesLocations.txt invocationLocations.txt \
								defsWithAssociation.txt cfMetricsWithIDs.txt \
								leftValueAccesses.txt annotations.txt \
								annotationBelongsToEntity.txt"
	
	return_code = os.system(remove_command)
	if return_code != EX_OK:
		print "Error cleaning up."
		return EX_IOERR
		
	print "Output written to", rsf_output_file_name
	
	return EX_OK

if __name__ == '__main__':
	if len(sys.argv) < 2:
	  print "Usage:",sys.argv[0],"cdif-input-file-name rsf-output-file-name"
	  sys.exit(EX_USAGE)
	
	input_file=sys.argv[1]
	
	cdif_input_file_name = sys.argv[1]
	rsf_output_file_name = sys.argv[2]
	
	exitCode = convertToRsf(cdif_input_file_name, rsf_output_file_name)
	
	sys.exit(exitCode)

