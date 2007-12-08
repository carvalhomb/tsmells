//#include <fstream>  // C++ file streams
#include "src/file/ifstream.h"
#include <string>
#include <fcntl.h>  // 
#include <stdio.h>  // C fopen

#include "testFileInput.h"

//using std::ifstream;
//using std::string;

using namespace std;
using namespace cppunit;

TestFileInput::TestFileInput()
  : TestCase("testFileInput") {
}

TestFileInput::~TestFileInput() {
}


void TestFileInput::setUp() {
}


void TestFileInput::tearDown() {
}

void TestFileInput::testIfstream() {
	string line;

	std::ifstream file;
	file.open("filename.txt");
	if (file.is_open()) {
    	while (! file.eof() ) {
			getline(file,line);
		}
		myfile.close();
	}

	ifstream file2;
	file2.open("filename.txt");
	getline(file2, line);
	file2.close();
}

void TestFileInput::testCfopen() {
	FILE *filePtr;
	char string[100];

	filePtr = fopen("filename.txt","r");
	if (filePtr != NULL) {
		while ( fgets(string, 100, filePtr)) {
		}
		fclose(filePtr);
	}
}

void TestFileInput::testCopen() {
	int fd;
	char string[100];

	fd = open("filename.txt", O_RDWR);
	if (fd != -1) {
		while (  0 < read(fd, buffer, 100) ) {
		}
		close(fd);
	}
}

void TestFileInput::testXPopen() {
}

Test* TestFileInput::suite() {
	TestSuite *s = new TestSuite;
	s->addTest(new TestCaller<TestFileInput>("testIfstream", &TestFileInput::testIfstream));	
	s->addTest(new TestCaller<TestFileInput>("testCopen", &TestFileInput::testCopen));
	s->addTest(new TestCaller<TestFileInput>("testCfopen", &TestFileInput::testCfopen));
	s->addTest(new TestCaller<TestFileInput>("testXPopen", &TestFileInput::testXPopen));
	return s;
}
