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

        main.fillArray();
        System.out.println(Arrays.toString(main.arr));
        main.embeddedMethodCall();

        main.methodWithNativeBlock();

        main.iostream();
        
        System.out.println(main.echoInUTF("Hola UTF! "));
        System.out.println(main.echoInUnicode("Hola Unicode! "));
        System.out.println(main.echoInJava("Hola Java! "));

        System.out.println(Arrays.toString(main.helloFromJanet()));
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

    native "C++" String sortJavaStringInUTF(String s) {
        std::vector<char> sorted;
        {
            const char* content = `#&s`;
            int len = strlen(content);
            sorted.resize(len + 1);
            strcpy(&*sorted.begin(), content);
            std::sort(sorted.begin(), sorted.end() - 1);  // Don't sort the terminal '\0'
            const char* sorted_content = &*sorted.begin();
            `return #$(sorted_content);`
        }
    }

    native "C++" String echoInUTF(String s) {
        const char* content = `#&s`;
        int len = strlen(content);
        std::vector<char> result(2 * len + 1);  // Leave space for terminal '\0'
        strncpy(&result[0], content, len);
        strncpy(&result[len], content, len);
        `return #$(&result[0]);`
    }

    native "C++" String echoInUnicode(String s) {
        const jchar* content = `&s`;
        int len = `s.length()`;
        std::vector<jchar> result(2 * len);
        std::copy(content, content + len, result.begin());
        std::copy(content, content + len, result.begin() + len);
        `return #$$(len > 0 ? &result[0] : 0, 2 * len);`
    }

    native "C++" String echoInJava(String s) {
        int len = `s.length()`;
        std::vector<jchar> result;
        for (int i = 0; i < len; ++i) result.push_back(`s.charAt(#(i))`);
        for (int i = 0; i < len; ++i) result.push_back(`s.charAt(#(i))`);
        `return #$$(len > 0 ? &result[0] : 0, 2 * len);`
    }

    native "C++" String[] helloFromJanet() {
        `String[] result = new String[3];
        result[0] = #$("Hello");
        result[1] = #$("from");
        result[2] = #$("Janet!");
        return result;`
    }
}
