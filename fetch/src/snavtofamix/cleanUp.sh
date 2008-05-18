#!/bin/sh

echo "Cleaning *.pyc *~ *.cdif"
for i in `find . \( -name ".pyc" -o -name "*~" -o -name "*.cdif" \)` -print; do rm $i; done

