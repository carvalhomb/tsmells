//
// Preparation.cpp
//
// $Id: //poco/Main/Data/ODBC/src/Preparation.cpp#5 $
//
// Library: Data
// Package: DataCore
// Module:  Preparation
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


#include "Poco/Data/ODBC/Preparation.h"
#include "Poco/Data/ODBC/ODBCMetaColumn.h"


namespace Poco {
namespace Data {
namespace ODBC {


const std::size_t Preparation::INVALID_ROW = std::numeric_limits<std::size_t>::max();


Preparation::Preparation(const StatementHandle& rStmt, 
	const std::string& statement, 
	std::size_t maxFieldSize,
	DataExtraction dataExtraction): 
	_rStmt(rStmt),
	_maxFieldSize(maxFieldSize),
	_dataExtraction(dataExtraction)
{
	SQLCHAR* pStr = (SQLCHAR*) statement.c_str();
	if (Utility::isError(SQLPrepare(_rStmt, pStr, (SQLINTEGER) statement.length())))
		throw StatementException(_rStmt);
}


Preparation::Preparation(const Preparation& other): 
	_rStmt(other._rStmt),
	_maxFieldSize(other._maxFieldSize),
	_dataExtraction(other._dataExtraction)
{
	resize();
}


Preparation::~Preparation()
{
	freeMemory();
}


void Preparation::freeMemory() const
{
	IndexMap::iterator it = _varLengthArrays.begin();
	IndexMap::iterator end = _varLengthArrays.end();
	for (; it != end; ++it)	
	{
		switch (it->second)
		{
		case DT_BOOL:
			deleteCachedArray<bool>(it->first);
			break;
		
		case DT_CHAR:
			deleteCachedArray<char>(it->first);
			break;

		case DT_CHAR_ARRAY:
		{
			char* pc = AnyCast<char>(&_values[it->first]);
			std::free(pc);
			break;
		}

		case DT_BOOL_ARRAY:
		{
			bool* pb = AnyCast<bool>(&_values[it->first]);
			std::free(pb);
			break;
		}

		case DT_DATE:
			deleteCachedArray<SQL_DATE_STRUCT>(it->first);
			break;

		case DT_TIME:
			deleteCachedArray<SQL_TIME_STRUCT>(it->first);
			break;

		case DT_DATETIME:
			deleteCachedArray<SQL_TIMESTAMP_STRUCT>(it->first);
			break;
		}
	}
}


std::size_t Preparation::columns() const
{
	if (_values.empty()) resize();
	return _values.size();
}


void Preparation::resize() const
{
	SQLSMALLINT nCol = 0;
	if (!Utility::isError(SQLNumResultCols(_rStmt, &nCol)) && 0 != nCol)
	{
		_values.resize(nCol, 0);
		_lengths.resize(nCol, 0);
		_lenLengths.resize(nCol);
		if(_varLengthArrays.size())
		{
			freeMemory();
			_varLengthArrays.clear();
		}
	}
}


std::size_t Preparation::maxDataSize(std::size_t pos) const
{
	poco_assert_dbg (pos < _values.size());

	std::size_t sz = 0;
	std::size_t maxsz = getMaxFieldSize();

	try 
	{
		ODBCMetaColumn mc(_rStmt, pos);
		sz = mc.length();

		// accomodate for terminating zero (non-bulk only!)
		if (!isBulk() && ODBCMetaColumn::FDT_STRING == mc.type()) ++sz;
	}
	catch (StatementException&) { }

	if (!sz || sz > maxsz) sz = maxsz;

	return sz;
}


std::size_t Preparation::actualDataSize(std::size_t col, std::size_t row) const
{
	SQLLEN size = (INVALID_ROW == row) ? _lengths.at(col) :
		_lenLengths.at(col).at(row);

	// workaround for drivers returning negative length
	if (size < 0 && SQL_NULL_DATA != size) size *= -1;

	return size;
}


void Preparation::prepareCharArray(std::size_t pos, SQLSMALLINT valueType, std::size_t size, std::size_t length)
{
	poco_assert_dbg (DE_BOUND == _dataExtraction);
	poco_assert_dbg (pos < _values.size());
	poco_assert_dbg (pos < _lengths.size());
	poco_assert_dbg (pos < _lenLengths.size());

	char* pArray = (char*) std::calloc(length * size, sizeof(char));

	_values[pos] = Any(pArray);
	_lengths[pos] = 0;
	_lenLengths[pos].resize(length);
	_varLengthArrays.insert(IndexMap::value_type(pos, DT_CHAR_ARRAY));

	if (Utility::isError(SQLBindCol(_rStmt, 
		(SQLUSMALLINT) pos + 1, 
		valueType, 
		(SQLPOINTER) pArray, 
		(SQLINTEGER) size, 
		&_lenLengths[pos][0])))
	{
		throw StatementException(_rStmt, "SQLBindCol()");
	}
}


void Preparation::prepareBoolArray(std::size_t pos, SQLSMALLINT valueType, std::size_t length)
{
	poco_assert_dbg (DE_BOUND == _dataExtraction);
	poco_assert_dbg (pos < _values.size());
	poco_assert_dbg (pos < _lengths.size());
	poco_assert_dbg (pos < _lenLengths.size());

	bool* pArray = (bool*) std::calloc(length, sizeof(bool));

	_values[pos] = Any(pArray);
	_lengths[pos] = 0;
	_lenLengths[pos].resize(length);
	_varLengthArrays.insert(IndexMap::value_type(pos, DT_BOOL_ARRAY));

	if (Utility::isError(SQLBindCol(_rStmt, 
		(SQLUSMALLINT) pos + 1, 
		valueType, 
		(SQLPOINTER) pArray, 
		(SQLINTEGER) sizeof(bool), 
		&_lenLengths[pos][0])))
	{
		throw StatementException(_rStmt, "SQLBindCol()");
	}
}


} } } // namespace Poco::Data::ODBC
