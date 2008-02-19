#!/usr/bin/python
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
# Copyright 2007 Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>
#

import sys, os, copy, tempfile

# some default xUnit RML initialization scripts
XINIT_DEFAULTS = \
    {
        "junit3"   :"src/initJavaTestEntities.rml",
        "junitana"    :"src/initJavaTestEntitiesAnastacia.rml",
        "cppunit12":"src/initCppUnitv1.12.rml",
        "cppunit06":"src/initCppUnitTeamspirit.rml",
        "cppunitpt":"src/initCppTestEntities.rml",
    }

# all m4-smell keys
SMELLS = \
    [
        "ASSERTIONLESS",
        "ASSERTION_ROULETTE",
        "DUPLICATED_CODE",
        "FOR_TESTERS_ONLY"
        "INDIRECT_TEST",
        "INDENTED_TEST",
        "MYSTERY_GUEST",
        "SENSITIVE_EQUALITY",
    ]

# additional smell parameters passed to m4
PARAMS_DEFAULTS = \
    {
        "ASSERTION_ROULETTE_TRESHOLD":"3",
        "DUPLICATED_CODE_PYGEN":"src/DuplicatedCode.py",
        "DUPLICATED_CODE_TRESHOLD":"8",
        "INDIRECT_TEST_TRESHOLD":"5",
        "MYSTERY_GUEST_BLACKLIST": 
        {
            "junit3":"src/MysteryBlacklistJava.rml",
            "junitana" :"src/MysteryBlacklistJava.rml",
            "cppunit12":"src/MysteryBlacklistCppFile.rml",
            "cppunit06":"src/MysteryBlacklistCppFile.rml",
            "cppunitpt":"src/MysteryBlacklistCppFile.rml"
        }
    }

def usage():
    scr = sys.argv[0].split('/')[-1]
    print "usage: " + scr + " <project> <xinit>"
    print "       " + scr + " <config>"
    print ""
    print "  xinit:   xUnit framework initialization RML file path"
    print "           or a predefined one: ",
    print XINIT_DEFAULTS.keys()
    print "  project: project source root directory"
    print "  config:  configuration file"
    print ""
    print "TSMELLS variable should point to your tsmells installation root"
    sys.exit(1)

def buildM4Input(xinit, smells, params, tsmellsRoot):
    ''' construct the input passed to m4

        xinit: xUnit initialization RML
        smells: smell keys
        params: aditional m4 input'''

    m4Input = " -DTSMELLS=" + tsmellsRoot + " -DXUNIT_INIT=" + xinit 
    for smell in smells:
        m4Input += " -D" + smell
    for k,v in params.iteritems():
        m4Input += " -D" + k + "=" + v

    return m4Input

def fillDefault(xinit):
    ''' fill rootdir, rsf, smells & params '''

    if not XINIT_DEFAULTS.has_key(xinit): usage()
    tsmellsRoot = os.getenv("TSMELLS")
    if not tsmellsRoot: usage()
    xinitFile = os.path.join(tsmellsRoot, XINIT_DEFAULTS[xinit])

    # default to all smells
    smells = copy.copy(SMELLS)
    params = copy.copy(PARAMS_DEFAULTS)

    params["MYSTERY_GUEST_BLACKLIST"] = \
        os.path.join(tsmellsRoot,params["MYSTERY_GUEST_BLACKLIST"][xinit])

    params["DUPLICATED_CODE_PYGEN"] = \
        os.path.join(tsmellsRoot, params["DUPLICATED_CODE_PYGEN"])

    m4Def = os.path.join(tsmellsRoot, "scripts/tsmells.m4")
    if not os.path.exists(m4Def):
        print "m4 macro script does not exist [" + m4Def + "]"

    return tsmellsRoot, xinitFile, smells, params, m4Def

def checkFilesExist(toCheck):
    for file in toCheck:
        if not os.path.exists(file[0]):
            print file[1] + " does not exist [" + file[0] + "]"
            print ""
            usage()

if __name__ == '__main__':
    tsmellsRoot = ""
    rsf = ""
    smells = [] # fill this, gets passed to m4 script
    params = {} # this too, { name x value }

    if len(sys.argv) == 1 or len(sys.argv) > 3:
        usage()
    elif len(sys.argv) == 2:
        configPath = sys.argv[1]
        pass; print "config TODO"; exit(2)
    elif len(sys.argv) == 3:
        xinit = sys.argv[2]
        rsf = sys.argv[1]
        if not os.path.exists(rsf):
            print "rsf does not exist [" + rsf + "]"
            usage()

        proj = rsf.split('.rsf')[0]
        tsmellsRoot, xinit, smells, params, m4Def = fillDefault(xinit)

    checkFilesExist([(rsf, "rsf"), (xinit, "xUnit RML"), (m4Def, "m4 script"),
                     (params["DUPLICATED_CODE_PYGEN"], "duplicated code python source "),
                     (params["MYSTERY_GUEST_BLACKLIST"], "mystery guest blacklist")])

    m4Input = buildM4Input(xinit, smells, params, tsmellsRoot)
    tmpFile = tempfile.mkstemp()[1]
    os.system("m4 " + m4Input + " " + m4Def + " > " + tmpFile)
    os.system("cp " + tmpFile + " " + proj + ".rml")
    os.system(os.path.join(tsmellsRoot,"scripts","rsf2tsmell.sh") +\
              " " + rsf + " " + tmpFile + " " + " " + proj)
    os.remove(tmpFile)
