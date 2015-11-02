/* -*-Java-*- */

package examples;

public class Literals {

    public native "C" void test() {
        printf("literal demo: %i %li %f %lf %c %s\n", 
               (int)`12345`,
               (long)`12345L`, 
               (float)`123.45F`, 
               (double)`123.45`, 
               (char)`'$'`,
               (char*)`#&"Java string literal"`);
    }
}
