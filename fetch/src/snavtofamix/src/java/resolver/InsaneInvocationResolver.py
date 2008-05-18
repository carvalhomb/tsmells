# !/usr/bin/python
# This file is part of snavtofamix (Source Navigator to FAMIX).
#
# snavtofamix is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation; either version 2 of the License, or (at your
# option) any later version.
#
# snavtofamix is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
# details.
#
# You should have received a copy of the GNU General Public License along
# with snavtofamix; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
#
# Copyright 2007 University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

from common.SourceLocation  import SourceLocation
from common                 import utils
from log4py                 import Logger
 
class InsaneInvocationResolver:

    def __init__(self,pkgDict,impDict,inhDict, classDict,methDict,mtdSrcDict,miList, mtdpfDict, sFile,nr,content, raw):
        self.log = Logger().get_instance(self)
        self.pkgDict = pkgDict
        self.impDict = impDict
        self.inhDict = inhDict
        self.classDict = classDict
        self.methDict = methDict
        self.mtdSrcDict = mtdSrcDict
        self.miList = miList
        self.sourceFile = sFile
        self.lineNr = nr
        self.content = content
        self.raw = raw # full grep content
        self.mtdpfDict = mtdpfDict

        # to be filled in
        self.src_unqName = None # fully qualified caller method
        self.src_name = None    # caller method
        self.src_param = None   # caller method parameters

        self.dst_base = None
        self.dst_name = None
        self.dst_param = None

        self.srcLoc = None      # caller method filename
        self.srcLineNr = None   # caller method line number

        self.dstLoc = None
        self.dstLineNr = None

    def explore(self):
        """ First remove all useless or pre-existing candidates,
        then apply strict checking on remaining ones """
        #self.log.info("EXPLORING " + self.content)
        if self.__isMethodDef():
            self.log.warn("Method definition [" + self.content + "]")
            return False
        self.content = self.__cleanName()
        if "" == self.content:
            #self.log.warn("Empty content")
            return False
        if not self.__retrieveSourceMethod():
            self.log.warn("Failed to retrieve source method [" + self.content + "]")
            return False
        if not self.__uniqueDestination():
            self.log.warn("No unique destination [" + self.content + "]")
            return False
        if not self.__isExistingInvocation():
            return True

        # if self.__retrieveDstMethod():
        #     return True
        #self.log.warn("Existing invocation " + self.content)
        return False

    def __isMethodDef(self):
        """ calls on method def line are considered the def itself """
        mSrcLocs = self.mtdSrcDict.dict.values()
        srcLoc = [ self.sourceFile + ":" + self.lineNr ]
        return srcLoc in mSrcLocs

    def __cleanName(self):
        """ extract the identifier, e.g. "Blah._r483", before the ( """
        c = self.content
        name = ""
        i = len(c)-1
        if i < 0: return name
        while i >= 0:
            if c[i].isalnum() or "_" == c[i] or "." == c[i]:
                name = c[i] + name
                i = i - 1
            else:
                return name
        return name

    def __retrieveSourceMethod(self):
        """ retrieve the method definition with closest lineNr to the cand. """

        self.src_unqName, self.srcLineNr = self.mtdpfDict.getEnclosing(self.sourceFile, self.lineNr)
        if not self.src_unqName: return False
        #self.log.debug(">> srcMethod for " + self.content +" >> "+ self.src_unqName)
        self.src_name = self.src_unqName.split(".")[1].split("(")[0]
        signature = self.src_unqName.split("(")[1]
        self.src_param = signature[:len(signature)-1]
        self.srcLoc = self.sourceFile
        self.srcLineNr = str(self.srcLineNr)
        return True

    def __isExistingInvocation(self):
        """ Already an invocation with this name @ file:lineNr ? """
        #for j in self.miList:
        #    mi = j[0]

        #   unqName = mi[3] + "." + mi[4] + "(" + mi[5] + ")" # destination
        #    if self.sourceFile == mi[6] and self.lineNr == mi[7] and\
        #            self.dst_name in unqName:
                    #self.dst_name + self.dst_param in unqName:
        #        return True
        #return False		
        for j in self.miList:
            mi = j[0]
            if self.sourceFile != j[0][6] or self.lineNr != mi[7]:
                continue

            unqName = mi[3] + "." + mi[4] + "(" + mi[5] + ")" # destination
            if self.dst_name in unqName:
                #self.dst_name + self.dst_param in unqName:
                return True
        return False

    def __singleCandidateDestination(self, signature, dstType, dstName):
        """ helper for __uniqueDestination """
        found = False
        owners = self.methDict.dict[signature]
        if 1 == len(owners.keys()): # single owner candidate
            owner = owners.keys()[0]
            packages = self.methDict.dict[signature][owner]
            if 1 == len(packages): # single package
                package = self.methDict.dict[signature][owner][0]
                qMtdName = ""
                if "" != package: package += "::"
                qMtdName = package + owner + "." + signature
                sLoc = self.mtdSrcDict.getSourceLocFor(qMtdName)
                #self.dst_base = package+"::"+owner
                self.dst_base = qMtdName.split(".")[0]
                self.dstLoc = sLoc[0].split(":")[0]
                self.dstLineNr = sLoc[0].split(":")[1]
                signature = qMtdName.split("(")[1]
                self.dst_name = qMtdName.split(".")[1].split("(")[0]
                self.dst_param = signature[:len(signature)-1]
                found = True
        else: # overriden method?
            owner_sign = owners.keys()
            if dstName.endswith('toString'):
                self.log.warn("Hardcoded toString ..." + self.content)
                self.dst_name = "java::lang::Object.toString"
                self.dst_base = "java::lang::Object"
                sLoc = self.mtdSrcDict.getSourceLocFor("java::lang::Object.toString()")
                self.dstLoc   = sLoc[0].split(":")[0]
                self.dstLineNr = sLoc[0].split(":")[1]
                self.dst_param = ""
                found = True
        return found

    def __pickDecentCandidate(self, dstName, mtdCands):
        """ Choose!! a candidate destination. """
        cc, exact = countCommas(dstName, self.raw)
        #self.log.debug("cc " + str(cc) + " raw " + self.raw + " dstName " + dstName + " exact " + str(exact))

        # then screw it, we're talking overloaded methods. 
        # Just pick the first one with at least cc parameters.
        signature = None
        for sgntr in mtdCands.keys():
            if exact and sgntr.count(',') == cc:
                # found a signature with an identical number of parameters.
                # good enough.
                signature = sgntr
                break
            elif not exact and sgntr.count(',') >= cc:
                # we'r not sure about the exact number of parameters,
                # so more or equal fits fine
                signature = sgntr
                break
        return signature

    def __multipleCandidateDestinations(self, mtdCands, dstType, dstName):
        """ helper for __uniqueDestination """
        #self.log.debug("MultipleCandidateDestinations ..... " + self.raw)
        ownerList = [] # class x [pkgs]
        for i in mtdCands.keys(): 
            ownerList.extend(self.methDict.dict[i].keys())
        ownerSet = set(ownerList)  
        if 1 != len(ownerSet): # multiple owner classes, lets not guess
            return False

        # the owner list comes down to a single owner
        signature = self.__pickDecentCandidate(dstName, mtdCands)
        if signature == None: # no decent signature found, give up
            return False

        # duplication heaven
        owner = ownerList[0]
        packages = self.methDict.dict[signature][owner]
        if 1 != len(packages): # not a single package ...
            return False

        package = self.methDict.dict[signature][owner][0]
        qMtdName = ""
        self.dst_base = "" 
        if "" != package: package += "::"

        qMtdName = package + owner + "." + signature
        self.dst_base = package + owner
        sLoc = self.mtdSrcDict.getSourceLocFor(qMtdName)
        self.dstLoc = sLoc[0].split(":")[0]
        self.dstLineNr = sLoc[0].split(":")[1]
        signature = qMtdName.split("(")[1]
        self.dst_name = qMtdName.split(".")[1].split("(")[0]
        self.dst_param = signature[:len(signature)-1]

        return True

    # don't try this at home
    def __uniqueDestination(self):
        """ Entry point for trying to resolve to a unique destination """
        found = False
        dstType = utils.spliceLastPackageName(self.content, ".")
        dstName = utils.spliceAllButLastPackageName(self.content, ".")
        mtdCands = self.methDict.getMethodsNamed(dstName)
        if 1 == len(mtdCands): # single method name candidate
            signature = mtdCands.keys()[0]
            found = self.__singleCandidateDestination(signature, dstType, dstName)
        else:
            found = self.__multipleCandidateDestinations(mtdCands, dstType, dstName)
        return found


    def getOwnerUniqueName(self):
        return utils.spliceLastPackageName(self.src_unqName, ".")

    def getDstBase(self):
        return self.dst_base

def countCommas(dstName, raw):
    """ count the number of commas, an indication for the number of parameters.
        skip nested ones. Useful for overloaded methods.
        dstName is the method name without parameters.
        raw is the grep'd line to parse"""
    if not dstName: return None, False
    spl = raw.split(dstName)
    if len(spl) < 2 : return None, False
    cc = 0
    inStr = False
    brackets = []
    for c in spl[1]:
        if c == '"': # might fail with escaped quotes
            if inStr: inStr = False
            else: inStr = True
            continue
        if inStr: continue
        if c == ',' and 1 == len(brackets):
            cc += 1
        elif c == '(':
            brackets.append(1)
        elif c == ')':
            brackets.pop()
            if len(brackets) == 0: break
    return cc, len(brackets) == 0
