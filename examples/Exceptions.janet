/* -*-Java-*- */

package examples;

public class Exceptions {

    public void test() {
        try1();
        try2();
        try {
            throw1();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Native code try/throw/catch/finally.
     */
    public native "C" void try1() {
        /* try/throw/catch/finally */
        `try` {
            printf("In try\n");
            `throw new Exception();`
        } `catch (Exception e)` {
            printf("In catch\n");
        } `finally` {
            printf("In finally\n");
        }
    }

    /**
     * Native code catching exception from callback
     */
    public native "C" void try2() {
        /* try/throw/catch/finally */
        `try` {
            printf("In try\n");
            `foo();`
        } `catch (Exception e)` {
            printf("In catch\n");
            `System.out.println(e.getMessage())`;
        } `finally` {
            printf("In finally\n");
        }
    }

    /**
     * Native code throwing exception
     */
    public native "C" void throw1() throws Exception {
        /* try/throw/catch/finally */
        `try` {
            printf("In try\n");
            `throw new Exception("sample exception from C");`
        } `finally` {
            printf("In finally\n");
        }
    }


    void foo() throws Exception {
        throw new Exception("sample exception");
    }
                

}
