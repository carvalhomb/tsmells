define(`TSMELLS', `/home/nix/JaarProj/SmellsGgl')

include(TSMELLS`/src/initJavaTestEntities.rml')

ifdef(`INDIRECT_TEST_TRESHOLD', `', `define(`INDIRECT_TEST_TRESHOLD', `5')')
ifdef(`INDIRECT_TEST', `include(TSMELLS`/src/IndirectTest.rml')', `BOO')

