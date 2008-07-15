#
# This file is part of TSmells
#
# TSmells is free software; you can redistribute it and/or modify it 
# under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 2 of the License, or (at your 
# option) any later version.
#
# TSmells is distributed in the hope that it will be useful, but WITHOUT 
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more 
# details.
#
# You should have received a copy of the GNU General Public License along 
# with TSmells; if not, write to the Free Software Foundation, Inc., 
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA 
#
# Copyright 2007-2008 Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>
#

import os
from time import sleep

def exeExists(exe):
    found = 0
    for dir in os.getenv("PATH").split(':'):
        if os.path.exists(os.path.join(dir, exe)):
            found = 1
            break
    return found

def openEditor(file, line):
    ''' spawn an external source code viewer and open the given file + jump
        to line '''
    toExec = ""
    if exeExists("kwrite"):
        toExec = "kwrite " + file + " --line " + str(line)
    elif exeExists("gvim"):
        toExec = "gvim +" + str(line) + " " + file
    elif exeExists("gedit"):
        toExec = "gedit +" + str(line) + " " + file
    elif os.name == "mac":
        toExec = "open " + file
    if toExec == "":
        print "No decent editor found. Failed to open source."
        return
    print "Executing `" + toExec + "'"
    Runtime.getRuntime().exec(toExec)

def getLocations(target):
    global srcDict
    locations = []
    if not srcDict.has_key(target.name): 
        print "No sourcelocation for " + target
    else: locations = srcDict[target.name]
    return locations

def toSourceAction(targetNode):
    ''' callback for the context menu '''
    global rootDir
    # open an editor for all the source locations related to targetNode
    cnt = 0
    for location in getLocations(targetNode[0]):
        if cnt == 10:
            # dont open too much editors at once, stuff might crash
            sleep(0.5)
            cnt = 0
        cnt += 1
        openEditor(rootDir + location[0], location[1])

def dumpDupli(targetNode):
    global rootDir
    for location in getLocations(targetNode[0]):
        print "---------------------------------------------------------------->>"
        print location[0] + ":[" + str(location[1]) + "-" + str(location[2]) + "]"
        print "---------------------------------------------------------------->>"
        f = open(rootDir + location[0], 'r')
        lines = f.readlines()
        for line in lines[location[1]:location[2]]:
            print line,
        f.close()

def addToSourceAction():
    ''' add the toSource context action to the guess graph'''
    toSrcA = NodeEditorPopup.addItem("toSource")
    toSrcA.menuEvent = toSourceAction

def addDumpDupliAction():
    dumpDupliA = NodeEditorPopup.addItem("dumpDupli")
    dumpDupliA.menuEvent = dumpDupli
