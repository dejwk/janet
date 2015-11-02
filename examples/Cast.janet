/* -*-Java-*- */

package examples;

public class Cast {

    public void test() {
        castDemo();
    }

    /**
     * This method shows embedded cast expressions. In fact, the
     * whole native method consists of single, big embedded Java
     * statement.
     */
    native "C" void castDemo() {
       `int i = 10;
        char c = (char)i;
        try {
            Object o = new Object();
            System.out.println(o.getClass());
            (String)o;
        } catch (ClassCastException e) {
            System.out.println("Cast exception caught as expected");
        }`
    }
}
