#include <string>

namespace std {

class streamsize;

class istream {
public:
    istream& getline(char* s, streamsize n);
    istream& getline(char* s, streamsize n, char delim);
    istream& read(char* s, streamsize n);
    streamsize readsome(char* s, streamsize n);
};

istream& operator>> (istream& is, string& str);
//istream& getline(istream& is, string& str, char delim);
istream& getline(istream& is, string& str);

class ifstream : public istream {
public:
    ifstream();
    explicit ifstream(const char* filename, ios_base::openmode mode = ios_base::in );
    void open(const char* filename, ios_base::openmode mode = ios_base::in);
    bool is_open();
    void close();
};

}

