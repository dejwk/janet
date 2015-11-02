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
 * Represents a contiguous region in an array of floats. The region may be
 * empty, or it may refer to the whole array. The original array is referenced
 * rather than copied; changes made to it are immediately visible to region
 * objects. This class may be used for passing "pointers into the middle
 * of arrays" to native methods.
 *
 * @author Dawid Kurzyniec
 * @version 1.0, 2002/01/02
 */
public final class FloatRegion implements Region {

    /**
     * The constant representing an empty region.
     */
    public static final FloatRegion EMPTY =
        new FloatRegion(new float[0]);

    /**
     * The base array reference.
     */
    float[] baseArray;

    /**
     * Offset in the base array where this region begins.
     */
    int offset;

    /**
     * The length of this region.
     */
    int length;

    /**
     * Creates new region containing the whole array.
     *
     * @param array the base array
     * @throws NullPointerException if the array is null
     */
    public FloatRegion(float[] array) {
        this(array, 0, array.length);
    }

    /**
     * Creates new region starting at the given offset and ending at the
     * end of the array.
     *
     * @param array the base array
     * @param offset the offset in the base array where the region starts
     *
     * @throw NullPointerException if the array is null
     * @throw ArrayIndexOutOfBoundsException if the offset is negative
     *        or if it is greater than length of the array
     */
    public FloatRegion(float[] array, int offset) {
        this(array, offset, array.length - offset);
    }

    /**
     * Creates new region of specified length, starting at the given offset.
     *
     * @param array the base array
     * @param offset the offset in the base array where the region starts
     * @param length the length of the region
     *
     * @throw NullPointerException if the array is null
     * @throw ArrayIndexOutOfBoundsException if either offset or length
     *        are negative, or if their sum exceeds the length of the array
     */
    public FloatRegion(float[] array, int offset, int length) {
        if (array == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || length < 0) {
            throw new IllegalArgumentException();
        }
        if (offset + length < array.length) {
            throw new ArrayIndexOutOfBoundsException(offset + length);
        }
        this.baseArray = array;
        this.offset = offset;
        this.length = length;
    }

    /**
     * Returns the base array.
     *
     * @return the base array
     */
    public float[] baseArray() {
        return baseArray;
    }

    /**
     * Returns the base array as an object.
     *
     * @return the base array
     */
    public Object baseArrayAsObject() {
        return baseArray;
    }
    /**
     * Returns the offset in the base array where the region starts.
     *
     * @return the offset in the base array where the region starts
     */
    public int offset() {
        return offset;
    }

    /**
     * Returns the length of the region.
     *
     * @return the length of the region
     */
    public int length() {
        return length;
    }

    /**
     * The same as {@link #offset()}.
     *
     * @return the offset in the base array where the region starts
     */
    public int startIdx() {
        return offset;
    }

    /**
     * Returns the offset in the base array where the region ends.
     *
     * @return the offset in the base array where the region ends
     */
    public int endIdx() {
        return offset + length;
    }

    /**
     * Tests if the region is empty (has a length of 0).
     *
     * @return  <code>true</code> if the region is empty;
     *          <code>false</code> otherwise.
     */
    public final boolean isEmpty() {
        return length() == 0;
    }

    /**
     * Returns a new array being a copy of this region.
     *
     * @return a copy of this region, as a new array.
     */
    public float[] toArray() {
        float[] array = new float[length];
        System.arraycopy(baseArray, offset, array, 0, length);
        return array;
    }
}

