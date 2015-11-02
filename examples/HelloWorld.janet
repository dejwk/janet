/* -*-Java-*- */

package examples;

public class HelloWorld {

    public void test() {
        testHello();
        testHello("New World");
        testHelloCallback();
    }

    public native "C" void testHello() {
        printf("Hello world!\n");
    }

    public native "C" void testHello(String text) {
        printf("Hello %s!\n", `#&text`);
    }

    public native "C" void testHelloCallback() {
        `System.out.println("Hello world from callback")`;
    }
        
}
