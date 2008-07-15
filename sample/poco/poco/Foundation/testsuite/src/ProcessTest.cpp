//
// ProcessTest.cpp
//
// $Id: //poco/svn/Foundation/testsuite/src/ProcessTest.cpp#2 $
//
// Copyright (c) 2005-2006, Applied Informatics Software Engineering GmbH.
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


#include "ProcessTest.h"
#include "CppUnit/TestCaller.h"
#include "CppUnit/TestSuite.h"
#include "Poco/Process.h"
#include "Poco/Pipe.h"
#include "Poco/PipeStream.h"


using Poco::Process;
using Poco::ProcessHandle;
using Poco::Pipe;
using Poco::PipeInputStream;
using Poco::PipeOutputStream;


ProcessTest::ProcessTest(const std::string& name): CppUnit::TestCase(name)
{
}


ProcessTest::~ProcessTest()
{
}


void ProcessTest::testLaunch()
{
	std::string name("TestApp");
	std::string cmd;
#if defined(_DEBUG)
	name += "d";
#endif

#if defined(POCO_OS_FAMILY_UNIX)
	cmd = "./";
	cmd += name;
#else
	cmd = name;
#endif

	std::vector<std::string> args;
	args.push_back("arg1");
	args.push_back("arg2");
	args.push_back("arg3");
	ProcessHandle ph = Process::launch(cmd, args);
	int rc = ph.wait();
	assert (rc == 3);
}


void ProcessTest::testLaunchRedirectIn()
{
	std::string name("TestApp");
	std::string cmd;
#if defined(_DEBUG)
	name += "d";
#endif

#if defined(POCO_OS_FAMILY_UNIX)
	cmd = "./";
	cmd += name;
#else
	cmd = name;
#endif

	std::vector<std::string> args;
	args.push_back("-count");
	Pipe inPipe;
	ProcessHandle ph = Process::launch(cmd, args, &inPipe, 0, 0);
	PipeOutputStream ostr(inPipe);
	ostr << std::string(100, 'x');
	ostr.close();
	int rc = ph.wait();
	assert (rc == 100);
}


void ProcessTest::testLaunchRedirectOut()
{
	std::string name("TestApp");
	std::string cmd;
#if defined(_DEBUG)
	name += "d";
#endif

#if defined(POCO_OS_FAMILY_UNIX)
	cmd = "./";
	cmd += name;
#else
	cmd = name;
#endif

	std::vector<std::string> args;
	args.push_back("-hello");
	Pipe outPipe;
	ProcessHandle ph = Process::launch(cmd, args, 0, &outPipe, 0);
	PipeInputStream istr(outPipe);
	std::string s;
	int c = istr.get();
	while (c != -1) { s += (char) c; c = istr.get(); }
	assert (s == "Hello, world!");
	int rc = ph.wait();
	assert (rc == 1);
}


void ProcessTest::setUp()
{
}


void ProcessTest::tearDown()
{
}


CppUnit::Test* ProcessTest::suite()
{
	CppUnit::TestSuite* pSuite = new CppUnit::TestSuite("ProcessTest");

	CppUnit_addTest(pSuite, ProcessTest, testLaunch);
	CppUnit_addTest(pSuite, ProcessTest, testLaunchRedirectIn);
	CppUnit_addTest(pSuite, ProcessTest, testLaunchRedirectOut);

	return pSuite;
}
