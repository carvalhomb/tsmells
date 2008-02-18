# !/usr/bin/python
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
#
# v0.3

import unittest
import popen2
import time
import hashlib
import signal
import os
import difflib

class OutputTest(unittest.TestCase):
    """ Used to test output of exterior commands against
    string or filecontent """
    
    def assertOutputEqual(self, command, expected, message=None, timeout=10):
        """ Executes command and compares its output against expected
            Raises an AssertionError with diff if not equal. 
            (throws some other stuff, too)"""
        
        validate_input(command, expected)
        output, return_val = execute(command, timeout)
        self.__compare(output, expected, message)
    
    def assertOutputEqualFile(self, command, filename, message=None, timeout=10):
        """ Equal to ``assertOutputEqual'' but gets the expected output from
            the file with name ``filename'' """
        validate_input(command, filename)
        output, return_val = execute(command, timeout)
        expected = open(filename).read()
        self.__compare(output, expected, message)
    
    def assertEqualFiles(self, file1, file2, binary=False):
        """ Checks if 2 files have equal content.
            file1 & file2 are supposed to be filenames.
            If not equal and in text mode a diff is computed"""
        mode = 'r'
        if binary:
            mode = 'rb'
        if get_file_md5(file1, mode) == get_file_md5(file2, mode):
            #files are equal
            return
        if binary:
            # no diff
            self.fail(file1 + " differs from " + file2)
        else:
            self.fail(get_diff(open(file1).read(), open(file2).read()))
    
    def __compare(self, output, expected, message):
        if not message:
            message = ""
        else:
            message += "\n"
        result = compare(output, expected)
        if result != "":
            self.fail(message + result)

def execute(command, timeout=10):
    """ Executes the 'command' in a shell. After timeout a RuntimeError
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

def validate_input(command, expected):
    if not (type(expected) in [str, file]):
        raise TypeError('``expected\'\' parameter should be a string')
    if type(command) != str:
        raise TypeError('``command\'\' parameter should be a string')

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
        
def get_file_md5(filename, mode='r'):
        if not mode in ['r', 'rb'] :
            raise TypeError('Only read or read+binary modes allowed')
        file = open(filename, mode)
        md5 = hashlib.md5()
        chunk = file.read(1024)
        md5.update(chunk)
        while chunk:
            chunk = file.read(1024)
            md5.update(chunk)
        file.close()
        return md5.hexdigest()
