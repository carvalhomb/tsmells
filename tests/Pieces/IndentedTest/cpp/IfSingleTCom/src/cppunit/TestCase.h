#ifndef CPPUNIT_TESTCASE_H
#define CPPUNIT_TESTCASE_H

#include <cppunit/Portability.h>
#include <cppunit/TestLeaf.h>
#include <cppunit/TestAssert.h>
#include <cppunit/TestFixture.h>
#include <string>


namespace CppUnit
{

class TestResult;

class TestCase : public TestLeaf, public TestFixture
{
public:
    TestCase( const std::string &name );
    TestCase();
    ~TestCase();
    virtual void run(TestResult *result);
    std::string getName() const;
    virtual void runTest();
};

}

#endif // CPPUNIT_TESTCASE_H 
