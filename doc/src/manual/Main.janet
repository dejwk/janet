/* -*-Java-*- */

/*
 * This file contains examples discussed in ../Manual.md.
 */

package manual;

import java.util.Arrays;

public class Main {

native "C++" {
#include <algorithm>
#include <cstring>
#include <iostream>
#include <vector>
}

    public static void main(String[] args) {
        System.out.println(trivialStaticNativeMethod());
        System.out.println(staticNativeMethodWithParameter(3));
        
        Main main = new Main();

        main.basicExpressions();
        System.out.println(main.arr[3]);

        try {
            main.testExceptionPropagation();
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

        main.fillArray();
        System.out.println(Arrays.toString(main.arr));
        main.embeddedMethodCall();

        main.methodWithNativeBlock();

        main.iostream();
        
        System.out.println(main.echoInUTF("Hola UTF! "));
        System.out.println(main.echoInUnicode("Hola Unicode! "));
        System.out.println(main.echoInJava("Hola Java! "));

        System.out.println(Arrays.toString(main.helloFromJanet()));

        int arr[] = new int[10];
        for (int i=0; i<10; i++) { arr[i] = (i * 1549) % 87; }
        System.out.println("Array before sorting: " + Arrays.toString(arr));
        sortArray(arr);
        System.out.println("Array after sorting: " + Arrays.toString(arr));

        tryCatchFinally();
        boolean heldLock = synchronizedTest(main);
        if (!heldLock || Thread.holdsLock(main)) throw new AssertionError();
    }

    private static native "C++" int trivialStaticNativeMethod() {
        return 5;  // C++
    }

    private static native "C++" int staticNativeMethodWithParameter(int parameter) {
        return `parameter` + 5;
    }

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
    
    void methodWithNativeBlock() {
        int local = 7;
        native "C++" {
            int local_cpp = `local`;  // Reference to a Java local variable.
            `f1 = #(local_cpp + 1)`;  // Setting a Java instance field.
        }
        System.out.println(f1);
    }
    
    native "C++" void iostream() {
        `System.out.flush();`
        std::cout << "C++ std::cout\n";
        std::cout.flush();
    }

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

    native "C++" String echoInJava(String s) {
        std::vector<jchar> result;
        {
            int len = `s.length()`;
            for (int i = 0; i < len; ++i) result.push_back(`s.charAt(#(i))`);
            for (int i = 0; i < len; ++i) result.push_back(`s.charAt(#(i))`);
            `return #$$(len > 0 ? &result[0] : 0, 2 * len);`
        }
    }

    native "C++" String[] helloFromJanet() {
        `String[] result = new String[3];
        result[0] = #$("Hello");
        result[1] = #$("from");
        result[2] = #$("Janet!");
        return result;`
    }

    native "C++" static void sortArray(int[] arr) {
        std::sort(`&arr`, `&arr` + `arr.length`);
    }

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

    native "C++" static boolean synchronizedTest(Object target) {
        `synchronized(target) {
            `std::cout << "In synchronized\n";`
            return Thread.holdsLock(target);
         }`
    }
}
