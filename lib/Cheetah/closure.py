import sys
from pprint import pprint
from Cheetah.Template import Template

def printSrc(src):
    print src
    print '-'*80

def outputTransformer(template):
    #print template._CHEETAH_generatedModuleCode
    src = unicode(template)
    printSrc(src)
    return src

################################################################################
## 

src='''
#1-set foo = $1-a
#1-def func1: #def func(arg): $arg("***")
#2^set bar = $1-foo*1000

$1-foo*10 $2^bar
$($1-foo*10) $2^bar

$1-func1
$func(lambda x:c"--$x--$1-a")
'''
printSrc(src)
klass = Template.compile(src, preprocessors=[dict(tokens='$1- #1-',
                                                  outputTransformer=outputTransformer,
                                                  namespaces=dict(a=99),
                                                  baseclass=dict),
                                dict(tokens='$2^ #2^',
                                      namespaces=dict(b=58))
                                ])
print klass()
print '='*80


################################################################################
src='''
%set foo = @a
%def func1: #def func(arg): $arg("***")
%% comment

@foo*10

@func1
$func(lambda x:c"--$x--@a")
'''
printSrc(src)
klass2 = Template.compile(src,
                         preprocessors=dict(tokens='@ %',
                                             outputTransformer=outputTransformer,
                                             namespaces=dict(a=99))
                                             )
print klass2()
print '='*80

sys.exit(0)
################################################################################

src='''
#1:from HTMLgen import *
#def link(links)
  #for url, title in links
   $1:A("$url","$title")
  #end for 
#end def
$link([("/one/","Link1"), ("/two/","Link2")])
$1:a
##1: a comment
'''

src='''
#1:from HTMLgen import A
$1:(" ".join(str(A(url,title)) for url, title in $1:links))
$1:(" ".join(str($1:A(url,title)) for url, title in $1:links))
$1:num

$1:(" ".join(str(A(url,title)) for url, title in $1:links))

#def linkList(links): #for url, title in links#$1:A("$url","$title") #end for 
$linkList($links)
$(" ".join(str($A(url,title)) for url, title in $links))
'''
printSrc(src)

from HTMLgen import A
links = [("/one/","Link1"), ("/two/","Link2")]
preprocessNamespace = dict(num=123409, links=links, A=A)
runtimeNamespace = preprocessNamespace

#klass = Template.compile(src, preprocessors=[('1:',preprocessNamespace)])
klass = Template.compile(src, preprocessors=dict(tokens='$1: #1:',
                                    outputTransformer=outputTransformer,
                                    namespaces=preprocessNamespace))
print klass(searchList=[runtimeNamespace])

sys.exit(0)

################################################################################

#$1:(" ".join(c"$1:A($1:url,$1:title)" for url, title in $1:links))

##$(links {|url, title|A(url,title)})
##$(links | c"$A(url,title)")
##
##$(links | c"<A HREF="$a">$b</A>")
##$(links |url,title|c"<A HREF="$url">$title</A>")
##


src = '''\
#set foo = 1234
<% def test1():
    return foo*10
%>
#closure test2():$foo
#closure test3()
 $foo
#end closure
$test1()
$test2()
$test3()
$**(123)
$**(123*2)
$**test
$1@test
$**author
##$test2()
##$test1()

#def meth1
  #set foo = 99
  #def func(i):$foo - $i
  #for i in range(10)
    $func(i)
  #end for
#end def
$meth1
'''
print Template.compile(src, returnAClass=False)
klass = Template.compile(src)
print klass()

#
#+ int
#+ find_string_in_tuple(PyObject *tuple, PyObject *str)
#+ {
#+ 	PyObject *tmp;
#+ 	int i, n;
#+ 
#+ 	n = PyTuple_GET_SIZE(tuple);
#+ 	for (i = 0; i < n; i++) {
#+ 		tmp = PyTuple_GET_ITEM(tuple, i);
#+ 		/* XXX these must be strings */
#+ 		if (PyObject_Compare(str, tmp) == 0)
#+ 			return i;
#+ 	}
#+ 	return -1;
#+ }
#+ 
#+ PyObject *
#+ PyFrame_LookupName(PyFrameObject *f, PyObject *name)
#+ {
#+ 	 PyCodeObject *co;
#+ 	 PyObject *val;
#+ 	 int i;
#+ 
#+ 	 co = f->f_code;
#+ 
#+ 	 /* Figure out what kind of name it is.  Check co_cellvars first,
#+ 	    because a cell var also shows up in locals.
#+ 	 */ 
#+ 	 i = find_string_in_tuple(co->co_cellvars, name);
#+ 	 if (i >= 0) {
#+ 		 val = f->f_localsplus[f->f_nlocals + i];
#+ 		 return PyCell_Get(val);
#+ 	 }
#+ 	 i = find_string_in_tuple(co->co_varnames, name);
#+ 	 if (i >= 0) {
#+ 		 val = f->f_localsplus[i];
#+ 		 Py_INCREF(val);
#+ 		 return val;
#+ 	 }
#+ 	 i = find_string_in_tuple(co->co_freevars, name);
#+ 	 if (i >= 0) {
#+ 		 val = f->f_localsplus[f->f_nlocals + f->f_ncells + i];
#+ 		 return PyCell_Get(val);
#+ 	 }
#+ 	 val = PyDict_GetItem(f->f_globals, name);
#+ 	 if (val)
#+ 		 Py_INCREF(val);
#+ 	 else
#+ 		 PyErr_SetObject(PyExc_NameError, name);
#+ 	 return val;
#  }
