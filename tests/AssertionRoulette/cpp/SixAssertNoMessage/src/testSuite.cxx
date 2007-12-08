#include <cppunit/TextTestResult.h>
#include "testStuff.h"

using CppUnit::TestSuite;
using CppUnit::TextTestResult;
using std::cout;
using std::endl;

int main(int argc, char*argv[]) {
	TestSuite suite;
	TextTestResult testResult;
	
	suite.addTest(TestStuff::suite());
	suite.run(&testResult);
	
	cout << testResult << endl;
	
	return 0;
}

