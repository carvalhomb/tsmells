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
        "cppunit1-12":"src/initCppUnitv1-12.rml",
        "cppunit0-06":"src/initCppUnitv0-06.rml",
        "cppunitpoco":"src/initCppUnitPoco.rml"
    }

AS    = "ASSERTIONLESS"
AR    = "ASSERTION_ROULETTE"
DC    = "DUPLICATED_CODE"
FTO   = "FOR_TESTERS_ONLY"
INDIR = "INDIRECT_TEST"
INDEN = "INDENTED_TEST"
MG    = "MYSTERY_GUEST"
SE    = "SENSITIVE_EQUALITY"

# all m4-smell keys
SMELLS = [ AS,AR,DC,FTO,INDIR,INDEN,MG,SE ]

TRESH = "_TRESHOLD"
AR_TRESH = AR + TRESH
DC_TRESH = DC + TRESH
DC_SCRIPT = DC + "_PYGEN"
INDIR_TRESH = INDIR + TRESH
MG_BLACK = MG + "_BLACKLIST"


# additional smell parameters passed to m4
PARAMS_DEFAULTS = \
    {
        AR_TRESH    :"3",
        DC_SCRIPT   :"src/DuplicatedCode.py",
        DC_TRESH    :"8",
        INDIR_TRESH :"4",
        MG_BLACK    : 
        {
            "junit3":"src/MysteryBlacklistJava.rml",
            "junitana" :"src/MysteryBlacklistJava.rml",
            "cppunit1-12":"src/MysteryBlacklistCppFile.rml",
            "cppunit0-06":"src/MysteryBlacklistCppFile.rml",
            "cppunitpoco":"src/MysteryBlacklistCppFile.rml"
        }
    }

def usage():
    scr = sys.argv[0].split('/')[-1]
    print "usage: " + scr + " <rsf> <xinit> [<mem>]"
    print "       " + scr + " <config>"
    print ""
    print "  xinit:   xUnit framework initialization RML file path"
    print "           or a predefined one: ",
    print XINIT_DEFAULTS.keys()
    print "  rsf:     source RSF model"
    print "  config:  configuration file"
    print "  mem:     # megabytes crocopat may use"
    print ""
    print "TSMELLS variable should point to your tsmells installation root"
    sys.exit(1)

def buildM4Input(xinit, smells, params, tsmellsRoot):
    ''' construct the input passed to m4

        xinit: xUnit initialization RML
        smells: smell keys
        params: aditional m4 input'''

    m4Input = " -DTSMELLS=" + tsmellsRoot + " -DXUNIT_INIT=" + xinit \
              + " -DDUMP_TEST_ENTITIES"
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

    # default to all smells
    smells = copy.copy(SMELLS)
    params = copy.copy(PARAMS_DEFAULTS)

    xinitFile =         \
        os.path.join(tsmellsRoot, XINIT_DEFAULTS[xinit])
    params[MG_BLACK] =  \
        os.path.join(tsmellsRoot,params[MG_BLACK][xinit])
    params[DC_SCRIPT] = \
        os.path.join(tsmellsRoot, params[DC_SCRIPT])
    m4Def =             \
        os.path.join(tsmellsRoot, "scripts/tsmells.m4")

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

    if len(sys.argv) == 1 or len(sys.argv) > 4:
        usage()
    elif len(sys.argv) == 2:
        configPath = sys.argv[1]
        pass; print "config TODO"; usage()
    elif len(sys.argv) in [ 3, 4]:
        xinit = sys.argv[2]
        rsf   = sys.argv[1]
        proj  = rsf.split('.rsf')[0]
        tsmellsRoot, xinit, smells, params, m4Def = fillDefault(xinit)
        mem = "50"
        if len(sys.argv) == 4:
            mem = str(int(sys.argv[3]))

    checkFilesExist([(rsf, "rsf"), (xinit, "xUnit RML"), (m4Def, "m4 script"),
                     (params[DC_SCRIPT], "duplicated code python source "),
                     (params[MG_BLACK], "mystery guest blacklist")])

    m4Input = buildM4Input(xinit, smells, params, tsmellsRoot)
    tmpFile = tempfile.mkstemp()[1]

    # construct the RML with m4
    os.system("m4 " + m4Input + " " + m4Def + " > " + tmpFile)

    sh     = os.path.join(tsmellsRoot,"scripts","rsf2tsmell.sh")
    params = " " + rsf + " " + tmpFile + " " + " " + proj + " " + mem
    # call rsf2tsmell.sh, which will call crocopat
    os.system( sh + params)

    os.remove(tmpFile)
