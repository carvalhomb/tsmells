#!/usr/bin/env python
"""Programming-by-contract for Python, based on Eiffel's DBC.

Programming by contract documents class and modules with invariants,
expressions that must be true during the lifetime of a module or
instance; and documents functions and methods with pre- and post-
conditions that must be true during entry and return.

Copyright (c) 2003, 2006, 2007 Terence Way

This program is free software; you can redistribute it and/or modify
it under the terms of either:

a) GNU Library or Lesser General Public License as published by the
   Free Software Foundation; either version 3, or (at your option)
   any later version, or

b) Python Software Foundation License
   You may redistribute and/or modify this program under the same
   terms as Python itself, so long as this copyright message and
   disclaimer are retained in their original form, or

c) The "Artistic License" which comes with this Kit.

You should have received a copy of the Artistic License with this
Kit, in the file named "Artistic".  If not, I'll be glad to provide one.

You should also have received a copy of the GNU Lesser General Public
License along with this program in the files named "COPYING" and
"COPYING.LESSER".  If not, write to the Free Software Foundation, Inc.,
59 Temple Place, Suite 330, Boston MA 02111-1307, USA or visit their
web page on the internet at http://www.gnu.org/copyleft/lgpl.html.

IN NO EVENT SHALL THE AUTHOR BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF
THIS CODE, EVEN IF THE AUTHOR HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
DAMAGE.

THE AUTHOR SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE.  THE CODE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS,
AND THERE IS NO OBLIGATION WHATSOEVER TO PROVIDE MAINTENANCE,
SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
"""

# Changes:
#   ttw001 2003-05-26
#   Jeff Epler points out we should have separate assertion exception
#   base class ContractViolationError, then exception classes
#   PreconditionViolationError, PostconditionViolationError, and
#   InvariantViolationError inheriting from ContractViolationError.
#
#   ttw002 2003-05-26
#   Support restructured text by allowing double colon (::) in
#   addition to single colon (:) after 'pre' 'inv' 'post' or
#   post variable declarations.
#
#   ttw003 2003-05-29
#   Dickon Reed discovers problem: invariants mustn't be checked after
#   a constructor throws an exception.
#
#   ttw004 2003-06-02
#   Make sure that objects returned in _get_members only include
#   things defined by that module
#
#   ttw005 2003-06-02
#   Save line number information in parse_docstring, _read_block, to
#   be trapped if a TokenError or ParseError occurs, and to pass along
#   to any contract exceptions raised.
#
#   ttw006 2003-06-06
#   The _read_block internal function past the last line of a docstring
#   contract.  This saves some hackery in parse_docstring, and makes
#   _read_block more useful for external tools.
#
#   ttw007 2003-06-06
#   Phillip Eby points out that OR-ed pre-conditions are worse than
#   useless.  So instead of OR-ing pre-conditions of a method with all
#   overridden methods, we check if a pre-condition fails whether *any*
#   overridden pre-condition would succeed, if so we raise an
#   InvalidPreconditionError
#
#   ttw008 2003-06-13
#   Support partial contract enforcement: check preconditions only.
#   add flag to checkmod to specify checklevel: CHECK_NONE,
#   CHECK_PRECONDITIONS, or CHECK_ALL
#
#   ttw009 2003-06-13
#   String module names can be passed into checkmod as well.
#
#   ttw010 2003-06-20
#   Python 2.2.1 has bool, True, False but Python 2.2 doesn't.  To
#   support built-in Python on Mac OS X, define local bool() if not
#   defined globally
#
#   ttw011 2003-06-21
#   Support Jython (Python 2.1).  Refactored the tokenize logic in
#   _read_block() to support older tokenize.tokenize(), backtracked
#   some 2.2 conveniences (like 'x in {dict}').
#
#   ttw012 2005-10-19
#   From Gintautas Miliauskas:
#   "I discovered one problem though: you expect __import__(modname) to
#   return the corresponding module, however, it does not work like
#   that.  For example, if you invoke __import__('a.b.c.d'), the result
#   will be module <a>, not module <d>.  Yes, I think this is dumb too,
#   but that's how it works.  A workaround is to use __import__ to
#   import the module and then get the actual module from sys.modules."
#
#   ttw013 2006-04-22
#   From Aaron Bingham:
#   "The subclass's method defines no new pre- or postconditions; the
#   script testunchecked.py thus exits normally, even though the
#   superclass's contract is violated!"
#
#   All methods need to have checking code installed, even if there
#   are not any conditions in the method docstring, because the
#   super-class may have conditions.
#
#   A method without any docstring conditions is implicitly declaring
#   "pre: True"
#
#   rr001 2007-06-02
#   From Ruben Reifenberg
#   "There is some behaviour in contract.py that might be a bug.
#   It happened that line 1300 re-raised the AttributeError from 1292
#   although a PreconditionVioloation was going on.  Changing 1300 to:
#    1300: raise args
#   solved the problem"
#
#   ab001 2007-08-31
#   From Aaron Bingham
#   "Fix handling of constructor preconditions.
#
#   Subclass constructors' parameter lists may be legitimately completely
#   different from the superclass constructor's parameter list. Thus, for
#   constructors we only evaluate preconditions of the current class's
#   constructor and do not follow the mro.  See Meyer, Object Oriented
#   Software Construction, 2nd Edition, p. 466."
# 
#   ttw015 2007-08-31
#   Avoid building a list of methods for preconditions.
#
#   ttw016 2007-08-31
#   All installed checker functions return True, so they can be used
#   in contracts, ex: "pre: Base.method.__assert_pre(self, a, b)"
#
__author__ = "Terence Way"
__email__ = "terry@wayforward.net"
__version__ = "1.4: August 31, 2007"
MODULE = 'contract'

import new
import re
import sys
import tokenize

