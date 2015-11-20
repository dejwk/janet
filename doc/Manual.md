
# How to use JANET

## What is JANET?

JANET simplifies development of Java-to-native interfaces by allowing you to mix
Java and native code (C and C++) in your source files. JANET is two things:

1. A language extension that defines how the code can be mixed in `.janet` files;
2. A code generating tool that translates `.janet` files into Java and native source files that
   contain generated JNI bindings.

The language extension allows you to define your native methods in-place. Furthermore, the native
definitions can contain snippets of Java code, in which you can use most Java expressions and
certain statements (e.g. `return`, `synchronized`, `try/catch/finally`), as well as certain new
operators, e.g. to convert strings and primitive arrays between Java and native counterparts.
JANET tool translated these Java snippets to JNI calls.

## What is JANET not?

JANET is not a complete build system. It will not build your Java code and your native shared
libraries. The exact way of building shared libraries depends on an operating system and on a
compiler. Also, there are many different valid strategies for packaging native code into libraries.
Making these choices and building native libs is still on you.

For the most part, JANET does not understand your native code. It only does cursory parsing to
detect high-level constructs like comments, blocks, and paired parentheses. It makes certain
things simpler - e.g. you don't need to provide paths to resolve the includes - but it also
introduces some constraints on the native code you write, and some weirdness in the operator
syntax. (In contrast, JANET does analyze all your Java code semantically, so it is much smarter
about it than about your native code).

## Preliminaries

In the following examples, we assume that `JANET_HOME` points to your root JANET directory,
and that `janet` is an alias that points to `$JANET_HOME/janet.jar`. On Linux systems, you can 
achieve this by running the following commands:

    $ export JANET_HOME=`pwd`
    $ alias janet=${JANET_HOME}/janet.jar

from JANET's root directory.

To build native libraries, you'll need to tell your compiler where to find the JNI header file and
its dependencies. The JNI heaer file
is normally found in `${JAVA_HOME}/include/${PLATFORM}/jni.h`. Assuming that you have `javac`
on your path, you should be able to resolve `${JAVA_HOME}` and `${PLATFORM}` in the following way:

    $ export JAVA_HOME=`realpath \`which javac\` | sed 's/\/bin\/javac//'`
    $ export PLATFORM=`basename \`find ${JAVA_HOME}/include/* -type d\``

## Basic usage

You may remember that Java allows you to _declare_ a method as `native`. JANET
extends it by allowing you to immediately provide the native _implementation_ inline. As an
example, let's create a file [manual/Main.janet](src/manual/Main.janet):

```Java
package manual;

public class Main {
    public static void main(String[] args) {
        System.out.println(trivialStaticNativeMethod());
    }
    private static native "C++" int trivialStaticNativeMethod() {
        return 5;  // C++
    }
}
```

Now let's run `janet` on it:

    $ janet manual/Main.janet

This should have generated Java and C/C++ files in the `manual` directory:

    $ ls manual
    Main.c MainImpl.cc Main.janet Main.java

The new files contain your generated JNI bindings:

* `Main.java` file contains pure-Java part of your
  `Main.janet`;
* `Main.c` is JNI glue code;
* `MainImpl.cc` contains the implementation of your native
       method (or methods).
    
Now let's compile the Java file:

    $ javac manual/Main.java

And, we also need to compile the native code into a shared library. Assuming Linux and gcc, the
following command should do:

    $ JNIFLAGS="-I${JAVA_HOME}/include -I${JAVA_HOME}/include/${PLATFORM}"
    $ CFLAGS="-fPIC -I${JANET_HOME}/native/c/include ${JNIFLAGS}"
    $ gcc ${CFLAGS} -c manual/Main.c  -o manual/Main.o
    $ g++ ${CFLAGS} -c manual/MainImpl.cc -o manual/MainImpl.o
    $ gcc ${CFLAGS} -c ${JANET_HOME}/native/c/janet.c -o manual/janet.o
    $ g++ -shared manual/Main.o manual/MainImpl.o manual/janet.o -o libmanual.so

Note that we needed to compile the `janet.c` file (a small run-time library) into our shared lib.

Now you are ready to run the example. The final gotcha is that you need to tell the Java
VM where to find the native library (or else you will get an UnsatisfiedLinkError). You can do that
by setting the 'java.library.path' property accordingly:

    $ java -Djava.library.path=. manual.Main
    5

