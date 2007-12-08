#include <cppunit/TestCase.h>
#include <cppunit/TestSuite.h>
#include <cppunit/TestCaller.h>

using CppUnit::TestSuite;
using CppUnit::TestCase;
using CppUnit::Test;
using CppUnit::TestCaller;


class TestStuff : public TestCase {
	public:

		void setUp();
		void tearDown();		
		
		void testCommand();
		static Test* suite();

};
