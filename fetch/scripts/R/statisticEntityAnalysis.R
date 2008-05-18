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
# R script to characterize metrics of a given input file.
#
# The script takes its input from the system environment variables:
#	INPUT_FILE: the name of the input file
#	OUTPUT+DIR: the name of the output directory
#
# The output comprises files representing:
#	+Model fitting
#		+scatter plot + regression line of CC in function of LOC
#		+summary of the linear model between CC and LOC
#	+Box plots of LOC,CC,NOS,FanOut,FanIn,MPC
#	+Histograms of these metrics.
#
# Tested on MacOSX 10.4.8 with R for Mac OS X, v1.16 (3198).
# http://www.R-project.org
#
# Author: Bart Du Bois
# Contact: bart.dubois@ua.ac.be
###

# Root directory of the experimental data.
#mainDirectory=".."

inputFile=Sys.getenv("INPUT_FILE")
outputDirectory=Sys.getenv("OUTPUT_DIR")

# Read the data and store it in an object.
# The first line contains the column-headers.
theData <- read.table(inputFile, header=TRUE)

###### MODEL FITTING ######
# For each version, create a model of CC in function of LOC.
# Plot the data and regression lines for this model in a
# scatterplot.
# Report on the quality of the model.
###### MODEL FITTING ######

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"modelFitting.ps",sep="/")
postscript(file=plotFile)

##
# Plot the before data and regression line.
##

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

ylabel="CC"
xlabel="LOC"

ydata=theData$CC
xdata=theData$LOC
beforeColor="grey"
plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel, col=beforeColor)

# Draw a grid on top of the box-plot, with line-width 3.
grid(nx=NULL,ny=NULL,lwd=3)

# Do not clean the plotting frame before drawing.
# Ensures that the boxplot is drawn on top of the grid.
par(new=TRUE)

plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel, col=beforeColor)

beforeModel <- lm(ydata ~ xdata )
abline(beforeModel, col="grey")

##
# Report on the quality of the model
##

# Divert R output to a file
outputFileName=paste(outputDirectory,"modelFitting.txt",sep="/")
sink(outputFileName)

summary(theData$LOC)
summary(theData$CC)
summary(beforeModel)

# End diversion to a file
sink()

###### MODEL FITTING FOR FAN OUT <-> FAN IN ######
# For each version, create a model of FanOut in function of FanIn.
# Plot the data and regression lines for this model in a
# scatterplot.
# Report on the quality of the model.
###### MODEL FITTING ######

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"modelFittingFanOutIn.ps",sep="/")
postscript(file=plotFile)

##
# Plot the before data and regression line.
##

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

ylabel="FanOut"
xlabel="FanIn"
yrange <- range(0,theData$FanOut)
xrange <- range(0,theData$FanIn)

ydata=theData$FanOut
xdata=theData$FanIn
beforeColor="grey"
plot(ydata ~ xdata, horizontal=TRUE,xlab=xlabel, ylab=ylabel, xlim=xrange, ylim=yrange, col=beforeColor)

# Draw a grid on top of the box-plot, with line-width 3.
grid(nx=NULL,ny=NULL,lwd=3)

# Do not clean the plotting frame before drawing.
# Ensures that the boxplot is drawn on top of the grid.
par(new=TRUE)

plot(ydata ~ xdata, horizontal=TRUE,xlab=xlabel, ylab=ylabel, xlim=xrange, ylim=yrange, col=beforeColor)

beforeModel <- lm(ydata ~ xdata )
abline(beforeModel, col="grey")

# Shutdown the postscript graphics device.
dev.off()

##
# Report on the quality of the model
##

# Divert R output to a file
outputFileName=paste(outputDirectory,"modelFittingFanOutIn.txt",sep="/")
sink(outputFileName)

summary(theData$FanIn)
summary(theData$FanOut)
summary(beforeModel)

# End diversion to a file
sink()


###### BOXPLOTS ######
# For each metric, draw 2 boxplots comparing the metric values
# of the before and after version.
###### BOXPLOTS ######

##
# Draw boxplots for the LOC metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"loc.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
boxplot(theData$LOC,horizontal=TRUE,xlab="LOC")
dev.off()

##
# Draw boxplots for the CC metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"CC.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
boxplot(theData$CC,horizontal=TRUE,xlab="CC")

dev.off()

##
# Draw limited boxplot for the CC metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"CClimited.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
maxCCvalue=20
boxplot(theData$CC[theData$CC<=maxCCvalue],horizontal=TRUE,xlab="CC")

##
# Draw boxplots for the NOS metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"com.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
boxplot(theData$NOS,horizontal=TRUE,xlab="NOS")
dev.off()

##
# Draw boxplots for the FanOut metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"fanOut.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
boxplot(theData$FanOut,horizontal=TRUE,xlab="FanOut")
dev.off()

##
# Draw boxplots for the FanIn metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"fanIn.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
boxplot(theData$FanIn,horizontal=TRUE,xlab="FanIn")
dev.off()

##
# Draw boxplots for the MPC metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"mpc.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
boxplot(theData$MPC,horizontal=TRUE,xlab="MPC")
dev.off()

###### HISTOGRAMS ######
# For each metric, draw 2 histograms comparing the metric values
# of the before and after version.
###### HISTOGRAMS ######

##
# Draw histograms for the LOC metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"locHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

xlabel="LOC"
loc=theData$LOC
hist(loc, breaks=max(theData$LOC),xlab=xlabel,col=beforeColor)

# Shutdown the postscript graphics device.
dev.off()

##
# Draw histograms for the CC metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"CCHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

xlabel="CC"
cc=theData$CC
hist(cc, breaks=max(theData$CC),xlab=xlabel,col=beforeColor)

# Shutdown the postscript graphics device.
dev.off()

##
# Draw histograms for the NOS metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"comHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

xlabel="NOS"
nos=theData$NOS
hist(nos, breaks=max(theData$NOS),xlab=xlabel,col=beforeColor)

# Shutdown the postscript graphics device.
dev.off()

##
# Draw histograms for the FanOut metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"fanOutHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

xlabel="FanOut"
fanOut=theData$FanOut
hist(fanOut, breaks= max(theData$FanOut),xlab=xlabel,col=beforeColor)

# Shutdown the postscript graphics device.
dev.off()

##
# Draw histograms for the FanIn metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"fanInHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

xlabel="FanIn"
fanIn=theData$FanIn
hist(fanIn, breaks=max(theData$FanIn),xlab=xlabel,col=beforeColor)

# Shutdown the postscript graphics device.
dev.off()

##
# Draw histograms for the MPC metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"mpcHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

xlabel="MPC"
mpc=theData$MPC
hist(mpc, breaks=max(theData$MPC),xlab=xlabel,col=beforeColor)

# Shutdown the postscript graphics device.
dev.off()

# Delete the objects
remove(theData)
remove(xdata)
remove(ydata)
remove(beforeColor)
remove(outputDirectory)
remove(inputFile)
