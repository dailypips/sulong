#include <stdlib.h>

#pragma pack(1)

struct test {
  char a;
  int b;
  int c;
};

int main() {
  struct test t = { 3, 5, 0 };
  long val = *((long *)&t.a);
  if (t.a != 3 || t.b != 5 || t.c != 0) {
    abort();
  }
  if (val != 1283L) {
    abort();
  }
  return 0;
}
