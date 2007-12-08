FILE* fopen(const char* filename, const char* mode);
char* fgets (char * str, int num, FILE* stream);
int fgetc( FILE * stream);
size_t fread(void* ptr, size_t size, size_t count, FILE* stream);
int fclose ( FILE * stream );