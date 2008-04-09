#include "cppunit/TestSuite.h"
#include "cppunit/TestCaller.h"
#include "MyTest.h"
#include "Stuff.h"

using CppUnit::TestSuite;
using CppUnit::TestCaller;

void MyTest::testCommand() {
    A a;
    B b;
    a.a();
    b.b();

    D d;
    int i;
    i = d.d;
}

Test* MyTest::suite() {
    TestSuite *suite = new TestSuite();
    suite->addTest(new TestCaller<MyTest>("testCommand", &MyTest::testCommand));
    return suite;
}