from cStringIO import StringIO
from types import *

# Programming-by-contract extends the language to include invariant
# expressions for classes, and pre-condition and post-condition
# expressions for methods.  These are very similar to assertions,
# expressions which must be true or the program is stopped.
#
# Class invariants are expressions which must be true at the end of a
# class's constructor, and at the beginning and end of each public
# method.
#
# Pre-conditions must be true at the start of each method.  Post-
# conditions must be true at the end of each method.  Post-condition
# logic has access to an 'old' variable which records variable values
# before they were changed by the method.
#
# This implementation tries to fix what I consider a defect in
# Eiffel's DBC -- while it is easy to specify what does change after
# invoking a method, it is impossible to specify what doesn't change.
# This makes it hard to reason about methods, and limits portability
# to more formal techniques such as Z.
#
# A simple fix is to declare what parts change.  Simply modify the
# line 'post:' to 'post [a]:' to declare that the variable a is the
# only thing changed by the sort function.  This is modelled after Z
# schemas [2].
#
# Simply saying 'post:' means anything can be changed: this is the
# original semantics of the Eiffel ENSURES statement.  Saying 'post
# []:' means nothing is changed.
#
# Python Specifications
# ---------------------
# The docstring of any module or class can have invariants documented
# by having the keyword 'inv' followed by a colon (:) at the start of
# a line.  Whitespace is ignored.  This is either followed by a single
# expression on the same line, or a series of expressions on following
# lines indented past the 'post' key- word.  The normal Python rules
# about implicit and explicit line continuations are followed here.
#
# To support Re-structured text, two colons (::) after the keyword is
# supported.
#
# Module invariants must be true at module load time, and at the entry
# and return of every public function within the module.
#
# Class invariants must be true after the __init__ function returns,
# and at the entry and return of every public method of the class.
#
# The docstring of any function or method can have pre-conditions
# documented with the keyword 'pre' following the same rules above,
# and can have post-conditions documented with the keyword 'post'
# optionally followed by a list of variables.  The variables are in
# the same scope as the body of the function or method.
#
# Expressions have access to some additional convenience values.  To
# make evaluating sequences easier, there are two functions: forall(a)
# and exists(a).  To make implication easier, there is a
# implies(x,a,b=True) function which mirrors C's conditional
# expression (x?a:c).
#
# The expressions in the post-conditions have access to two additional
# variables, __old__ which is filled with variables declared in the
# 'post' statement bound to shallow copies before the function or
# method is called; and __return__ which is bound to the return value
# of the function or method.
#
# Implementation
# --------------
# This module is divided into four parts:
#
# 1 -- Find docstrings and parse them for contract expressions...
#
#      This is accomplished by:
#      a. recursive enumerating elements in modules, classes using code
#         lifted from the 'inspect' Python standard module
#         see: checkmod, _check
#      b. scanning the docstrings of public functions with a regular
#         expression looking for lines that matches 'pre:' 'post:' or
#         'inv:' at the start
#         see: parse_docstring
#      c. Using the 'tokenize' Python tokenizer to build expressions
#         see: _read_block, _read_decl, _read_token
#
# 2 -- Construct functions that do contract checking...
#      This is done by just constructing big strings that are function
#      definitions.  Each function or method gets associated with four
#      'helper' functions... to check pre-conditions, to save old
#      values, to check post-conditions, and of course the saved
#      original code.  These are stored as function attributes.
#         see: _install_wrapper, _define_checker
#
# 3 -- Run-time support for call checking
#         see: call_public_function_*, call_private_function_*,
#              call_public_method_*, call_private_method_*,
#              call_constructor_*, call_destructor_*
#
# 4 -- Declarations for use within contract expressions
#      includes 'forall' 'exists' 'implies' and exception classes


# change the keywords here, if necessary
INV = 'inv'
PRE = 'pre'
POST = 'post'
TYPE_CONTRACTS = [INV]
CODE_CONTRACTS = [PRE, POST]
OLD = '__old__'
RETURN = '__return__'
PREFIX = '__assert_'

# enumeration to pass to checkmod: how extensive checking should be
CHECK_DEFAULT, CHECK_NONE, CHECK_PRECONDITIONS, CHECK_ALL = range(4)

_ORIG = 'orig'
_SAVE = 'save'
_CHK = 'chk'

_CONTRACTS = tuple(TYPE_CONTRACTS + CODE_CONTRACTS)

# look for word+ [expr] space* ':' at the start of a line
#
_re_start = re.compile(r'^\s*(%s|%s|%s)\s*(\[|:)' % _CONTRACTS,
                       re.MULTILINE)
_RE_KEYWORD = 1

# the tokenizer only seems to return OP(50) for any operators like : [ ] . ,
# we need to do a further lookup to get the operator value, but only for the
# tokens we care about
#
_OPS = {':': tokenize.COLON,
        '[': tokenize.LSQB,
        ']': tokenize.RSQB,
        '.': tokenize.DOT,
        ',': tokenize.COMMA}

_EXCEPTIONS = {PRE: 'PreconditionViolationError',
               POST: 'PostconditionViolationError',
               INV: 'InvariantViolationError'}

# ttw010 support pre 2.2.1...
try:
    bool, True, False = bool, True, False
except NameError:
    False, True = 0, 1
    def bool(x):
        return not not x
# ...ttw010

###############################################################################
# Part 1 -- Find docstrings and parse them for contract expressions...
#

