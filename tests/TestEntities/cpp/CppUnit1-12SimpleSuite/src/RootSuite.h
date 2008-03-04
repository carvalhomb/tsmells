//
// RootSuite.h
//
// $Id: /teststuite/src/RootSuite.h#1 $
//
// Some test runners
//
// Copyright (c) 2008 tmc @ University of Antwerp.
//     Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be> 
//     Christophe Dedecker <christophe.dedecker@student.ua.ac.be>
//     Tim Saquet <tim.saquet@student.ua.ac.be>
//
// This file is part of tmcup
//
// tmcup is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// tmcup is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with tmcup.  If not, see <http://www.gnu.org/licenses/>.
//

#include <cppunit/TestSuite.h>

#include "FooTest.h"
#include "BarTest.h"
#include "BazTest.h"

class RootSuite
/// Combines all testcases into a
/// a single suite.
/// 
/// Allows for easy toggling.
{
public:
	CppUnit::Test* suite();
};

CppUnit::Test* RootSuite::suite()
{
	CppUnit::TestSuite* pSuite = new CppUnit::TestSuite;

	// add all sub-suites to pSuite
	pSuite->addTest(FooTest::suite());
	pSuite->addTest(BarTest::suite());
	pSuite->addTest(BazTest::suite());

	return pSuite;
}
