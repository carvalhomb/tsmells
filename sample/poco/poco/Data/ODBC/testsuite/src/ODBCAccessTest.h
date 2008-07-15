//
// ODBCAccessTest.h
//
// $Id: //poco/Main/Data/ODBC/testsuite/src/ODBCAccessTest.h#4 $
//
// Definition of the ODBCAccessTest class.
//
// Copyright (c) 2006, Applied Informatics Software Engineering GmbH.
// and Contributors.
//
// Permission is hereby granted, free of charge, to any person or organization
// obtaining a copy of the software and accompanying documentation covered by
// this license (the "Software") to use, reproduce, display, distribute,
// execute, and transmit the Software, and to prepare derivative works of the
// Software, and to permit third-parties to whom the Software is furnished to
// do so, all subject to the following:
// 
// The copyright notices in the Software and this entire statement, including
// the above license grant, this restriction and the following disclaimer,
// must be included in all copies of the Software, in whole or in part, and
// all derivative works of the Software, unless such copies or derivative
// works are solely in the form of machine-executable object code generated by
// a source language processor.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
// SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
// FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//


#ifndef ODBCAccessTest_INCLUDED
#define ODBCAccessTest_INCLUDED


#include "Poco/Data/ODBC/ODBC.h"
#include "Poco/Data/Session.h"
#include "Poco/Data/ODBC/Utility.h"
#include "Poco/SharedPtr.h"
#include "CppUnit/TestCase.h"
#include "SQLExecutor.h"


class ODBCAccessTest: public CppUnit::TestCase
	/// MS Access ODBC test class
	/// Tested:
	/// 
	/// Driver		|	DB		| OS
	/// ------------+-----------+------------------------------------------
	///	4.00.6305.00| Jet 4.0	| MS Windows XP Professional x64 v.2003/SP1
{
public:
	ODBCAccessTest(const std::string& name);
	~ODBCAccessTest();

	void testSimpleAccess();

	void setUp();
	void tearDown();

	static CppUnit::Test* suite();

private:
	void dropTable(const std::string& tableName);
	void recreatePersonTable();

	static bool init(const std::string& driver, const std::string& dsn);
	static bool canConnect(const std::string& driver, const std::string& dsn);

	static Poco::Data::ODBC::Utility::DriverMap _drivers;
	static std::string _dbConnString;
	static Poco::Data::Session* _pSession;
	bool _owner;
};


#endif // ODBCAccessTest_INCLUDED
