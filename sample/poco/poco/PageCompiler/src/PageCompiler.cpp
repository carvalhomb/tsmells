//
// PageCompiler.cpp
//
// $Id: //poco/Main/PageCompiler/src/PageCompiler.cpp#2 $
//
// A compiler that compiler HTML pages containing JSP directives into C++ classes.
//
// Copyright (c) 2008, Applied Informatics Software Engineering GmbH.
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


#include "Poco/Util/Application.h"
#include "Poco/Util/Option.h"
#include "Poco/Util/OptionSet.h"
#include "Poco/Util/HelpFormatter.h"
#include "Poco/Util/AbstractConfiguration.h"
#include "Poco/AutoPtr.h"
#include "Poco/FileStream.h"
#include "Poco/Path.h"
#include "Poco/DateTime.h"
#include "Poco/DateTimeFormatter.h"
#include "Poco/DateTimeFormat.h"
#include "Poco/StringTokenizer.h"
#include "Poco/LineEndingConverter.h"
#include "Page.h"
#include "PageReader.h"
#include "CodeWriter.h"
#include "ApacheCodeWriter.h"
#include "OSPCodeWriter.h"
#include <cctype>
#include <sstream>
#include <iostream>
#include <memory>


using Poco::Util::Application;
using Poco::Util::Option;
using Poco::Util::OptionSet;
using Poco::Util::HelpFormatter;
using Poco::Util::AbstractConfiguration;
using Poco::Util::OptionCallback;
using Poco::AutoPtr;
using Poco::FileInputStream;
using Poco::FileOutputStream;
using Poco::Path;
using Poco::DateTime;
using Poco::DateTimeFormatter;
using Poco::DateTimeFormat;
using Poco::StringTokenizer;
using Poco::OutputLineEndingConverter;


class CompilerApp: public Application
{
public:
	CompilerApp(): 
		_helpRequested(false),
		_generateOSPCode(false),
		_generateApacheCode(false)
	{
	}

protected:	
	void initialize(Application& self)
	{
		loadConfiguration(); // load default configuration files, if present
		Application::initialize(self);
	}
	
	void defineOptions(OptionSet& options)
	{
		Application::defineOptions(options);

		options.addOption(
			Option("help", "h", "display help information on command line arguments")
				.required(false)
				.repeatable(false)
				.callback(OptionCallback<CompilerApp>(this, &CompilerApp::handleHelp)));

		options.addOption(
			Option("define", "D", "define a property")
				.required(false)
				.repeatable(true)
				.argument("name=value")
				.callback(OptionCallback<CompilerApp>(this, &CompilerApp::handleDefine)));
				
		options.addOption(
			Option("config-file", "f", "load configuration data from a file")
				.required(false)
				.repeatable(true)
				.argument("file")
				.callback(OptionCallback<CompilerApp>(this, &CompilerApp::handleConfig)));

		options.addOption(
			Option("osp", "O", "add factory class definition/implementation for use with OSP")
				.required(false)
				.repeatable(false)
				.callback(OptionCallback<CompilerApp>(this, &CompilerApp::handleOSP)));

		options.addOption(
			Option("apache", "A", "add factory class definition/implementation and shared library manifest for use with ApacheConnector")
				.required(false)
				.repeatable(false)
				.callback(OptionCallback<CompilerApp>(this, &CompilerApp::handleApache)));
	}
	
	void handleHelp(const std::string& name, const std::string& value)
	{
		_helpRequested = true;
		displayHelp();
		stopOptionsProcessing();
	}
	
	void handleDefine(const std::string& name, const std::string& value)
	{
		defineProperty(value);
	}
	
	void handleConfig(const std::string& name, const std::string& value)
	{
		loadConfiguration(value);
	}

	void handleOSP(const std::string& name, const std::string& value)
	{
		_generateOSPCode = true;	
	}

	void handleApache(const std::string& name, const std::string& value)
	{
		_generateApacheCode = true;
	}

	void displayHelp()
	{
		HelpFormatter helpFormatter(options());
		helpFormatter.setCommand(commandName());
		helpFormatter.setUsage("OPTIONS PAGES");
		helpFormatter.setHeader("The POCO C++ Server Page Compiler.");
		helpFormatter.format(std::cout);
	}
	
	void defineProperty(const std::string& def)
	{
		std::string name;
		std::string value;
		std::string::size_type pos = def.find('=');
		if (pos != std::string::npos)
		{
			name.assign(def, 0, pos);
			value.assign(def, pos + 1, def.length() - pos);
		}
		else name = def;
		config().setString(name, value);
	}

	int main(const std::vector<std::string>& args)
	{
		if (_helpRequested)
			return Application::EXIT_OK;

		for (std::vector<std::string>::const_iterator it = args.begin(); it != args.end(); ++it)
		{
			compile(*it);
		}

		return Application::EXIT_OK;
	}

	void compile(const std::string& path)
	{
		Page page;

		FileInputStream srcStream(path);
		PageReader pageReader(page, path);
		pageReader.parse(srcStream);

		Path p(path);
		config().setString("inputFileName", p.getFileName());
		config().setString("inputFilePath", p.toString());

		DateTime now;
		config().setString("dateTime", DateTimeFormatter::format(now, DateTimeFormat::SORTABLE_FORMAT));

		std::string clazz;
		if (page.has("page.class"))
		{
			clazz = page.get("page.class");
			p.setBaseName(clazz);
		}
		else
		{
			clazz = p.getBaseName() + "Handler";
			clazz[0] = std::toupper(clazz[0]);
		}			

		std::auto_ptr<CodeWriter> pCodeWriter(createCodeWriter(page, clazz));

		p.setExtension("h");
		std::string headerPath = p.toString();
		std::string headerFileName = p.getFileName();
		config().setString("outputFileName", p.getFileName());
		config().setString("outputFilePath", headerPath);
		FileOutputStream headerStream(headerPath);
		OutputLineEndingConverter headerLEC(headerStream);
		writeFileHeader(headerLEC);
		pCodeWriter->writeHeader(headerLEC, p.getFileName());

		p.setExtension("cpp");
		std::string implPath = p.toString();
		config().setString("outputFileName", p.getFileName());
		config().setString("outputFilePath", implPath);
		FileOutputStream implStream(implPath);
		OutputLineEndingConverter implLEC(implStream);
		writeFileHeader(implLEC);
		pCodeWriter->writeImpl(implLEC, headerFileName);
	}

	void writeFileHeader(std::ostream& ostr)
	{
		std::string fileHeader = config().getString("PageCompiler.fileHeader", "");
		if (!fileHeader.empty())
		{
			ostr << fileHeader << std::endl;
			ostr << "\n\n";
		}
	}

	CodeWriter* createCodeWriter(const Page& page, const std::string& clazz)
	{
		if (_generateOSPCode)
			return new OSPCodeWriter(page, clazz);
		else if (_generateApacheCode)
			return new ApacheCodeWriter(page, clazz);
		else
			return new CodeWriter(page, clazz);
	}

private:
	bool _helpRequested;
	bool _generateOSPCode;
	bool _generateApacheCode;
};


POCO_APP_MAIN(CompilerApp)
