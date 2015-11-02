/* -*-Java-*- */

package examples;

public class Strings {

    public void test() {
        System.out.println(getNativeString());
        printJavaString("java string");
        javaStringToUnicodeNative("java string");
    }

    /**
     * Converting C string to Java string
     */
    public native "C" String getNativeString() {
        `return  #$("C string literal");`
    }
    
    /**
     * Converting Java string to native string
     */
    public native "C" String printJavaString(String s) {
        printf("%s\n", `#&s`);
    }
    
    /**
     * Converting Java string to native unicode string
     */
    public native "C" String javaStringToUnicodeNative(String s) {
        const jchar* string = `&s`;
    }
}
