//
// FooTest.cpp
//
// $Id: /teststuite/src/FooTest.cpp#1 $
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

#include "FooTest.h"

#include <cppunit/TestAssert.h>
#include <cppunit/TestCaller.h>
#include <cppunit/TestSuite.h>

FooTest::FooTest()
{
}

FooTest::~FooTest()
{
}

void FooTest::setUp()
{
	_fooBar = 10;
	_pFooBar = new int;
}

void FooTest::tearDown()
{
	delete _pFooBar;
}

void FooTest::testSomething()
{
	CPPUNIT_ASSERT_EQUAL_MESSAGE("sanity check", 2, 1+1);
}

void FooTest::testSomeMore()
{
	_fooBar = 2;
	CPPUNIT_ASSERT_EQUAL(2, _fooBar);

	*_pFooBar = 2;
	CPPUNIT_ASSERT_EQUAL(2, *_pFooBar);
}

CppUnit::Test* FooTest::suite()
{
	CppUnit::TestSuite* pSuite = new CppUnit::TestSuite("FooTest");
	typedef CppUnit::TestCaller<FooTest> fooCaller;

	pSuite->addTest(new fooCaller("testSomething", &FooTest::testSomething));
	pSuite->addTest(new fooCaller("testSomeMore", &FooTest::testSomeMore));

	return pSuite;
}