def checkmod(module, checklevel = CHECK_DEFAULT):
    """Add invariant, pre- and post-condition checking to a module.

    pre::
        isstring(module) or isinstance(module, ModuleType)
        checklevel in [CHECK_DEFAULT, CHECK_NONE, CHECK_PRECONDITIONS,
                       CHECK_ALL]
    """
    # ttw009 string module names...
    if isinstance(module, StringType) or isinstance(module, UnicodeType):
	# ttw012 imports...
	__import__(module)
	module = sys.modules[module]
	# ...ttw012
    # ...ttw009

    # ttw008 partial contract enforcement...
    if checklevel == CHECK_DEFAULT:
        if __debug__:
            checklevel = CHECK_ALL
        else:
            checklevel = CHECK_PRECONDITIONS
    # ...ttw008

    if checklevel != CHECK_NONE:
        # get members *before* we start adding stuff to this module
        path = [module]
        members = _get_members(module, path)
        invs = parse_docstring(module.__doc__, TYPE_CONTRACTS)[0]
        name = PREFIX + INV
        func = getattr(module, name, None)
        # should we override
        if not func or func.__name__.startswith(PREFIX):
            if invs[2]:
                func = _define_checker(name, '', invs, path)
            else:
                func = __assert_inv
                module.__assert_inv = func
        _check_members(members, path, checklevel)
        # check module invariants now
        func()

def _check_type(code, path, checklevel):
    """Modify a class to add invariant checking.

    pre::
        isstring(code[0])
        type(code[1]) == code[2]
        isinstance(code[0], MethodType) or isinstance(code[0], FunctionType)
        isinstance(path[0], ModuleType)
        forall(path[1:], isclass)
    """
    name, obj = code

    # get members *before* we start adding stuff to this class
    path = path + [obj]
    members = _get_members(obj, path)
    invs = parse_docstring(obj.__doc__, TYPE_CONTRACTS)[0]
    if invs[2]:
        func = _define_checker(_mkname(path, INV), 'self', invs, path)
        setattr(obj, PREFIX + INV, func)
        delattr(path[0], func.func_name)
    _check_members(members, path, checklevel)

def _check_proc(code, path, checklevel):
    """Modify a module or class to add invariant checking.
    """
    name, obj = code
    _install_wrapper(code, parse_docstring(obj.__doc__, CODE_CONTRACTS), path,
                     is_public = _ispublic(name), checklevel = checklevel)

def _get_location(f):
    """Get function location as tuple (name, filename, lineno).

    pre::
        isinstance(f, MethodType) or isinstance(f, FunctionType)
    post[]::
        isstring(__return__[0])
        isstring(__return__[1])
        isinstance(__return__[2], int)
    """
    if isinstance(f, MethodType):
        f = f.im_func
    c = f.func_code
    return f.func_name, c.co_filename, c.co_firstlineno

def _get_members(obj, path):
    """Returns two lists (procs, types) where each list contains (name,
    value) tuples.

    For classes, only attributes defined by the specific class are returned,
    i.e. not inherited attributes.  Attributes created by this module
    (prefixed by '__assert_') are skipped as well.

    Examples:
    >>> import contract
    >>> path = [contract]
    >>> hasattr(contract, '_re_start')
    1
    >>> '_re_start' in [x[0] for x in _get_members(contract, path)[0]]
    0
    >>> '_get_members' in [x[0] for x in _get_members(contract, path)[0]]
    1
    >>> 'checkmod' in [x[0] for x in _get_members(contract, path)[0]]
    1
    >>> class base:
    ...     def foo(self): pass
    >>> class derived(base):
    ...     def bar(self): pass

    hasattr can get inherited attributes:
    >>> hasattr(derived, 'foo')
    1

    but we don't:
    >>> path = [__import__('__main__')]
    >>> 'foo' in [x[0] for x in _get_members(derived, path)[0]]
    0
    """
    module_name = path[0].__name__
    module_dict = path[0].__dict__
    parent = path[-1]
    procs = []
    types = []
    for key in obj.__dict__.keys():
        if not key.startswith(PREFIX):
            m = getattr(obj, key)
            # ttw004 only objects that belong to this module...
            if (isinstance(m, MethodType) and
                m.im_class is parent) or \
               (isinstance(m, FunctionType) and \
                m.func_globals is module_dict):
                procs.append((key, m))
            elif isclass(m) and getattr(m, '__module__', None) is module_name:
                types.append((key, m))
            # ...ttw004
    return (procs, types)

def _check_members((procs, types), path, checklevel):
    for p in procs:
        _check_proc(p, path, checklevel)
    for t in types:
        _check_type(t, path, checklevel)

def _ispublic(name):
    """Checks if a name is public (starts and ends with '__' or doesn't
    start with a _ at all).

    Examples:
    >>> _ispublic('__init__')
    1
    >>> _ispublic('foo')
    1
    >>> _ispublic('_ispublic')
    0
    """
    return not name.startswith('_') or \
        (name.startswith('__') and name.endswith('__'))

def parse_docstring(docstring, keywords):
    """Parse a docstring, looking for design-by-contract expressions.

    Returns a list of tuples: the list is the same length as keywords, and
    matches each keyword.  The tuple is (keyword, [decls], [exprs]), namely
    the keyword, a list of string declarations, and a list of tuples (string,
    lineno).

    Examples::
    >>> from pprint import pprint
    >>> pprint( parse_docstring(parse_docstring.__doc__, ['post', 'pre']) )
    [('post', [], [('[ x [ 0 ] for x in __return__ ] == keywords', 22)]),
     ('pre',
      [],
      [('docstring is None or isstring ( docstring )', 18),
       ('forall ( keywords , isstring )', 19)])]

    pre::
        docstring is None or isstring(docstring)
        forall(keywords, isstring)

    post[]::
        [x[0] for x in __return__] == keywords
    """
    result = [(x, [], []) for x in keywords]

    if docstring is None:
        return result

    # step 1: scan through docstring looking for keyword
    input = StringIO(docstring)

    offset = 0
    assert input.tell() == 0

    line = input.readline()
    lineno = 0 # zero-origined because tokenizer keeps 1-origined
    while line != '':
        a = _re_start.split(line)

        if len(a) > _RE_KEYWORD and a[_RE_KEYWORD] in keywords:
            # step 2: found a keyword, now rewind and scan looking
            # for either an inline expression or a series of sub-
            # indented expressions
            input.seek(offset)

            # ttw005... get lineno info and add to exception's lineno
            #           if a TokenError occurs...
            try:
                l = _read_block(input, lineno)
                lineno = l[3]
                # returns (keyword, decls, exprs, lineno)
            except tokenize.TokenError, ex:
                # reformat to include new line info
                raise tokenize.TokenError(ex[0],
                                          (lineno + ex[1][0],) + ex[1][1:])
            # ...ttw005

            # Find the right result index based on keyword
            r = result[keywords.index(l[0])]
            r[1].extend(l[1])
            r[2].extend(l[2])
        else:
            lineno += 1
        if offset == input.tell(): break
        offset = input.tell()
        line = input.readline()
    return result

