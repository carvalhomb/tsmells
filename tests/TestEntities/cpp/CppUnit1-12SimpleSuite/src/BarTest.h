//
// BarTest.h
//
// $Id: /teststuite/src/BarTest.h#1 $
//
// A stub testcase
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


#ifndef BARTEST_INCLUDED
#define BARTEST_INCLUDED

#include <cppunit/Test.h>
#include <cppunit/TestFixture.h>

class BarTest : public CppUnit::TestFixture
{
public:
	BarTest();
	virtual ~BarTest();

	void testRoulette();

	void setUp();
	void tearDown();

	static CppUnit::Test* suite();

private:
	void helperThrow();
    void helperNoThrow();
};

#endif // BARTEST_INCLUDED
