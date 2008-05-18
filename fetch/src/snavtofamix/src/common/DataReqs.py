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
# Copyright 2007  University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

import os

from log4py import Logger

###############################################################################
# check functions
###############################################################################

log = Logger().get_instance()

def ensureFileExists(fileName):
    if not os.path.exists(fileName):
        log.info("database ", fileName," does not exist")
        #sys.exit(2)
        theFile = open(fileName, 'w')
        theFile.close()

def checkFileExists(fileName):
    if not os.path.exists(fileName):
        log.info("database ", fileName," does not exist")
        return False
    else:
		return True

def checkfamixL1Reqs(DbLoc):
    """ checks information requirements for Famix level 1"""
    ensureFileExists(DbLoc + ".classes")
    ensureFileExists(DbLoc + ".inheritance")
    ensureFileExists(DbLoc + ".methods")
    ensureFileExists(DbLoc + ".methoddefs")
    ensureFileExists(DbLoc + ".functions")
    ensureFileExists(DbLoc + ".functiondefs")

def checkfamixL2Reqs(DbLoc):
    """ checks information requirements for Famix level 2"""
    ensureFileExists(DbLoc + ".attributes")
    ensureFileExists(DbLoc + ".globalvar")
    ensureFileExists(DbLoc + ".constants")

def checkfamixL3Reqs(DbLoc):
    """ checks information requirements for Famix level 3"""
    ensureFileExists(DbLoc + ".references")

def checkfamixL4Reqs(DbLoc):
    """ checks information requirements for Famix level 4"""
    ensureFileExists(DbLoc + ".localvars")

def checkfamixCppReqs(DbLoc):
    """ checks information requirements for C++ specific model entities"""
    ensureFileExists(DbLoc + ".files")
    ensureFileExists(DbLoc + ".typedef")
    ensureFileExists(DbLoc + ".includes")
    ensureFileExists(DbLoc + ".includes2")

def checkfamixPPReqs(DbLoc):
    """ checks information requirements for preprocessor specifics"""
    ensureFileExists(DbLoc + ".condcomp")

def checkPmcmetricReqsSatisfied(DbLoc):
    """ checks information requirements for pmccabe metrics"""
    ensureFileExists(DbLoc + ".pmcmetrics")

def checkJavaNCSSReqsSatisfied(DbLoc):
    """ checks information requirements for Java NCSS metrics"""
    return checkFileExists(DbLoc + ".ncssmetrics")

def checkVCSMetricsReqsSatisfied(DbLoc):
	""" checks information requirements for VCS metrics"""
	return checkFileExists(DbLoc + ".vcs")

def checkCtrlStructReqsSatisfied(DbLoc):
    """ check information requirements for control structure metrics """
    return checkFileExists(DbLoc + ".ctrlstruct")

def checkConditionalsReqsSatisfied(DbLoc):
    """ check information requirements for conditionals metrics"""
    return checkFileExists(DbLoc + ".conditionals")

def checkLoopsReqsSatisfied(DbLoc):
    """ check information requirements for loops metrics"""
    return checkFileExists(DbLoc + ".loops")