# ttw011 refactor tokenize parser to use older tokenize.tokenize()...
#
# [INDENT] NAME
# [LSQB (NAME (DOT NAME)* (COMMA NAME (DOT NAME)*)*)* RSQB]
# COLON [COLON]
# (NEWLINE INDENT (not DEDENT)*) | (not NEWLINE)*

class Done(Exception): pass

class tokenizer:
    def __init__(self, input, startlineno):
        self.input = input
        self.startlineno = startlineno
        self.endlineno = self.lineno = startlineno + 1
        self.state = self.start
        self.decl, self.decls, self.expr, self.exprs = [], [], [], []
        self.keyword, self.offset = '', input.tell()

    def next(self, token, string, start, end, line):
        if token != tokenize.COMMENT and token != tokenize.NL:
            if token == tokenize.OP and _OPS.has_key(string):
                token = _OPS[string]
            self.state(token, string)
        if token == tokenize.NEWLINE or token == tokenize.NL:
            self.lineno = self.startlineno + start[0] + 1

    # all following methods are states in the state machine.
    # the self.state variable indicates which function/state we're in
    def start(self, token, string):
        if token == tokenize.INDENT:
            self.state = self.indent
        else:
            self.indent(token, string)

    def indent(self, token, string):
        if token == tokenize.NAME:
            self.keyword = string
            self.state = self.name
        else:
            raise SyntaxError("expected pre, post, or inv")

    def name(self, token, string):
        if token == tokenize.LSQB:
            self.state = self.decl0
        else:
            self.colon(token, string)

    def decl0(self, token, string):
        if token == tokenize.NAME:
            self.decl.append(string)
            self.state = self.decl1
        elif token == tokenize.RSQB:
            self.state = self.colon
        else:
            raise SyntaxError("expected variable name or ]")

    def decl1(self, token, string):
        if token == tokenize.DOT:
            self.state = self.decln
        elif token == tokenize.COMMA:
            self.decls.append(self.decl)
            self.decl = []
            self.state = self.decln
        elif token == tokenize.RSQB:
            self.decls.append(self.decl)
            self.state = self.colon
        else:
            raise SyntaxError("expected one of (,.])")

    def decln(self, token, string):
        if token == tokenize.NAME:
            self.decl.append(string)
            self.state = self.decl1
        else:
            raise SyntaxError("expected name")

    def colon(self, token, string):
        if token == tokenize.COLON:
            self.state = self.colon1
        else:
            raise SyntaxError("expected colon(:)")

    def colon1(self, token, string):
        if token == tokenize.COLON:
            self.state = self.colon2
        else:
            self.colon2(token, string)

    def colon2(self, token, string):
        if token == tokenize.NEWLINE:
            self.state = self.newline
        else:
            self.endtoken = tokenize.NEWLINE
            self.state = self.rest
            self.rest(token, string)

    def newline(self, token, string):
        if token == tokenize.INDENT:
            self.endtoken = tokenize.DEDENT
            self.state = self.rest
        else:
            raise IndentationError("expected an indented block")

    def rest(self, token, string):
        if token == self.endtoken or token == tokenize.ENDMARKER:
            if self.expr:
                self.exprs.append( (' '.join(self.expr), self.lineno) )
            raise Done()
        self.offset, self.endlineno = self.input.tell(), self.lineno
        if token == tokenize.NEWLINE:
            self.exprs.append( (' '.join(self.expr), self.lineno) )
            self.expr = []
        else:
            self.expr.append(string)

def _read_block(input, startlineno):
    r"""Read an indented block of expressions

    startlineno is *zero* origined line number.

    pre::
        input.readline  # must have readline function

    Examples:
    #>>> _read_block(StringIO('\tfoo:\n'), 0)
    #0
    >>> _read_block(StringIO('\tpost[]: True\n'), 0)
    ('post', [], [('True', 1)], 1)
    >>> _read_block(StringIO('\tpre: 5 + 6 > 10\n'), 0)
    ('pre', [], [('5 + 6 > 10', 1)], 1)
    >>> _read_block(StringIO('\tpost:\n\t\t5 + 6 < 12\n\t\t2 + 2 == 4\n'), 0)
    ('post', [], [('5 + 6 < 12', 2), ('2 + 2 == 4', 3)], 3)
    >>> _read_block(StringIO('\tpost[foo.bar]: # changes\n' \
    ...                      '\t\tlen(foo.bar) > 0\n'), 0)
    ('post', [['foo', 'bar']], [('len ( foo . bar ) > 0', 2)], 2)

    Handles double colons (for re-structured text)::
    >>> _read_block(StringIO('\tpre:: 5 + 6 > 10\n'), 0)
    ('pre', [], [('5 + 6 > 10', 1)], 1)
    """
    t = tokenizer(input, startlineno)
    try:
        tokenize.tokenize(input.readline, t.next)
    except Done:
        pass
    input.seek(t.offset)
    return (t.keyword, t.decls, t.exprs, t.endlineno)
