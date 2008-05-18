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
dataFileNameBefore=paste(inputDirectory,"entityMetricsBefore.txt",sep="/")
dataFileNameAfter=paste(inputDirectory,"entityMetricsAfter.txt",sep="/")

# Read the data and store it in an object.
# The first line contains the column-headers.
beforeData <- read.table(dataFileNameBefore, header=TRUE)
afterData <- read.table(dataFileNameAfter, header=TRUE)

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
max_LOC=max(beforeData$LOC,afterData$LOC)
max_MVG=max(beforeData$MVG,afterData$MVG)
yrange <- range(0,max_MVG)
xrange <- range(0,max_LOC)

ydata=beforeData$MVG
xdata=beforeData$LOC
beforeColor="grey"
plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel, xlim=xrange, ylim=yrange, col=beforeColor)

# Draw a grid on top of the box-plot, with line-width 3.
grid(nx=NULL,ny=NULL,lwd=3)

# Do not clean the plotting frame before drawing.
# Ensures that the boxplot is drawn on top of the grid.
par(new=TRUE)

plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel, xlim=xrange, ylim=yrange, col=beforeColor)

beforeModel <- lm(ydata ~ xdata )
abline(beforeModel, col="grey")

##
# Plot the after data and regression line.
##

par(new=TRUE)

ydata=afterData$MVG
xdata=afterData$LOC
plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel, xlim=xrange, ylim=yrange)

afterModel <- lm(ydata ~ xdata )
abline(afterModel)

# Shutdown the postscript graphics device.
dev.off()

##
# Report on the quality of the model
##

# Divert R output to a file
outputFileName=paste(outputDirectory,"modelFitting.txt",sep="/")
sink(outputFileName)

summary(beforeData$LOC)
summary(beforeData$MVG)
summary(beforeModel)
summary(afterData$LOC)
summary(afterData$MVG)
summary(afterModel)

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
postscript(file= plotFile)

##
# Plot the before data and regression line.
##

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

ylabel="FanOut"
xlabel="FanIn"
max_FanOut=max(beforeData$FanOut,afterData$FanOut)
max_FanIn=max(beforeData$FanIn,afterData$FanIn)
yrange <- range(0,max_FanOut)
xrange <- range(0,max_FanIn)

ydata=beforeData$FanOut
xdata=beforeData$FanIn
beforeColor="grey"
plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel, xlim=xrange, ylim=yrange, col=beforeColor)

# Draw a grid on top of the box-plot, with line-width 3.
grid(nx=NULL,ny=NULL,lwd=3)

# Do not clean the plotting frame before drawing.
# Ensures that the boxplot is drawn on top of the grid.
par(new=TRUE)

plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel, xlim=xrange, ylim=yrange, col=beforeColor)

beforeModel <- lm(ydata ~ xdata )
abline(beforeModel, col="grey")

##
# Plot the after data and regression line.
##

par(new=TRUE)

ydata=afterData$FanOut
xdata=afterData$FanIn
plot(ydata ~ xdata, xlab=xlabel, ylab=ylabel, xlim=xrange, ylim=yrange)

afterModel <- lm(ydata ~ xdata )
abline(afterModel)

# Shutdown the postscript graphics device.
dev.off()

##
# Report on the quality of the model
##

# Divert R output to a file
outputFileName=paste(outputDirectory,"modelFittingFanOutIn.txt",sep="/")
sink(outputFileName)

summary(beforeData$FanIn)
summary(beforeData$FanOut)
summary(beforeModel)
summary(afterData$FanIn)
summary(afterData$FanOut)
summary(afterModel)

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
yrange <- range(0,max_LOC)
boxplot(afterData$LOC, beforeData$LOC,horizontal=TRUE,xlab="LOC",xlim=xrange,names=c("after","before"))
dev.off()

##
# Draw boxplots for the MVG metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"mvg.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
yrange <- range(0,max_MVG)
boxplot(afterData$MVG, beforeData$MVG,horizontal=TRUE,xlab="MVG",ylim=yrange,names=c("after","before"))
dev.off()

##
# Draw boxplots for the COM metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"com.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
max_COM=max(beforeData$COM,afterData$COM)
yrange <- range(0, max_COM)
boxplot(afterData$COM, beforeData$COM,horizontal=TRUE,xlab="COM",xlim=xrange,names=c("after","before"))
dev.off()

##
# Draw boxplots for the FanOut metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"fanOut.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
yrange <- range(0, max_FanOut)
boxplot(afterData$FanOut, beforeData$FanOut,horizontal=TRUE,xlab="FanOut",xlim=xrange,names=c("after","before"))
dev.off()

##
# Draw boxplots for the FanIn metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"fanIn.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
yrange <- range(0, max_FanIn)
boxplot(afterData$FanIn, beforeData$FanIn,horizontal=TRUE,xlab="FanIn",xlim=xrange,names=c("after","before"))
dev.off()

##
# Draw boxplots for the MPC metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"mpc.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)
max_MPC=max(beforeData$MPC,afterData$MPC)
yrange <- range(0, max_MPC)
boxplot(afterData$MPC, beforeData$MPC,horizontal=TRUE,xlab="MPC",xlim=xrange,names=c("after","before"))
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

