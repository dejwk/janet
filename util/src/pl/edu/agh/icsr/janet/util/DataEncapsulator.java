/* ***** BEGIN LICENSE BLOCK *****
 *
 * THIS FILE IS AN ADD-ON TO THE JANET TOOL.
 * For more information, see http://www.icsr.agh.edu.pl/janet.
 *
 * You may use, reproduce, modify, display, perform, sublicense, distribute
 * in source and/or binary form under a license of your choice, sell, offer
 * for sale, and/or otherwise dispose of this file (or portions thereof)
 * subject to the following: every modified version of this file must contain
 * description of changes, information about the original author in the class
 * documentation comment, and the URL: http://www.icsr.agh.edu.pl/janet.
 *
 * THE CODE IS PROVIDED UNDER THIS LICENSE ON AN "AS IS" BASIS, WITHOUT WARRANTY
 * OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION,
 * WARRANTIES THAT THE CODE IS FREE OF DEFECTS, MERCHANTABLE, FIT FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGING. THE ENTIRE RISK AS TO THE QUALITY AND
 * PERFORMANCE OF THE CODE IS WITH YOU. SHOULD THE CODE PROVE DEFECTIVE IN ANY
 * RESPECT, YOU (NOT THE INITIAL DEVELOPER OR ANY OTHER CONTRIBUTOR OF THE JANET
 * TOOL) ASSUME THE COST OF ANY NECESSARY SERVICING, REPAIR OR CORRECTION. THIS
 * DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE. NO USE
 * OF THE CODE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 *
 * ***** END LICENSE BLOCK *****/

package pl.edu.agh.icsr.janet.util;

/**
 * Wrapper for a native variable (usually a pointer). Conceptually, object
 * of this class may represent a native data structure referenced by a given
 * pointer. The pointer is stored as a <code>protected long data</code> field.
 * In subclasses you may define additional accessor methods in order to
 * provide a Java interface to the encapsulated native data structure.
 */
public abstract class DataEncapsulator {

    /**
     * Returns the value representing empty reference (default 0).
     *
     * @return the value representing empty reference
     */
    protected long getNullValue() { return 0; }

    /**
     * Value of a native variable. Initially equals to the value returned
     * by the #getNullValue() method.
     */
    protected long data = getNullValue();

    /**
     * Creates new DataEncapsulator object with empty data field (as obtained
     * from the #getNullValue() method).
     */
    public DataEncapsulator() {
        data = getNullValue();
    }

    /**
     * Creates new DataEncapsulator object with given data.
     */
    protected DataEncapsulator(long data) {
        this.data = data;
    }

    /**
     * Checks if the data field is empty (equals to the value returned
     * by the #getNullValue() method).
     */
    public final boolean isNull() { return data == getNullValue(); }

    /**
     * This method is invoked when the encapsulated data variable is being
     * changed or cleared, so that the native data structure should be disposed.
     * You should usually override this method to perform any neccessary
     * clean-up.
     */
    protected void freeNative() throws Throwable {}

    /**
     * Disposes attached native data structure (by invoking #freeNative()
     * method) and clears the data variable. This method should be used to
     * dispose native data before the reference is dropped, in preference
     * to relying on object finalization.
     *
     * #throws NullPointerException if the data variable is already empty,
     * i.e. equals to the value returned by #getNullValue().
     */
    public synchronized void free() throws Throwable {
        if (isNull()) {
            throw new NullPointerException("Object represents null data.");
        }
        freeNative();
        data = getNullValue();
    }

    /**
     * If the data value is not empty, invokes #free() during object
     * finalization to perform native-side  clean-up.
     */
    protected final void finalize() throws Throwable {
        if (!isNull()) { free(); }
        super.finalize();
    }
}



