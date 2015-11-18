
# How to use JANET

## What is JANET?

JANET simplifies development of Java-to-native interfaces by allowing you to mix
Java and native syntax in your source files. JANET is two things:

1. A language extension that defines how the code can be mixed in `.janet` files;
2. A code generating tool that translates `.janet` files into Java and native source files that
   contain generated JNI bindings.

## What is JANET not?

JANET is not a complete build system. It will not build your Java code and your native shared
libraries. The exact way of building shared libraries depends on an operatin system and on a
compiler. Also, there are many different valid strategies for packaging native code into libraries.
Making these choices and building native libs is still on you.

## Preliminaries

In the following examples, we assume that `JANET_HOME` points to your root JANET directory,
and that `janet` is an alias that points to `$JANET_HOME/janet.jar`. On Linux systems, you can 
achieve this by running the following commands:

    $ export JANET_HOME=`pwd`
    $ alias janet=${JANET_HOME}/janet.jar

from JANET's root directory.

To build native libraries, you'll need to tell your compiler where to find the JNI header file.
It is normally found in `${JAVA_HOME}/include/${PLATFORM}/jni.h`. Assuming that you have `javac`
on your path, you should be able to resolve `${JAVA_HOME}` and `${PLATFORM}` in the following way:

    $ export JAVA_HOME=`realpath \`which javac\` | sed 's/\/bin\/javac//'`
    $ export PLATFORM=`basename \`find ${JAVA_HOME}/include/* -type d\``

## Basic usage

You may remember that Java allows you to _declare_ a method as `native`. JANET
extends it by allowing you to provide the implementation inline. As an example,
let's create a file `manual/Main.janet`:

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
    $ 5

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
    $ ...
    $ 8

## Embedding other Java expressions in native code

Reference to a method's parameter, which you have used in the previous example, is in fact a
simple Java expression. Importantly, the back-tick syntax can be used not just for referring to
parameters, but for embedding nearly _any_ Java expression, including field accesses, method
invocations, literals, `new`, `instanceof`, array access, and more (see inclusions and exclusions
in README.md). You can also assign values to Java variables. The following code snippet illustrates
the point:

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

    $ In foo()
    $ More sophisticated example
    $ 5

### Evaluation semantics

Importantly, even though the embedded Java expressions are internally translated to JNI
calls in native code, Janet preserves strict Java evaluation semantics. Method parameters
are evaluated right-to-left, and any exceptions immediately terminate evaluation and get
propagated out of the method.

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

Note the cast in front of the `#()` expression in `embeddedMethodCall()`. JANET takes a safe
path and requires such casts for disambiguation - in this case, to specify which of the two `bar()`
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

## Static native blocks

In all but trivial cases, you will want to include some boilerplate native code in your source
files, such as include directives or license headers. You can so so using static native blocks,
that look the same as 'plain' native blocks except that they are defined outside of any method:

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

Note how we flushed both Java's `System.out` and C++'s `std::cout`. This is necessary if you
want to get sequentially consistent log output. It is so because both streams are independently
buffered in their respective language libraries.
 
## Strings

There are tree ways to handle and convert strings in Janet:

1. Convert Java strings to and from C const char* arrays using the modified UTF8 encoding;
2. Convert Java strings to and from C const jchar* (uint32) arrays, representing Unicode
   characters;
3. Using Java APIs to refer to the original Java string content.

### UTF-8 strings

Janet provides a pair of operators: `#&` and `#$', to convert Java strings to const char* arrays,
and vice versa. The characters are encoded in Sun's
[modified UTF8](https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8)
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
of the string's data. You should not assume either way. Don't be tempted to cast away the const
operator to directly mutate the data.

The pointer returned by the `#&` operator is valid till the end of the block. Using it past that
block results in undefined behavior. If you need to make a long-lived reference to a Java string,
either make an explicit copy, or keep the reference to the original string.

In this example, we have assumed that the resulting array is zero-terminated. Technically, this is
not guaranteed by the JNI specification. That said, it has once been documented in the JNI tutorial
and appeared in print, and it was close to making it to the JNI specification (See this
[Stack Overflow discussion](http://stackoverflow.com/questions/16694239/java-native-code-string-ending)),
and therefore so much existing code depends on it that no sane JVM implementer is likely to diverge
from it.

The `#$` operator takes a zero-terminated modified UTF-8 character array, and returns a new Java
String with the equivalent content. The data is copied, so it is your responsibility to release
the original memory buffer (in our example, managed by `vector<char>`).

You may have noted the weirdness that we've declared the `vector` at the beginning of the method,
and wrapped everything else in a nested block. Unfortunately, this is currently necessary, due
to the way Janet handles Java exceptions in generated C++ code (with setjmp/longjmp rather than
C++ exceptions). As a consequence, any automatic object with non-trivial destructor (such as
`std::vector`) must be declared outside of any block with embedded Java (using back-ticks),
or else you'll get undefined behavior in case the embedded Java throws any exceptions.

### Unicode strings

Janet allows to convert Java strings to and from two-byte Unicode, represented in C/C++ as
`const jchar*` array. (The `jchar` is unsigned 16-bit integer). You can do that using another
pair of operators: '&' and '#$$':

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

As you can see, te usage of these operators is similar to the UTF-8 operators, with few important
exceptions:

* When converting from Java string to `const jchar*` with the `&` operator, the resulting array is
  _not_ zero-terminated. You must manage string length explicitly.
* Conversely, when creating a new Java string from `const jchar*` with the `#$$` operator, you must
  provide the length.

Just like with UTF-8 strings, the pointers are valid till the end of the block.

The '&' operator may or may not copy the original string content. In theory, JVM is more likely
to internally represent strings in a format compatible with const jchar* arrays, but don't get
your hopes too high; many JVMs will still make a copy.

### Java APIs

If all you need is to access individual characters or small fragments of a Java string, it may
be the most efficient to simply use methods of the `String` class from your native code:

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

