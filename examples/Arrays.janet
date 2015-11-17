/* -*-Java-*- */

package examples;

public class Arrays {

    public void test() {
        float f[] = new float[100];
        /* initialize the array */
        for (int i=0; i<100; i++) {
            f[i] = (float)(Math.random() * 20000.0);
        }

        System.out.println("array access demo: " +
            arrayAccessDemo(f, 0) + ", " +
            arrayAccessDemo(f, 1) + ", " +
            arrayAccessDemo(f, 2) + ", " +
            arrayAccessDemo(f, 3) + ", " +
            arrayAccessDemo(f, 4));
        
        sortArrayDemo(f);
        
        int[][][] t = arraysExpressionDemo();
        displayArray(t);
    }

    /**
     * Simple array access demo.
     */
    public native "C" float arrayAccessDemo(float[] arr, int idx) {
        `return arr[idx];`
    }
  
    /**
     * Sorting Java array of floats using native qsort() routine from
     * standard C library. Additionally, this method shows example
     * usage of native { ... } statement.
     */
    public void sortArrayDemo(float f[]) {

        /* display the array */
        System.out.println("Array before sorting:");
        for (int i=0; i<100; i++) System.out.print(" " + f[i]);
        System.out.println("\n"); System.out.flush();

        /* sort the array using native qsort() routine */
        native {
            qsort(`&f`, `f.length`, sizeof(jfloat), _cmpjfloat);
        }
 
        /* display the sorted array */
        System.out.println("Array after sorting:");
        for (int i=0; i<100; i++) System.out.print(" " + f[i]);
        System.out.println("\n"); System.out.flush();
        
    }

    /* comparator for qsort() on jfloats */   
    native "C" {
#include <stdio.h>
#include <stdlib.h>
        static int _cmpjfloat(const void *keyval, const void* datum) {
            return *((jfloat*)keyval) < *((jfloat*)datum)
                ? -1 
                : *((jfloat*)keyval) == *((jfloat*)datum) 
                    ? 0 
                    : 1;
        }
    }

    /**
     * This example shows sophisticated expressions involving
     * arrays which may be embedded into native code.
     */
    native int[][][] arraysExpressionDemo() {
        `int[][][] o;`
        `((o = new int[3][5][])[0][1] = new int[5])[3] = 5;`
        `return o;`
    }

    public static void displayArray(Object o) {
        displayArray(o, 0);
    }

    /**
     * This method displays the content of one- or multidimensional
     * array of ints or objects on screen. It is a quite sophisticated
     * example how Java operations can be performed from native method.
     * These operations include variable declarations, field accesses,
     * method invocations, array accesses, string operations.
     */
    private static native "C" void displayArray(Object o, int level) {
        int i, j, len;
        if (`o instanceof int[]`) {
            for (j=0; j<`level`; j++) {
                `System.out.print("    ")`;
            }
            `System.out.print("{ ")`;
            `int[] iarr = (int[])o;`
            for (i=0; i<`iarr.length`; i++) {
                `System.out.print("".concat(Integer.toString(iarr[#(i)])).concat(" "))`;
            }
            `System.out.println(" }")`;
        } else if (`o instanceof Object[]`) {
            `Object[] oarr = (Object[])o;`
            for (j=0; j<`level`; j++) {
                `System.out.print("    ")`;
            }
            `System.out.println("{")`;
            for (i=0; i<`oarr.length`; i++) {
                `displayArray(oarr[#(i)], #(`level`+1))`;
            }
            for (j=0; j<`level`; j++) {
               `System.out.print("    ")`;
            }
            `System.out.println("}")`;
        }
        else {
            for (j=0; j<`level`; j++) {
                `System.out.print("    ")`;
            }
            `System.out.println(o)`;
        }
    }

}
