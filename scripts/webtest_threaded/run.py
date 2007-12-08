#!/usr/bin/python

import sys
import os
import threading
import Queue
from os import listdir
from os import path
from os import chdir
from os import remove
from glob import glob
from time import sleep
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

def grabSources(path):
	""" Finds all sources and puts their conent in a list"""
	result = []
	srcFiles = 	glob(path + "/src/*.java") + glob(path + "/src/*/*.java") + \
				glob(path + "/src/*/*/*.java") + glob(path + "/src/*.cpp") + \
				glob(path + "/src/*.h")
	for src in srcFiles:
		if src.endswith("TestCase.java"):
			continue
		elif -1 != src.find("file/"):
			continue
		result.append([src, text2html(open(src).read())])
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
		
def evalTest(path):
	""" runs a single test
		name should contain the testname, ie the
		filename of the *.exp, *.cmd etc """
	
	res = TestResult()
	res.name = os.path.basename(path)
	fp = os.path.join(path, res.name)
	
	try:
		res.cmd = readFile(fp + '.cmd')
		res.got, ret = execute(res.cmd)

		res.sources = grabSources(path)

		if not ret:
			res.status = ERROR
			res.color  = "black"
			res.what = "Failed to excute"
			
		else:
			res.exp  = readFile(fp + '.exp')
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

	res.cmd  = text2html(res.cmd)
	res.got  = text2html(res.got)
	res.what = text2html(res.what)
	res.exp  = text2html(res.exp)
	return res

def updateStats(test):
	gStatsLock.acquire()
	gStats[0] += 1
	if test.status == PASS:
		gStats[1] += 1
	elif test.status == FAIL:
		gStats[2] += 1
	elif test.status == ERROR:
		gStats[3] += 1
	gStatsLock.release()
	
class TestRunner (threading.Thread):
	def run(self):
		while not gTests.empty():
			smell = gTests.get()
			print "Adding " + smell
			res = evalTest(smell)
			updateStats(res)
			gResults.put_nowait(res)

gTests     = Queue.Queue(0)
gResults   = Queue.Queue(0)
gStats     = [0, 0, 0, 0] # total, passed, failed, error'ed
gStatsLock = threading.Lock()

def runTests(root, smells):
	#root   -> (absolute) directory to look for smells
	#smells -> tests to run
	
	current = os.getcwd()
	gResults = Queue.Queue(0) # contains tuples (name, results)

	for smell in smells:
		for i in glob(root + smell + "/*/*"):
			if not i.endswith(".svn"):
				gTests.put_nowait(i)

	for x in xrange(5): # use max 5 threads for test-running
		print "Threadje gestart"
		TestRunner().start()

	#gTests.join()
	while not gTests.empty():
		print "gTests ni leeg nog niet"
		sleep(1)
		
	os.chdir(current)
	return gResults, gStats

if __name__ == '__main__':
	res, stats = runTests("/home/nix/JaarProj/Smells/tests/", ["MysteryGuest", "AssertionRoulette", "AssertionLess"])
	
	print "Total " + str(stats[0]) + " pass " + str(stats[1]) + " fail " + str(stats[2]) + " error " + str(stats[3]) 
	while not gResults.empty():
		tres = gResults.get()
		print "***********"
		print tres.name
		print tres.cmd
		print tres.what
		print tres.status
		print "***********"
	