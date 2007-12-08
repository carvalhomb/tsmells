#include <cppunit/TestCase.h>
#include <cppunit/TestSuite.h>

using CppUnit::TestSuite;
using CppUnit::TestCase;
using CppUnit::Test;


class TestFileInput : public TestCase {
public:
			TestFileInput();
			~TestFileInput();

	void 	setUp();
	void 	tearDown();

	void 	testIfstream();
	void 	testCopen();
	void 	testCfopen();
	void 	testXPopen();

	static 	Test* suite();
};
