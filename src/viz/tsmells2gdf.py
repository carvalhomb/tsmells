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

import os, sys
sys.path.append(os.environ['TSMELLS']+ "/lib/Contract/")
import inspect, cPickle, time, contract

from convert.gdfelements import Node, Edge
from convert.testentityz import *
from convert.smellentitiez import *



def isDumpEntityCollection(object):
    """ introspection predicate to filter only those classes
        of this module which are entity dump processors """
    return inspect.isclass(object) and\
            object.__name__.endswith('s') and\
            hasattr(object, 'parse')

class EntityConverter():
    def __init__(self):
        me = __import__(__name__) # this module
        self.entities = {} # instantations of all test entites & smells
                           # with their classname as key
        for entity in inspect.getmembers(me, isDumpEntityCollection):
            # introspect all processor classes
            # eg TestCases, TestCommands, AssertionRoulettes etc
            self.entities[entity[0]] = entity[1]()

    def readDump(self, filename):
        dump = open(filename, 'r')
        for line in dump:
            splitted = [filterComma(elem.strip('"')) for elem in line.split('\t')]
            if splitted[-1] == '\n': splitted = splitted[:-1]
            #dispatch
            #print splitted
            try: self.entities[splitted[0] + "s"].parse(splitted)
            except KeyError, e:
                continue
                #print "keyerror: ",
                #print e
        dump.close()

    def __writeSpacer(self, output):
        output.write(5*'\n')

    def __generateGdfPart(self, generator, output):
        for entity in self.entities.itervalues():
            for gdfEntity in getattr(entity, generator)():
                gdfEntity.write(output)

    def writeGdf(self, filename):
        output = open(filename, 'w')

        Node.writeHeader(output)
        self.__generateGdfPart('generateNodes', output)
        self.__writeSpacer(output)

        Edge.writeHeader(output)
        self.__generateGdfPart('generateEdges', output)
        output.close()

    def writeSourceLocationInfo(self, filename, root):
        output = open(filename, 'wb')
        srcDict = {}
        srcDict['ProjectSourceRootDirectory'] = root
        for entity in self.entities.itervalues():
            entity.appendLocationInfo(srcDict)
        cPickle.dump(srcDict, output)
        output.close()


    def writeMetricInfo(self, filename):
        output = open(filename, 'wb')
        metricDict = {}
        metricDict["Translation"] = {}
        for entity in self.entities.itervalues():
            entity.appendMetricInfo(metricDict)
        cPickle.dump(metricDict, output)
        output.close()

#contract.checkmod(__name__)

def usage():
    print "usage " + sys.argv[0] + " <tsmell-dump> <testsuite> [<src-root>]"
    print "\t<tsmell-dump>:\ttest smell dump as generated by rsf2tsmell.py"
    print "\t<src-root>:\tproject source directory."
    sys.exit(-1)

if __name__=='__main__':
    before = time.time()
    if len(sys.argv) < 1:
        usage()
    elif len(sys.argv) == 2:
        # auto load from 'tsmells' directory
        root = os.path.abspath(sys.argv[1])
        prefix = sys.argv[1]
        smellDump = 'tsmells/' + prefix + ".tsmell"
        testSuite = 'tsmells/' + prefix + ".testsuite"
    else:
        root = os.path.abspath(sys.argv[3])
        smellDump = sys.argv[1]
        testSuite = sys.argv[2]
        prefix = sys.argv[1].split('.')[0]

    #smellDump = sys.argv[1]
    #if len(sys.argv) > 2:
        #root = os.path.abspath(sys.argv[2])
    if root[-1] != '/': root = root + '/'

    temp = "tsmells/temp_123.456"
    os.system("cat " + testSuite + " " + smellDump + " > " + temp)
    converter = EntityConverter()
    converter.readDump(temp)

    gdfFile = "tsmells/" +  prefix + ".gdf"
    converter.writeGdf(gdfFile)

    srcFile = "tsmells/" + prefix + ".srcloc"
    converter.writeSourceLocationInfo(srcFile, root)

    metricFile = "tsmells/" + prefix + ".metrics"
    converter.writeMetricInfo(metricFile)

    os.system("rm " + temp)
    print "Convertion took " + str(round(time.time() - before, 3)) + " sec"
    print "Output written to ['" + gdfFile + "', '" + srcFile + "', '" + metricFile + "']"