Woohoo!

## Passing parameters

Our first native method was not very interesting; the C++ code did not interact with Java at all.
Now, we will extend it by adding a parameter to our method. In the native code, you can refer to
parameters by enclosing them in back-tick quotes:

```Java
public class Main {
    public static void main(String[] args) {
        ...
        System.out.println(staticNativeMethodWithParameter(3));
    }
    ...
    private static native "C++" int staticNativeMethodWithParameter(int parameter) {
        return `parameter` + 5;  // C++
    }
}
```

And, after building:

    $ java -Djava.library.path=manual manual.Main
    ...
    8

## Embedding other Java expressions in native code

Reference to a method's parameter, which you have used in the previous example, is in fact a
simple Java expression. Importantly, the back-tick syntax can be used not just for referring to
parameters, but for embedding nearly _any_ Java expression, including field accesses, method
invocations, literals, `new`, `instanceof`, array access, and more (see inclusions and exclusions
in the [README](../README.md)). You can also assign values to Java variables. The following code
snippet illustrates the point:

```Java
public class Main {
    public static void main(String[] args) {
        ...
        Main main = new Main();
        main.basicExpressions();
        System.out.println(main.arr[3]);
    }
    ...
    native "C++" void basicExpressions() {
        
        /* field access */
        int i = `f1`;

        /* method invocation */
        `foo()`;

        /* more sophisticated invocation + field accesses + string */
        `System.out.println("More sophisticated example")`;

        /* assignment + instance creation */
        `f2 = new Object();`

        /* assignment into an array */
        `arr[3] = 5;`
    }
    
    void foo() {
        System.out.println("In foo()");
    }
    
    int f1;
    Object f2;
    int[] arr = new int[5];
}
```

Which yields:

    In foo()
    More sophisticated example
    5

## Evaluation semantics

Importantly, even though the embedded Java expressions are internally translated to JNI
calls, JANET preserves strict and safe Java evaluation semantics. Method parameters
are evaluated left-to-right, and any exceptions immediately terminate evaluation and get
propagated. Dereferencing `null` causes a `NullPointerException`, rather than program crash. It is
illustrated in the following example:

```Java
public class Main {
    public static void main(String[] args) {
        ...
        try {
            main.testExceptionPropagation();
            throw new AssertionError();
        } catch (Exception e) {
            if (e.getMessage().equals("from throwingMethod")) {
                System.out.println("Exception thrown and caught as expected.");
            } else {
                throw e;
            }
        }

        try {
            main.testNullPointer();
            throw new AssertionError();
        } catch (NullPointerException e) {
            System.out.println("NullPointerException caught as expected.");
        }
    }

    ...
    int throwingMethod() throws Exception {
        throw new Exception("from throwingMethod");
    }

    int testMethod() {
        throw new AssertionError();
    }

    void foo(int a, int b) {
        throw new AssertionError();
    }

    native "C++" void testExceptionPropagation() {
        // We expect to see the exception propagated,
        // and neither testMethod() nor foo() to be called
        `foo(throwingMethod(), testMethod())`;
    }

    native "C++" void testNullPointer() {
        // Should throw NPE,
        `Main obj = null; obj.testMethod();`
    }
}
```

Which yields

    ...
    Exception thrown and caught as expected.
    NullPointerException caught as expected.

## Using native sub-expressions in embedded Java expressions

Often, there's need to use a native expression as part of Java expression. You can do that using
the `#(expr)` syntax:

```Java
...
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        ...
        main.fillArray();
        System.out.println(Arrays.toString(main.arr));
        main.embeddedMethodCall();
    }
    ...
    native "C++" void fillArray() {
        for (int i = 0; i < `arr.length`; ++i) {
            `arr[#(i)] = #(i)`;
        }
    }

    native "C++" void embeddedMethodCall() {
        `bar((long)#(5))`;
    }
    
    void bar(int i) { System.out.println("bar(int)"); }
    void bar(long l) { System.out.println("bar(long)"); }
}
```

Which will print

    ...
    [0, 1, 2, 3, 4]
    bar(long)

Note the cast in front of the `#()` expression in `embeddedMethodCall()`. JANET plays it safe
and requires such casts for disambiguation - in this case, to specify which of the two `bar()`
methods should be called.

## Native blocks

As an alternative to full-blown native methods, you can also embed smaller snippets of native
code in normal Java methods, using the following syntax:

