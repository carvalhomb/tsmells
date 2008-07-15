//
// TextTestResult.cpp
//
// $Id: //poco/Main/CppUnit/src/TextTestResult.cpp#7 $
//


#include "CppUnit/TextTestResult.h"
#include "CppUnit/CppUnitException.h"
#include "CppUnit/Test.h"
#include "CppUnit/estring.h"
#include <iostream>
#include <iomanip>


namespace CppUnit {


void TextTestResult::addError(Test* test, CppUnitException* e)
{
	TestResult::addError(test, e);
	std::cerr << "ERROR" << std::flush;
}


void TextTestResult::addFailure(Test* test, CppUnitException* e)
{
	TestResult::addFailure(test, e);
	std::cerr << "FAILURE" << std::flush;
}


void TextTestResult::startTest(Test* test)
{
	TestResult::startTest(test);
	std::cerr << "\n" << shortName(test->toString()) << ": ";
}


void TextTestResult::printErrors(std::ostream& stream)
{
	if (testErrors() != 0) 
	{
		stream << "\n";

		if (testErrors() == 1)
			stream << "There was " << testErrors() << " error: " << std::endl;
		else
			stream << "There were " << testErrors() << " errors: " << std::endl;

		int i = 1;
		for (std::vector<TestFailure*>::iterator it = errors().begin(); it != errors().end(); ++it)
		{
			TestFailure* failure = *it;
			CppUnitException* e = failure->thrownException();

			stream << std::setw(2) << i
			       << ": "
			       << failure->failedTest()->toString() << "\n"
			       << "    \"" << (e ? e->what() : "") << "\"\n"
			       << "    in \"" 
			       << (e ? e->fileName() : std::string())
			       << "\", line ";
			if (e == 0)
			{
				stream << "0";
			}
			else
			{
				stream << e->lineNumber();
				if (e->data2LineNumber() != CppUnitException::CPPUNIT_UNKNOWNLINENUMBER)
				{
					stream << " data lines " << e->data1LineNumber()
                                               << ", " << e->data2LineNumber();
				}
				else if (e->data1LineNumber() != CppUnitException::CPPUNIT_UNKNOWNLINENUMBER)
				{
					stream << " data line " << e->data1LineNumber();
				}
			}
			stream << "\n";
			i++;
		}
	}
}


void TextTestResult::printFailures(std::ostream& stream)
{
	if (testFailures() != 0)
	{
		stream << "\n";
		if (testFailures() == 1)
			stream << "There was " << testFailures() << " failure: " << std::endl;
		else
			stream << "There were " << testFailures() << " failures: " << std::endl;

		int i = 1;

		for (std::vector<TestFailure*>::iterator it = failures().begin(); it != failures().end(); ++it)
		{
			TestFailure* failure = *it;
			CppUnitException* e = failure->thrownException();

			stream << std::setw(2) << i
			       << ": "
			       << failure->failedTest()->toString() << "\n"
			       << "    \"" << (e ? e->what() : "") << "\"\n"
			       << "    in \"" 
			       << (e ? e->fileName() : std::string())
			       << "\", line ";
			if (e == 0)
			{
				stream << "0";
			}
			else
			{
				stream << e->lineNumber();
				if (e->data2LineNumber() != CppUnitException::CPPUNIT_UNKNOWNLINENUMBER)
				{
					stream << " data lines " 
					       << e->data1LineNumber()
                           << ", " << e->data2LineNumber();
				}
				else if (e->data1LineNumber() != CppUnitException::CPPUNIT_UNKNOWNLINENUMBER)
				{
					stream << " data line " << e->data1LineNumber();
				}
			}
			stream << "\n";
			i++;
		}
	}
}


void TextTestResult::print(std::ostream& stream)
{
	printHeader(stream);
	printErrors(stream);
	printFailures(stream);
}


void TextTestResult::printHeader(std::ostream& stream)
{
	std::cout << "\n\n";
	if (wasSuccessful())
		std::cout << "OK (" 
		          << runTests() << " tests)" 
		          << std::endl;
	else
		std::cout << "!!!FAILURES!!!" << std::endl
		          << "Runs: "
		          << runTests ()
		          << "   Failures: "
		          << testFailures ()
		          << "   Errors: "
		          << testErrors ()
		          << std::endl;
}


std::string TextTestResult::shortName(const std::string& testName)
{
	std::string::size_type pos = testName.rfind('.');
	if (pos != std::string::npos)
		return std::string(testName, pos + 1);
	else
		return testName;
}


} // namespace CppUnit
