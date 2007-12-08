#include <cppunit/TestCase.h>

using CppUnit::TestCase;

class MyTest : public TestCase {
	public:
		void	testCommand() {
			CPPUNIT_ASSERT(1 < 2);
		}
};
