#include <cppunit/TestCase.h>
#include <cppunit/TestSuite.h>
#include <cppunit/TextTestResult.h>

using CppUnit::TestSuite;
using CppUnit::TextTestResult;
using std::cerr;
using std::cout;
using std::endl;

int main(int argc, char *argv[]) {
	TestSuite suite;
	TextTestResult testResult;
	
	suite.addTest(TestSymbolTable::suite());
	suite.addTest(TestFormulaParser::suite());
	suite.addTest(TestMatrixFormulaParser::suite());
	suite.run(&testResult);
	cout << testResult << endl;
	
	return 0;
}
