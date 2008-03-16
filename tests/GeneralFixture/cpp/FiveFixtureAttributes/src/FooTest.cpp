#include "FooTest.h"

void FooTest::setUp() 
{
	bar = new Bar;
	baz = new Baz;
	fal = new Fal;
	foo = new Foo;
	fuu = new Fuu;
}

void FooTest::testFoo() 
{
	CPPUNIT_ASSERT(true);
}
