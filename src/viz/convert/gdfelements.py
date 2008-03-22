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

colors = ['black', 'lightgray', 'red']

class Node():
	def __init__(self, name, color, entity, label, style):
		"""
		pre:
			isinstance(name, str)
			color in colors
			isinstance(entity, str)
			isinstance(label, str)
			isinstance(style, int)
		"""
		self.name = name
		self.color = color
		self.entity = entity
		self.label = label
		self.style = str(style)

	def __eq__(self, other):
		if self is other: return True
		if other is None: return False
		if not (isinstance(other, Node)): return False
		if self.name == other.name and\
			self.color == other.color and\
			self.entity == other.entity and\
			self.label == other.label and\
			self.style == other.style:
			return True
		return False

	def __str__(self):
		return "Node[" + self.name + "," + self.color + "," + \
				self.entity + "," + self.label + "," + self.style + "]"

	def write(self, output):
		output.write(self.name + "," + self.color + "," + self.entity + "," +\
					 self.label + "," + self.style + "\n")

	def writeHeader(output):
		output.write('nodedef>name,color,entity VARCHAR(32),label,style\n')
	writeHeader = staticmethod(writeHeader)

class Edge():
	def __init__(self, node1, node2, directed="false", visible="true", color="lightgray"):
		self.node1 = node1
		self.node2 = node2
		self.directed = directed
		self.visible = visible
		self.color = color

	def __eq__(self, other):
		if self is other: return True
		if other is None: return False
		if not (isinstance(other, Edge)): return False
		if self.node1 == other.node1 and\
			self.node2 == other.node2 and\
			self.directed == other.directed and\
			self.visible == other.visible and\
			self.color == other.color:
			return True
		return False

	def __str__(self):
		return "Edge[" + self.node1 + "," + self.node2 + "," +\
				self.directed + "," + self.visible + "," +\
				self.color + "]"

	def write(self, output):
		output.write(self.node1 + "," + self.node2 + "," +\
					 self.directed + "," + self.visible + "," +\
					 self.color + "\n")

	def writeHeader(output):
		output.write('edgedef>node1,node2,directed,visible,color\n')
	writeHeader = staticmethod(writeHeader)