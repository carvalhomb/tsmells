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
# R script to compare complexity metrics on two versions
# of a software system, typically before and after refactoring.
#
# The script takes its input from inputDirectory, and puts its output
# in outputDirectory. This output comprises:
#	+Model fitting
#		+scatter plot + regression line of MVG in function of LOC
#		+summary of the linear model between MVG and LOC
#	+Box plots of LOC,MVG,COM,FanOut,FanIn,MPC
#	+Histograms of these metrics.
#
# Tested on MacOSX 10.4.8 with R for Mac OS X, v1.16 (3198).
# http://www.R-project.org
#
# Author: Bart Du Bois
# Contact: bart.dubois@ua.ac.be
###

# Root directory of the experimental data.
mainDirectory=".."

# Sub directories and files of the experimental data.
inputDirectory=paste(mainDirectory,"input",sep="/")
outputDirectory=paste(mainDirectory,"output",sep="/")
dataFileNameBefore=paste(inputDirectory,"nrOfEntitiesBefore.txt",sep="/")
dataFileNameAfter=paste(inputDirectory,"nrOfEntitiesAfter.txt",sep="/")

# Read the data and store it in an object.
# The first line contains the column-headers.
beforeData <- read.table(dataFileNameBefore, header=TRUE)
afterData <- read.table(dataFileNameAfter, header=TRUE)

###### HISTOGRAMS ######
# For each metric, draw 2 histograms comparing the metric values
# of the before and after version.
###### HISTOGRAMS ######

##
# Draw histograms for the number of methods and functions per file.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"nrOfEntitiesHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

par(mfrow=c(2,1))

xlabel="Number of methods and functions per file"
max_nrOfEntities = max(beforeData$nrOfEntities,afterData$nrOfEntities)
xrange <- range(0, max_nrOfEntities)
xdata=beforeData$nrOfEntities
beforeColor="grey"
hist(xdata, breaks=max_nrOfEntities,xlab=xlabel,xlim=xrange,col=beforeColor,main="Before")

xdata=afterData$nrOfEntities
hist(xdata,breaks=max_nrOfEntities,xlab=xlabel,xlim=xrange,main="After")

# Shutdown the postscript graphics device.
dev.off()

##
# Draw histograms for the number of classes per file.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"nrOfClassesHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

par(mfrow=c(2,1))

xlabel="Number of classes per file"
max_nrOfClasses = max(beforeData$nrOfClasses,afterData$nrOfClasses)
xrange <- range(0, max_nrOfClasses)
xdata=beforeData$nrOfClasses
beforeColor="grey"
hist(xdata, breaks=max_nrOfClasses,xlab=xlabel,xlim=xrange,col=beforeColor,main="Before")

xdata=afterData$nrOfClasses
hist(xdata,breaks=max_nrOfClasses,xlab=xlabel,xlim=xrange,main="After")
# Shutdown the postscript graphics device.
dev.off()


# Delete the objects
remove(beforeData)
remove(afterData)
remove(max_nrOfEntities)
remove(max_nrOfClasses)
remove(xdata)
remove(ydata)
remove(beforeColor)
remove(inputDirectory)
remove(outputDirectory)
remove(dataFileNameBefore)
remove(dataFileNameAfter)
remove(mainDirectory)
