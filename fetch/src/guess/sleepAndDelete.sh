#!/bin/sh
while true; 
do 
	sleep 1 
	if [ -f tmp.png ] 
	then 
		# to write away png file completely?
		sleep 10
		cp tmp.png tc`date "+%Y%m%d-%H%M%S"`.png 
		echo "wrote away PNG file"
		rm tmp.png
		killall java
	fi	
done
