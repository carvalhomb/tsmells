#ifndef FOOTEST_INCLUDED
#define FOOTEST_INCLUDED

#include <cppunit/Test.h>
#include <cppunit/TestFixture.h>

class FooTest : public CppUnit::TestFixture
{
public:
    void testFoo();
};

#endif // FOOTEST_INCLUDED
