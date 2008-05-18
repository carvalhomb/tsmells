# !/usr/bin/python
# This file is part of snavtofamix (Source Navigator to FAMIX).
#
# snavtofamix is free software; you can redistribute it and/or modify it 
# under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 2 of the License, or (at your 
# option) any later version.
#
# snavtofamix is distributed in the hope that it will be useful, but WITHOUT 
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more 
# details.
#
# You should have received a copy of the GNU General Public License along 
# with snavtofamix; if not, write to the Free Software Foundation, Inc., 
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA 
#
# Copyright 2006,2007 Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>, 
#                     Bart Du Bois <bart.dubois@ua.ac.be>
#
# This module writes metrics data on several versions (metrics as columns) to 
# files for each metric (versions as columns).
# The resulting files can be immediately read by R as tables,
# for which boxplots across multiple versions can be drawn easily
# for each metric:
#
# data <- read.table(outputFileName,header=TRUE)
# boxplot(data,horizontal=TRUE)
# summary(data)

#######################################################################
# imports
#######################################################################

# python std + external libs
import sys
import os

#######################################################################
# global variables
#######################################################################

# first columns: ID (0), name (1), filename (2) => 3
classIdIndex = 0
classNameIndex = classIdIndex+1
fileNameIndex = classNameIndex + 1
firstMetricColumnIndex = fileNameIndex + 1

#######################################################################
# helper functions
#######################################################################

# get list of file info objects for files of particular extensions"
def listDirectory(directory, fileExtList):
	fileList = [os.path.normcase(f) for f in os.listdir(directory)]
	fileList = [os.path.join(directory, f) for f in fileList \
			if os.path.splitext(f)[1] in fileExtList]
	return fileList

#######################################################################
# main
#######################################################################

# check command line arguments
if len(sys.argv) < 3:
	print "Usage: python",sys.argv[0],"inputDir","outputDir"
	sys.exit(1)
else:
	inputDir = sys.argv[1].rstrip("/")
	if not os.path.isdir(inputDir):
		print "Input directory \"" + inputDir + "\" does not exist."
		sys.exit(1)
	
	outputDir = sys.argv[2].rstrip("/")
	if not os.path.isdir(outputDir):
		print "Output directory \"" + outputDir + "\" does not exist."
		sys.exit(1)

# collect list of files and versions
fileList=listDirectory(inputDir, ['.txt'])

versionNames = [os.path.splitext(os.path.split(fileName)[1])[0] for fileName in fileList]
print "Versions:",versionNames

# map files to versions
versions = {} # fileName x versionName
fileIndex = 0
while fileIndex < len(fileList):
	versions[fileList[fileIndex]] = versionNames[fileIndex]
	fileIndex +=1 

allData = {} # metricName x version [ x className@fileName x metricValue ]

columnNames = []
metricNames = []
allUniqueClassNames = []

print "Retrieving raw data...",

# retrieve and store all data
maxNrOfDataInVersions=0
for fileName in fileList:
	file = open(fileName,'r')

	versionName = versions[fileName]
	
	isColumnHeaderLine=True
	nrOfDataInCurrentVersion=0

	for line in file:
		line = line.strip()
		
		if isColumnHeaderLine:
			#retrieve metric names from column header line
			columnNames=line.split("	")
			
			metricNames=columnNames[firstMetricColumnIndex:len(columnNames)]
			
			for metricName in metricNames:
				if not(metricName in allData):
					allData[metricName] = {}
				
				allData[metricName][versionName] = {}
			
			isColumnHeaderLine=False
			continue

		# line is not a column header
		className = line.split("\t")[classNameIndex].strip("'")
		fileName = line.split("\t")[fileNameIndex].strip("'")
		uniqueClassName = "@".join([className, fileName])
		
		if not uniqueClassName in allUniqueClassNames:
			allUniqueClassNames.append(uniqueClassName)
		
		metricValues = line.split("\t")[firstMetricColumnIndex:len(columnNames)]
		
		metricIndex=0
		for metricValue in metricValues:
			allData[metricNames[metricIndex]][versionName][uniqueClassName] = int(metricValue)
			metricIndex += 1
		
		nrOfDataInCurrentVersion += 1
	
	maxNrOfDataInVersions=max(maxNrOfDataInVersions,nrOfDataInCurrentVersion)
	
	file.close()

print "[retrieved]"

