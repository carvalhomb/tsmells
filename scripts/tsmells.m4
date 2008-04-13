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
define(`TSMELLS_LOG', tsmells.log)
define(`TSMELLS_TMP', _now_tmp)
define(`TIMER', ` [$(TSMELLS`/scripts/timer.sh' TSMELLS_TMP) s] >> TSMELLS_LOG')
define(`ECHO', echo '->' )
define(`START_TIMER2', `date +%s > TSMELLS_TMP2')
define(`TIMER2', ` [$(TSMELLS`/scripts/timer.sh' TSMELLS_TMP2) s] >> TSMELLS_LOG')
define(`ECHO2', echo '    ->')

PRINT "(01) RSF model in memory ... ", ENDL TO STDERR;
EXEC "ECHO RSF loaded TIMER";

PRINT "(02) extracting xUnit entities ... ", ENDL TO STDERR;
include(XUNIT_INIT)
include(DUMP_DIR`provideCount.rml')
include(DUMP_DIR`initAuxiliary.rml')
ifdef(`DUMP_TEST_ENTITIES', 
    `include(DUMP_DIR`writeTestEntities.rml')' , `')
EXEC "ECHO xUnit initializated TIMER";

ifdef(`ASSERTIONLESS',
    `PRINT "(03) AssertionLess ... ", ENDL TO STDERR;'
    `include(DUMP_DIR`computeAssertionLess.rml')'
    EXEC "ECHO AssertionLess TIMER";
,
    `')

ifdef(`ASSERTION_ROULETTE',
    `PRINT "(04) AssertionRoulette ... ", ENDL TO STDERR;'
    `include(DUMP_DIR`computeAssertionRoulette.rml')'
    EXEC "ECHO AssertionRoulette TIMER";
,
    `')

ifdef(`DUPLICATED_CODE',
    `PRINT "(05) DuplicatedCode ... ", ENDL TO STDERR;'
    `include(DUMP_DIR`computeDuplicatedCode.rml')'
    EXEC "ECHO DuplicatedCode TIMER";
,
    `')

ifdef(`EAGER_TEST',
    `PRINT "(06) EagerTest ... ", ENDL TO STDERR;'
    `include(DUMP_DIR`computeEagerTest.rml')'
    EXEC "ECHO EagerTest TIMER";
,
    `')

ifdef(`EMPTY_TEST',
    `PRINT "(07) EmptyTest ... ", ENDL TO STDERR;'
    `include(DUMP_DIR`computeEmptyTest.rml')'
    EXEC "ECHO EmptyTest TIMER";
,
    `')

ifdef(`FOR_TESTERS_ONLY',
    `PRINT "(08) ForTestersOnly ... ", ENDL TO STDERR;'
    `include(DUMP_DIR`computeForTestersOnly.rml')'
    EXEC "ECHO ForTestersOnly TIMER";
,
    `')

ifdef(`GENERAL_FIXTURE',
    `PRINT "(09) GeneralFixture ... ", ENDL TO STDERR;'
    `include(DUMP_DIR`computeGeneralFixture.rml')'
    EXEC "ECHO GeneralFixture TIMER";
,
    `')

ifdef(`INDENTED_TEST',
    `PRINT "(10) IndentedTest ...", ENDL TO STDERR;'
    `include(DUMP_DIR`computeIndentedTest.rml')'
    EXEC "ECHO IndentedTest TIMER";
,
    `')

ifdef(`INDIRECT_TEST',
    `PRINT "(12) IndirectTest ... ", ENDL TO STDERR;'
    `include(DUMP_DIR`computeIndirectTest.rml')'
    EXEC "ECHO IndirectTest TIMER";
,
    `')

ifdef(`MYSTERY_GUEST', 
    `PRINT "(13) MysteryGuest ... ", ENDL TO STDERR;'
    `include(DUMP_DIR`computeMysteryGuest.rml')'
    EXEC "ECHO MysteryGuest TIMER";
,
    `')

ifdef(`SENSITIVE_EQUALITY',
    `PRINT "(14) SensitiveEquality ... ", ENDL TO STDERR;'
    `include(DUMP_DIR`computeSensitiveEquality.rml')'
    EXEC "ECHO SensitiveEquality TIMER";
,
    `')

ifdef(`VERBOSE_TEST',
    `PRINT "(15) VerboseTest ... ", ENDL TO STDERR;'
    `include(DUMP_DIR`computeVerboseTest.rml')'
    EXEC "ECHO VerboseTest TIMER";
,
    `')

EXEC "rm -f TSMELLS_TMP";
