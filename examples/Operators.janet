/* -*-Java-*- */

package examples;

public class Operators {

    public void test() {
        operatorDemo();
    }
    
    /**
     * Example of embedded Java arithmetic operators. They have
     * the same semantics as in pure Java. Currently, only '+', '-',
     * '*' and '/' operators are supported.
     */
    public native "C" void operatorDemo() {
        printf("2 + 2 = %d\n", `2 + 2`);
        `double[] d = new double[5];`
        `d[0] = 4.0;`
        `d[1] = d[0] * (5.3 + d.length / 3);`
        printf("result = %f\n", `d[1]`);
    }
}