# ...ttw011

# ...part 1
###############################################################################

###############################################################################
# Part 2 -- Construct functions that do contract checking...
#
def _install_wrapper(code, contracts, path, is_public, checklevel):
    """Creates and installs a function/method checker.

    pre::
        contracts[0][0] == PRE and contracts[1][0] == POST
        isinstance(path[0], ModuleType)
        forall(path[1:], isclass)
    """
    name, obj = code

    newpath = path + [obj]

    ismethod = isinstance(obj, MethodType)

    if ismethod:
        func = obj.im_func
        invs = hasattr(path[-1], PREFIX + INV)
    else:
        func = obj
        invs = hasattr(path[0], PREFIX + INV)

    # we must create a checker if:
    #  1. there are any pre-conditions or any post-conditions OR
    #  2. this is public AND there are invariants
    # ttw013...
    #  3. this is a method call, to check any super-class method
    #     conditions
    # ...ttw003
    if ismethod or contracts[0][2] or contracts[1][2] or (is_public and invs):
        argspec = getargspec(func)
        args = _format_args(argspec)

        # argl: argument list suitable for appending at the end of other args
        if args:
            argl = ', ' + args
        else:
            argl = args

        output = StringIO()

        output.write('def %s(%s):\n' % (_mkname(path, name, _CHK), args))
        output.write('\timport %s\n' % MODULE)

        classname = '.'.join([c.__name__ for c in path[1:]])

        if isinstance(obj, FunctionType):
            if is_public:
                chkname, chkargs = 'call_public_function', '__assert_inv, '
            else:
                chkname, chkargs = 'call_private_function', ''
        else:
            if not is_public:
                chkname = 'call_private_method'
            elif name == '__init__':
                chkname = 'call_constructor'
            elif name == '__del__':
                chkname = 'call_destructor'
            else:
                chkname = 'call_public_method'
            chkargs = classname + ', '

        # ttw008 partial contract enforcement...
        if checklevel == CHECK_ALL:
            suffix = '_all'
        else:
            suffix = '_pre'
        # ...ttw008

        output.write('\treturn %s.%s%s(%s' % (MODULE, chkname, suffix,
                                              chkargs))
        if classname:
            output.write(classname)
            output.write('.')
        output.write(name)
        output.write(argl)
        output.write(')\n')

        newfunc = _define(_mkname(path, name, _CHK), output.getvalue(),
                          path[0])

        # if there are default arguments
        if argspec[3]:
            # install the default arguments... don't try to put them into
            # our printed function definition, above, as they have already
            # been evaluated.
            newfunc = new.function(newfunc.func_code, newfunc.func_globals,
                                   newfunc.func_name, argspec[3])

        setattr(newfunc, PREFIX + _ORIG, getattr(func, PREFIX + _ORIG, func))

        # write preconditions checker
        if contracts[0][2]:
            pre = _define_checker(_mkname(path, name, contracts[0][0]), args,
                                  contracts[0], newpath)

            delattr(path[0], pre.func_name)
            setattr(newfunc, PREFIX + contracts[0][0], pre)

        if checklevel == CHECK_ALL:
            # write __old__ saver
            if contracts[1][1]:
                saver = _define_saver(_mkname(path, name, _SAVE), OLD + argl,
                                      contracts[1][1], path[0])
                delattr(path[0], saver.func_name)
                setattr(newfunc, PREFIX + _SAVE, saver)

            # write postconditions checker
            if contracts[1][2]:
                post = _define_checker(_mkname(path, name, contracts[1][0]),
                                       OLD + ', ' + RETURN + argl,
                                       contracts[1], newpath)

                delattr(path[0], post.func_name)
                setattr(newfunc, PREFIX + contracts[1][0], post)

        newname = newfunc.func_name
        newfunc.__doc__ = obj.__doc__

        if isclass(path[-1]) and isinstance(obj, FunctionType):
            # static method
            newfunc = staticmethod(newfunc)

        setattr(path[-1], name, newfunc)

        if name != newname or len(path) > 1:
            delattr(path[0], newname)
    # end _install_wrapper

def isclass(obj):
    return isinstance(obj, TypeType) or isinstance(obj, ClassType)

def isstring(obj):
    return isinstance(obj, StringType) or isinstance(obj, UnicodeType)

def _define_checker(name, args, contract, path):
    """Define a function that does contract assertion checking.

    args is a string argument declaration (ex: 'a, b, c = 1, *va, **ka')
    contract is an element of the contracts list returned by parse_docstring
    module is the containing module (not parent class)

    Returns the newly-defined function.

    pre::
        isstring(name)
        isstring(args)
        contract[0] in _CONTRACTS
        len(contract[2]) > 0
    post::
        isinstance(__return__, FunctionType)
        __return__.__name__ == name
    """
    output = StringIO()
    output.write('def %s(%s):\n' % (name, args))
    # ttw001... raise new exception classes
    ex = _EXCEPTIONS.get(contract[0], 'ContractViolationError')
    output.write('\tfrom %s import forall, exists, implies, %s\n' % \
                 (MODULE, ex))
    loc = '.'.join([x.__name__ for x in path])
    for c in contract[2]:
        output.write('\tif not (')
        output.write(c[0])
        output.write('): raise %s("%s", %u)\n' % (ex, loc, c[1]))
    # ...ttw001

    # ttw016: return True for superclasses to use in preconditions
    output.write('\treturn True')
    # ...ttw016

    return _define(name, output.getvalue(), path[0])

def _define_saver(name, args, decls, module):
    """Create a function that saves values into an __old__ variable.

    pre:: decls
    post:: isinstance(__return__, FunctionType)
    """
    output = StringIO()
    output.write('def %s(%s):\n' % (name, args))
    output.write('\timport %s, copy\n' % MODULE)

    _save_decls(output, '', _decltodict(decls))

    return _define(name, output.getvalue(), module)

