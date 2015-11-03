/* -*-Java-*- */

package examples;

class ControlFlow {

native "C" {
#include <stddef.h>
}

    public void test() {
        nativeReturnDemo();
        System.out.println("return test with try/finally" +
            " (should be 'b'): " + returnDemo());
        breakDemo();
    }

    /**
     * Use C return statement only for void methods with no embedded
     * Java at all
     */
    public native "C" void nativeReturnDemo() {
        printf("native return\n");
        return;
        /* ... */
    }
    
    public native "C" String nativeReturnWRONG() {
        printf("%s", `#&"native return\n"`);
        /* DO NOT DO IT EVER!!! No return type check, and 
           "finally" clause semantics is broken. */
        return NULL;
        /* ... */
    }

    /**
     * It is always better to use embedded Java return statement.
     * The Java type ckeck is performed, and it preserves Java
     * semantics of try/finally.
     */
    public native "C" String returnDemo() {
        `try` {
            `return "a";`
        } `finally` {
            `return "b";`
        }
    }

    /**
     * Use C break, continue, longjmp, and goto only within pure C  blocks.
     */
    public native "C" String breakDemo() {
        `int a;`
        while (1) {
            /* OK */
            break;
        }
    }
    
    public native "C" String breakDemoWRONG() {
        while (1) {
            `int a;`
            /* DO NOT DO IT EVER!!! */
            break;
        }
        
        do {
            `synchronized (this)` {
                /* this is especially evil, leaves monitor locked */
                break;
            }
        } while (0);
    }
}
