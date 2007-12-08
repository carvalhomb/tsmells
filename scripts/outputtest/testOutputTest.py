#!/usr/bin/python
# This file is part of outputtest
#
# outputtest is free software; you can redistribute it and/or modify it 
# under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 2 of the License, or (at your 
# option) any later version.
#
# outputtest is distributed in the hope that it will be useful, but WITHOUT 
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more 
# details.
#
# You should have received a copy of the GNU General Public License along 
# with outputtest; if not, write to the Free Software Foundation, Inc., 
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA 
#
# Copyright 2007 Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>

import unittest
import outputtest
import time

class ExecuteTest(unittest.TestCase):
	""" Tests the execute function """
	
	def runTest(self):
		from outputtest import execute
		
		output, retVal = execute("echo a")
		self.assertEquals(output, "a\n")
		self.assertEquals(retVal, True)
		
		output, retVal = execute("cat multiline.txt")
		self.assertEquals(output, "aap\nbeer\n")
		self.assertEquals(retVal, True)
		
		output, retVal = execute("thiscommanddoesntexist")
		self.assertEquals(retVal, False)
		
		cmd = "while [ 1 -lt 2 ]; do echo 1; done;"
		self.assertRaises(RuntimeError, execute, cmd, timeout=1)
		
		

class GetDiffTest(unittest.TestCase):
	""" Checks the GetDiff helper function """
	
	def runTest(self):
		from outputtest import get_diff
	
		self.assertEquals(get_diff("ab","ab"), 
			"", 
			"Diff of equal content should return empty")
		self.assertEquals(get_diff("ab", ""),
			"---  \n\n+++  \n\n@@ -1,1 +1,0 @@\n\n-ab", 
			"Diff of \"ab\" and \"\" didnt return as expected")
		self.assertEquals(get_diff("", "ab"), 
			"---  \n\n+++  \n\n@@ -1,0 +1,1 @@\n\n+ab")
		#	"Diff of \"\" and \"ab\" didnt return as expected")
		self.assertEquals(get_diff("ab\ncd\n", "ab\ncd\n"), 
			"", 
			"Multiline diff of equal content should return empty")
		self.assertEquals(get_diff("ab\ncd\n", ""),
			"---  \n\n+++  \n\n@@ -1,2 +1,0 @@\n\n-ab\n-cd")
		
class GetFileMd5Test(unittest.TestCase):
	""" Tests the get_file_md5 helper function """
	
	def setUp(self):
		self.filename = "sunny.txt"
		self.file = open(self.filename)
		self.filecont = self.file.read()
		self.file.close()
	
	def runTest(self):
		from outputtest import get_file_md5
		
		sunny_md5 = "60b725f10c9c85c70d97880dfe8191b3"
		self.assertEquals(get_file_md5(self.filename), sunny_md5)
		self.assertEquals(self.filecont, open(self.filename).read(), 
												"shouldnt change file content")
		
		
		self.assertEquals(get_file_md5(self.filename, 'rb'), sunny_md5)
		self.assertEquals(self.filecont, open(self.filename).read(), 
												"shouldnt change file content")

		self.assertRaises(TypeError, get_file_md5, "sunny.txt", 'w')
		self.assertEquals(self.filecont, open(self.filename).read(), 
												"shouldnt change file content")

		self.assertRaises(IOError, get_file_md5, "nonexistantfilename")
		self.assertEquals(self.filecont, open(self.filename).read(), 
												"shouldnt change file content")

class AssertEqualFilesTest(unittest.TestCase):
	def setUp(self):
		class EmptyOutputTest(outputtest.OutputTest):
			def runTest(self):
				pass
		self.emptyTest = EmptyOutputTest()
		self.f1 = open("file1", "w")
		self.f2 = open("file2", "w")
		
	def testEqual(self):
		self.f1.write("aap")
		self.f1.close()
		self.f2.write("aap")
		self.f2.close()
		try:
			self.emptyTest.assertEqualFiles("file1", "file2")
		except AssertionError, e:
			self.fail("Equal files shouldnt raise an assertion error: " + str(e))
			
	def testDifferent(self):
		self.f1.write("aap")
		self.f1.close()
		self.f2.write("beer")
		self.f2.close()
		self.assertRaises(AssertionError, self.emptyTest.assertEqualFiles, "file1", "file2")

