//
// XMLConfigurationTest.cpp
//
// $Id: //poco/Main/Util/testsuite/src/XMLConfigurationTest.cpp#5 $
//
// Copyright (c) 2004-200, Applied Informatics Software Engineering GmbH.
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


#include "XMLConfigurationTest.h"
#include "CppUnit/TestCaller.h"
#include "CppUnit/TestSuite.h"
#include "Poco/Util/XMLConfiguration.h"
#include "Poco/AutoPtr.h"
#include "Poco/Exception.h"
#include <sstream>
#include <algorithm>


using Poco::Util::XMLConfiguration;
using Poco::Util::AbstractConfiguration;
using Poco::AutoPtr;
using Poco::NotImplementedException;
using Poco::NotFoundException;


XMLConfigurationTest::XMLConfigurationTest(const std::string& name): CppUnit::TestCase(name)
{
}


XMLConfigurationTest::~XMLConfigurationTest()
{
}


void XMLConfigurationTest::testLoad()
{
	static const std::string xmlFile = 
		"<config>"
		"	<prop1>value1</prop1>"
		"	<prop2>value2</prop2>"
		"	<prop3>"
		"		<prop4 attr='value3'/>"
		"		<prop4 attr='value4'/>"
		"	</prop3>"
		"	<prop5>value5</prop5>"
		"	<prop5>value6</prop5>"
		"</config>";
		
	std::istringstream istr(xmlFile);	
	AutoPtr<XMLConfiguration> pConf = new XMLConfiguration(istr);
	
	assert (pConf->getString("prop1") == "value1");
	assert (pConf->getString("prop2") == "value2");
	assert (pConf->getString("prop3.prop4[@attr]") == "value3");
	assert (pConf->getString("prop3.prop4[1][@attr]") == "value4");
	assert (pConf->getString("prop5") == "value5");
	assert (pConf->getString("prop5[0]") == "value5");
	assert (pConf->getString("prop5[1]") == "value6");
	
	AbstractConfiguration::Keys keys;
	pConf->keys(keys);
	assert (keys.size() == 5);
	assert (std::find(keys.begin(), keys.end(), "prop1") != keys.end());
	assert (std::find(keys.begin(), keys.end(), "prop2") != keys.end());
	assert (std::find(keys.begin(), keys.end(), "prop3") != keys.end());
	assert (std::find(keys.begin(), keys.end(), "prop5") != keys.end());
	assert (std::find(keys.begin(), keys.end(), "prop5[1]") != keys.end());
	
	pConf->keys("prop3", keys);
	assert (keys.size() == 2);
	assert (std::find(keys.begin(), keys.end(), "prop4") != keys.end());
	assert (std::find(keys.begin(), keys.end(), "prop4[1]") != keys.end());

    pConf->setString("prop1", "value1_changed");
	assert (pConf->getString("prop1") == "value1_changed");

    pConf->setString("prop2", "value2_changed");
	assert (pConf->getString("prop2") == "value2_changed");

    pConf->setString("prop3.prop4[@attr]", "value3_changed");
	assert (pConf->getString("prop3.prop4[@attr]") == "value3_changed");

    pConf->setString("prop3.prop4[1][@attr]", "value4_changed");
	assert (pConf->getString("prop3.prop4[1][@attr]") == "value4_changed");

    pConf->setString("prop5", "value5_changed");
	assert (pConf->getString("prop5") == "value5_changed");
	
    pConf->setString("prop5[0]", "value5_changed");
	assert (pConf->getString("prop5[0]") == "value5_changed");

    pConf->setString("prop5[1]", "value5_changed");
	assert (pConf->getString("prop5[1]") == "value5_changed");

	try
	{
		pConf->setString("foo", "bar");
		fail("node not found - must throw");
	}
	catch (NotFoundException&)
	{
	}
	
	try
	{
		std::string s = pConf->getString("foo");
		fail("No property - must throw");
	}
	catch (NotFoundException&)
	{
	}
}


void XMLConfigurationTest::setUp()
{
}


void XMLConfigurationTest::tearDown()
{
}


CppUnit::Test* XMLConfigurationTest::suite()
{
	CppUnit::TestSuite* pSuite = new CppUnit::TestSuite("XMLConfigurationTest");

	CppUnit_addTest(pSuite, XMLConfigurationTest, testLoad);

	return pSuite;
}