```Java
public class Main {
    public static void main(String[] args) {
        ...
        main.methodWithNativeBlock();
    }
    ...
    void methodWithNativeBlock() {
        int local = 7;
        native "C++" {
            int local_cpp = `local`;  // Reference to a Java local variable.
            `f1 = #(local_cpp + 1)`;  // Setting a Java instance field.
        }
        System.out.println(f1);
    }
}
```

Which will print

    ...
    8

As you can see, in the native block you can read local variables in the Java scope. Unfortunately,
you cannot set any such local variables. You still can set fields and call methods to change
state, though.

Use native blocks if you have pure-Java boilerplate either at the beginning or at the end of
your native method. Running Java code in Java will generally be more efficient than calling it
via JNI calls. 

## Static native blocks

In all but trivial cases, you will want to include raw native code into your source
files, such as include directives, license headers, or helper functions. You can so so using static
native blocks, that look the same as regular native blocks except that they are defined outside of
        any method:

```Java
public class Main {

native "C++" {
#include <iostream>
}

    public static void main(String[] args) {
        ...
        main.iostream();
    }
    ...
    native "C++" void iostream() {
        `System.out.flush();`
        std::cout << "C++ std::cout\n";
        std::cout.flush();
    }
}
```

Which will print

    ...
    C++ std::out

The order of static native blocks and native method implementations in the generated native files
will match the order of their declaration in the `.janet` file.

You cannot embed any Java code in static native blocks.

Note how we flushed both Java's `System.out` and C++'s `std::cout` in this example. This is
necessary if you want to get sequentially consistent log output. It is so because both streams are
independently buffered in their respective language libraries.

## Return statements and native control flow

So far, our native methods were all `void`. If your native method has a non-void result,
you will want to use a `return` statement.

As a rule, _always put the entire `return` statement in back-tick quotes_; for example:

```Java
`retun #(i);`  // Good!
```

rather than

```Java
return i;    // Yikes!
```

It is important for program correctness, due to the way JANET propagates Java exceptions, as
explained in detail in the [Understanding the generated code](#understanding-the-generated-code)
section.

Similarly, you need to follow certain rules when using native control flow statements, such as
`break`, `continue`, or `goto`. Specifically, these should never break out of a block that
directly contains any embedded Java code. For example:

```Java
while (`foo()`) {
    if (y()) continue;  // OK; this is a pure-native block
    if (x()) break;  // OK; this is a pure-native block
}

for (int = 0; i < 10; ++i) {
    if (`arr[#(i)] > 0`) break;  // Yikes! Breaking out of the block.
}

for (int = 0; i < 10; ++i) {
    bool result;
    { result = `arr[#(i)] > 0`; }
    if (result) break;  // This is OK now.
}
```

This restriction is likely to be lifted in the future for C++ (but not for C).

## Variable declarations

Your native code may contain embedded declarations of Java local variables, and use them just
like in Java; for instance:

    `List list = new ArrayList();`
    ...
    `list.add(new Integer(#(x));`
    ...

## Compound statements

You can skip adjacend back-tick characters if you have a sequence of embedded Java statements;
for example:

    native "C++" int foo() {
        `int i = 0; i = i + 1; return i;`
    }

Or, in the multi-line form:

    native "C++" int foo() {
        `int i = 0;
        i = i + 1;
        return i;`
    }

## Strings

There are tree ways to handle and convert strings in Janet:

1. Convert Java strings to and from C const char* arrays using the modified UTF-8 encoding;
2. Convert Java strings to and from C const jchar* (uint32) arrays, representing Unicode
   characters;
3. Using Java APIs to refer to string content directly.

### UTF-8 strings

Janet provides a pair of operators: `#&` and `#$`, to convert Java strings to const char* arrays,
and vice versa. The characters are encoded in Sun's
[modified UTF-8](https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8)
format. The following snippet takes a Java string, and returns a new string that contains two
concatenated repetition of the input string, using these Janet operators:

```Java
public class Main {

native "C++" {
#include <algorithm>
#include <cstring>
#include <iostream>
#include <vector>
}

    public static void main(String[] args) {
        ...
        System.out.println(main.echoInUTF("Hola UTF! "));
    }
    ...
    native "C++" String echoInUTF(String s) {
        std::vector<char> result;
        {
            const char* content = `#&s`;
            int len = strlen(content);
            result.resize(2 * len + 1);  // Leave space for terminal '\0'
            strncpy(&result[0], content, len);
            strncpy(&result[len], content, len);
            `return #$(&result[0]);`
        }
    }
}
```

Which prints:

    ...
    Hola UTF! Hola UTF! 

Depending on how JVM represents strings internally, the `#&` operator may or may not make a copy
of the string's data. You should not assume either way. Don't be tempted to cast away constness
to directly mutate the data.

