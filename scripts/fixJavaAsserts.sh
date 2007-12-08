#!/bin/bash

#
# Aims to prepend 'this.' to all jUnit assert-checker methods
#

echo "==>> Fixxxing buggy java SN parser"
for i in $(find -L $1 -name "*.java" | grep -Ev \(TestCase.java$\|Assert.java$\)); do
	mv $i $i.tmp
	echo "     > processing $i"
	sed -r -e 's/([^.])assert(Equals|False|Null|NotNull)\(/\1this.assert\2\(/g' -e 's/([^.])fail\(/\1this.fail\(/g' $i.tmp > $i
	rm $i.tmp
done;
