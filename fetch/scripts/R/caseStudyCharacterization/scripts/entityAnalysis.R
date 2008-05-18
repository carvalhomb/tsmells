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
# R script to characterize size, complexity and coupling.
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
zoomedOutputDirectory=paste(outputDirectory,"zoomed",sep="/")
dataFileName=paste(inputDirectory,"entityMetrics.txt",sep="/")

# Read the data and store it in an object.
# The first line contains the column-headers.
dataTable <- read.table(dataFileName, header=TRUE)

###### MODEL FITTING ######
# For each version, create a model of MVG in function of LOC.
# Plot the data and regression lines for this model in a
# scatterplot.
# Report on the quality of the model.
###### MODEL FITTING ######

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"modelFitting.ps",sep="/")
postscript(file= plotFile)

##
# Plot the before data and regression line.
##

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

ylabel="MVG"
xlabel="LOC"

ydata=dataTable$MVG
xdata=dataTable$LOC
plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel)

# Draw a grid on top of the box-plot, with line-width 3.
grid(nx=NULL,ny=NULL,lwd=3)

# Do not clean the plotting frame before drawing.
# Ensures that the boxplot is drawn on top of the grid.
par(new=TRUE)

plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel)

linearModel <- lm(ydata ~ xdata )
abline(linearModel)

# Shutdown the postscript graphics device.
dev.off()


##
# Report on the quality of the model
##

# Divert R output to a file
outputFileName=paste(outputDirectory,"modelFitting.txt",sep="/")
sink(outputFileName)

summary(dataTable$LOC)
summary(dataTable$MVG)
summary(linearModel)

# End diversion to a file
sink()

## Zoom in on 75% percentile
# Start the postscript graphics device driver.
plotFile=paste(zoomedOutputDirectory,"modelFitting.ps",sep="/")
postscript(file=plotFile)
##
# Plot the before data and regression line.
##

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

ylabel="MVG"
xlabel="LOC"

ydata=dataTable$MVG[dataTable$LOC < 100]
xdata=dataTable$LOC[dataTable$LOC < 100]
plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel,main="Entities < 100 LOC")

# Draw a grid on top of the box-plot, with line-width 3.
grid(nx=NULL,ny=NULL,lwd=3)

# Do not clean the plotting frame before drawing.
# Ensures that the boxplot is drawn on top of the grid.
par(new=TRUE)

plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel)

linearModel <- lm(ydata ~ xdata )
abline(linearModel)

# Shutdown the postscript graphics device.
dev.off()

##
# Report on the quality of the model
##

# Divert R output to a file
outputFileName=paste(zoomedOutputDirectory,"modelFitting.txt",sep="/")
sink(outputFileName)

summary(dataTable$LOC)
summary(dataTable$MVG)
summary(linearModel)


###### MODEL FITTING FOR FAN OUT <-> FAN IN ######
# For each version, create a model of FanOut in function of FanIn.
# Plot the data and regression lines for this model in a
# scatterplot.
# Report on the quality of the model.
###### MODEL FITTING ######

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"modelFittingFanOutIn.ps",sep="/")
postscript(file= plotFile)

##
# Plot the before data and regression line.
##

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

ylabel="FanOut"
xlabel="FanIn"
ydata=dataTable$FanOut
xdata=dataTable$FanIn
plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel)

# Draw a grid on top of the box-plot, with line-width 3.
grid(nx=NULL,ny=NULL,lwd=3)

# Do not clean the plotting frame before drawing.
# Ensures that the boxplot is drawn on top of the grid.
par(new=TRUE)

plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel)

linearModel <- lm(ydata ~ xdata )
abline(linearModel)

# Shutdown the postscript graphics device.
dev.off()

##
# Report on the quality of the model
##

# Divert R output to a file
outputFileName=paste(outputDirectory,"modelFittingFanOutIn.txt",sep="/")
sink(outputFileName)

