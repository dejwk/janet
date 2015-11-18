# This is GNU-MAKE input file for building examples from Manual.md. For a
# more sophisticated makefile, see also ${JANET_HOME}/examples.

# USER DEFINED PARAMETERS

#JAVA_HOME=

JAVAC=javac
CC=gcc
CPPC=g++
LD=g++

JANET_HOME = ../../

JANET=${JANET_HOME}/janet.jar -comments

#try to guess JAVA_HOME
JAVA_HOME ?= ${shell realpath `which javac` | sed 's/\/bin\/javac//'}

#try to guess PLATFORM
PLATFORM ?= ${shell basename `find ${JAVA_HOME}/include/* -type d`}

#try to guess CFLAGS
CFLAGS ?= ${shell if test ${PLATFORM} = linux; then echo "-fPIC"; else echo ""; fi }

#try to guess LDFLAGS
LDFLAGS ?= ${shell if test ${PLATFORM} = linux; then echo "-shared"; elif test ${PLATFORM} = solaris; then echo "-G"; else echo "-shared"; fi }

LIBRARY = libmanual.so

# END OF USER DEFINED PARAMETERS

.SUFFIXES:
.SUFFIXES: .class .java .janet $(SUFFIXES)


JANET_HOME=${shell realpath ../..}

CFLAGS += -ansi \
          -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/$(PLATFORM) \
          -I$(JANET_HOME)/native/c/include

all: $(LIBRARY) manual/Main.class

clean:
	rm -rf $(LIBRARY) manual/*.class manual/*~ manual/*.o manual/*.c manual/*.cc manual/*.java

manual/Main.class: manual/Main.java
	$(JAVAC) manual/Main.java

manual/Main.java manual/Main.c manual/MainImpl.cc: manual/Main.janet
	$(JANET) manual/Main.janet

$(LIBRARY): manual/Main.o manual/MainImpl.o manual/janet.o
	$(LD) $(LDFLAGS) -lstdc++ $^ -o $@

manual/janet.o: $(JANET_HOME)/native/c/janet.c
	$(CC) -c $(CFLAGS) $(JANET_HOME)/native/c/janet.c -o manual/janet.o

manual/%.o: manual/%.c
	$(CC) -c $(CFLAGS) $? -o $@

manual/%.o: manual/%.cc
	$(CPPC) -c $(CFLAGS) $? -o $@
