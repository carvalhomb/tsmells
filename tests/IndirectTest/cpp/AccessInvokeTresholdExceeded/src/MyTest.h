#include "cppunit/Test.h"
#include "cppunit/TestCase.h"


using CppUnit::Test;
using CppUnit::TestCase;

class MyTest : public TestCase
{
public:
    void testCommand();
    static Test* suite();
};
