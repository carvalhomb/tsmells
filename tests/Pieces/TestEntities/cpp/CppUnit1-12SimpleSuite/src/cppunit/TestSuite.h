#ifndef CPPUNIT_TESTSUITE_H    // -*- C++ -*-
#define CPPUNIT_TESTSUITE_H

#include <cppunit/Portability.h>

#if CPPUNIT_NEED_DLL_DECL
#pragma warning( push )
#pragma warning( disable: 4251 )  // X needs to have dll-interface to be used by clients of class Z
#endif

#include <cppunit/TestComposite.h>
#include <cppunit/portability/CppUnitVector.h>

namespace CppUnit
{


class TestSuite
{
public:
  TestSuite( std::string name = "" );
  ~TestSuite();
  void addTest( Test *test );
  const CppUnitVector<Test *> &getTests() const;
  virtual void deleteContents();
  int getChildTestCount() const;
  Test *doGetChildTestAt( int index ) const;
};


}

#endif // CPPUNIT_TESTSUITE_H
