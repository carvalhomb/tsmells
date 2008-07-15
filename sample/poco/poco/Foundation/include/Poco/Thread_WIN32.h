//
// Thread_WIN32.h
//
// $Id: //poco/svn/Foundation/include/Poco/Thread_WIN32.h#2 $
//
// Library: Foundation
// Package: Threading
// Module:  Thread
//
// Definition of the ThreadImpl class for WIN32.
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


#ifndef Foundation_Thread_WIN32_INCLUDED
#define Foundation_Thread_WIN32_INCLUDED


#include "Poco/Foundation.h"
#include "Poco/Runnable.h"
#include "Poco/UnWindows.h"


namespace Poco {


class Foundation_API ThreadImpl
{
public:	
	enum Priority
	{
		PRIO_LOWEST_IMPL  = THREAD_PRIORITY_LOWEST,
		PRIO_LOW_IMPL     = THREAD_PRIORITY_BELOW_NORMAL,
		PRIO_NORMAL_IMPL  = THREAD_PRIORITY_NORMAL,
		PRIO_HIGH_IMPL    = THREAD_PRIORITY_ABOVE_NORMAL,
		PRIO_HIGHEST_IMPL = THREAD_PRIORITY_HIGHEST
	};

	ThreadImpl();				
	~ThreadImpl();

	void setPriorityImpl(int prio);
	int getPriorityImpl() const;
	void startImpl(Runnable& target);

	void joinImpl();
	bool joinImpl(long milliseconds);
	bool isRunningImpl() const;
	static void sleepImpl(long milliseconds);
	static void yieldImpl();
	static ThreadImpl* currentImpl();

protected:
#if defined(_DLL)
	static DWORD WINAPI entry(LPVOID pThread);
#else
	static unsigned __stdcall entry(void* pThread);
#endif

private:
	Runnable* _pTarget;
	HANDLE    _thread;
	int       _prio;

	static DWORD _currentKey;
};


//
// inlines
//
inline int ThreadImpl::getPriorityImpl() const
{
	return _prio;
}


inline void ThreadImpl::sleepImpl(long milliseconds)
{
	Sleep(DWORD(milliseconds));
}


inline void ThreadImpl::yieldImpl()
{
	Sleep(0);
}


} // namespace Poco


#endif // Foundation_Thread_WIN32_INCLUDED
