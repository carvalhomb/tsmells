//
// FooTest.h
//
// $Id: /teststuite/src/FooTest.h#1 $
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
// Foobar is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Foobar is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
//


#ifndef FOOTEST_INCLUDED
#define FOOTEST_INCLUDED

#include <cppunit/Test.h>
#include <cppunit/TestFixture.h>

class FooTest : public CppUnit::TestFixture
{
public:
	FooTest();
	virtual ~FooTest();

	void setUp();
	void tearDown();

	void testSomething();
	void testSomeMore();

	static CppUnit::Test* suite();

private:
	int	_fooBar;
	int* _pFooBar;
};

#endif // FOOTEST_INCLUDED
