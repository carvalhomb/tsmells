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

define(`TSMELLS', `/home/nix/JaarProj/SmellsGgl')
include(XUNIT_INIT)
include(TSMELLS`/src/count.rml')
include(TSMELLS`/src/initAuxiliaryTestRelations.rml')

ifdef(`ASSERTIONLESS', `include(TSMELLS`/src/AssertionLess.rml')', `')
ifdef(`ASSERTION_ROULETTE_TRESHOLD', `', `define(`ASSERTION_ROULETTE_TRESHOLD', `3')')
ifdef(`ASSERTION_ROULETTE', `include(TSMELLS`/src/AssertionRoulette.rml')', `')
ifdef(`DUPLICATED_CODE_TRESHOLD', `', `define(`DUPLICATED_CODE_TRESHOLD', `5')')
ifdef(`DUPLICATED_CODE', `include(TSMELLS`/src/DuplicatedCode.rml')', `')
ifdef(`FOR_TESTERS_ONLY', `include(TSMELLS`/src/ForTestersOnly.rml')', `')
ifdef(`INDENTED_TEST', `include(TSMELLS`/src/IndentedTest.rml')', `')
ifdef(`INDIRECT_TEST_TRESHOLD', `', `define(`INDIRECT_TEST_TRESHOLD', `5')')
ifdef(`INDIRECT_TEST', `include(TSMELLS`/src/IndirectTest.rml')', `')
ifdef(`MYSTERY_GUEST_BLACKLIST', `include(MYSTERY_GUEST_BLACKLIST)', `')
ifdef(`MYSTERY_GUEST', `include(TSMELLS`/src/MysteryGuest.rml')', `')
ifdef(`SENSITIVE_EQUALITY', `include(TSMELLS`/src/SensitiveEquality.rml')', `')
