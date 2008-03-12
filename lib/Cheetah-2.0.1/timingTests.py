from pprint import pprint
from Cheetah.Template import Template

src_nameMapper_noTransArg = """
#def foo
This is bar: $bar(1234)
#end def
#def bar(arg)
--- $arg ---
#end def
#def main
 This is foo: $foo
#end def 

A test template
This is main: $main

#for i in range(100)
  $i
#end for

#raw
  #for i in range(100)
  $i
  #end for
#end raw

#for i in range(10)
  $main
#end for
"""

src_nameMapper_withTransArg = """
#def foo
This is bar: $bar(arg=1234,trans=trans)
#end def
#def bar(arg)
--- $arg ---
#end def
#def main
 This is foo: $foo(trans=trans)
#end def 

A test template
This is main: $main(trans=trans)

#for i in range(100)
  $i
#end for

#raw
  #for i in range(100)
  $i
  #end for
#end raw

#for i in range(10)
  $main(trans=trans)
#end for
"""

src_noNameMapper_withTransArg = """
#def foo
This is bar: $self.bar(trans=trans,arg=1234)
#end def
#def bar(arg)
--- $arg ---
#end def
#def main
 This is foo: $self.foo(trans)
#end def 

A test template
This is main: $self.main(trans)

#for i in range(100)
  $i
#end for

#raw
  #for i in range(100)
  $i
  #end for
#end raw

#for i in range(10)
  $self.main(trans)
#end for
"""

src_noNameMapper_noTransArg = """
#def foo
This is bar: $self.bar(arg=1234)
#end def
#def bar(arg)
--- $arg ---
#end def
#def main
 This is foo: $self.foo()
#end def 

A test template
This is main: $self.main()

#for i in range(100)
  $i
#end for

#raw
  #for i in range(100)
  $i
  #end for
#end raw

#for i in range(10)
  $self.main()
#end for
"""

settings1 = {'useNameMapper':True,
             'useStackFrames':False,
             'alwaysFilterNone':False,
             'useFilters':True,
             'includeRawExprInFilterArgs':True,            
             }
settings2 = {'useNameMapper':True,
             'useStackFrames':True,
             'alwaysFilterNone':False,
             'useFilters':True,
             'includeRawExprInFilterArgs':True,                        
             }
settings3 = {'useNameMapper':True,
             'useStackFrames':True,
             'alwaysFilterNone':True,
             'useFilters':True,
             'includeRawExprInFilterArgs':True,                        
             }
settings4 = {'useNameMapper':True,
             'useStackFrames':True,
             'alwaysFilterNone':True,
             'useFilters':True,
             'includeRawExprInFilterArgs':False,             
             }
settings5 = {'useNameMapper':True,
             'useStackFrames':True,
             'alwaysFilterNone':True,
             'useFilters':False,
             #'includeRawExprInFilterArgs':False,             
             }
settings6 = {'useNameMapper':False,
             #'useStackFrames':True,
             'alwaysFilterNone':True,
             'useFilters':False,
             #'includeRawExprInFilterArgs':False,             
             }

includeTransArg = True

def getTemplateSrc(settings):
    if settings['useNameMapper']:
        if includeTransArg:
            return src_nameMapper_withTransArg
        else:
            return src_nameMapper_noTransArg
    else:
        if includeTransArg:
            return src_noNameMapper_withTransArg
        else:
            return src_noNameMapper_noTransArg

precompiledTemplates = {}
def getTemplate(i):
    settings = globals().get('settings%i'%i)
    if i not in precompiledTemplates:
        precompiledTemplates[i] = Template(getTemplateSrc(settings), compilerSettings=settings)
    return precompiledTemplates[i]

#template = T(templateSrc_NameMapper)
#print template._generatedClassCode
#print str(template)

def printSettings(i):
    settings = globals().get('settings%i'%i)
    items = settings.items()
    items.sort()
    for set in items:
        print '  %s=%s'%set 
    #pprint(items, indent=4)

def printTemplateCode(i):
    settings = globals().get('settings%i'%i)
    print getTemplate(i)._generatedClassCode  

def testPrecompiled(i):
    str(getTemplate(i))

def testWithCompilation(i):
    settings = globals().get('settings%i'%i)                
    str(Template(getTemplateSrc(settings), compilerSettings=settings))


