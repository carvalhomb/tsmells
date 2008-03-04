RSF=CppUnit1-12SimpleSuite.rsf &&\
M4SCRIPT=$TSMELLS/scripts/tsmellsCpp.m4 &&\
MACRO="-DXUNIT_INIT=$TSMELLS/src/initCppUnitv1-12.rml -DDUMP_TEST_ENTITIES" &&\
RML=$(mktemp) && \
m4  $MACRO $M4SCRIPT > $RML &&\
cat $RSF | crocopat $RML && \
rm -rf $RML &> /dev/null;
