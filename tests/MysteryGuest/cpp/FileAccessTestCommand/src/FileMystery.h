#include <cppunit/TestCase.h>

using CppUnit::TestCase;

class TestFileInput : public TestCase {
public:
    void   testIfstream();
    void   testCopen();
    void   testCfopen();
};
