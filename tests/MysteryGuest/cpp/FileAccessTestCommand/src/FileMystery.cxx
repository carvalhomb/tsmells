#include <string>
#include "FileMystery.h"
#include "file/ifstream.h"
#include "file/stdio.h"
#include "file/unix_fileio.h"

using namespace std;

void TestFileInput::testIfstream() {
    string line;

    std::ifstream file;
    file.open("filename.txt");
    if (file.is_open()) {
        while (! file.eof() ) {
            getline(file,line);
        }
        file.close();
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

#define O_RDWR 1

void TestFileInput::testCopen() {
    int fd;
    char* buffer;
    char string[100];

    //fd = open("filename.txt", O_RDWR);
    if (fd != -1) {
        while (  0 < read(fd, buffer, 100) ) {
        }
        close(fd);
    }
}