class AssertOutputEqualTest(unittest.TestCase):
	""" tests for assertOutputEqual method """
	def setUp(self):
		class EmptyOutputTest(outputtest.OutputTest):
			def runTest(self):
				pass
		self.emptyTest = EmptyOutputTest()
		
	def testSunny(self):
		""" sunny day scenario """
		try:
			self.emptyTest.assertOutputEqual("echo a", "a\n")
			self.assert_(True)
		except AssertionError, e:
			self.fail("testSunny should pass." \
				+ "\ngot Assertion failure:"\
				+ str(e))
	
	def testMultiLine(self):
		try:
			cmd = 'perl -e \'print "a\nbb\nccc\n"\''
			self.emptyTest.assertOutputEqual(cmd, "a\nbb\nccc\n")
			self.assert_(True)
		except AssertionError, e:
			self.fail("testMultiline should pass." \
				+ "\ngot Assertion failure:"\
				+ str(e))
				
	def testFail(self):
		self.assertRaises(AssertionError, \
				self.emptyTest.assertOutputEqual, "echo a", "b\n")
		try:
			self.emptyTest.assertOutputEqual("echo a", "b\n", "Didnt echo correct")
			self.fail()
		except AssertionError, e:
			self.assertEqual(str(e), "Didnt echo correct\n"\
						 "---  \n\n"\
						 "+++  \n\n"\
						 "@@ -1,1 +1,1 @@\n\n"\
						 "-b\n+a")
	
	def testListInput(self):
		self.assertRaises(TypeError, \
				self.emptyTest.assertOutputEqual, [1, 2], "a\n")
		self.assertRaises(TypeError, \
				self.emptyTest.assertOutputEqual, "echo a", [1, 2])
		self.assertRaises(TypeError, \
				self.emptyTest.assertOutputEqual, [1, 2], [2, 3])
	
	def testIntInput(self):
		self.assertRaises(TypeError, \
				self.emptyTest.assertOutputEqual, 1, "a\n")
		self.assertRaises(TypeError, \
				self.emptyTest.assertOutputEqual, "echo a", 1)
		self.assertRaises(TypeError, \
				self.emptyTest.assertOutputEqual, 1, 2)
				
	def testInfinite(self):
		cmd = "while [ 1 ]; do echo 1; done;"
		self.assertRaises(RuntimeError, \
				self.emptyTest.assertOutputEqual, cmd, "a\n",
				None, timeout=1)
	
	def testCommandStdErr(self):
		cmd = "ls thisshouldbeanonexistantfileordirectory"
		expected = \
			"ls: cannot access thisshouldbeanonexistantfileordirectory: No such file or directory"
		try:
			self.emptyTest.assertOutputEqual(cmd, expected)
		except AssertionError, e:
			self.fail("Problem with erronous commands" + str(e))

class AssertOutputEqualFileTest(unittest.TestCase):
	""" tests for assertOutputEqualFile method """
	def setUp(self):
		class EmptyOutputTest(outputtest.OutputTest):
			def runTest(self):
				pass
		self.emptyTest = EmptyOutputTest()
		
	def testSunny(self):
		try:
			self.emptyTest.assertOutputEqualFile("echo a", "sunny.txt")
			self.assert_(True)
		except AssertionError, e:
			self.fail("testSunny should pass." \
				+ "\ngot Assertion failure:\n"\
				+ str(e))

	def testFail(self):
		self.assertRaises(AssertionError, \
				self.emptyTest.assertOutputEqualFile, "echo a", "fail.txt")
		try:
			self.emptyTest.assertOutputEqual("echo a", "b\n", "Didnt echo correct")
			self.fail()
		except AssertionError, e:
			self.assertEqual(str(e), "Didnt echo correct\n"\
					+ "---  \n\n+++  \n\n@@ -1,1 +1,1 @@\n\n-b\n+a")
	
	def testNonExistantFile(self):
		filename="thisfilenameshouldnotexist_ifitdoesthistestwillfail"
		self.assertRaises(IOError, \
				self.emptyTest.assertOutputEqualFile, "echo a", filename)

class OutputTestTest(unittest.TestCase):
	""" Unittest for OutputTest """
	def setUp(self):
		self.oTests = unittest.TestSuite()
		self.result = unittest.TestResult()
	
	def testEqual(self):
		""" sunny day scenario """
		class BasicOutputTest(outputtest.OutputTest):
			def testEcho(self):
				self.assertOutputEqual("echo a", "a\n")
				
		self.oTests.addTest(BasicOutputTest('testEcho'))
		self.oTests.run(self.result)
		
		# sensitive equality ...
		expected = "<unittest.TestResult run=1 errors=0 failures=0>"
		self.assertEqual(str(self.result), expected, \
			    "This outputtest should pass.\ngot:\t" \
			    + str(self.result) + "\nexp:\t" + expected)			


	
if __name__ == '__main__':
	unittest.main()
	