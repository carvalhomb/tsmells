//
// SecureSocketImpl.cpp
//
// $Id: //poco/svn/NetSSL_OpenSSL/src/SecureSocketImpl.cpp#1 $
//
// Library: NetSSL_OpenSSL
// Package: SSLSockets
// Module:  SecureSocketImpl
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


#include "Poco/Net/SecureSocketImpl.h"
#include "Poco/Net/SSLException.h"
#include "Poco/Net/SSLManager.h"
#include "Poco/Net/Utility.h"
#include "Poco/Net/SecureStreamSocketImpl.h"
#include "Poco/Net/StreamSocketImpl.h"
#include "Poco/Net/NetException.h"
#include "Poco/Net/DNS.h"
#include "Poco/NumberFormatter.h"
#include "Poco/NumberParser.h"
#include "Poco/String.h"
#include "Poco/RegularExpression.h"
#include <openssl/x509v3.h>
#include <openssl/err.h>


using Poco::IOException;
using Poco::TimeoutException;
using Poco::InvalidArgumentException;
using Poco::NumberFormatter;
using Poco::Timespan;


// workaround for C++-incompatible macro
#define POCO_BIO_set_nbio_accept(b,n) BIO_ctrl(b,BIO_C_SET_ACCEPT,1,(void*)((n)?"a":NULL))



