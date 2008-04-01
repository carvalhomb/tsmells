m4 -DTSMELLS=$TSMELLS -DXUNIT_INIT=$TSMELLS/src/dump/rml/initQtUnitEntities.rml $TSMELLS/scripts/tsmells.m4 > tmp.rml
cat QtUnitTestCase1.rsf | crocopat tmp.rml