summary(dataTable$FanIn)
summary(dataTable$FanOut)
summary(linearModel)

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
boxplot(dataTable$LOC,horizontal=TRUE,xlab="LOC")
dev.off()


##
# Draw boxplots for the MVG metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"mvg.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
boxplot(dataTable$MVG,horizontal=TRUE,xlab="MVG")
dev.off()

##
# Draw boxplots for the COM metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"com.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
boxplot(dataTable$COM,horizontal=TRUE,xlab="COM")
dev.off()

##
# Draw boxplots for the FanOut metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"fanOut.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
boxplot(dataTable$FanOut,horizontal=TRUE,xlab="FanOut")
dev.off()

##
# Draw boxplots for the FanIn metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"fanIn.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
boxplot(dataTable$FanIn,horizontal=TRUE,xlab="FanIn")

dev.off()

##
# Draw boxplots for the MPC metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"mpc.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
boxplot(dataTable$MPC,horizontal=TRUE,xlab="MPC")

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
hist(dataTable$LOC, breaks=max(dataTable$LOC),xlab=xlabel)

# Shutdown the postscript graphics device.
dev.off()

## Zoom in!
# Start the postscript graphics device driver.
plotFile=paste(zoomedOutputDirectory,"locHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
hist(dataTable$LOC[dataTable$LOC < quantile(dataTable$LOC,c(0.75))],xlim=range(0,quantile(dataTable$LOC,c(0.75))),xlab=xlabel,main="Histogram of 75% percentile")
dev.off()

##
# Draw histograms for the MVG metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"mvgHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

xlabel="MVG"
hist(dataTable$MVG, breaks=max(dataTable$MVG),xlab=xlabel)
dev.off()

## Zoom in!
# Start the postscript graphics device driver.
plotFile=paste(zoomedOutputDirectory,"mvgHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
hist(dataTable$MVG[dataTable$MVG < quantile(dataTable$MVG,c(0.75))],xlim=range(0,quantile(dataTable$MVG,c(0.75))),xlab=xlabel,main="Histogram of 75% percentile")
dev.off()


##
# Draw histograms for the COM metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"comHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

xlabel="COM"
hist(dataTable$COM, breaks=max(dataTable$COM),xlab=xlabel)

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
hist(dataTable$FanOut, breaks=max(dataTable$FanOut),xlab=xlabel)

# Shutdown the postscript graphics device.
dev.off()

## Zoom in!
# Start the postscript graphics device driver.
plotFile=paste(zoomedOutputDirectory,"fanOutHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
hist(dataTable$FanOut[dataTable$FanOut < quantile(dataTable$FanOut,c(0.75))],xlim=range(0,quantile(dataTable$FanOut,c(0.75))),xlab=xlabel,main="Histogram of 75% percentile")
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
hist(dataTable$FanIn, breaks=max(dataTable$FanIn),xlab=xlabel)

# Shutdown the postscript graphics device.
dev.off()

## Zoom in!
# Start the postscript graphics device driver.
plotFile=paste(zoomedOutputDirectory,"fanInHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
hist(dataTable$FanIn[dataTable$FanIn < quantile(dataTable$FanIn,c(0.75))],xlim=range(0,quantile(dataTable$FanIn,c(0.75))),xlab=xlabel,main="Histogram of 75% percentile")
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
hist(dataTable$MPC, breaks=max(dataTable$MPC),xlab=xlabel)

# Shutdown the postscript graphics device.
dev.off()

## Zoom in!
# Start the postscript graphics device driver.
plotFile=paste(zoomedOutputDirectory,"mpcHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
hist(dataTable$MPC[dataTable$MPC < quantile(dataTable$MPC,c(0.75))],xlim=range(0,quantile(dataTable$MPC,c(0.75))),xlab=xlabel,main="Histogram of 75% percentile")
dev.off()


# Delete the objects
remove(dataTable)
remove(xdata)
remove(ydata)
remove(inputDirectory)
remove(outputDirectory)
remove(dataFileName)
remove(mainDirectory)
