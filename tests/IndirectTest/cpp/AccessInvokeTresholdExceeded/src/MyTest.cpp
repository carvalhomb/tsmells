#include "cppunit/TestSuite.h"
#include "cppunit/TestCaller.h"
#include "MyTest.h"
#include "Stuff.h"

using CppUnit::TestSuite;
using CppUnit::TestCaller;

void MyTest::testCommand() {
    A a;
    B b;
    C c;
    a.a();
    b.b();
    c.c();

    D d;
    E e;
    F f;
    int i;
    i = d.d;
    i = e.e;
    i = f.f;
}

Test* MyTest::suite() {
    TestSuite *suite = new TestSuite();
    suite->addTest(new TestCaller<MyTest>("testCommand", &MyTest::testCommand));
    return suite;
}
