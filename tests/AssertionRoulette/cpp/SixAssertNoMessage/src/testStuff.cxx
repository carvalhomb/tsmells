#include "testStuff.h"

void TestStuff::setUp() {
}

void TestStuff::tearDown() {
}

void TestStuff::testCommand() {
	CPPUNIT_ASSERT( 1 > 2 );
	CPPUNIT_ASSERT( 2 > 3 );
	CPPUNIT_ASSERT( 3 > 4 );
	CPPUNIT_ASSERT( 4 > 5 );
	CPPUNIT_ASSERT( 5 > 6 );
	CPPUNIT_ASSERT( 6 > 7 );
}

Test* TestStuff::suite() {
	TestSuite *suite = new TestSuite;
	sumFormulaSuite->addTest(new TestCaller<TestStuff>("testCommand", &TestStuff::testCommand));
	return suite;
}

	