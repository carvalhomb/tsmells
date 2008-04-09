//
// BazTest.cpp
//
// $Id: /teststuite/src/BazTest.cpp#1 $
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

#include "BazTest.h"

#include <cppunit/TestAssert.h>
#include <cppunit/TestCaller.h>
#include <cppunit/TestSuite.h>

BazTest::BazTest()
{
}

BazTest::~BazTest()
{
}

void BazTest::setUp()
{
	_fooBar = 10;
	_pFooBar = new int;
}

void BazTest::tearDown()
{
	delete _pFooBar;
}

void BazTest::testCommand0()
{
}

void BazTest::testCommand1()
{
}

void BazTest::testCommand2()
{
}

void BazTest::testCommand3()
{
}

void BazTest::testCommand4()
{
}

void BazTest::testCommand5()
{
}

void BazTest::testCommand6()
{
}

void BazTest::testCommand7()
{
}

void BazTest::testCommand8()
{
}

void BazTest::testCommand9()
{
}

CppUnit::Test* BazTest::suite()
{
	CppUnit::TestSuite* pSuite = new CppUnit::TestSuite("BazTest");
	typedef CppUnit::TestCaller<BazTest> bazCaller;

	pSuite->addTest(new bazCaller("testCommand0", &BazTest::testCommand0));
	pSuite->addTest(new bazCaller("testCommand1", &BazTest::testCommand1));
	pSuite->addTest(new bazCaller("testCommand2", &BazTest::testCommand2));
	pSuite->addTest(new bazCaller("testCommand3", &BazTest::testCommand3));
	pSuite->addTest(new bazCaller("testCommand4", &BazTest::testCommand4));
	pSuite->addTest(new bazCaller("testCommand5", &BazTest::testCommand5));
	pSuite->addTest(new bazCaller("testCommand6", &BazTest::testCommand6));
	pSuite->addTest(new bazCaller("testCommand7", &BazTest::testCommand7));
	pSuite->addTest(new bazCaller("testCommand8", &BazTest::testCommand8));
	pSuite->addTest(new bazCaller("testCommand9", &BazTest::testCommand9));

	return pSuite;
}
