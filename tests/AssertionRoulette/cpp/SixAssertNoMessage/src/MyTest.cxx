#include "MyTest.h"

void TestStuff::testCommand() {
    CPPUNIT_ASSERT( 1 > 2 );
    CPPUNIT_ASSERT( 2 > 3 );
    CPPUNIT_ASSERT( 3 > 4 );
    CPPUNIT_ASSERT( 4 > 5 );
    CPPUNIT_ASSERT( 5 > 6 );
    CPPUNIT_ASSERT( 6 > 7 );
}