namespace Poco {
namespace Net {


SecureSocketImpl::SecureSocketImpl():_pBIO(0), _pSSL(0)
{
}


SecureSocketImpl::SecureSocketImpl(SSL *pSSL): _pSSL(pSSL)
{
	poco_check_ptr (_pSSL);

	_pBIO = SSL_get_rbio(_pSSL);
	poco_check_ptr (_pBIO);
	int tmpSocket = 0;
	BIO_get_fd(_pBIO, &tmpSocket);
	setSockfd(tmpSocket);

}


SecureSocketImpl::~SecureSocketImpl()
{
	close();
}

	
SocketImpl* SecureSocketImpl::acceptConnection(SocketAddress& clientAddr)
{
	poco_assert (sockfd() != POCO_INVALID_SOCKET);
	poco_check_ptr (_pBIO);

	BIO* pClient = 0;
	int rc = 0;

	do
	{
		rc = BIO_do_accept(_pBIO);
	}
	while (rc <= 0 && _socket.lastError() == POCO_EINTR);

	if (rc > 0)
	{
		pClient = BIO_pop(_pBIO);
		poco_check_ptr (pClient);

		SSL* pSSL = SSL_new(SSLManager::instance().defaultServerContext()->sslContext());
		if (pSSL)
		{
			SSL_set_accept_state(pSSL);
			SSL_set_bio(pSSL, pClient, pClient);
			int err = SSL_accept(pSSL);
			
			if (err > 0)
			{
				SecureStreamSocketImpl* pSI = new SecureStreamSocketImpl(pSSL);
				clientAddr = pSI->peerAddress();
				std::string clientName = clientAddr.host().toString();

				if (X509_V_OK != postConnectionCheck(true, pSSL, clientName))
				{
					delete pSI;
					pSI = 0;
					SSL_shutdown(pSSL);
					SSL_free(pSSL);
					pClient = 0;
					SocketImpl::error("postConnectionCheck failed"); // will throw
				}

				return pSI;
			}
			else
			{
				std::string errMsg = Utility::convertSSLError(pSSL, err);
				SSL_shutdown(pSSL);
				SSL_free(pSSL);
				SocketImpl::error(std::string("failed to acceptConnection: ") + errMsg);
			}
		}
		else
		{
			BIO_free(pClient);
		}
		
	}
	SocketImpl::error(); // will throw
	return 0;
}


void SecureSocketImpl::connect(const SocketAddress& address)
{
	if (sockfd() == POCO_INVALID_SOCKET)
	{
		if (!_pBIO)
			_pBIO = BIO_new(BIO_s_connect());
	}

	int rc = 0;
	do
	{
		BIO_set_conn_hostname(_pBIO, address.host().toString().c_str());
		int tmp = address.port();
		BIO_set_conn_int_port(_pBIO, &tmp);
		rc = BIO_do_connect(_pBIO); // returns 1 in case of ok!
	}
	while (rc != 1 && _socket.lastError() == POCO_EINTR);

	if (rc != 1) SocketImpl::error(address.toString());

	establishTunnel();
	connectSSL(address);
	poco_check_ptr (_pSSL);
}


void SecureSocketImpl::connect(const SocketAddress& address, const Poco::Timespan& timeout)
{
	poco_assert (sockfd() == POCO_INVALID_SOCKET);
	poco_assert (_pSSL == 0);
	poco_assert (_pBIO == 0);

	_pBIO = BIO_new(BIO_s_connect());
	POCO_BIO_set_nbio_accept(_pBIO, 1); // set nonblocking
	
	try
	{
		BIO_set_conn_hostname(_pBIO, address.host().toString().c_str());
		int tmp = address.port();
		BIO_set_conn_int_port(_pBIO, &tmp);
		int rc = BIO_do_connect(_pBIO); // returns 1 in case of ok!

		if (rc != 1)
		{
			if (_socket.lastError() != POCO_EINPROGRESS && _socket.lastError() != POCO_EWOULDBLOCK)
				SocketImpl::error(address.toString());
			if (!_socket.poll(timeout, SocketImpl::SELECT_READ | SocketImpl::SELECT_WRITE))
				throw Poco::TimeoutException("connect timed out", address.toString());
			int err = _socket.socketError();
			if (err != 0) SocketImpl::error(err);
		}

		establishTunnel();
		connectSSL(address);
		poco_check_ptr (_pSSL);
	}
	catch (Poco::Exception&)
	{
		POCO_BIO_set_nbio_accept(_pBIO, 0);
		throw;
	}
	POCO_BIO_set_nbio_accept(_pBIO, 0);
}


void SecureSocketImpl::connectNB(const SocketAddress& address)
{
	if (sockfd() == POCO_INVALID_SOCKET)
	{
		if(!_pBIO)
			_pBIO = BIO_new(BIO_s_connect());
	}

	POCO_BIO_set_nbio_accept(_pBIO, 1); //setnonBlocking
	BIO_set_conn_hostname(_pBIO, address.host().toString().c_str());
	int tmp = address.port();
	BIO_set_conn_int_port(_pBIO, &tmp);

	int rc = BIO_do_connect(_pBIO); // returns 1 in case of ok!

	if (rc != 1)
	{
		if (_socket.lastError() != POCO_EINPROGRESS && _socket.lastError() != POCO_EWOULDBLOCK)
			SocketImpl::error(address.toString());
	}
	else
	{
		establishTunnel();
		connectSSL(address);
		poco_check_ptr (_pSSL);
	}
}


void SecureSocketImpl::bind(const SocketAddress& address, bool reuseAddress)
{
	_socket.bind(address, reuseAddress);
}

	
void SecureSocketImpl::listen(int backlog)
{
	_socket.listen(backlog);
	_pBIO = BIO_new (BIO_s_accept());
	BIO_set_fd(_pBIO, (int)sockfd(), BIO_CLOSE);
}


void SecureSocketImpl::close()
{
	if (_pSSL)
	{
		if (SSL_get_shutdown(_pSSL) & SSL_RECEIVED_SHUTDOWN)
		{
			SSL_shutdown(_pSSL);
		}
		else
		{
			SSL_clear(_pSSL);
		}
		SSL_free(_pSSL); // frees _pBIO
		_pSSL = 0;
		_pBIO = 0;
	}

	if (_pBIO)
	{
		BIO_free_all(_pBIO); //free all, even BIOs for pending connections
		_pBIO = 0;
	}
	invalidate(); // the socket is already invalid, although the fd still contains a meaningful value, correct that
}


int SecureSocketImpl::sendBytes(const void* buffer, int length, int flags)
{
	poco_assert (sockfd() != POCO_INVALID_SOCKET);
	poco_check_ptr (_pSSL);

	int rc;
	do
	{
		rc = SSL_write(_pSSL, buffer, length);
		if (rc < 0)
		{
			std::string errMsg = Utility::convertSSLError(_pSSL, rc);
		}
	}
	while (rc < 0 && _socket.lastError() == POCO_EINTR);
	if (rc < 0) SocketImpl::error();
	return rc;
}


int SecureSocketImpl::receiveBytes(void* buffer, int length, int flags)
{
	poco_assert (sockfd() != POCO_INVALID_SOCKET);
	poco_check_ptr (_pSSL);	

	int rc;
	bool renegotiating = false;
	do
	{
		rc = SSL_read(_pSSL, buffer, length);
		if (rc <= 0)
		{
			switch (SSL_get_error(_pSSL, rc))
			{
			case SSL_ERROR_ZERO_RETURN:
				// connection closed
				close();
				break;
			case SSL_ERROR_NONE:
			case SSL_ERROR_WANT_WRITE: //renegotiation
			case SSL_ERROR_WANT_READ: //renegotiation
				renegotiating = true;
				break;
			default:
				;
			}
		}
	}
	while (rc < 0 && _socket.lastError() == POCO_EINTR);
	if (rc < 0) 
	{
		if (renegotiating || _socket.lastError() == POCO_EAGAIN || _socket.lastError() == POCO_ETIMEDOUT)
			throw TimeoutException();
		else
			SocketImpl::error("failed to read bytes");
	}
	return rc;
}


int SecureSocketImpl::sendTo(const void* buffer, int length, const SocketAddress& address, int flags)
{
	throw NetException("sendTo not possible with SSL");
}


int SecureSocketImpl::receiveFrom(void* buffer, int length, SocketAddress& address, int flags)
{
	throw NetException("receiveFrom not possible with SSL");
}


void SecureSocketImpl::sendUrgent(unsigned char data)
{
	// SSL doesn't support out-of-band data
	sendBytes(reinterpret_cast<const void*>(&data), sizeof(data));
}


long SecureSocketImpl::postConnectionCheck(bool server, SSL* pSSL, const std::string& hostName)
{
	static std::string locHost("127.0.0.1");

	SSLManager& mgr = SSLManager::instance();
	Context::VerificationMode mode = server? mgr.defaultServerContext()->verificationMode() : mgr.defaultClientContext()->verificationMode();
	if (hostName == locHost && mode != Context::VERIFY_STRICT)
		return X509_V_OK;

	X509* cert = 0;
	X509_NAME* subj = 0;
	char* host = const_cast<char*>(hostName.c_str());
	
	int extcount=0;

	if (mode == Context::VERIFY_NONE) // should we allow none on the client side?
	{
		return X509_V_OK;
	}

	cert = SSL_get_peer_certificate(pSSL);
	
	// note: the check is used by the client, so as long we don't set None at the client we reject
	// cases where no certificate/incomplete info is presented by the server
	if ((!cert || !host) && mode != Context::VERIFY_NONE)
	{
		if (cert)
			X509_free(cert);
		return X509_V_ERR_APPLICATION_VERIFICATION;
	}

	bool ok = false;

	if ((extcount = X509_get_ext_count(cert)) > 0)
	{
		for (int i = 0; i < extcount && !ok; ++i)
		{
			const char* extstr = 0;
			X509_EXTENSION* ext;
			ext = X509_get_ext(cert, i);
			extstr = OBJ_nid2sn(OBJ_obj2nid(X509_EXTENSION_get_object(ext)));

			if (!strcmp(extstr, "subjectAltName"))
			{
				X509V3_EXT_METHOD* meth = X509V3_EXT_get(ext);
				if (!meth)
					break;

#if OPENSSL_VERSION_NUMBER >= 0x00908000
				const unsigned char* pData = ext->value->data;
				const unsigned char** ppData = &pData;
#else
				unsigned char* pData = ext->value->data;
				unsigned char** ppData = &pData;
#endif
				STACK_OF(CONF_VALUE)* val = meth->i2v(meth, meth->d2i(0, ppData, ext->value->length), 0);

				for (int j = 0; j < sk_CONF_VALUE_num(val) && !ok; ++j)
				{
					CONF_VALUE* nval = sk_CONF_VALUE_value(val, j);
					if (!strcmp(nval->name, "DNS") && !strcmp(nval->value, host))
					{
						ok = true;
					}
				}
			}
		}
	}

	char data[256];
	if (!ok && (subj = X509_get_subject_name(cert)) && X509_NAME_get_text_by_NID(subj, NID_commonName, data, 256) > 0)
	{
		data[255] = 0;
		
		std::string strData(data); // commonName can contain wildcards like *.appinf.com
		try
		{
			// two cases: strData contains wildcards or not
			if (SecureSocketImpl::containsWildcards(strData))
			{
				// a compare by IPAddress is not possible with wildcards
				// only allow compare by name
				const HostEntry& heData = DNS::resolve(hostName);
				ok = SecureSocketImpl::matchByAlias(strData, heData);
			}
			else
			{
				// it depends on hostname if we compare by IP or by alias
				IPAddress ip;
				if (IPAddress::tryParse(hostName, ip))
				{
					// compare by IP
					const HostEntry& heData = DNS::resolve(strData);
					const HostEntry::AddressList& addr = heData.addresses();
					HostEntry::AddressList::const_iterator it = addr.begin();
					HostEntry::AddressList::const_iterator itEnd = addr.end();
					for (; it != itEnd && !ok; ++it)
					{
						ok = (*it == ip);
					}
				}
				else
				{
					// compare by name
					const HostEntry& heData = DNS::resolve(hostName);
					ok = SecureSocketImpl::matchByAlias(strData, heData);
				}
			}
		}
		catch(HostNotFoundException&)
		{
			if (cert)
				X509_free(cert);
			return X509_V_ERR_APPLICATION_VERIFICATION;
		}
	}

	if (cert)
		X509_free(cert);

	// we already have a verify callback registered so no need to ask twice SSL_get_verify_result(pSSL);
	if (ok)
		return X509_V_OK;

	return X509_V_ERR_APPLICATION_VERIFICATION;
}


void SecureSocketImpl::connectSSL(const SocketAddress& address)
{
	if (!_pSSL)
	{
		_pSSL = SSL_new(SSLManager::instance().defaultClientContext()->sslContext());
		SSL_set_bio(_pSSL, _pBIO, _pBIO);
	}
	std::string errMsg;

	int ret = SSL_connect(_pSSL);
	
	if (ret <= 0)
	{
		errMsg = Utility::convertSSLError(_pSSL, ret);
		throw SSLException(errMsg);
	}
	
	std::string serverName = address.host().toString();
	long errCode = 0;
	if (_endHost.empty())
		postConnectionCheck(false, _pSSL, serverName);
	else
		postConnectionCheck(false, _pSSL, _endHost);
	bool err = false;

	if (errCode != X509_V_OK)
	{
		err = true;
		errMsg = Utility::convertCertificateError(errCode);
	}
	else
	{
		int tmpSocket=0;
		BIO_get_fd(_pBIO,&tmpSocket);
		poco_assert (-1 != tmpSocket);
		setSockfd(tmpSocket);
	}

	if (err)
	{
		SSL_free(_pSSL); // dels _pBIO too
		_pSSL = 0;
		_pBIO = 0;
		invalidate();
		throw InvalidCertificateException(errMsg);
	}
}


void SecureSocketImpl::establishTunnel()
{
	if (!_endHost.empty())
	{
		poco_check_ptr (_pBIO);
		// send CONNECT proxyHost:proxyPort HTTP/1.0\r\n\r\n
		std::string connect("CONNECT ");
		connect.append(_endHost);
		connect.append(":");
		connect.append(Poco::NumberFormatter::format(_endPort));
		connect.append(" HTTP/1.0\r\n\r\n");
		int rc = BIO_write(_pBIO, (const void*) connect.c_str(), (int)(connect.length()*sizeof(char)));
		if (rc != connect.length())
			throw SSLException("Failed to establish connection to proxy");
		// get the response
		char resp[512];
		rc = BIO_read(_pBIO, resp, 512*sizeof(char));
		std::string response(resp);
		if (response.find("200") == std::string::npos)
			throw SSLException("Failed to establish connection to proxy");
	}
}


bool SecureSocketImpl::containsWildcards(const std::string& commonName)
{
	return (commonName.find('*') != std::string::npos || commonName.find('?') != std::string::npos);
}


bool SecureSocketImpl::matchByAlias(const std::string& alias, const HostEntry& heData)
{
	// fix wildcards
	std::string aliasRep = Poco::replace(alias, "*", ".*");
	Poco::replaceInPlace(aliasRep, "..*", ".*");
	Poco::replaceInPlace(aliasRep, "?", ".?");
	Poco::replaceInPlace(aliasRep, "..?", ".?");
	// compare by name
	Poco::RegularExpression expr(aliasRep);
	bool found = false;
	const HostEntry::AliasList& aliases = heData.aliases();
	HostEntry::AliasList::const_iterator it = aliases.begin();
	HostEntry::AliasList::const_iterator itEnd = aliases.end();
	for (; it != itEnd && !found; ++it)
	{
		found = expr.match(*it);
	}

	return found;
}


} } // namespace Poco::Net
