//
// AsyncStreamChannel.h
//
// $Id: //poco/svn/Foundation/include/Poco/AsyncStreamChannel.h#2 $
//
// Library: Foundation
// Package: AsyncIO
// Module:  AsyncStreamChannel
//
// Definition of the AsyncStreamChannel class.
//
// Copyright (c) 2007, Applied Informatics Software Engineering GmbH.
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


#ifndef Foundation_AsyncStreamChannel_INCLUDED
#define Foundation_AsyncStreamChannel_INCLUDED


#include "Poco/Foundation.h"
#include "Poco/AsyncIOChannel.h"
#include <istream>
#include <ostream>
#include <iostream>


namespace Poco {


class Foundation_API AsyncStreamChannel: public AsyncIOChannel
	/// AsyncStreamChannel provides an AsyncIOChannel for I/O streams.
	///
	/// Usage Example:
	///     std::stringstream str;
	///     AsyncStreamChannel channel(str);
	///     channel.enqueue(new AsyncWriteCommand("Hello", 5));
	///     channel.enqueue(new AsyncWriteCommand(", ", 2));
	///     ActiveResult<int> result = channel.enqueue(new AsyncWriteCommand("world!", 6));
	///     result.wait();
	///     std::string s(str.str());
{
public:
	AsyncStreamChannel(std::istream& istr);
		/// Creates an AsyncStreamChannel using the given input stream.
		/// Only read and seek operations will be allowed.
		
	AsyncStreamChannel(std::ostream& ostr);
		/// Creates an AsyncStreamChannel using the given output stream.
		/// Only write and seek operations will be allowed.

	AsyncStreamChannel(std::iostream& iostr);
		/// Creates an AsyncStreamChannel using the given input/output stream.

	~AsyncStreamChannel();
		/// Destroys the AsyncStreamChannel.
	
	// AsyncIOChannel
	int write(const void* buffer, int length);
	int read(void* buffer, int length);
	int seek(std::streamoff off, std::ios::seekdir dir);
			
private:
	AsyncStreamChannel();
	
	std::istream* _pIstr;
	std::ostream* _pOstr;
};


} // namespace Poco


#endif // Foundation_AsyncStreamChannel_INCLUDED