The pointer returned by the `#&` operator is valid till the end of the block. Using it past that
block results in undefined behavior. If you need to make a long-lived reference to a Java string,
either make an explicit copy, or keep the reference to the original string.

In this example, we have assumed that the resulting array is zero-terminated. Technically, this is
not guaranteed by the JNI specification. That said, it has once been documented in the JNI tutorial
and appeared in print, and it was close to making it to the JNI specification (See this
[Stack Overflow discussion](http://stackoverflow.com/questions/16694239/java-native-code-string-ending)),
and therefore so much existing code depends on it that no sane JVM implementer is likely to diverge
from this de-facto standard.

The `#$` operator takes a zero-terminated modified UTF-8 character array, and returns a new Java
string with the same content. The data is copied, so it is your responsibility to release
the original memory buffer (in our example, managed by `vector<char>`).

You may have noticed that we've declared the `vector` at the beginning of the method,
and wrapped everything else in a nested block. Unfortunately, this weirdness is currently
necessary, due to the way Janet handles Java exceptions in generated C++ code (with
setjmp/longjmp rather than C++ exceptions). As a consequence, any automatic object with non-trivial
destructor (such as `std::vector`) must be declared outside of any block with embedded Java (using
back-ticks), or else you'll get undefined behavior in case the embedded Java throws any exceptions.

### Unicode strings

Janet allows to convert Java strings to and from two-byte Unicode, represented in C/C++ as
`const jchar*` array. (The `jchar` is unsigned 16-bit integer). You can perform the conversions
using the following pair of operators: `&` and `#$$`:

```Java
public class Main {
    ...
    public static void main(String[] args) {
        ...
        System.out.println(main.echoInUnicode("Hola Unicode! "));
    }
    ...
    native "C++" String echoInUnicode(String s) {
        std::vector<jchar> result;
        {
            const jchar* content = `&s`;
            int len = `s.length()`;
            result.resize(2 * len);
            std::copy(content, content + len, result.begin());
            std::copy(content, content + len, result.begin() + len);
            `return #$$(len > 0 ? &result[0] : 0, 2 * len);`
        }
    }
}
```

Which prints

    ...
    Hola Unicode! Hola Unicode!

As you can see, te usage of these operators is similar to the use of the UTF-8 operators, with a
few important exceptions:

* When converting from Java string to `const jchar*` with the `&` operator, the resulting array is
  _not_ zero-terminated. You must manage string length explicitly.
* Conversely, when creating a new Java string from `const jchar*` with the `#$$` operator, you must
  provide the length.

Just like with UTF-8 strings, the pointers are valid till the end of the block.

At the discretion of the underlying JNI implementation, the '&' operator may or may not copy the
original string content. In theory, JVM is more likely to internally represent strings in a format
compatible with const jchar* arrays, but don't get your hopes too high; many JVMs will still make
a copy.

### Java APIs

If all you need is to access individual characters or small fragments of a Java string, it may
be best to simply use methods of the `String` class as embedded expressions:

