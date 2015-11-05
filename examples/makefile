# This is GNU-MAKE input file for building JANET examples.
# As a minimum, you should set JAVA_HOME environment variable prior to
# invoking this makefile

# USER DEFINED PARAMETERS

#JAVA_HOME=

JAVAC=javac
CC=gcc
CPPC=g++
LD=g++

JANET_HOME = ../

JANET=${JANET_HOME}/janet.jar -comments

#try to guess JAVA_HOME
JAVA_HOME ?= ${shell realpath `which javac` | sed 's/\/bin\/javac//'}

#try to guess PLATFORM
PLATFORM ?= ${shell basename `find ${JAVA_HOME}/include/* -type d`}

#try to guess CFLAGS
CFLAGS ?= ${shell if test ${PLATFORM} = linux; then echo "-fPIC"; else echo ""; fi }

#try to guess LDFLAGS
LDFLAGS ?= ${shell if test ${PLATFORM} = linux; then echo "-shared"; elif test ${PLATFORM} = solaris; then echo "-G"; else echo "-shared"; fi }

LIBRARY = libexamples.so

# END OF USER DEFINED PARAMETERS

.SUFFIXES:
.SUFFIXES: .class .java .janet $(SUFFIXES)


JANET_HOME=${shell realpath ..}

CFLAGS += -ansi \
          -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/$(PLATFORM) \
          -I$(JANET_HOME)/native/c/include

GENFILES= \
        genfiles/Arrays.java genfiles/Arrays.c genfiles/ArraysImpl.c \
        genfiles/Basic.java genfiles/Basic.c genfiles/BasicImpl.c \
        genfiles/Cast.java genfiles/Cast.c genfiles/CastImpl.c \
        genfiles/ControlFlow.java genfiles/ControlFlow.c genfiles/ControlFlowImpl.c \
        genfiles/Exceptions.java genfiles/Exceptions.c genfiles/ExceptionsImpl.c \
        genfiles/HelloWorld.java genfiles/HelloWorld.c genfiles/HelloWorldImpl.c \
        genfiles/Literals.java genfiles/Literals.c genfiles/LiteralsImpl.c \
        genfiles/Operators.java genfiles/Operators.c genfiles/OperatorsImpl.c \
        genfiles/Strings.java genfiles/Strings.c genfiles/StringsImpl.c \
        genfiles/Synchronized.java genfiles/Synchronized.c genfiles/SynchronizedImpl.c \
        genfiles/HelloWorldCpp.java genfiles/HelloWorldCpp.c genfiles/HelloWorldCppImpl.cc

OBJFILES= \
        obj/Arrays.o obj/ArraysImpl.o obj/Basic.o obj/BasicImpl.o \
        obj/Cast.o obj/CastImpl.o obj/ControlFlow.o obj/ControlFlowImpl.o \
        obj/Exceptions.o obj/ExceptionsImpl.o obj/HelloWorld.o obj/HelloWorldImpl.o \
        obj/Literals.o obj/LiteralsImpl.o obj/Operators.o obj/OperatorsImpl.o \
        obj/Strings.o obj/StringsImpl.o obj/Synchronized.o obj/SynchronizedImpl.o \
        obj/HelloWorldCpp.o obj/HelloWorldCppImpl.o

all: $(LIBRARY) classes/examples/Main.class

clean:
	rm -rf $(LIBRARY) *.class *~ genfiles obj classes

classes/examples/Main.class: Main.java
	mkdir -p classes
	$(JAVAC) -d classes Main.java genfiles/*.java

$(GENFILES): *.janet
	mkdir -p genfiles
	$(JANET) -d genfiles *.janet

$(LIBRARY): $(OBJFILES) obj/janet.o
	$(LD) $(LDFLAGS) -lstdc++ $^ -o $@

obj/janet.o: $(JANET_HOME)/native/c/janet.c
	mkdir -p obj
	$(CC) -c $(CFLAGS) $(JANET_HOME)/native/c/janet.c -o obj/janet.o

obj/%.o: genfiles/%.c
	mkdir -p obj
	$(CC) -c $(CFLAGS) $? -o $@

obj/%.o: genfiles/%.cc
	mkdir -p obj
	$(CPPC) -c $(CFLAGS) $? -o $@
