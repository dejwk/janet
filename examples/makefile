# This is GNU-MAKE input file for building JANET examples.
# As a minimum, you should set JAVA_HOME environment variable prior to
# invoking this makefile

# USER DEFINED PARAMETERS

#JAVA_HOME=

JAVAC=javac
CC=gcc

## LINUX ##
#
#CFLAGS=-shared -fPIC
#PLATFORM=linux

## SOLARIS ##
#
#CFLAGS=-G
#PLATFORM=solaris

JANET_HOME = ../

JANET=${JANET_HOME}/janet.jar -comments

#try to guess JAVA_HOME
JAVA_HOME ?= ${shell which java | sed 's/\/bin\/java//'}

#try to guess PLATFORM
PLATFORM ?= ${shell basename `find ${JAVA_HOME}/include/* -type d`}

#try to guess CFLAGS
CFLAGS ?= ${shell if test ${PLATFORM} = linux; then echo "-shared -fPIC"; elif test ${PLATFORM} = solaris; then echo "-G"; else echo "-shared"; fi }

LIBRARY = libexamples.so

# END OF USER DEFINED PARAMETERS

.SUFFIXES:
.SUFFIXES: .class .java .janet $(SUFFIXES)


JANET_DIR=..

CFLAGS += -ansi \
          -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/$(PLATFORM) \
          -I$(JANET_HOME)/native/c/include

FILES=$(JANET_HOME)/native/c/janet.c \
      genfiles/Arrays.c genfiles/ArraysImpl.c genfiles/Basic.c genfiles/BasicImpl.c \
      genfiles/Cast.c genfiles/CastImpl.c genfiles/ControlFlow.c genfiles/ControlFlowImpl.c \
      genfiles/Exceptions.c genfiles/ExceptionsImpl.c genfiles/HelloWorld.c genfiles/HelloWorldImpl.c \
      genfiles/Literals.c genfiles/LiteralsImpl.c genfiles/Operators.c genfiles/OperatorsImpl.c \
      genfiles/Strings.c genfiles/StringsImpl.c genfiles/Synchronized.c genfiles/SynchronizedImpl.c

all: $(LIBRARY) classes/examples/Main.class

clean:
	rm -rf $(LIBRARY) *.class *~ genfiles classes

classes/examples/Main.class: *.java genfiles/*.java
	mkdir -p classes
	$(JAVAC) -d classes *.java genfiles/*.java

genfiles/%.java genfiles/%.c genfiles/%Impl.c: %.janet
	mkdir -p genfiles
	$(JANET) -d genfiles *.janet

$(LIBRARY): $(FILES)
	$(CC) $(CFLAGS) $^ -o $@

