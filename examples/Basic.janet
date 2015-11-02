/* -*-Java-*- */

package examples;

class Basic {

    int f1;
    Object f2;
    int[] arr = new int[5];

    public void test() {
        nativeMethodDemo();
        nativeStatementDemo();
        basicExpressions();
        basicStatements();
    }

    public native "C" void nativeMethodDemo() {
        printf("Native method!\n");
    }

    public void nativeStatementDemo() {
        // java code here
        native "C" {
            printf("Native statement 1!\n");
        }
        // "C" can be omitted; C is default in JANET
        native {
            printf("Native statement 2!\n");
        }
    }

    /**
     * This method shows some embedded Java expressions.
     */
    native "C" void basicExpressions() {
        
        /* field access */
        int i = `f1`;

        /* method invocation */
        `foo()`;

        /* more sophisticated invocation + field accesses + string */
        `System.out.println("More sophisticated example")`;

        /* assignment + instance creation */
        `f2 = new Object();`

        /* native subexpressions inside embedded Java expressions */
        for (i=0; i<`arr.length`; i++) {
            `arr[#(i)] = #(i)`;
        }
    }
        
    /**
     * This method shows some embedded Java statements.
     */
    native void basicStatements() {

        /* variable declaration */
        `Object o;`

        /* declaration with initializer */
        `int[] arr = new int[5];`

        /* try/throw/catch/finally */
        `try` {
            printf("In try\n");
            `throw new Exception();`
        } `catch (Exception e)` {
            printf("In catch\n");
        } `finally` {
            printf("In finally\n");
        }
                
        /* synchronized */
        `synchronized (arr)` {
            /* ... */
        }

        /* block */
        `{ /* ... */ }`

        /* sequence of statements and blocks */
        `int i;

         try {
             /* ... */
         } finally {
             /* ... */
         }
         System.out.println("test");`
    }

    void foo() {
        System.out.println("In foo()");
    }
    

}
