CURR="$TSMELLS/tests/GenCtrlStructInfo/java/SingleIF" &&\
$TSMELLS/scripts/generateCtrlStructInfo.sh  ${CURR}/src src &&\
cat $CURR/src/dbdump/src.ctrlstruct