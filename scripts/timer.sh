#!/bin/bash

# load the previous stored time from a temp file 
# and print the difference. restart the stopwatch

NOW=$(date +%s)
expr $NOW - $(cat $1)
echo $NOW > $1