```Java
public class Main {
    ...
    public static void main(String[] args) {
        ...
        System.out.println(main.echoInJava("Hola Java! "));
    }
    ...
    native "C++" String echoInJava(String s) {
        std::vector<jchar> result;
        {
            int len = `s.length()`;
            for (int i = 0; i < len; ++i) result.push_back(`s.charAt(#(i))`);
            for (int i = 0; i < len; ++i) result.push_back(`s.charAt(#(i))`);
            `return #$$(len > 0 ? &result[0] : 0, 2 * len);`
        }
    }
}
```

Which prints

    ...
    Hola Java! Hola Java!



This way you can avoid potential memory costs of string copying that is inherent with the other two
methods. That said, JNI method calls are expensive, so making thousands of calls like these in
a loop will hurt performance.

## Arrays

JANET offers two ways of interacting with Java arrays from native code:

* Using standard Java APIs, that is, embedded Java array acess and array creation expressions;
* For arrays or primitive types, JANET provides the `&` operator that returns a native pointer to
  mutable array contents.

### Java APIs

In the following example, we use embedded Java expressions and assignments to create and initialize
a Java array out of native strings:

```Java
public class Main {
    ...
    public static void main(String[] args) {
        ...
        System.out.println(Arrays.toString(main.helloFromJanet()));
    }
    ...
    native "C++" String[] helloFromJanet() {
        `String[] result = new String[3];
        result[0] = #$("Hello");
        result[1] = #$("from");
        result[2] = #$("Janet!");
        return result;`
    }
```

Which prints

    ...
    [Hello, from, Janet!]

Use this technique in the following situations:

* Handling arrays of reference types, including upper dimensions of multi-dimensional arrays;
* Accessing few individual items of large primitive arrays.

### Obtaining native array pointers

In the following example, we sort a Java array of `int` with C++ `std::sort`, using the `&`
operator to obtain the direct array pointer:

```Java
public class Main {
    ...
    public static void main(String[] args) {
        ...
        int arr[] = new int[10];
        for (int i=0; i<10; i++) { arr[i] = (i * 1549) % 87; }
        System.out.println("Array before sorting: " + Arrays.toString(arr));
        sortArray(arr);
        System.out.println("Array after sorting: " + Arrays.toString(arr));
    }
    ...
    native "C++" static void sortArray(int[] arr) {
        std::sort(`&arr`, `&arr` + `arr.length`);
    }
```

Which prints

    ...
    Array before sorting: [0, 70, 53, 36, 19, 2, 72, 55, 38, 21]
    Array after sorting: [0, 2, 19, 21, 36, 38, 53, 55, 70, 72]

The `&` operator returns a pointer to the native equivalent of the appropriate Java type. In this
case, the operator will return jint*, that is, a pointer to an array of signed 32-bit integers.

Even though it feels like you're getting a direct pointer, the JVM is still at its discretion to
internally make the copy of the array before returning the pointer to you, and then copy data back
when the pointer is released (the release is performed implicitly at the end of the block).
Obviously, it can be very inefficient for large arrays. If you compile your JANET-generated
source files with a `#define JANET_USE_FAST_ARRAYS 1`, JANET will internally use a different
JNI method (`GetPrimitiveArrayCritical`), which has higher likelihood of obtaining a direct
pointer, but comes with
[its own set of restrictions](http://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/functions.html#GetPrimitiveArrayCritical).
If you do decide to use this flag, make sure that the application of the `&` operator is the _only_
backtick-embedded Java code in its block.

If you happen to apply the `&` operator multiple times to the same array instance within a
single code block, as we in fact did in the example above, JANET guarantees that you will always
get the same native pointer (even in case if the copy of the array has been made).

## Exceptions

You can throw and handle Java exceptions in your native methods by embedding familiar Java
statements: `try`, `catch`, `finally` and `throw`:

```Java
    native "C++" static void tryCatchFinally() {
        `try` {
            std::cout << "In try\n";
            `throw new Exception();`
        } `catch (Exception e)` {
            std::cout << "In catch\n";
        } `finally` {
            std::cout << "In finally\n";
        }
    }
```

Generally, when embedding Java statements that contain blocks, like the try/catch/finally
statement above, you have some flexibility whether
to use native blocks, like above, or Java blocks, like in the following equivalent example:

```Java
    native "C++" static void tryCatchFinally() {
        `try {
            `std::cout << "In try\n";`
            throw new Exception();
        } catch (Exception e) {
            `std::cout << "In catch\n";`
        } finally {
            `std::cout << "In finally\n";`
        }`
    }
```

As you can see, in the latter case you use back-ticks to recursively embed native statements in the
Java code. You may want to use this syntactic flavor in Java-heavy fragments of your code.

Exception handling semantics is the sae as in pure Java. Embedded Java statements and
expressions, such as early return or `synchronized`, play well with exceptions and you can use
them safely as long as you observe precautions discussed in the section on control flow.

## Synchronization

To synchronize on Java monitors, simply embed the `synchronized` statement into the native code,
as in the following example:

```Java
public class Main {
    ...
    public static void main(String[] args) {
        ...
        boolean heldLock = synchronizedTest(main);
        if (!heldLock || Thread.holdsLock(main)) throw new AssertionError();
    }
    ...
    native "C++" static boolean synchronizedTest(Object target) {
        `synchronized(target) {
            `std::cout << "In synchronized\n";`
            return Thread.holdsLock(target);
         }`
    }
```

As you can see, the embedded `synchronized` statement plays well with embedded `return`. It also
plays well with embedded exception statements. JANET-generated code will release all the monitors
as needed.

## Understanding the generated code

### Embedded expressions

JANET performs complete semantic analysis of all the Java code, but only very limited analysis
of your native code. It merely detects blocks, comments and matching parentheses. For this
reason, your native code is largely copied raw. JANET will do the following processing:

* it will decorate some of your blocks, by adding various auxiliary variable declarations,
  initialization code, and stack unwinding code;
* it will replace all the embedded Java with generated code, inserting special macros and JNI
  calls.

When JANET processes an embedded Java expression, such as `` `foo(x)` ``, it does not understand or
change the surrounding native context, so it must assume that the context may be expecting an
expression (e.g. as in ``bar(`foo(x)`)``. It implies that Java expressions must be converted to
native expressions. It turns out to be challenging, because Java expressions often require series
of operations and JNI calls. For example, to guarantee Java-compatible semantics of a method
invocation, JANET must place a null-check of the target, evaluate all the arguments in the proper
order, make the JNI call to invoke the method, and finally check for any exceptions. 

JANET solves this problem by extensively using the comma operator
(hidden in macros to some extent), and auxiliary variables inserted at beginnings
of blocks.

### Exception handling

JANET uses `setjmp/longjmp` to implement Java exception handling semantics in your native code.
It has the following caveats:

* In both C and C++, control flow may be disrupted by statements like `break`,
  `continue`, `goto`, and `return`. For example, breaking out of a `synchronized` block
  would leave the lock held forever. Currently, the only way to avoid that is to
  (1) always use the embedded Java `return` statement instead of the native return, and (2)
  never put put breaking statements in blocks that directly contain any
  embedded Java code. JANET will never put any important finalization code within such
  blocks.
* In C++, there is an additional problem that `longjmp` is not compatible with destructors.
  Specifically, the standard says that it is undefined behavior to jump out of a block
  containing any automatic (i.e. stack-allocated) object with a non-trivial destructor.
  Hence, you should declare any such objects in the outer-most scope (clear of any
  embedded Java code), and put all embedded Java code in a nested block, as in the
  examples earlier above. (JANET will generate a normal `return` statement to break out
  of the outer-most scope).

For C, there is no good alternative to `setjmp/longjmp`, short of full semantic analysis
and deep code rewriting. For C++, an arguably better alternative, that would allow to
avoid all these issues, would be to use native C++ exceptions. It is likely to be
implemented in a future version of JANET. (As with any other feature request, please use
the [issue tracker](https://github.com/dejwk/janet/issues) if you would like it to be
prioritized).

Exception handling requires unwinding stack frames (nested blocks) and performing any
necessary finalization (e.g. running Java finalizers, releasing local references, releasing
monitors). To that end, JANET use custom macros that you will see a lot of in the
generated code.

### Reference management

References to Java objects must be carefully managed, and released when necessary.
To avoid reference thrashing (e.g. when repeatedly referring to the same object in
a loop), JANET, when suitable, uses _multi-references_ that perform native reference
counting and release Java references lazily. In the
generated code, you will often see macros for assignments of multi-references,
and reference cleanup code inserted in 'destruction' sections of nested blocks. 

### Array de-duplication

To ensure that the array address operator `&` always returns the same pointer
when applied repetitively to the same object within a single block, JANET
uses custom, thread-local, append-only hash tables. The tables are keyed on
the array object's hash code, with clashes disambiguated by comparing
identity of the objects using JNI's `IsSameObject`. Consequently, the
the `&` operator makes two JNI method calls per use - so it is better,
when feasible, to just capture the pointer once in a native variable,
and reuse it.

The de-duplication functionality is implemented mostly in the JANET's run-time
library (in the file `janet.c`), and otherwise mostly hidden behind
macros.

### Cutting through the macros

If you would like to cut through the layer of JANET macros to deeply understand
what the generated code does, run your file through a C preprocessor and a
beautifier:

    g++ ${CFLAGS} -E -P <your file> | indent

## How to get more help

Look also at more [examples](../examples) (including examples in pure C).
Please visit the [project page](https://github.com/dejwk/janet/) if you want to learn more
or [report a bug](https://github.com/dejwk/janet/issues).
