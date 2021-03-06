//
// ODBC.h
//
// $Id: //poco/Main/Data/ODBC/include/Poco/Data/ODBC/ODBC.h#3 $
//
// Library: ODBC
// Package: ODBC
// Module:  ODBC
//
// Basic definitions for the Poco ODBC library.
// This file must be the first file included by every other ODBC
// header file.
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


#ifndef Data_ODBC_ODBC_INCLUDED
#define Data_ODBC_ODBC_INCLUDED


#include "Poco/Foundation.h"
#ifdef POCO_OS_FAMILY_WINDOWS
#include <windows.h>
#endif


//
// The following block is the standard way of creating macros which make exporting
// from a DLL simpler. All files within this DLL are compiled with the ODBC_EXPORTS
// symbol defined on the command line. this symbol should not be defined on any project
// that uses this DLL. This way any other project whose source files include this file see
// ODBC_API functions as being imported from a DLL, wheras this DLL sees symbols
// defined with this macro as being exported.
//
#if defined(_WIN32) && defined(POCO_DLL)
	#if defined(ODBC_EXPORTS)
		#define ODBC_API __declspec(dllexport)
	#else
		#define ODBC_API __declspec(dllimport)
	#endif
#endif


#if !defined(ODBC_API)
	#define ODBC_API
#endif



#include "Poco/Data/ODBC/Unicode.h"


//
// Automatically link Data library.
//
#if defined(_MSC_VER)
	#if !defined(POCO_NO_AUTOMATIC_LIBS) && !defined(ODBC_EXPORTS)
		#if defined(POCO_DLL)
			#if defined(_DEBUG)
				#pragma comment(lib, "PocoODBCd.lib")
			#else
				#pragma comment(lib, "PocoODBC.lib")
			#endif
		#else
			#if defined(_DEBUG)
				#pragma comment(lib, "PocoODBCmtd.lib")
			#else
				#pragma comment(lib, "PocoODBCmt.lib")
			#endif
		#endif
	#endif
#endif


#endif // ODBC_ODBC_INCLUDED
