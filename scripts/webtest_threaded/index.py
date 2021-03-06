#!/usr/bin/env python




##################################################
## DEPENDENCIES
import sys
import os
import os.path
from os.path import getmtime, exists
import time
import types
import __builtin__
from Cheetah.Version import MinCompatibleVersion as RequiredCheetahVersion
from Cheetah.Version import MinCompatibleVersionTuple as RequiredCheetahVersionTuple
from Cheetah.Template import Template
from Cheetah.DummyTransaction import DummyTransaction
from Cheetah.NameMapper import NotFound, valueForName, valueFromSearchList, valueFromFrameOrSearchList
from Cheetah.CacheRegion import CacheRegion
import Cheetah.Filters as Filters
import Cheetah.ErrorCatchers as ErrorCatchers
from master import master

##################################################
## MODULE CONSTANTS
try:
    True, False
except NameError:
    True, False = (1==1), (1==0)
VFFSL=valueFromFrameOrSearchList
VFSL=valueFromSearchList
VFN=valueForName
currentTime=time.time
__CHEETAH_version__ = '2.0'
__CHEETAH_versionTuple__ = (2, 0, 0, 'final', 0)
__CHEETAH_genTime__ = 1196089956.3333991
__CHEETAH_genTimestamp__ = 'Mon Nov 26 16:12:36 2007'
__CHEETAH_src__ = 'index.tmpl'
__CHEETAH_srcLastModified__ = 'Mon Nov 26 16:12:23 2007'
__CHEETAH_docstring__ = 'Autogenerated by CHEETAH: The Python-Powered Template Engine'

if __CHEETAH_versionTuple__ < RequiredCheetahVersionTuple:
    raise AssertionError(
      'This template was compiled with Cheetah version'
      ' %s. Templates compiled before version %s must be recompiled.'%(
         __CHEETAH_version__, RequiredCheetahVersion))

##################################################
## CLASSES

class index(master):

    ##################################################
    ## CHEETAH GENERATED METHODS


    def __init__(self, *args, **KWs):

        master.__init__(self, *args, **KWs)
        if not self._CHEETAH__instanceInitialized:
            cheetahKWArgs = {}
            allowedKWs = 'searchList namespaces filter filtersLib errorCatcher'.split()
            for k,v in KWs.items():
                if k in allowedKWs: cheetahKWArgs[k] = v
            self._initCheetahInstance(**cheetahKWArgs)
        

    def body(self, **KWS):



        ## CHEETAH: generated from #block body at line 10, col 1.
        trans = KWS.get("trans")
        if (not trans and not self._CHEETAH__isBuffering and not callable(self.transaction)):
            trans = self.transaction # is None unless self.awake() was called
        if not trans:
            trans = DummyTransaction()
            _dummyTrans = True
        else: _dummyTrans = False
        write = trans.response().write
        SL = self._CHEETAH__searchList
        _filter = self._CHEETAH__currentFilter
        
        ########################################
        ## START - generated method body
        
        write('''
''')
        #  Search form
        write('''<form method="POST">
\t<input type="hidden" name="go" value="1" />
\t<input type="submit" value="RUN" />
</form>

''')
        if (not VFFSL(SL,"results",True)): # generated from line 18, col 1
            write('''Tests not run yet
''')
        write('''
''')
        #  We have results show them
        if VFFSL(SL,"results",True): # generated from line 23, col 1
            write('''Found ''')
            _v = VFFSL(SL,"len",False)(VFFSL(SL,"results",True)) # '$len($results)' on line 24, col 7
            if _v is not None: write(_filter(_v, rawExpr='$len($results)')) # from line 24, col 7.
            write(''' result(s):
<table width="80%" border="1">
''')
            #  Header
            write('''    <tr>
        <td>Name</td>
        <td></td>
    </tr>
''')
            for smell in VFFSL(SL,"results",True): # generated from line 31, col 5
                write('''    <tr>
''')
                # <td><a href="/results/$test.name">$test.name</a></td>
                write('''        <td>''')
                _v = VFFSL(SL,"smell",True)[0] # '$smell[0]' on line 34, col 13
                if _v is not None: write(_filter(_v, rawExpr='$smell[0]')) # from line 34, col 13.
                write('''</td>
\t\t<td>''')
                _v = VFN(VFFSL(SL,"smell",True)[1][0],"name",True) # '$smell[1][0].name' on line 35, col 7
                if _v is not None: write(_filter(_v, rawExpr='$smell[1][0].name')) # from line 35, col 7.
                write('''</td>
    </tr>
''')
            write('''</table>

''')
        write('''
''')
        
        ########################################
        ## END - generated method body
        
        return _dummyTrans and trans.response().getvalue() or ""
        

    def writeBody(self, **KWS):



        ## CHEETAH: main method generated for this template
        trans = KWS.get("trans")
        if (not trans and not self._CHEETAH__isBuffering and not callable(self.transaction)):
            trans = self.transaction # is None unless self.awake() was called
        if not trans:
            trans = DummyTransaction()
            _dummyTrans = True
        else: _dummyTrans = False
        write = trans.response().write
        SL = self._CHEETAH__searchList
        _filter = self._CHEETAH__currentFilter
        
        ########################################
        ## START - generated method body
        
        #  Index page
        write('''
''')
        #  Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>
        write('''

''')
        #  Default attributes
        write('''
''')
        self.body(trans=trans)
        
        ########################################
        ## END - generated method body
        
        return _dummyTrans and trans.response().getvalue() or ""
        
    ##################################################
    ## CHEETAH GENERATED ATTRIBUTES


    _CHEETAH__instanceInitialized = False

    _CHEETAH_version = __CHEETAH_version__

    _CHEETAH_versionTuple = __CHEETAH_versionTuple__

    _CHEETAH_genTime = __CHEETAH_genTime__

    _CHEETAH_genTimestamp = __CHEETAH_genTimestamp__

    _CHEETAH_src = __CHEETAH_src__

    _CHEETAH_srcLastModified = __CHEETAH_srcLastModified__

    results = None

    _mainCheetahMethod_for_index= 'writeBody'

## END CLASS DEFINITION

if not hasattr(index, '_initCheetahAttributes'):
    templateAPIClass = getattr(index, '_CHEETAH_templateClass', Template)
    templateAPIClass._addCheetahPlumbingCodeToClass(index)


# CHEETAH was developed by Tavis Rudd and Mike Orr
# with code, advice and input from many other volunteers.
# For more information visit http://www.CheetahTemplate.org/

##################################################
## if run from command line:
if __name__ == '__main__':
    from Cheetah.TemplateCmdLineIface import CmdLineIface
    CmdLineIface(templateObj=index()).run()


