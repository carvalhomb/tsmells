//
// RandomTest.cpp
//
// $Id: //poco/svn/Foundation/testsuite/src/RandomTest.cpp#2 $
//
// Copyright (c) 2004-2006, Applied Informatics Software Engineering GmbH.
// and Contributors.
//
// Permission is hereby granted, free of charge, to any person or organization
// obtaining a copy of the software and accompanying documentation covered by
// this license (the "Software") to use, reproduce, display, distribute,
// execute, and transmit the Software, and to prepare derivative works of the
// Software, and to permit third-parties to whom the Software is furnished to
// do so, all subject to the following:
// 
// The copyright notices in the Software and this entire statement, including
// the above license grant, this restriction and the following disclaimer,
// must be included in all copies of the Software, in whole or in part, and
// all derivative works of the Software, unless such copies or derivative
// works are solely in the form of machine-executable object code generated by
// a source language processor.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
// SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
// FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//


#include "RandomTest.h"
#include "CppUnit/TestCaller.h"
#include "CppUnit/TestSuite.h"
#include "Poco/Random.h"
#include <vector>
#include <cmath>


using Poco::Random;
using Poco::UInt32;


RandomTest::RandomTest(const std::string& name): CppUnit::TestCase(name)
{
}


RandomTest::~RandomTest()
{
}


void RandomTest::testSequence1()
{
	Random rnd1;
	Random rnd2;
	rnd1.seed(12345);
	rnd2.seed(12345);
	for (int i = 0; i < 100; ++i)
	{
		assert (rnd1.next() == rnd2.next());
	}
}


void RandomTest::testSequence2()
{
	Random rnd1;
	Random rnd2;
	rnd1.seed(12345);
	rnd2.seed(54321);
	
	bool equals = true;	
	for (int i = 0; i < 20; ++i)
	{
		if (rnd1.next() != rnd2.next())
		{
			equals = false;
			break;
		}
	}
	assert (!equals);
}


void RandomTest::testDistribution1()
{
	Random rnd;
	rnd.seed(123456);
	const int n = 11;
	int d[n] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	for (int i = 0; i < 100; ++i)
	{
		d[rnd.next() % n] = 1;
	}
	int sum = 0;
	for (int k = 0; k < n; ++k) sum += d[k];
	
	assert (sum == n);
}


void RandomTest::testDistribution2()
{
	Random rnd;
	rnd.seed();
	const int n = 101;
	std::vector<int> d(n, 0);
	for (int i = 0; i < 10000; ++i)
	{
		d[rnd.next(n)]++;
	}
	int sum = 0;
	for (int k = 0; k < n; ++k) sum += d[k];
	int avg = sum/n;
	int var = 0;
	for (int k = 0; k < n; ++k) var += (d[k] - avg)*(d[k] - avg);
	var /= n;
	int sd = int(std::sqrt((double) var));
	
	assert (95 < avg && avg < 105);
	assert (sd < 15);
}


void RandomTest::testDistribution3()
{
	Random rnd;
	rnd.seed();
	const int n = 101;
	std::vector<int> d(n, 0);
	for (int i = 0; i < 10000; ++i)
	{
		d[int(rnd.nextFloat()*n)]++;
	}
	int sum = 0;
	for (int k = 0; k < n; ++k) sum += d[k];
	int avg = sum/n;
	int var = 0;
	for (int k = 0; k < n; ++k) var += (d[k] - avg)*(d[k] - avg);
	var /= n;
	int sd = int(std::sqrt((double) var));
	
	assert (95 < avg && avg < 105);
	assert (sd < 15);
}


void RandomTest::setUp()
{
}


void RandomTest::tearDown()
{
}


CppUnit::Test* RandomTest::suite()
{
	CppUnit::TestSuite* pSuite = new CppUnit::TestSuite("RandomTest");

	CppUnit_addTest(pSuite, RandomTest, testSequence1);
	CppUnit_addTest(pSuite, RandomTest, testSequence2);
	CppUnit_addTest(pSuite, RandomTest, testDistribution1);
	CppUnit_addTest(pSuite, RandomTest, testDistribution2);
	CppUnit_addTest(pSuite, RandomTest, testDistribution3);

	return pSuite;
}
