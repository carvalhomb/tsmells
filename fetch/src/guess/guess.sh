#!/bin/sh

# edit the line below
export GUESS_HOME="."
export GUESS_LIB="$GUESS_HOME/lib"

export GCLASSPATH="$GUESS_LIB/guess.jar:$GUESS_LIB/piccolo.jar:$GUESS_LIB/piccolox.jar:$GUESS_LIB/jung.jar:$GUESS_LIB/commons-collections.jar:$GUESS_LIB/hsqldb.jar:$GUESS_LIB/freehep-all.jar:$GUESS_LIB/colt.jar:$GUESS_LIB/prefuse.jar:$GUESS_LIB/TGGraphLayout.jar:$GUESS_LIB/looks.jar:$GUESS_LIB/mascoptLib.jar:$GUESS_LIB/jfreechart.jar:$GUESS_LIB/jide-components.jar:$GUESS_LIB/jide-common.jar:$GUESS_LIB/forms.jar:$GUESS_LIB/jcommon.jar"

echo $GCLASSPATH

java -DgHome=$GUESS_HOME -classpath $GCLASSPATH "-Dpython.home=$GUESS_HOME/src" com.hp.hpl.guess.Guess $@

echo $?
