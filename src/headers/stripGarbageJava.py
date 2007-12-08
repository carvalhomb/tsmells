#!/usr/bin/python
#
# Author:	Manuel Breugelmans
# Date  :	Oct 2007
#
# Removes implementation & documentation from java source files
# Manually inspect the result and correct errors!
#
# v0.2

import sys

content = open(sys.argv[1])
line    = content.readline()
output  = []
count   = 0

foundClassCommentBlock = False
#copyright & import statements should be copied
while (not foundClassCommentBlock) and (line!=''):
	output.append(line)
	if line.startswith('/**'):
		foundClassCommentBlock = True
	line = content.readline()

foundClassDeclaration = False
foundClassStart       = False
isInterface	= False
#remove evrything but /**
#                       * @...
#                       */
while (not foundClassDeclaration) and (line!=''):
	if line.startswith(' * @') or line.startswith(' */'):
		output.append(line)
	
	line2 = line.lstrip(' ').lstrip('\t')
	if line2.startswith('public') or line2.startswith('abstract'):
		output.append(line)
		if (-1!=line.find('{')):
			foundClassStart = True
		if (-1!=line.find('interface')):
			isInterface = True
		foundClassDeclaration = True;
		
#	if line.startswith('public class'):
#		output.append(line)
#		foundClassDeclaration = True
#		
#	if line.startswith('class'):
#		# public was on previous line?
#		output.append("public " + line)
#		foundClassDeclaration = True
#		if (-1!=line.find('{')):
#			foundClassStart = True
		
	line = content.readline()


while (not foundClassStart) and (line!=''):
	output.append(line)
	if (-1!=line.find('{')):
		foundClassStart = True
	if (-1!=line.find('interface')):
		isInterface = True
	line = content.readline()

def isDeclaration(line):
	line2 = line.lstrip(' ').lstrip('\t')
	return line2.startswith('public') \
		or line2.startswith('private')\
		or line2.startswith('static') \
		or line2.startswith('protected')
	

# inside class
while line!='':
	if isInterface:
		line2 = line.lstrip(' ').lstrip('\t')
		if not (line2.startswith('/**') or line2.startswith('*') or line2=='\n'):
			output.append(line)
	elif isDeclaration(line):
		lineEnd = "\n"
		stripped = line.rstrip(' ')
		if stripped.endswith('{\n'):
			lineEnd = "}\n"
		elif stripped.endswith(')\n'):
			lineEnd = " {}\n"
		elif stripped.endswith(',\n'):
			lineEnd = "==> COMPLETE MANUALLY\n"
		output.append(line[0:-1] + lineEnd)
	line = content.readline()

output.append("}\n")
print "".join(output)
