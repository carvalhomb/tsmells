#ifndef CPPUNIT_TESTCASE_H
#define CPPUNIT_TESTCASE_H

#include <cppunit/Portability.h>
#include <cppunit/Test.h>
#include <cppunit/TestAssert.h>
#include <string>


// added assert-method macro-alikes to play nice with SourceNavigator


void CPPUNIT_ASSERT(boolean condition);
void CPPUNIT_ASSERT_MESSAGE(std::string message, boolean condition);
void CPPUNIT_FAIL(std::string message);
void CPPUNIT_ASSERT_DOUBLES_EQUAL(double expected, double actual, double delta);
template<class T> void CPPUNIT_ASSERT_EQUAL(const T& expected, const T& actual);
template<class T> void CPPUNIT_ASSERT_EQUAL_MESSAGE(string message, const T& expected, const T& actual);

namespace CppUnit {

class TestResult;


/* FIXME: most of this documentation belongs to proposed class Fixture.
 */

/*! \brief A single test object.
 *
 * This class is used to implement a simple test case: define a subclass
 * that overrides the runTest method.
 *
 * A test case defines the fixture to run multiple tests. 
 * To define a test case
 * do the following:
 * - implement a subclass of TestCase 
 * - the fixture is defined by instance variables 
 * - initialize the fixture state by overriding setUp
 *   (i.e. construct the instance variables of the fixture)
 * - clean-up after a test by overriding tearDown.
 *
 * Each test runs in its own fixture so there
 * can be no side effects among test runs.
 * Here is an example:
 * 
 * \code
 * class MathTest : public TestCase {
 *     protected: int m_value1;
 *     protected: int m_value2;
 *
 *     public: MathTest (string name)
 *                 : TestCase (name) {
 *     }
 *
 *     protected: void setUp () {
 *         m_value1 = 2;
 *         m_value2 = 3;
 *     }
 * }
 * \endcode
 *
 * For each test implement a method which interacts
 * with the fixture. Verify the expected results with assertions specified
 * by calling CPPUNIT_ASSERT on the expression you want to test:
 * 
 * \code
 *    protected: void testAdd () {
 *        int result = value1 + value2;
 *        CPPUNIT_ASSERT (result == 5);
 *    }
 * \endcode
 * 
 * Once the methods are defined you can run them. To do this, use
 * a TestCaller.
 *
 * \code
 * Test *test = new TestCaller<MathTest>("testAdd", MathTest::testAdd);
 * test->run ();
 * \endcode
 *
 *
 * The tests to be run can be collected into a TestSuite. 
 * 
 * \code
 * public: static TestSuite *MathTest::suite () {
 *      TestSuite *suiteOfTests = new TestSuite;
 *      suiteOfTests->addTest(new TestCaller<MathTest>(
 *                              "testAdd", testAdd));
 *      suiteOfTests->addTest(new TestCaller<MathTest>(
 *                              "testDivideByZero", testDivideByZero));
 *      return suiteOfTests;
 *  }
 * \endcode
 * 
 *
 * \see TestResult
 * \see TestSuite 
 * \see TestCaller
 *
 */


class TestCase : public Test
{
public:

    TestCase         (std::string Name);
    //! \internal
    TestCase         ();
    ~TestCase        ();
    
    virtual void        run              (TestResult *result);
    virtual int         countTestCases   () const;
    std::string         getName          () const;
    std::string         toString         () const;

    //! FIXME: what is this for?
    virtual TestResult  *run             ();

    // FIXME: move back to class TestFixture, in future.
    virtual void        setUp            ();
    virtual void        tearDown         ();


  /*  public void         CPPUNIT_ASSERT_THROW(expression, ExceptionType);
    public void         CPPUNIT_ASSERT_NO_THROW(expression);
    public void         CPPUNIT_ASSERT_ASSERTION_FAIL(assertion);
    public void         CPPUNIT_ASSERT_ASSERTION_PASS(assertion); */
 
    
protected:
    //! FIXME: this should probably be pure virtual.
    virtual void        runTest          ();

    //! Create TestResult for the run(void) method.
    TestResult          *defaultResult   ();
    
private:
    TestCase (const TestCase& other); 
    TestCase& operator= (const TestCase& other); 
    
private:
    const std::string   m_name;
};

} // namespace CppUnit

#endif // CPPUNIT_TESTCASE_H 
