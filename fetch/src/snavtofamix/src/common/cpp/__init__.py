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
# Copyright 2007  University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>


from CppDictionary import CppDictionary
from ConditionalCompilationBlockResolver import CondBlockResolver

# CPP specific constants
C_PRIMITIVES = [
"bool",
"char",
"double",
"float",
"int",
"long",
"short",
"void",
"wchar_t",
"unsigned"
]

CPP_KEYWORDS = [ "asm"
,"auto"
,"bool"
,"break"
,"case"
,"catch"
,"char"
,"class"
,"const"
,"const_cast"
,"continue"
,"default"
,"delete"
,"do"
,"double"
,"dynamic_cast"
,"else"
,"enum"
,"explicit"
,"extern"
,"false"
,"float"
,"for"
,"friend"
,"goto"
,"if"
,"inline"
,"int"
,"long"
,"mutable"
,"namespace"
,"new"
,"operator"
,"private"
,"protected"
,"public"
,"register"
,"reinterpret_cast"
,"return"
,"short"
,"signed"
,"sizeof"
,"static"
,"static_cast"
,"struct"
,"switch"
,"template"
,"this"
,"throw"
,"true"
,"try"
,"typedef"
,"typeid"
,"typename"
,"union"
,"unsigned"
,"using"
,"virtual"
,"void"
,"volatile"
,"wchar_t"
,"while"
]

def isPrimitiveType(atype):
	if atype in C_PRIMITIVES: return True
	if " " in atype and atype.split(" ")[1] in C_PRIMITIVES and atype.split(" ")[0] in [ "unsigned", "signed" ]: return True
	if " " in atype and atype.split(" ")[0] in C_PRIMITIVES: return True
	return False

# Transforms a C++ fully fletched type into the underlying class.
def typeToClass(atype):
	aClass =  atype.replace("*", "")
	aClass = aClass.replace("&", "")
	aClass = aClass.replace("[", "")
	aClass = aClass.replace("]", "")
	aClass = aClass.replace("const", "")
	aClass = aClass.replace("mutable", "")
	if "<" in aClass: aClass = aClass.split("<")[0]
	aClass = aClass.strip()

	return aClass
