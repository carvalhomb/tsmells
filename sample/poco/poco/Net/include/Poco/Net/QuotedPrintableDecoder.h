//
// QuotedPrintableDecoder.h
//
// $Id: //poco/svn/Net/include/Poco/Net/QuotedPrintableDecoder.h#2 $
//
// Library: Net
// Package: Messages
// Module:  QuotedPrintableDecoder
//
// Definition of the QuotedPrintableDecoder class.
//
// Copyright (c) 2005-2006, Applied Informatics Software Engineering GmbH.
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


#ifndef Net_QuotedPrintableDecoder_INCLUDED
#define Net_QuotedPrintableDecoder_INCLUDED


#include "Poco/Net/Net.h"
#include "Poco/UnbufferedStreamBuf.h"
#include <istream>


namespace Poco {
namespace Net {


class Net_API QuotedPrintableDecoderBuf: public Poco::UnbufferedStreamBuf
	/// This streambuf decodes all quoted-printable (see RFC 2045) 
	/// encoded data read from the istream connected to it.
{
public:
	QuotedPrintableDecoderBuf(std::istream& istr);
	~QuotedPrintableDecoderBuf();
	
private:
	int readFromDevice();

	std::istream& _istr;
};


class Net_API QuotedPrintableDecoderIOS: public virtual std::ios
	/// The base class for QuotedPrintableDecoder.
	///
	/// This class is needed to ensure the correct initialization
	/// order of the stream buffer and base classes.
{
public:
	QuotedPrintableDecoderIOS(std::istream& istr);
	~QuotedPrintableDecoderIOS();
	QuotedPrintableDecoderBuf* rdbuf();

protected:
	QuotedPrintableDecoderBuf _buf;
};


class Net_API QuotedPrintableDecoder: public QuotedPrintableDecoderIOS, public std::istream
	/// This istream decodes all quoted-printable (see RFC 2045)
	/// encoded data read from the istream connected to it.
{
public:
	QuotedPrintableDecoder(std::istream& istr);
	~QuotedPrintableDecoder();
};


} } // namespace Poco::Net


#endif // Net_QuotedPrintableDecoder_INCLUDED
