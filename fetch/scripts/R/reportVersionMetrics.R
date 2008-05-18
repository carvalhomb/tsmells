# This file is part of Fetch (the Fact Extraction Tool CHain).
#
# Fetch is free software; you can redistribute it and/or modify it under the
# terms of the GNU General Public License as published by the Free Software
# Foundation; either version 2 of the License, or (at your option) any later
# version.
#
# Fetch is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
# details.
#
# You should have received a copy of the GNU General Public License along
# with Fetch; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
#
# Copyright 2006,2007 University of Antwerp
# Author(s):  Bart Du Bois <bart.dubois@ua.ac.be>

###
# R script to characterize metrics of multiple versions.
#
# The script takes its input from the system environment variables:
#	INPUT_DIR: the name of the directory in which the multiple
#				versions are located.
#	OUTPUT_DIR: the name of the output directory
#
# The output comprises files representing:
#	+Box plots of the metrics for all versions in INPUT_DIR
#
# Tested on MacOSX 10.4.8 with R for Mac OS X, v1.16 (3198).
# http://www.R-project.org
#
# Author: Bart Du Bois
# Contact: bart.dubois@ua.ac.be
###

inputDir=Sys.getenv("INPUT_DIR")
outputDir=Sys.getenv("OUTPUT_DIR")

metricFiles=list.files(inputDir)
nrOfMetrics=length(metricFiles)

print("Processing metric files...")
print(metricFiles)

metricIndex=1

while ( metricIndex <= nrOfMetrics ) {
	metricFile = metricFiles[metricIndex]
	metricFilePath=paste(inputDir,metricFile,sep="/")

	data <- read.table(metricFilePath, header=TRUE)
	metricIndex = metricIndex + 1

	# Start the postscript graphics device driver.
	metricName = strsplit(metricFile,".txt")
	plotFileBaseName=paste(outputDir, metricName,sep="/")
	plotFileName=paste(plotFileBaseName,"ps",sep=".")
	postscript(file=plotFileName)

	# Ensure that text and symbols are scaled 150%
	par(cex=1.5)

	# plot the boxplot in all its glory (excluding uniqueClassName)
	boxplot(data[,-1],horizontal=TRUE,main=metricName)

	dev.off()

	if ( length(boxplot.stats(data[2]$out)) > 0 ) {
		# calculate the minimum outlier across all versions
		minOutlier = min(boxplot.stats(data[2])$out)

		if ( minOutlier != Inf ) {
			for (i in 2:length(data)) {
					if ( length(boxplot.stats(data[i]$out)) > 0 ) {
						curOutlier = min(boxplot.stats(data[i])$out)
						minOutlier = min(minOutlier, curOutlier)
					}
			}
		}

		# if there are high outliers
		if ( minOutlier > median(data[,2],na.rm=TRUE) ) {
			if (minOutlier != Inf) {
				# Start the postscript graphics device driver.
				plotFileName = paste(plotFileBaseName, "normalRange", "ps", sep=".")
				postscript(file=plotFileName)

				# Ensure that text and symbols are scaled 150%
				par(cex=1.5)

				# plot the boxplot in all its glory
				boxplot(data[,-1],horizontal=TRUE,ylim=c(0,minOutlier),main=paste("Normal range of",metricName,sep=" "))

				dev.off()
			}
		}
	}

	# plot the lines plot
	plotFileName=paste(plotFileBaseName,"linesplot","ps",sep=".")
	postscript(file=plotFileName)

	# Ensure that text and symbols are scaled 150%
	par(cex=1.5)

	# create two plots one above the other
	par(mfrow=c(2,1),mar=c(3,3,3,3),mgp=c(2, 1, 0))

	# Ensure that text and symbols are scaled 150%
	par(cex=1.5)

	# take difference between last and first version
	versionDiff <- data[,length(data)] - data[,2]

	colorVector <- rep("black",length(versionDiff))
	colorVector[versionDiff==0] = "white"

	mainTitle=paste(metricName,"difference between versions",names(data)[2],"and",names(data)[length(data)],sep=" ")
	barplot(versionDiff,col=colorVector,border="white",main=mainTitle,ylab=paste(metricName,"delta",sep=" "),xlab="Classes sorted by baseline value")

	# Ensure that text and symbols are scaled 150%
	par(cex=1.5)

	mainTitle=paste(metricName,"deltas across all versions")

	versionColors <- rainbow(length(data)-1)

	plot(data[,(length(data))],type="l",col=versionColors[length(data)-1],main=mainTitle,axis=TRUE,ylab=metricName,xlab="Classes sorted by baseline value")

	legend("top",legend=names(data)[2:length(data)],horiz=TRUE,text.col=versionColors,bty="n")

	for (versionIndex in length(data)-1:2) {
		lines(data[,versionIndex],col=versionColors[versionIndex-1])
	}

	dev.off()

}

remove(mainTitle)
remove(plotFileName)
remove(versionIndex)
remove(colors)
remove(metricFiles)
remove(nrOfMetrics)
remove(metricFilePath)
remove(metricFile)
remove(metricIndex)
remove(minOutlier)
remove(data)
remove(outputDir)
remove(inputDir)
