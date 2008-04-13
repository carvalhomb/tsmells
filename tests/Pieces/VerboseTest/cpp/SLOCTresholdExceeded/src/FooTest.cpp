#include <cppunit/TestCase.h>
#include "UUT.h"

using CppUnit::TestCase;

class FooTest : public TestCase {
public:
	void testFoo();
private:
	UUT uut;
};

void FooTest::testFoo() {
	uut.foo();
	uut.fal();
	uut.bar();
	uut.baz();
	uut.git();

	uut.foo();
	uut.fal();
	uut.bar();
	uut.baz();
	uut.git();

	uut.foo();
	uut.fal();
	uut.bar();
	uut.baz();
	uut.git();

	uut.foo();
	uut.fal();
	uut.bar();
	uut.baz();
	uut.git();
}
