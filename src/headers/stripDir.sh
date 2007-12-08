#!/bin/bash

echo "Entering $1"
for i in $(ls headersources/$1); do 
	echo -e "\t>> Stripping $i"
	python stripGarbageJava.py headersources/$1/$i > headers/$1/$i; 
done;