def _define(name, text, module):
    #print text
    exec text in vars(module)
    return getattr(module, name)

def _format_args( (arguments, rest, keywords, default_values) ):
    """Formats an argument desc into a string suitable for both a function/
    method declaration or a function call.

    This does *not* handle default arguments.  Default arguments are already
    evaluated... use new.function() to create a function with pre-evaluated
    default arguments.

    Examples:
    >>> def foo(a, (b, c), d = 1, e = 2, *va, **ka):
    ...     pass
    >>> a = getargspec(foo)
    >>> a
    (['a', '(b, c)', 'd', 'e'], 'va', 'ka', (1, 2))
    >>> _format_args(a)
    'a, (b, c), d, e, *va, **ka'

    pre::
        isinstance(arguments, list)
        rest is None or isstring(rest)
        keywords is None or isstring(keywords)
    post[]:: True
    """
    if rest is not None: arguments = arguments + ['*' + rest]
    if keywords is not None: arguments = arguments + ['**' + keywords]
    return ', '.join(arguments)

CO_VARARGS, CO_VARKEYWORDS = 4, 8

try:
    import inspect

    def _format_arg(a):
        """Convert an argument list into a tuple string.

        >>> _format_arg(['a', 'b', 'c'])
        '(a, b, c)'
        >>> _format_arg(['a', ['b', 'c', 'd']])
        '(a, (b, c, d))'
        >>> _format_arg(['a'])
        '(a,)'
        >>> _format_arg('a')
        'a'
        """
        if isinstance(a, ListType):
            if len(a) == 1:
                return '(' + _format_arg(a[0]) + ',)'
            else:
                return '(' + ', '.join([_format_arg(z) for z in a]) + ')'
        else:
            return a

    def _getargs(function):
        t = inspect.getargspec(function)
        return ([_format_arg(a) for a in t[0]], t[1], t[2], t[3])

    getmro = inspect.getmro

except ImportError:
    # inspect module not available, on say Jython 2.1
    def _getargs(function):
        code = function.func_code
        i = code.co_argcount
        args = list(code.co_varnames[:i])
        if code.co_flags & CO_VARARGS:
            va = code.co_varnames[i]
            i += 1
        else:
            va = None
        if code.co_flags & CO_VARKEYWORDS:
            ka = code.co_varnames[i]
            i += 1
        else:
            ka = None
        return (args, va, ka, function.func_defaults)

    def _searchbases(cls, accum):
        # Simulate the "classic class" search order.
        if cls in accum:
            return
        accum.append(cls)
        for base in cls.__bases__:
            _searchbases(base, accum)

    def getmro(cls):
        """Return tuple of base classes (including cls) in method resolution
        order."""
        if hasattr(cls, "__mro__"):
            return cls.__mro__
        else:
            result = []
            _searchbases(cls, result)
            return tuple(result)

def getargspec(function):
    """Get argument information about a function.

    Returns a tuple (args, varargs, keywordvarargs, defaults) where
    args is a list of strings, varargs is None or the name of the
    *va argument, keywordvarargs is None or the name of the **ka
    argument, and defaults is a list of default values.

    This function is different from the Python-provided
    inspect.getargspec in that 1) tuple arguments are returned as
    a string grouping '(a, b, c)' instead of broken out "['a', 'b', 'c']"
    and 2) it works in Jython, which doesn't support inspect (yet).

    >>> getargspec(lambda a, b: a * b)
    (['a', 'b'], None, None, None)
    >>> getargspec(lambda a, (b, c, d) = (5, 6, 7), *va, **ka: a * b)
    (['a', '(b, c, d)'], 'va', 'ka', ((5, 6, 7),))

    pre::
        isinstance(function, FunctionType)
    post[]::
        # tuple of form (args, va, ka, defaults)
        isinstance(__return__, TupleType) and len(__return__) == 4
        # args is a list of strings
        isinstance(__return__[0], ListType)
        forall(__return__[0], isstring)
        # va is None or a string
        __return__[1] is None or isstring(__return__[1])
        # ka is None or a string
        __return__[2] is None or isstring(__return__[2])
        # defaults is None or a tuple
        __return__[3] is None or isinstance(__return__[3], TupleType)
    """
    return _getargs(function)

def _mkname(path, *va):
    """Define a name combining a path and arbitrary strings.

    pre::
       isinstance(path[0], ModuleType)
       forall(path[1:], isclass)

    Examples:
    >>> import contract
    >>> _mkname([contract], 'test')
    '__assert_test'
    >>> class foo:
    ...     pass
    >>> _mkname([contract, foo], 'func', 'pre')
    '__assert_foo_func_pre'
    """
    return PREFIX + '_'.join([c.__name__ for c in path[1:]] + list(va))

def _save_decls(output, name, d):
    """Recursively output a dictionary into a set of 'old' assignments.

    The dictionary d is a tree of variable declarations.  So, for example,
    the declaration [self, self.buf, self.obj.a] would turn into the dict
    {'self': {'buf': {}, 'obj': {'a': {}}}}, and would get output as
        __old__.self = contract._holder()
        __old__.self.buf = copy.copy(self.buf)
        __old__.self.obj = contract._holder()
        __old__.self.obj.a = copy.copy(self.obj.a)
    """
    for k, v in d.items():
        n = name + k
        output.write('\t%s.%s = ' % (OLD, n))
        if v == {}:
            output.write('copy.copy(%s)\n' % n)
        else:
            output.write('%s._holder()\n' % MODULE)
            _save_decls(output, n + '.', v)

