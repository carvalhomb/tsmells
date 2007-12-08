CURR="$TSMELLS/tests/GenCtrlStructInfo/java/MultiIF" &&\
$TSMELLS/scripts/generateCtrlStructInfo.sh  ${CURR}/src src &&\
cat $CURR/src/dbdump/src.ctrlstruct