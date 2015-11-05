/* -*-Java-*- */

package examples;

public class HelloWorldCpp {

native "C++" {
#include <iostream>
}

    public void test() {
        testHello("New World");
    }

    public native "C++" void testHello(String text) {
        std::cout << `#&text`;
    }
}
