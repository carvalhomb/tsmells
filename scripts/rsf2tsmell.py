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
        "junit-3.x"   :"src/dump/rml/initJavaTestEntities.rml",
        "junit-3.x_ana"    :"src/dump/rml/initJunit-3.x_Ana.rml",
        "junit-3.x_pmd-3.5" : "src/dump/rml/initJunit-3.x_Pmd-3.5.rml",
        "junit-3.8_nostub" : "src/dump/rml/initJunit-3.8_nostub.rml",
        "cppunit-1.12":"src/dump/rml/initCppUnitv1-12.rml",
        "cppunit-0.06":"src/dump/rml/initCppUnitv0-06.rml",
        "cppunit_poco":"src/dump/rml/initCppUnitPoco.rml",
        "qttest":"src/dump/rml/initQtUnitEntities.rml"
    }

AS    = "ASSERTIONLESS"
AR    = "ASSERTION_ROULETTE"
DC    = "DUPLICATED_CODE"
FTO   = "FOR_TESTERS_ONLY"
GF    = "GENERAL_FIXTURE"
INDIR = "INDIRECT_TEST"
INDEN = "INDENTED_TEST"
MG    = "MYSTERY_GUEST"
SE    = "SENSITIVE_EQUALITY"
ET    = "EAGER_TEST"
VT    = "VERBOSE_TEST"
EMP   = "EMPTY_TEST"

# all m4-smell keys
SMELLS = [ AS,AR,DC,FTO,INDIR,INDEN,MG,SE,GF,ET,VT,EMP ]

TRESH = "_TRESHOLD"
AR_TRESH = AR + TRESH
DC_TRESH = DC + TRESH
DC_SCRIPT = DC + "_PYGEN"
INDIR_TRESH = INDIR + TRESH
MG_BLACK = MG + "_BLACKLIST"
GF_NFOB = GF + "_NFOB" + TRESH
GF_NFPT = GF + "_NFPT" + TRESH
GF_NOBU = GF + "_NOBU" + TRESH
ET_PTMI = ET + "_PTMI" + TRESH
VT_LOC  = VT + "_LOC"  + TRESH

MG_JAVA_BLACKLIST = "src/dump/rml/provideMysteryBlacklistJava.rml"
MG_CPP_BLACKLIST = "src/dump/rml/provideMysteryBlacklistCppFile.rml"

# additional smell parameters passed to m4
PARAMS_DEFAULTS = \
    {
        AR_TRESH    :"5",
        DC_SCRIPT   :"src/dump/DuplicatedCode.py",
        DC_TRESH    :"6",
        INDIR_TRESH :"4",
        MG_BLACK    : 
        {
            "junit-3.x": MG_JAVA_BLACKLIST,
            "junit-3.x_ana" : MG_JAVA_BLACKLIST,
            "junit-3.x_pmd-3.5" : MG_JAVA_BLACKLIST,
            "junit-3.8_nostub" : MG_JAVA_BLACKLIST,
            "cppunit-1.12": MG_CPP_BLACKLIST,
            "cppunit-0.06": MG_CPP_BLACKLIST,
            "cppunit_poco": MG_CPP_BLACKLIST,
            "qttest" : MG_CPP_BLACKLIST
        },
        GF_NFOB : "5",
        GF_NFPT : "5",
        GF_NOBU : "5",
        ET_PTMI : "5",
        VT_LOC  : "15"
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

def buildM4Input(xinit, smells, params, tsmellsRoot, proj):
    ''' construct the input passed to m4

        xinit: xUnit initialization RML
        smells: smell keys
        params: aditional m4 input'''

    m4Input = " -DTSMELLS=" + tsmellsRoot + " -DXUNIT_INIT=" + xinit \
              + " -DDUMP_TEST_ENTITIES=tsmells/" + proj + ".testsuite"
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

    xinitFile = os.path.join(tsmellsRoot, XINIT_DEFAULTS[xinit])
    params[MG_BLACK] = os.path.join(tsmellsRoot,params[MG_BLACK][xinit])
    params[DC_SCRIPT] = os.path.join(tsmellsRoot, params[DC_SCRIPT])
    m4Def = os.path.join(tsmellsRoot, "scripts/tsmells.m4")

    return tsmellsRoot, xinitFile, smells, params, m4Def

def checkFilesExist(toCheck):
    for file in toCheck:
        if not os.path.exists(file[0]):
            print file[1] + " does not exist [" + file[0] + "]"
            print ""
            usage()

def removeHeaders(rmlFile):
    rml = open(rmlFile, 'r')
    new = open(rmlFile + "_new", 'w')
    for line in rml:
       if not (line.startswith(' *') or line.startswith('/*')):
           new.write(line)
    rml.close()
    new.close()
    os.system("mv " + rmlFile + "_new " + rmlFile)

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

    m4Input = buildM4Input(xinit, smells, params, tsmellsRoot, proj)
    rmlFile = "tsmells/" + proj + ".rml"
    try: os.mkdir("tsmells")
    except: pass
    # construct the RML with m4
    os.system("m4 " + m4Input + " " + m4Def + " > " + rmlFile)
    # removeHeaders(rmlFile)

    os.system("date +%s > _now_tmp")
    sh     = os.path.join(tsmellsRoot,"scripts","rsf2tsmell.sh")
    params = " " + rsf + " " + rmlFile + " " + " " + proj + " " + mem
    # call rsf2tsmell.sh, which will call crocopat
    os.system( sh + params)
