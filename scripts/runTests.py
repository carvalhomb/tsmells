#!/usr/bin/python

import sys
import os
from os import listdir
from os import path
from os import chdir
from os import remove

#sys.path.append('../../../outputtest')
from outputtest import execute
from outputtest import compare

def subdirs(dirname):
	return [f for f in listdir(dirname) if path.isdir(path.join(dirname,f))]

def removeFile(file):
	try:
		remove(file)
	except:
		pass
	
def toFile(file, msg):
	f = open(file, 'w')
	f.write(msg)
	f.close()

def readFile(file):
	f = open(file)
	content = f.read()
	f.close()
	return content

def getTabs(word):
	length = len(word)
	tabs = "\t"
	if length < 14:
		tabs = "\t"*3
	elif length < 21:
		tabs = "\t"*2
	return tabs
	
def evalTest(name):
	""" runs a single test
		name should contain the testname, ie the
		name of the *.exp, *.cmd etc """
	print "\t+ " + name,
	tabs = getTabs(name)
	
	try:
		removeFile(name + '.fail')
		removeFile(name + '.res')
		
		cmd = readFile(name + '.cmd')
		cmdOutput, retVal = execute(cmd)

		if not retVal:
			print tabs + "error"
			msg = "Failed to excute: " + cmd + "\n\t>> " + cmdOutput
			toFile(	name + '.fail', msg)
			return
		
		toFile(name + '.res', cmdOutput)
		exp  = readFile(name + '.exp')
		diff = compare(cmdOutput, exp)

		if diff == "":
			print tabs + "success"
		else:
			print tabs + "failure"
			msg = "Output differs from expected\n" + diff + "\n"
			toFile( name + '.fail', msg)

	except Exception, e:
		print tabs + "error"
		msg = "General error occured during test:\n\t" + str(e)
		toFile( name + '.fail', msg)

def runTests(dir):
	chdir(dir)
	print "  >> " + path.basename(dir)
	for test in subdirs('.'):
		chdir(test)
		evalTest(test)
		chdir('..')
	chdir('..')

def traverseDirs(dirList):
	for i in dirList:
		print ">> Running " + path.basename(i) + " tests"
		runTests(path.join(i, "cpp"))
		runTests(path.join(i, "java"))
	

if __name__ == '__main__':
	traverseDirs(sys.argv[1:])
