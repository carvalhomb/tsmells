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

PRINT "(01) RSF model loaded in memory", ENDL TO STDERR;
PRINT "(02) extracting xUnit entities ... ", ENDL TO STDERR;
include(XUNIT_INIT)
include(TSMELLS`/src/count.rml')
include(TSMELLS`/src/initAuxRel.rml')

PRINT "(03) AssertionLess ... ", ENDL TO STDERR;
ifdef(`ASSERTIONLESS', `include(TSMELLS`/src/AssertionLess.rml')', `')

PRINT "(04) AssertionRoulette ... ", ENDL TO STDERR;
ifdef(`ASSERTION_ROULETTE', `include(TSMELLS`/src/AssertionRoulette.rml')', `')

PRINT "(05) DuplicatedCode ... ", ENDL TO STDERR;
ifdef(`DUPLICATED_CODE', `include(TSMELLS`/src/DuplicatedCode.rml')', `')

PRINT "(06) ForTestersOnly ... ", ENDL TO STDERR;
ifdef(`FOR_TESTERS_ONLY', `include(TSMELLS`/src/ForTestersOnly.rml')', `')

PRINT "(07) IndentedTest ...", ENDL TO STDERR;
ifdef(`INDENTED_TEST', `include(TSMELLS`/src/IndentedTest.rml')', `')

PRINT "(08) IndirectTest ... ", ENDL TO STDERR;
ifdef(`INDIRECT_TEST', `include(TSMELLS`/src/IndirectTest.rml')', `')

PRINT "(09) MysteryGuest ... ", ENDL TO STDERR;
ifdef(`MYSTERY_GUEST', `include(TSMELLS`/src/MysteryGuest.rml')', `')

PRINT "(10) SensitiveEquality ... ", ENDL TO STDERR;
ifdef(`SENSITIVE_EQUALITY', `include(TSMELLS`/src/SensitiveEquality.rml')', `')
