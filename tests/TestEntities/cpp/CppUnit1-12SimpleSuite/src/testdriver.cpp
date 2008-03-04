//
// testdriver.cpp
//
// $Id: /teststuite/src/testdriver.cpp#1 $
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

#include <cppunit/CompilerOutputter.h>
#include <cppunit/TestResult.h>
#include <cppunit/TestResultCollector.h>
#include <cppunit/TestRunner.h>
#include <cppunit/BriefTestProgressListener.h>
#include <cppunit/ui/text/TestRunner.h>

#include "RootSuite.h"

RootSuite theSuite;

bool runSilent()
/// Silent test runner
/// Prints a dot for each test and
/// some elaboration in case of failure
{
	CppUnit::TextUi::TestRunner runner;
	runner.addTest(theSuite.suite());
	return runner.run();
}

bool runVerbose()
/// Verbose test runner
/// Prints each test case + testcommand while running
{
	CppUnit::TestResult testresult;
	CppUnit::TestResultCollector collectedresults;
    testresult.addListener(&collectedresults);

	CppUnit::BriefTestProgressListener progress;
	testresult.addListener (&progress);

	CppUnit::TestRunner testrunner;
	testrunner.addTest(theSuite.suite());
	testrunner.run (testresult);

	CppUnit::CompilerOutputter compileroutputter(&collectedresults, std::cout);
	compileroutputter.write();

	return collectedresults.wasSuccessful();
}

int main( int argc, char **argv)
{
	bool wasSuccess;
	wasSuccess = runSilent();
	//wasSuccess = runVerbose();
	return wasSuccess ? 0 : 1;
}
