#ifndef CPPUNIT_TESTCASE_H
#define CPPUNIT_TESTCASE_H

#include <string>

// added assert-method macro-alikes to play nice
void CPPUNIT_ASSERT(boolean condition);
void CPPUNIT_ASSERT_MESSAGE(std::string message, boolean condition);
void CPPUNIT_FAIL(std::string message);
void CPPUNIT_ASSERT_DOUBLES_EQUAL(double expected, double actual, double delta);
template<class T> void CPPUNIT_ASSERT_EQUAL(const T& expected, const T& actual);
template<class T> void CPPUNIT_ASSERT_EQUAL_MESSAGE(string message, const T& expected, const T& actual);

namespace CppUnit {

class TestResult;

class TestCase
{
public:

    TestCase         (std::string Name);
    TestCase         ();
    ~TestCase        ();

    virtual void        run              (TestResult *result);
    virtual int         countTestCases   () const;
    std::string         getName          () const;
    std::string         toString         () const;
    virtual TestResult  *run             ();
    virtual void        setUp            ();
    virtual void        tearDown         ();

protected:

    virtual void        runTest          ();
    TestResult          *defaultResult   ();
};

} // namespace CppUnit

#endif // CPPUNIT_TESTCASE_H 
