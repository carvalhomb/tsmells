#ifndef CPPUNIT_EXTENSIONS_HELPERMACROS_H
#define CPPUNIT_EXTENSIONS_HELPERMACROS_H

#include <cppunit/TestCaller.h>
#include <cppunit/TestSuite.h>
#include <cppunit/extensions/AutoRegisterSuite.h>
#include <cppunit/extensions/ExceptionTestCaseDecorator.h>
#include <cppunit/extensions/TestFixtureFactory.h>
#include <cppunit/extensions/TestNamer.h>
#include <cppunit/extensions/TestSuiteBuilderContext.h>
#include <memory>

void CPPUNIT_ASSERT(bool condition);
void CPPUNIT_ASSERT_MESSAGE(std::string message, bool condition);
void CPPUNIT_FAIL(std::string message);
void CPPUNIT_ASSERT_DOUBLES_EQUAL(double expected, double actual, double delta);
template<class T> void CPPUNIT_ASSERT_EQUAL(const T& expected, const T& actual);
template<class T> void CPPUNIT_ASSERT_EQUAL_MESSAGE(string message, const T& expected, const T& actual);

#endif  // CPPUNIT_EXTENSIONS_HELPERMACROS_H