def _decltodict(l):
    """Converts a list of list of names into a hierarchy of dictionaries.

    Examples:
    >>> d = _decltodict([['self', 'buf'],
    ...                  ['self', 'item', 'a'],
    ...                  ['self', 'item', 'b']])
    >>> d == {'self': {'buf': {}, 'item': {'a': {}, 'b': {}}}}
    1
    """
    result = {}
    for i in l:
        d = result
        for n in i:
            # n in d ? d[n] : d[n] = {}
            d = d.setdefault(n, {})
    return result

#
# ...part 2
###############################################################################

###############################################################################
# Part 3... Run-time support for call checking
#

#####################################
# ttw001 add new assertion classes...
class ContractViolationError(AssertionError):
    pass

class PreconditionViolationError(ContractViolationError):
    pass

class PostconditionViolationError(ContractViolationError):
    pass

class InvariantViolationError(ContractViolationError):
    pass
# ...ttw001
#####################################

# ttw006 correctly weaken pre-conditions...
class InvalidPreconditionError(ContractViolationError):
    """Method pre-conditions can only weaken overridden methods'
    preconditions.
    """
    pass
# ...ttw006

# ttw008 partial contract enforcement

# these are 'drivers': functions which check contracts and call the
# original wrapped functions.  Each driver has two variants: one for
# CHECK_PRECONDITIONS and one for CHECK_ALL: the CHECK_ALL checks
# preconditions and invariants on entry, and postconditions and
# invariants on exit.  The CHECK_PRECONDITIONS only checks pre-
# conditions and invariants on entry.  There needs to be a driver
# for public and private functions, public and private methods, and
# constructors and destructors.  Hence:
#   call_public_function_pre/call_public_function_all
#       checks module invariants and function pre[post] conditions
#   call_private_function_pre/call_private_function_all
#       checks function pre[post] conditions
#   call_public_method_pre/call_public_method_all
#       checks class/type invariants and method pre[post] conditions
#   call_private_method_pre/call_private_method_all
#       checks method pre[post] conditions
#   call_constructor_pre/call_constructor_all
#       checks method pre-conditions [type/class invariants and post-
#       conditions on exit]
#   call_destructor_pre/call_destructor_all
#       checks method pre-conditions and class/type invariants on entry.

def call_public_function_all(inv, func, *va, **ka):
    """Check the invocation of a public function or static method.

    Checks module invariants on entry and exit.  Checks any
    pre-conditions and post-conditions.
    """
    inv()
    try:
        # ttw015: avoid making a list of functions...
        return _call_one_all(func, va, ka)
        # ...ttw015
    finally:
        inv()

def call_public_function_pre(inv, func, *va, **ka):
    """Check the invocation of a public function or static method.

    Checks module invariants on entry.  Checks any pre-conditions.
    """
    inv()
    # ttw015: avoid making a list of functions...
    return _call_one_pre(func, va, ka)
    # ...ttw015

def call_private_function_all(func, *va, **ka):
    """Check the invocation of a private function or static method.

    Only checks pre-conditions and post-conditions.
    """
    # ttw015: avoid making a list of functions...
    return _call_one_all(func, va, ka)
    # ...ttw015

def call_private_function_pre(func, *va, **ka):
    """Check the invocation of a private function or static method.

    Only checks pre-conditions
    """
    # ttw015: avoid making a list of functions...
    return _call_one_pre(func, va, ka)
    # ...ttw015

def call_public_method_all(cls, method, *va, **ka):
    """Check the invocation of a public method.

    Check this class and all super-classes invariants on entry and
    exit.  Checks all post-conditions of this method and all over-
    ridden method.
    """
    mro = getmro(cls)
    _check_class_invariants(mro, va[0])
    try:
        return _method_call_all(mro, method, va, ka)
    finally:
        _check_class_invariants(mro, va[0])

def call_public_method_pre(cls, method, *va, **ka):
    """Check the invocation of a public method.

    Check this class and all super-classes invariants on entry.
    exit.
    """
    mro = getmro(cls)
    _check_class_invariants(mro, va[0])
    return _method_call_pre(mro, method, va, ka)

def call_constructor_all(cls, method, *va, **ka):
    """Check the invocation of an __init__ constructor.

    Checks pre-conditions and post-conditions, and only checks
    invariants on successful completion.
    """
    # ttw003 invariants mustn't be checked if constructor raises exception...

    # ab001 fix handling of constructor preconditions...
    # mro = getmro(cls)
    # result = _method_call_all(mro, method, va, ka)
    # _check_class_invariants(mro, va[0])
    result = _method_call_all([cls], method, va, ka)
    _check_class_invariants(getmro(cls), va[0])
    # ...ab001

    return result
    # ...ttw003

def call_constructor_pre(cls, method, *va, **ka):
    """Check the invocation of an __init__ constructor.

    Checks pre-conditions and post-conditions, and only checks
    invariants on successful completion.
    """
    # ttw003 invariants mustn't be checked if constructor raises exception...

    # ab001 fix handling of constructor preconditions...
    # mro = getmro(cls)
    # result = _method_call_pre(mro, method, va, ka)
    # _check_class_invariants(mro, va[0])
    result = _method_call_pre([cls], method, va, ka)
    _check_class_invariants(getmro(cls), va[0])
    # ...ab001

    return result
    # ...ttw003

def call_destructor_all(cls, method, *va, **ka):
    """Check the invocation of a __del__ destructor.

    Checks pre-conditions and post-conditions, and only checks
    invariants on entry.
    """
    mro = getmro(cls)
    _check_class_invariants(mro, va[0])
    return _method_call_all(mro, method, va, ka)

def call_destructor_pre(cls, method, *va, **ka):
    """Check the invocation of a __del__ destructor.

    Checks pre-conditions and post-conditions, and only checks
    invariants on entry.
    """
    mro = getmro(cls)
    _check_class_invariants(mro, va[0])
    return _method_call_pre(mro, method, va, ka)

