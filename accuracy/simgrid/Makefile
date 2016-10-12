
CC=gcc
FLAGS=-I/opt/json-c/include -I/opt/simgrid/include
CCFLAGS=-g -I/opt/json-c/inlude -I/opt/simgrid/include
LDFLAGS=-L/opt/json-c/lib -L/opt/simgrid/lib -lsimgrid -ljson-c -lpthread

all: migrate_vm

migrate_vm.o : ./migrate_vm.c
	${CC} ${CCFLAGS} ${FLAGS} ${LDFLAGS} migrate_vm.c -c -o $@

migrate_vm : ./migrate_vm.o
	${CC} ${CCFLAGS} ${FLAGS} ${LDFLAGS} migrate_vm.o ./json-parser/json-parser.o -o $@

debug: ./migrate_vm
	(echo "break main"; echo "r topology.xml test.csv test.json"; cat) | gdb ./migrate_vm

test: ./migrate_vm
	./migrate_vm topology.xml test.csv test.json

memcheck: ./migrate_vm
	valgrind --leak-check=full --track-origins=yes --db-attach=yes  ./migrate_vm topology.xml test.csv test.json

clean:
	rm -f migrate_vm
	rm -f *.o
	rm -f *~ 
	rm -f *.csv
