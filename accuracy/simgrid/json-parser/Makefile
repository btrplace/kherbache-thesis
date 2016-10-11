CC=gcc

CFLAGS=-g -I/opt/json-c/include
LDFLAGS=-L/opt/json-c/lib -ljson-c

all: json-parser.o main


json-parser.o: ./json-parser.c ./json-parser.h
	${CC} ${CFLAGS} json-parser.c -c -o json-parser.o

main: ./main.c ./json-parser.o ./json-parser.h
	${CC} ${CFLAGS}  main.c -c -o main.o
	${CC} ${LDFLAGS} main.o json-parser.o -o test-json-parser

clean:
	rm -f *.o
	rm -f test-json-parser
