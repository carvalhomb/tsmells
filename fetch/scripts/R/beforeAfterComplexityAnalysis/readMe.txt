Author: Bart Du Bois
      	Lab On REengineering
      	Dept. of Mathematics-Computer Science
      	University of Antwerp
      	bart.dubois@ua.ac.be
      	
This directory presents a R script for comparing metrics on two versions of a software system, 
typically before and after refactoring. The contents of this directory is structured as follows:
	+input
		Contains two tab separated data files (entityMetricsBefore.txt and entityMetricsAfter.txt). 
		Each line in these files contains the values for a number of complexity and coupling metrics 
		(LOC,MVG,COM,FanIn,FanOut,MPC) for a method or function having a unique entityId.
	
	+output
		Directory where plots and reports created by the R script will be stored.
	
	+scripts
		Contains a single R script named entityAnalysis.R

To run the script, launch the following command in the ./scripts directory:
	R --no-save < entityAnalysis.R

This command launches R (the R project for statistical computing, http://www.r-project.org/) given
the script entityAnalysis.R as input. The script takes no arguments as input.

When the script completed, you should notice the following files in the ./output directory:
	someMetric.ps -  - boxplots of the someMetric values before and after
	someMetricHistogram.ps -  - histogram of the someMetric values before and after
		e.g.:
			for LOC:	loc.ps, locHistogram.ps
			for MVG:	mvg.ps, mvgHistogram.ps
			for COM:	com.ps, comHistogram.ps
			for FanIn:	fanIn.ps, fanInHistogram.ps
			for FanOut:	fanOut.ps, fanOutHistogram.ps
			for MPC:	mpc.ps, mpcHistogram.ps

	modelFitting.ps - Scatterplot and regression line of MVG in function of LOC before (grey) and after (black)
	modelFitting.txt - Descriptive statistics of LOC and MVG before and after refactoring, and a summary
	                   of the linear model fitted between MVG and LOC.
	modelFittingFanOutIn.ps - Scatterplot and regression line of FanOut in function of FanIn before (grey) and after (black)
	modelFittingFanOutIn.txt - Descriptive statistics of FanIn and FanOut before and after refactoring, and a summary
	                           of the linear model fitted between FanOut and FanIn.
