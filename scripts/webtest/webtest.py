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

from os                 import environ
from testrunner         import runTests
from time               import time
from cherrypy           import expose,\
                               quickstart
from Cheetah.Template   import Template

def get_page(name):
    '''Get a page template'''
    return Template(file="%s.tmpl" % name)

class Root():

    def __init__(self):
        self.results = None
        self.all = ["All"]
        self.smellSuites = ["MysteryGuest", "AssertionRoulette", "AssertionLess", \
                            "SensitiveEquality", "IndentedTest", "ForTestersOnly",\
                            "IndirectTest", "DuplicatedCode", "GeneralFixture", \
                            "EagerTest", "VerboseTest", "EmptyTest" ]
        self.smellSuites.sort()
        self.otherSuites = ["TestEntities", "AddObject", "RmComments", "GenCtrlStructInfo"]
        self.suites  = self.all + self.smellSuites + self.otherSuites

    @expose
    def index(self, go=None, suite=None):
        page = get_page("index")
        page.suites = self.suites;
        if not go:
            return str(page)

        root = environ["TSMELLS"] + "/tests/Pieces"
        if suite == 'All':
            toRun = self.smellSuites
        else:
            toRun = [suite]
        before = time()
        self.results, page.stats = runTests(root, toRun)
        after = time()
        page.secElapsed = int(after - before)
        page.usecElapsed = int( ((after - before) - page.secElapsed) * 1000)
        page.results = self.results

        return str(page)

    @expose
    def default(self, testname):
        page = get_page("result")
        result = None
        for i in self.results:
            for j in i[1]:
                if j.name + "cpp" == testname:
                    result = j
                    break
            for j in i[2]:
                if j.name + "java" == testname:
                    result = j
                    break

            if result != None:
                break
        page.result = result
        return str(page)


if __name__=='__main__':
    quickstart(Root())
