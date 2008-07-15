//
// XMLReader.cpp
//
// $Id: //poco/svn/XML/src/XMLReader.cpp#2 $
//
// Library: XML
// Package: SAX
// Module:  SAX
//
// Copyright (c) 2004-2006, Applied Informatics Software Engineering GmbH.
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


#include "Poco/SAX/XMLReader.h"


namespace Poco {
namespace XML {


const XMLString XMLReader::FEATURE_VALIDATION                  = toXMLString("http://xml.org/sax/features/validation");
const XMLString XMLReader::FEATURE_NAMESPACES                  = toXMLString("http://xml.org/sax/features/namespaces");
const XMLString XMLReader::FEATURE_NAMESPACE_PREFIXES          = toXMLString("http://xml.org/sax/features/namespace-prefixes");
const XMLString XMLReader::FEATURE_EXTERNAL_GENERAL_ENTITIES   = toXMLString("http://xml.org/sax/features/external-general-entities");
const XMLString XMLReader::FEATURE_EXTERNAL_PARAMETER_ENTITIES = toXMLString("http://xml.org/sax/features/external-parameter-entities");
const XMLString XMLReader::FEATURE_STRING_INTERNING            = toXMLString("http://xml.org/sax/features/string-interning");
const XMLString XMLReader::PROPERTY_DECLARATION_HANDLER        = toXMLString("http://xml.org/sax/properties/declaration-handler");
const XMLString XMLReader::PROPERTY_LEXICAL_HANDLER            = toXMLString("http://xml.org/sax/properties/lexical-handler");


XMLReader::~XMLReader()
{
}


} } // namespace Poco::XML
