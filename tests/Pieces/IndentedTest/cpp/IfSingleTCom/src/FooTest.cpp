#include "FooTest.h"

#include <cppunit/TestAssert.h>
#include <cppunit/TestCaller.h>
#include <cppunit/TestSuite.h>

void FooTest::testFoo()
{
    if (true) 
    {
        CPPUNIT_ASSERT(true);
    }
}