print "Metrics:", metricNames

sortedClassNames = {} # metricName x [ uniqueClassName ]

print "Sorting data by metric value of initial version...",
# sort allData[metricName][version1] by the metricValue of each uniqueClassName
for metricName in metricNames:
	sortedClassNames[metricName] = []

	versionIndex = 0
	versionName = versionNames[versionIndex]
	
	metricValueDict = allData[metricName][versionName]
	sortedMetricValueDict = {}
	
	# create a map of [metricValue, uniqueClassName] items sorted descending
	# by metricValue
	aMap = map(lambda t: list(t),metricValueDict.items()) # keys map	map(lambda r: r.reverse(),aMap) # values map	aMap.sort()
	aMap.reverse()
	
	itemIndex = 0
	while itemIndex < len(aMap):
		curItem = aMap[itemIndex]
		uniqueClassName = curItem[1]
		metricValue = curItem[0]
		
		sortedMetricValueDict[uniqueClassName] = metricValue
		
		if not(uniqueClassName in sortedClassNames[metricName]):
			sortedClassNames[metricName].append(uniqueClassName)
		
		itemIndex += 1
	
	# replace the old dict with the sorted version
	allData[metricName][versionName] = sortedMetricValueDict
	
# for each i in 2:length(versions)
# sort allData[metricName][version_i] by the order of allData[metricName][version_i-1],
# and further on by the metricValue of each left-over uniqueClassName
for metricName in metricNames:
	versionIndex = 1
	while versionIndex < len(versionNames):
		versionName = versionNames[versionIndex]
		
		metricValueDict = allData[metricName][versionName]
		sortedMetricValueDict = {}
	
		# create a map of [metricValue, uniqueClassName] items sorted descending
		# by metricValue
		aMap = map(lambda t: list(t),metricValueDict.items()) # keys map		map(lambda r: r.reverse(),aMap) # values map		aMap.sort()
		aMap.reverse()
		
		itemIndex = 0
		while itemIndex < len(aMap):
			curItem = aMap[itemIndex]
			uniqueClassName = curItem[1]
			metricValue = curItem[0]
			
			sortedMetricValueDict[uniqueClassName] = metricValue

			if not(uniqueClassName in sortedClassNames[metricName]):
				sortedClassNames[metricName].append(uniqueClassName)

			itemIndex += 1
		
		# metric value dict sorted relative to previous version
		relativeSortedMetricValueDict = {}
		
		handledClassNames = []
		# iterate over class names of previous version
		for uniqueClassName in allData[metricName][versionNames[versionIndex-1]]:
			if uniqueClassName in sortedMetricValueDict:
				relativeSortedMetricValueDict[uniqueClassName] = sortedMetricValueDict[uniqueClassName]
				handledClassNames.append(uniqueClassName)
		
		# iterate over class names of previous version	
		for uniqueClassName in sortedMetricValueDict:
			if not(uniqueClassName in handledClassNames):
				relativeSortedMetricValueDict[uniqueClassName] = sortedMetricValueDict[uniqueClassName]
	
		# replace the old dict with the relatively sorted version
		allData[metricName][versionName] = relativeSortedMetricValueDict
		versionIndex += 1

print "[sorted]"

# write all data
metricIndex = 0
for metricName in metricNames:
	outputFileName = os.path.join(outputDir,metricName) + ".txt"
	print "Exporting",metricName,"data to file",outputFileName+"...",
	
	outputFile = open(outputFileName,'w')
	
	outputFile.write("uniqueClassName	")
	
	for versionName in versionNames:
		outputFile.write(versionName)

		isLastVersionName = (versionName == versionNames[len(versionNames)-1])
		if isLastVersionName:
			outputFile.write("\n")
		else:
			outputFile.write("	")

	for uniqueClassName in sortedClassNames[metricName]:
		outputFile.write("'" + uniqueClassName + "'	")
		
		for versionName in versionNames:
			curData = "NA"
			
			versionHasData = uniqueClassName in allData[metricName][versionName]
			if versionHasData:
				curData = `allData[metricName][versionName][uniqueClassName]`
			
			outputFile.write(curData)

			isLastVersionName = (versionName == versionNames[len(versionNames)-1])
			if isLastVersionName:
				outputFile.write("\n")
			else:
				outputFile.write("	")
	
	outputFile.close()
	print "[done]"
	metricIndex += 1