/**
 * This file is part of TSmells
 *
 * TSmells is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation; either version 2 of the License, or (at your 
 * option) any later version.
 *
 * TSmells is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more 
 * details.
 *
 * You should have received a copy of the GNU General Public License along 
 * with TSmells; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA 
 *
 * Copyright 2007 Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be>
 */

/**
 * Main script. Select the smells scripts by passing
 * '-DSMELL_NAME' to m4. Be sure to adapt the 'TSMELLS'
 * variable correctly. These macros are desribed in MACROS.
 *
 **/

define(`DUMP_DIR',TSMELLS`/src/dump/rml/')

PRINT "(01) RSF model loaded in memory", ENDL TO STDERR;
PRINT "(02) extracting xUnit entities ... ", ENDL TO STDERR;
include(XUNIT_INIT)
include(DUMP_DIR`provideCount.rml')
include(DUMP_DIR`initAuxiliary.rml')
ifdef(`DUMP_TEST_ENTITIES', `include(DUMP_DIR`writeTestEntities.rml')' , `')

PRINT "(03) AssertionLess ... ", ENDL TO STDERR;
ifdef(`ASSERTIONLESS', `include(DUMP_DIR`computeAssertionLess.rml')', `')

PRINT "(04) AssertionRoulette ... ", ENDL TO STDERR;
ifdef(`ASSERTION_ROULETTE', `include(DUMP_DIR`computeAssertionRoulette.rml')', `')

PRINT "(05) DuplicatedCode ... ", ENDL TO STDERR;
ifdef(`DUPLICATED_CODE', `include(DUMP_DIR`computeDuplicatedCode.rml')', `')

PRINT "(06) EagerTest ...", ENDL TO STDERR;
ifdef(`EAGER_TEST', `include(DUMP_DIR`computeEagerTest.rml')', `')

PRINT "(06) ForTestersOnly ... ", ENDL TO STDERR;
ifdef(`FOR_TESTERS_ONLY', `include(DUMP_DIR`computeForTestersOnly.rml')', `')

PRINT "(07) GeneralFixture ... ", ENDL TO STDERR;
ifdef(`GENERAL_FIXTURE', `include(DUMP_DIR`computeGeneralFixture.rml')', `')

PRINT "(08) IndentedTest ...", ENDL TO STDERR;
ifdef(`INDENTED_TEST', `include(DUMP_DIR`computeIndentedTest.rml')', `')

PRINT "(09) IndirectTest ... ", ENDL TO STDERR;
ifdef(`INDIRECT_TEST', `include(DUMP_DIR`computeIndirectTest.rml')', `')

PRINT "(10) MysteryGuest ... ", ENDL TO STDERR;
ifdef(`MYSTERY_GUEST', `include(DUMP_DIR`computeMysteryGuest.rml')', `')

PRINT "(11) SensitiveEquality ... ", ENDL TO STDERR;
ifdef(`SENSITIVE_EQUALITY', `include(DUMP_DIR`computeSensitiveEquality.rml')', `')
