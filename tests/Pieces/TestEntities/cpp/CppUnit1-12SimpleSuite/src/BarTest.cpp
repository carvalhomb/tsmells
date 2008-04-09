//
// BarTest.cpp
//
// $Id: /teststuite/src/BarTest.cpp#1 $
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

#include "BarTest.h"

#include <cppunit/TestAssert.h>
#include <cppunit/TestCaller.h>
#include <cppunit/TestSuite.h>

class MyException {};

BarTest::BarTest()
{
}

BarTest::~BarTest()
{
}

void BarTest::setUp()
{
}

void BarTest::tearDown()
{
}

void BarTest::testRoulette()
{
	//Assertions that a condition is true. 
	CPPUNIT_ASSERT(true);

	// Assertion with a user specified message. 
	CPPUNIT_ASSERT_MESSAGE("failure message", true);

	// Asserts that two values are equals. 
	CPPUNIT_ASSERT_EQUAL(5, 5);

	// Asserts that two values are equals, provides additional messafe on failure.
	CPPUNIT_ASSERT_EQUAL_MESSAGE("failure message", 5, 5); 

	// Macro for primitive value comparisons. 
	CPPUNIT_ASSERT_DOUBLES_EQUAL(5.0, 5.001, 0.1);

	// Asserts that the given expression throws an exception of the specified type. 
	CPPUNIT_ASSERT_THROW(BarTest::helperThrow(), MyException);

    // Asserts that the given expression does not throw any exceptions. 
	CPPUNIT_ASSERT_NO_THROW(BarTest::helperNoThrow());

	// Fails with the specified message. 
	CPPUNIT_FAIL("failure message"); 
}

void BarTest::helperThrow()
{
	MyException e;
	throw e;
}

void BarTest::helperNoThrow()
{
}

CppUnit::Test* BarTest::suite()
{
	CppUnit::TestSuite* pSuite = new CppUnit::TestSuite("BarTest");
	typedef CppUnit::TestCaller<BarTest> barCaller;

	pSuite->addTest(new barCaller("testRoulette", &BarTest::testRoulette));

	return pSuite;
}