par(mfrow=c(2,1))

xlabel="LOC"
xrange <- range(0,max_LOC)
xdata=beforeData$LOC
#yrange <- range(0,length(xdata))
#hist(xdata, breaks=max_LOC,xlab=xlabel,xlim=xrange,ylim=yrange,col=beforeColor,main="Before")
hist(xdata, breaks=max_LOC,xlab=xlabel,xlim=xrange,col=beforeColor,main="Before")

xdata=afterData$LOC
#yrange <- range(0,length(xdata))
#hist(xdata,breaks=max_LOC,xlab=xlabel,ylim=yrange,xlim=xrange,main="After")
hist(xdata,breaks=max_LOC,xlab=xlabel,xlim=xrange,main="After")

# Shutdown the postscript graphics device.
dev.off()

##
# Draw histograms for the MVG metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"mvgHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

par(mfrow=c(2,1))

xlabel="MVG"
xrange <- range(0,max_MVG)
xdata=beforeData$MVG
#yrange <- range(0,length(xdata))
#hist(xdata, breaks=max_MVG,xlab=xlabel,xlim=xrange,ylim=yrange,col=beforeColor,main="Before")
hist(xdata, breaks=max_MVG,xlab=xlabel,xlim=xrange,col=beforeColor,main="Before")

xdata=afterData$MVG
#yrange <- range(0,length(xdata))
#hist(xdata,breaks=max_MVG,xlab=xlabel,ylim=yrange,xlim=xrange,main="After")
hist(xdata,breaks=max_MVG,xlab=xlabel,xlim=xrange,main="After")

##
# Draw histograms for the COM metric.
##

# Start the postscript graphics device driver.
plotFile=paste(outputDirectory,"comHistogram.ps",sep="/")
postscript(file=plotFile)

# Ensure that text and symbols are scaled 150%
par(cex=1.5)

par(mfrow=c(2,1))

xlabel="COM"
xrange <- range(0,max_COM)
xdata=beforeData$COM
#yrange <- range(0,length(xdata))
#hist(xdata, breaks=max_COM,xlab=xlabel,xlim=xrange,ylim=yrange,col=beforeColor,main="Before")
hist(xdata, breaks=max_COM,xlab=xlabel,xlim=xrange,col=beforeColor,main="Before")

xdata=afterData$COM
#yrange <- range(0,length(xdata))
#hist(xdata,breaks=max_COM,xlab=xlabel,ylim=yrange,xlim=xrange,main="After")
hist(xdata,breaks=max_COM,xlab=xlabel,xlim=xrange,main="After")

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

par(mfrow=c(2,1))

xlabel="FanOut"
xrange <- range(0,max_FanOut)
xdata=beforeData$FanOut
#yrange <- range(0,length(xdata))
#hist(xdata, breaks=max_FanOut,xlab=xlabel,xlim=xrange,ylim=yrange,col=beforeColor,main="Before")
hist(xdata, breaks=max_FanOut,xlab=xlabel,xlim=xrange,col=beforeColor,main="Before")

xdata=afterData$FanOut
#yrange <- range(0,length(xdata))
#hist(xdata,breaks=max_FanOut,xlab=xlabel,ylim=yrange,xlim=xrange,main="After")
hist(xdata,breaks=max_FanOut,xlab=xlabel,xlim=xrange,main="After")

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

par(mfrow=c(2,1))

xlabel="FanIn"
xrange <- range(0,max_FanIn)
xdata=beforeData$FanIn
#yrange <- range(0,length(xdata))
#hist(xdata, breaks=max_FanIn,xlab=xlabel,xlim=xrange,ylim=yrange,col=beforeColor,main="Before")
hist(xdata, breaks=max_FanIn,xlab=xlabel,xlim=xrange,col=beforeColor,main="Before")

xdata=afterData$FanIn
#yrange <- range(0,length(xdata))
#hist(xdata,breaks=max_FanIn,xlab=xlabel,ylim=yrange,xlim=xrange,main="After")
hist(xdata,breaks=max_FanIn,xlab=xlabel,xlim=xrange,main="After")

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

par(mfrow=c(2,1))

xlabel="MPC"
xrange <- range(0,max_MPC)
xdata=beforeData$MPC
#yrange <- range(0,length(xdata))
#hist(xdata, breaks=max_MPC,xlab=xlabel,xlim=xrange,ylim=yrange,col=beforeColor,main="Before")
hist(xdata, breaks=max_MPC,xlab=xlabel,xlim=xrange,col=beforeColor,main="Before")

xdata=afterData$MPC
#yrange <- range(0,length(xdata))
#hist(xdata,breaks=max_MPC,xlab=xlabel,ylim=yrange,xlim=xrange,main="After")
hist(xdata,breaks=max_MPC,xlab=xlabel,xlim=xrange,main="After")

# Shutdown the postscript graphics device.
dev.off()

# Delete the objects
remove(beforeData)
remove(afterData)
remove(xdata)
remove(ydata)
remove(beforeColor)
remove(inputDirectory)
remove(outputDirectory)
remove(dataFileNameBefore)
remove(dataFileNameAfter)
remove(mainDirectory)
