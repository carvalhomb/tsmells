//
// BinaryReaderWriter.cpp
//
// $Id: //poco/svn/Foundation/samples/BinaryReaderWriter/src/BinaryReaderWriter.cpp#1 $
//
// This sample demonstrates the BinaryWriter and BinaryReader classes.
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


#include "Poco/BinaryWriter.h"
#include "Poco/BinaryReader.h"
#include <sstream>
#include <iostream>


using Poco::BinaryWriter;
using Poco::BinaryReader;


int main(int argc, char** argv)
{
	std::stringstream str;
	
	BinaryWriter writer(str);
	writer << true
	       << 'x'
	       << 42
	       << 3.14159265
	       << "foo bar";
	       
	bool   b;
	char   c;
	int    i;
	double d;
	std::string s;
	
	BinaryReader reader(str);
	reader >> b
	       >> c
	       >> i
	       >> d
	       >> s;
	       
	std::cout << b << std::endl
	          << c << std::endl
	          << i << std::endl
	          << d << std::endl
	          << s << std::endl;
	          	
	return 0;
}
