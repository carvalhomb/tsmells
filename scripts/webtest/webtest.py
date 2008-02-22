#!/usr/bin/python

from os                 import environ
from run                import runTests
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
                            "IndirectTest", "DuplicatedCode" ]
        self.smellSuites.sort()
        self.otherSuites = ["TestEntities", "AddObject", "RmComments", "GenCtrlStructInfo"]
        self.suites  = self.all + self.smellSuites + self.otherSuites

    @expose
    def index(self, go=None, suite=None):
        page = get_page("index")
        page.suites = self.suites;
        if go:
            root = environ["TSMELLS"] + "/tests"
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