def call_private_method_all(cls, method, *va, **ka):
    """Check the invocation of a private method call.

    Checks pre-conditions and post-conditions.
    """
    return _method_call_all(getmro(cls), method, va, ka)

def call_private_method_pre(cls, method, *va, **ka):
    """Check the invocation of a private method call.

    Checks pre-conditions.
    """
    return _method_call_pre(getmro(cls), method, va, ka)

def _check_class_invariants(mro, instance):
    """Checks class invariants on an instance.

    mro - list of classes in method-resolution order
    instance - object to test

    pre::
        # instance must be an instance of each class in mro
        forall(mro, lambda x: isinstance(instance, x))
    """
    # ab002: Avoid generating AttributeError exceptions...
    for c in mro:
        if hasattr(c, '__assert_inv'):
            c.__assert_inv(instance)
    # ...ab002

def _method_call_all(mro, method, va, ka):
    """Check the invocation of a method.

    mro -- list/tuple of class objects in method resolution order
    """
    # NO CONTRACTS... recursion
    assert isinstance(method, MethodType)

    func = method.im_func
    name = func.__assert_orig.__name__
    # list of all method functions with name
    a = [getattr(c, name).im_func for c in mro if _has_method(c, name)]

    return _call_all(a, func, va, ka)

def _method_call_pre(mro, method, va, ka):
    """Check the invocation of a method.

    mro -- list/tuple of class objects in method resolution order
    """
    # NO CONTRACTS... recursion
    func = method.im_func
    name = func.__assert_orig.__name__
    # list of all method functions with name
    a = [getattr(c, name).im_func for c in mro if _has_method(c, name)]

    return _call_pre(a, func, va, ka)

def _call_one_all(func, va, ka):
    if hasattr(func, '__assert_pre'):
        func.__assert_pre(*va, **ka)

    # save old values
    old = _holder()

    if hasattr(func, '__assert_save'):
        func.__assert_save(old, *va, **ka)

    result = func.__assert_orig(*va, **ka)
    # check post-conditions
    if hasattr(func, '__assert_post'):
        func.__assert_post(old, result, *va, **ka)

    return result

def _call_one_pre(func, va, ka):
    if hasattr(func, '__assert_pre'):
        func.__assert_pre(*va, **ka)

    return func.__assert_orig(*va, **ka)

def _call_all(a, func, va, ka):
    _check_preconditions(a, func, va, ka)

    # save old values
    old = _holder()

    # ab002: Avoid generating AttributeError exceptions...
    for f in a:
        if hasattr(f, '__assert_save'):
            f.__assert_save(old, *va, **ka)

    result = func.__assert_orig(*va, **ka)
    for f in a:
        # check post-conditions
        if hasattr(f, '__assert_post'):
            f.__assert_post(old, result, *va, **ka)
    # ...ab002

    return result

def _call_pre(a, func, va, ka):
    _check_preconditions(a, func, va, ka)
    return func.__assert_orig(*va, **ka)

def _check_preconditions(a, func, va, ka):
    # ttw006: correctly weaken pre-conditions...
    # ab002: Avoid generating AttributeError exceptions...
    if hasattr(func, '__assert_pre'):
        try:
            func.__assert_pre(*va, **ka)
        except PreconditionViolationError, args:
            # if the pre-conditions fail, *all* super-preconditions
            # must fail too, otherwise
            for f in a:
                if f is not func and hasattr(f, '__assert_pre'):
                    f.__assert_pre(*va, **ka)
                    raise InvalidPreconditionError(args)
            # rr001: raise original PreconditionViolationError, not
            # inner AttributeError...
            # raise
            raise args
            # ...rr001
    # ...ab002
    # ...ttw006

def _has_method(cls, name):
    """Test if a class has a named method.

    pre::
        isclass(cls)
        isstring(name)
    post:: __return__ == (hasattr(cls, name) and \
                         isinstance(getattr(cls, name), MethodType))
    """
    return isinstance(getattr(cls, name, None), MethodType)

def __assert_inv():
    """Empty invariant assertions
    """
    pass

# ...part 3
###############################################################################

###############################################################################
# Part 4 -- Declarations for use within contract expressions...
#
def forall(a, fn = bool):
    """Checks that all elements in a sequence are true.

    Returns True(1) if all elements are true.  Return False(0) otherwise.

    Examples:
    >>> forall([True, True, True])
    1
    >>> forall( () )
    1
    >>> forall([True, True, False, True])
    0
    """
    for i in a:
        if not fn(i):
            return False
    return True

def exists(a, fn = bool):
    """Checks that at least one element in a sequence is true.

    Returns True(1) if at least one element is true.  Return False(0)
    otherwise.

    Examples:
    >>> exists([False, False, True])
    1
    >>> exists([])
    0
    >>> exists([False, 0, '', []])
    0
    """
    for i in a:
       if fn(i):
           return True
    return False

def implies(test, then_val, else_val = True):
    """Logical implication.

    implies(x, y) should be read 'x implies y' or 'if x then y'
    implies(x, a, b) should be read 'if x then a else b'

    Examples:
    >>> implies(False, False)
    1
    >>> implies(False, True)
    1
    >>> implies(True, False)
    0
    >>> implies(True, True)
    1
    """
    if test:
        return then_val
    else:
        return else_val

class _holder:
    """Placeholder for arbitrary 'old' values.
    """
    pass

#
# ...part 4
###############################################################################

__test__ = {
    '_ispublic': _ispublic, '_get_members': _get_members,
    '_decltodict': _decltodict, '_read_block': _read_block,
    '_format_args': _format_args, '_mkname': _mkname}

if __name__ == '__main__':
    import doctest, contract
    #contract.checkmod(contract)
    doctest.testmod(contract)
