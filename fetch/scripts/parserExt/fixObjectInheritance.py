#!/usr/bin/python
# This file is part of Tsmells
#
# Tsmells is free software; you can redistribute it and/or modify it 
# under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 2 of the License, or (at your 
# option) any later version.
#
# Tsmells is distributed in the hope that it will be useful, but WITHOUT 
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more 
# details.
#
# You should have received a copy of the GNU General Public License along 
# with outputtest; if not, write to the Free Software Foundation, Inc., 
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA 
#
# Copyright 2007 Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>
#

import sys
import glob

DUMPDIR=None

def readClasses():
    """ builds a set containing all classes, fully qualified """
    classFilename = glob.glob(DUMPDIR + "/*.classes")[0]
    classFile     = open(classFilename)
    classes = set()
    #TODO remove interfaces
    for cl in classFile:
        # use filepath + name for qualification
        classes.add((cl.split(";")[2], cl.split(';')[3].split('.')[0]))
    classFile.close()
    return classes

def readInheritances():
    """ builds a set containing all classes with an explicit superclass """
    #AddBuddy;JFrame;000039.030;gui/AddBuddy.java;39.36;0x4;{};{};{};{}
    inhFilename = glob.glob(DUMPDIR + "/*.inheritance")[0]
    inhFile     = open(inhFilename)
    inhs = set()
    for inh in inhFile:
        inhs.add((inh.split(";")[3], inh.split(';')[4].split('.')[0]))
    inhFile.close()
    return inhs

def writeObjectInheritances(toAdd):
    """ write Object inheritances to dump file"""
    strInh = "" # append this to .inheritance
    for cl in toAdd:
        if cl == "java.lang.Object":
            continue
        fname = cl[0]
        clname = fname.split('/')[-1].split('.')[0]
        loc = cl[1]
        strInh += clname + ";Object;000000.000;"\
                                    + fname + ";" + loc + ";0x4;{};{};{};{}\n"
    inhFilename = glob.glob(DUMPDIR + "/*.inheritance")[0]
    inhFile     = open(inhFilename, 'a')
    inhFile.write(strInh)
    inhFile.close()


if __name__=='__main__':
    """ find all classes which inherit directly from Object, add those to .inheritance"""
    if len(sys.argv) < 2:
        print "Provide dbdump directory"
        sys.exit(-1)
    DUMPDIR=sys.argv[1].rstrip("/")
    cls =  readClasses()
    ins =  readInheritances()
    #print "classes:"
    #print cls
    #print "inheritances:"
    #print ins
    #print "diference:"
    #print cls.difference(ins)
    writeObjectInheritances(cls.difference(ins))
