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
# Copyright 2006,2007   University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>

primTypes = [    "byte", "short", "int", "long", \
                 "float", "double", "boolean", "char" ]

jdkClass = [ 	"Vector",
				"ArrayList",
				"String",
				"JLabel",
				"JButton",
				"JTextField",
				"HashMap",
				"Set",
				"Collection",
				"URL",
				"JMenuItem",
				"JMenu",
				"JPanel",
				"JScrollPane",
				"Thread",
				]

jdkMethod = [	"String.charAt()",
				"Socket.close()",
				"Long.valueOf()",
				"JPanel.add()",
				"JMenu.add()",
				"JMenuItem.addActionListener()",
				"Integer.intValue()",
				"Integer.toHexString()",
				"HashMap.put()",
				"DataInputStream.read()",
				"Calendar.get()",
				"Vector.addElement()",
				"Vector.elementAt()",
				"Vector.size()",
				"Vector.clear()",
                "Vector.add()",
                "Vector.get()",
                "Vector.insertElementAt()",
                "Vector.lastElement()",
                "Vector.removeElementAt()",
                "Vector.insertElementAt()",
                "String.getBytes()",
                "String.length()",
                "String.substring()",
                "StringBuffer.toString()",
                "StringBuffer.append()",
                "String.equals()",
                "String.indexOf()",
                "System.out.print()",
                "System.out.println()",
                "TableModel.getValueAt()",
                "TestSuite.addTest()",
                "junit.textui.TestRunner.run()",
                "JMenuItem.setActionCommand()"
			]

def isPrimitiveType(typeName):
    return typeName in primTypes

def isLibraryType(className):
	return className in jdkClass

def typeToClass(typeName):
    return typeName.replace("[]","")

def isJDKMethod(methName):
	return methName in jdkMethod



