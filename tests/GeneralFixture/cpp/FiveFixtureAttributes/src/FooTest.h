
#include <cppunit/TestCase.h>

#include "Foo.h"
#include "Fuu.h"
#include "Fal.h"
#include "Bar.h"
#include "Baz.h"

using CppUnit::TestCase;

class FooTest : public TestCase 
{

public:
    void setUp();
    void testFoo();

private:
    Bar* bar;
    Baz* baz;
    Fal* fal;
    Foo* foo;
    Fuu* fuu;

};
