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

define(`TSMELLS', `/home/nix/JaarProj/SmellsGgl/')
define(`DUMP_DIR', TSMELLS`src/dump/')
include(DUMP_DIR`provideCount.rml')

ifdef(`BASIC_JUNIT', `include(DUMP_DIR`initJavaTestEntities.rml')', `')
ifdef(`ANAST_JUNIT', `include(DUMP_DIR`initJavaTestEntitiesAnastacia.rml')', `')
include(DUMP_DIR`initAuxiliary.rml')
ifdef(`DUMP_TEST_ENTITIES', `include(DUMP_DIR`writeTestEntities.rml')', `')
ifdef(`ASSERTIONLESS', `include(DUMP_DIR`computeAssertionLess.rml')', `')
ifdef(`ASSERTION_ROULETTE_TRESHOLD', `', `define(`ASSERTION_ROULETTE_TRESHOLD', `3')')
ifdef(`ASSERTION_ROULETTE', `include(DUMP_DIR`computeAssertionRoulette.rml')', `')
ifdef(`DUPLICATED_CODE_TRESHOLD', `', `define(`DUPLICATED_CODE_TRESHOLD', `5')')
ifdef(`DUPLICATED_CODE', `include(DUMP_DIR`computeDuplicatedCode.rml')', `')
ifdef(`FOR_TESTERS_ONLY', `include(DUMP_DIR`computeForTestersOnly.rml')', `')
ifdef(`INDENTED_TEST', `include(DUMP_DIR`computeIndentedTest.rml')', `')
ifdef(`INDIRECT_TEST_TRESHOLD', `', `define(`INDIRECT_TEST_TRESHOLD', `5')')
ifdef(`INDIRECT_TEST', `include(DUMP_DIR`computeIndirectTest.rml')', `')
ifdef(`MYSTERY_GUEST_BLACKLIST', `include(MYSTERY_GUEST_BLACKLIST)', `')
ifdef(`MYSTERY_GUEST', `include(DUMP_DIR`computeMysteryGuest.rml')', `')
ifdef(`SENSITIVE_EQUALITY', `include(DUMP_DIR`computeSensitiveEquality.rml')', `')
