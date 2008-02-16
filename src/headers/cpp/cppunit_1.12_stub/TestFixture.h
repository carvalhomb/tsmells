#ifndef CPPUNIT_TESTFIXTURE_H    // -*- C++ -*-
#define CPPUNIT_TESTFIXTURE_H

#include <cppunit/Portability.h>

namespace CppUnit 
{

class TestFixture
{
public:
  virtual ~TestFixture() {};
  virtual void setUp() {};
  virtual void tearDown() {};
};

}

#endif
