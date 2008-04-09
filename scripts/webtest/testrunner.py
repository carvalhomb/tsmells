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

import sys, os, popen2, hashlib, signal, difflib, time

from os import listdir, path, chdir, remove
from glob import glob


def execute(command, timeout=10):
    """ Executes the 'command' in a shell. After timeout RuntimeError
        gets raised. stdout & stderr get returned, together with a boolean
        indicating success/failure"""

    # run command
    #process   = popen2.Popen4(command, True)
    process   = popen2.Popen3(command, True)
    sleepTime = 0.1
    current   = 0.0

    # wait for completion or timeout
    while (process.poll() == -1) and (current < timeout):
        current = current + sleepTime
        time.sleep(sleepTime)

    # throw if we got a timeout
    if current >= timeout:
        os.kill(process.pid, signal.SIGTERM)
        raise RuntimeError(command + " is taking too long,"\
                +  " raise timeout?")

    # check if if command was successful
    success = True
    if process.poll() != 0:
        success = False

    return "".join(process.fromchild.readlines()), \
           "".join(process.childerr.readlines()), success


def compare(output, expected):
    """ Compares the string in output with expected 
        returns the empty string if equal, a unified diff otherwise """

    outputHash   = hashlib.md5(output).hexdigest()
    expectedHash = hashlib.md5(expected).hexdigest()
    if outputHash == expectedHash:
        # equal, return empty
        return ""
    else:
        return get_diff(expected, output)


def get_diff(a, b):
    aLines = [ l.rstrip('\t') for l in a.splitlines()]
    bLines = [ l.rstrip('\t') for l in b.splitlines()]
    gen = difflib.unified_diff(aLines,bLines)
    diff_output = list(gen)
    if len(diff_output) != 0:
        return "\n".join(diff_output)
    else:
        return ""

def subdirs(dirname):
    dirs = [f for f in listdir(dirname) if path.isdir(path.join(dirname,f))]
    dirs.sort()
    return dirs

def readFile(file):
    f = open(file)
    content = f.read()
    f.close()
    return content

PASS  = 0
FAIL  = 1
ERROR = 2
TDD   = 3

class TestResult():
    def __init__(self):
        self.status   = PASS
        self.color    = "green"
        self.name     = ""
        self.cmd      = ""
        self.exp      = ""
        self.got      = ""
        self.err      = ""
        self.what     = ""
        self.sources  = []

def grabSources():
    """ Finds all sources and puts their conent in a list"""
    result = []
    chdir('src')
    srcFiles = glob("*.java") + glob("*/*.java") + glob("*/*/*.java") \
                + glob("*.cpp") + glob("*.h") + glob("*.cxx")
    for src in srcFiles:
        if src.endswith("TestCase.java"):
            continue
        elif -1 != src.find("file/"):
            continue
        html = text2html(open(src).read())
        linesAdded = ""
        cntr = 1;
        for i in html.split('<br>'):
            linesAdded += str(cntr) + ". " + i + "<br>"
            cntr += 1
        result.append([src, linesAdded])
    chdir('..')
    return result

def text2html(ascii):
    """convert spaces, tabs, newlines etc """
    html = ""
    for i in ascii:
        if i in ['\n', '\r']:
            i = "<br>"
        elif i in ['\t']:
            i = "&nbsp;&nbsp;&nbsp;&nbsp;"
        elif i in [' ']:
            i = "&nbsp;"
        elif i == '<':
            i = "&lt;"
        elif i == '>':
            i = "&gt;"
        html += i
    return html

def evalTest(name):
    """ runs a single test
        name should contain the testname, ie the
        filename of the *.exp, *.cmd etc """

    res = TestResult()
    res.name = name

    try:
        res.cmd = readFile(name + '.cmd')
        res.got, res.err, ret = execute(res.cmd)
        res.err = text2html(res.err)

        #linesAdded = ""
        #cntr = 1;
        #for i in res.got.split('\n'):
        #    linesAdded += str(cntr) + ". " + i + "\n"
        #    cntr += 1
        #res.got = linesAdded

        res.sources = grabSources()

        if not ret:
            res.status = ERROR
            res.color  = "black"
            res.what = "Failed to excute\n" +\
                        res.got

        else:
            res.exp  = readFile(name + '.exp')
            res.what = compare(res.got, res.exp)

            if res.what == "" or len(res.what) == 0:
                # success
                res.color  = "green"
                res.status = PASS
            elif os.access(name + '.tdd', os.F_OK):
                # todo test
                res.color = "#151B54" # some dark blue
                res.status = TDD
            else:
                res.color  = "red"
                res.status = FAIL
                f = open(name + '.res', 'w')
                f.write(res.got)
                f.close()

    except Exception, e:
        res.status = ERROR
        res.color  = "black"
        res.what = "Error: " + str(e)

    res.got  = text2html(res.got)
    res.what = text2html(res.what)
    res.exp  = text2html(res.exp)
    return res

def testStats(result):
    total   = 0
    passed  = 0
    failed  = 0
    errored = 0
    tdd     = 0
    for smell in result:
        for test in smell[1] + smell[2]:
            total += 1
            if test.status == PASS:
                passed += 1
            elif test.status == FAIL:
                failed += 1
            elif test.status == ERROR:
                errored += 1
            elif test.status == TDD:
                tdd += 1

    return total, passed, failed, errored, tdd

def runTests(root, smells):
    #root -> (absolute) directory to look for smells
    #smells -> tests to run

    current = os.getcwd()
    chdir(root)
    results = [] # contains triples. smell name, cpp-results, java-results
    for smell in smells:
        java = []
        cpp  = []
        chdir(smell + "/cpp")
        for test in subdirs('.'):
            if test == '.svn': continue
            chdir(test)
            cpp.append(evalTest(test))
            chdir('..')
        chdir("../java")
        for test in subdirs('.'):
            if test == '.svn': continue
            chdir(test)
            java.append(evalTest(test))
            chdir("..")
        chdir("../..")
        results.append([smell, cpp, java])
    chdir(current)
    return results, testStats(results)

