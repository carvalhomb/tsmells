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
# Copyright 2007-2008 Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>
#

import os

from testrunner         import *
from time               import time

SMELL_SUITES = ["MysteryGuest", "AssertionRoulette", "AssertionLess", \
                "SensitiveEquality", "IndentedTest", "ForTestersOnly",\
                "IndirectTest", "DuplicatedCode", "GeneralFixture", \
                "EagerTest" ]
OTHER_SUITES = ["TestEntities", "AddObject", "RmComments",\
                "GenCtrlStructInfo"]

def print_(result):
    print "\t" + result.name + "\t" + str(result.status)

def printStats(stats):
    print " pass: "  + str(stats[1]) + "/" + str(stats[0]) + \
          " fail: "  + str(stats[2]) + \
          " error: " + str(stats[3]) + \
          " tdd: "   + str(stats[4])

def runSuite():
    results = []
    for test in subdirs('.'):
        if test == '.svn': continue
        chdir(test)
        result = evalTest(test)
        print_(result)
        results.append(result)
        chdir('..')
    return results

def runTests(root, smells):
    current = os.getcwd()
    chdir(root)
    results = [] # contains triples. smell name, cpp-results, java-results
    for smell in smells:
        print smell
        java = []
        cpp  = []
        print "cpp"
        chdir(smell + "/cpp")
        cpp.extend(runSuite())
        print "java"
        chdir("../java")
        java.extend(runSuite())
        results.append([smell, cpp, java])
        chdir('../..')
    chdir(current)
    printStats(testStats(results))

if __name__ == '__main__':
    root = os.environ['TSMELLS'] + "/tests/Pieces"
    runTests(root, SMELL_SUITES)



#class Root():

    #def __init__(self):
        #self.results = None
        #self.all = ["All"]
        #self.smellSuites = 
        #self.smellSuites.sort()
        #self.suites  = self.all + self.smellSuites + self.otherSuites

    #@expose
    #def index(self, go=None, suite=None):
        #page = get_page("index")
        #page.suites = self.suites;
        #if not go:
            #return str(page)

        #root = environ["TSMELLS"] + "/tests/Pieces"
        #if suite == 'All':
            #toRun = self.smellSuites
        #else:

        #return str(page)

    #@expose
    #def default(self, testname):
        #page = get_page("result")
        #result = None
        #for i in self.results:
            #for j in i[1]:
                #if j.name + "cpp" == testname:
                    #result = j
                    #break
            #for j in i[2]:
                #if j.name + "java" == testname:
                    #result = j
                    #break

            #if result != None:
                #break
        #page.result = result
        #return str(page)

### Index page

### Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>

##extends master

### Default attributes
##attr $results = None
##attr $stats = None
##attr $color = None
##attr $suites = None
##attr $secElapsed = None
##attr $usecElapsed = None


##if (not $results)
#Tests not run yet
##end if

##if $results
	##if $stats[0] == $stats[1] + $stats[4]
		##set $color = "green"
	##else
		##set $color = "red"
	##end if

#<table align="center" border="1"><tr><td width=400 height=20 bgcolor="$color"></td></tr></table><br>
#<center>Time elapsed: ${secElapsed}s ${usecElapsed}  </center>
#<center>Total: $stats[0], pass: $stats[1], fail: $stats[2], error: $stats[3], tdd: $stats[4]</center>
#<table align="center" width="400" border="1">
	### Header
	#<tr>
		#<th>test</th>
		#<th width="5">result</th>
	#</tr>
	##for $smell in $results
	#<tr>
		#<td align="center" colspan="2"><b>$smell[0]</b> cpp</td>
    #</tr>
	##for $test  in $smell[1]
	#<tr>
		#<td>+ <a href="${test.name}cpp">$test.name</a></td>
		#<td width="5" bgcolor="$test.color"></td>
    #</tr>
	##end for
	#<tr>
		#<td align="center" colspan="2"><b>$smell[0]</b> java</td>
	#</tr>
	##for $test  in $smell[2]
	#<tr>
		#<td>+ <a href="${test.name}java">$test.name</a></td>
		#<td width="5" bgcolor="$test.color"></td>
    #</tr>
	##end for
    ##end for
#</table>

##end if

##end block body
