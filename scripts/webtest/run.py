#!/usr/bin/python

import sys
import os
from os import listdir
from os import path
from os import chdir
from os import remove
from glob import glob

from outputtest import execute
from outputtest import compare

def subdirs(dirname):
	return [f for f in listdir(dirname) if path.isdir(path.join(dirname,f))]

def readFile(file):
	f = open(file)
	content = f.read()
	f.close()
	return content

PASS  = 0
FAIL  = 1
ERROR = 2

class TestResult():
	def __init__(self):
		self.status   = PASS
		self.color    = "green"
		self.name     = ""
		self.cmd      = ""
		self.exp      = ""
		self.got      = ""
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
		result.append([src, text2html(open(src).read())])
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
		res.got, ret = execute(res.cmd)

		res.sources = grabSources()

		if not ret:
			res.status = ERROR
			res.color  = "black"
			res.what = "Failed to excute"
			
		else:
			res.exp  = readFile(name + '.exp')
			res.what = compare(res.got, res.exp)
	
			if res.what == "":
				res.color  = "green"
				res.status = PASS
			else:
				res.color  = "red"
				res.status = FAIL
		
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
	for smell in result:
		for test in smell[1] + smell[2]:
			total += 1
			if test.status == PASS:
				passed += 1
			elif test.status == FAIL:
				failed += 1
			elif test.status == ERROR:
				errored += 1

	return total, passed, failed, errored
	
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

if __name__ == '__main__':
	res = runTests("/home/nix/JaarProj/Smells/tests/", ["MysteryGuest", "AssertionRoulette", "AssertionLess"])
	for smell in res:
		print smell[0]
		print ">> cpp"
		for res in smell[1]:
			print "  >> " + res.name + "\t-> " + str(res.status)
		print "java"
		for res in smell[2]:
			print "  >> " + res.name + "\t-> " + str(res.status)
