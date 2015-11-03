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

import java.util.Arrays;

/**
 * This class contains various methods for manipulating array regions (such as
 * sorting and searching). There are equivalents of appropriate array methods
 * from class {@link java.util.Arrays}.
 */
public class Regions {
    // Suppresses default constructor, ensuring non-instantiability.
    private Regions() {
    }

    /**
     * Sorts the specified region of bytes into ascending numerical order,
     * using {@link Arrays#sort(byte[], int, int)} method.
     *
     * @param a the region to be sorted.
     */
    public static void sort(ByteRegion a) {
        Arrays.sort(a.baseArray(), a.startIdx(), a.endIdx());
    }

    /**
     * Sorts the specified region of shorts into ascending numerical order,
     * using {@link Arrays#sort(short[], int, int)} method.
     *
     * @param a the region to be sorted.
     */
    public static void sort(ShortRegion a) {
        Arrays.sort(a.baseArray(), a.startIdx(), a.endIdx());
    }

    /**
     * Sorts the specified region of chars into ascending numerical order,
     * using {@link Arrays#sort(char[], int, int)} method.
     *
     * @param a the region to be sorted.
     */
    public static void sort(CharRegion a) {
        Arrays.sort(a.baseArray(), a.startIdx(), a.endIdx());
    }

    /**
     * Sorts the specified region of integers into ascending numerical order,
     * using {@link Arrays#sort(int[], int, int)} method.
     *
     * @param a the region to be sorted.
     */
    public static void sort(IntRegion a) {
        Arrays.sort(a.baseArray(), a.startIdx(), a.endIdx());
    }

    /**
     * Sorts the specified region of longs into ascending numerical order,
     * using {@link Arrays#sort(long[], int, int)} method.
     *
     * @param a the region to be sorted.
     */
    public static void sort(LongRegion a) {
        Arrays.sort(a.baseArray(), a.startIdx(), a.endIdx());
    }

    /**
     * Sorts the specified region of floats into ascending numerical order,
     * using {@link Arrays#sort(float[], int, int)} method.
     *
     * @param a the region to be sorted.
     */
    public static void sort(FloatRegion a) {
        Arrays.sort(a.baseArray(), a.startIdx(), a.endIdx());
    }

    /**
     * Sorts the specified region of doubles into ascending numerical order,
     * using {@link Arrays#sort(double[], int, int)} method.
     *
     * @param a the region to be sorted.
     */
    public static void sort(DoubleRegion a) {
        Arrays.sort(a.baseArray(), a.startIdx(), a.endIdx());
    }

    /**
     * Searches the specified region of bytes for the specified value using the
     * binary search algorithm.  The region <strong>must</strong> be sorted (as
     * by the {@link #sort} method) prior to making this call.  If it
     * is not sorted, the results are undefined.  If the region contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the region to be searched.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the region;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the region: the index of the first
     *	       element greater than the key, or <tt>region.length()</tt>, if all
     *	       elements in the region are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     * @see #sort(ByteRegion)
     */
    public static int binarySearch(ByteRegion a, byte key) {
        byte[] arr = a.baseArray();
        int low = a.offset();
        int high = a.length() - 1;

        while (low <= high) {
            int mid =(low + high)/2;
            byte midVal = arr[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified region of shorts for the specified value using the
     * binary search algorithm.  The region <strong>must</strong> be sorted (as
     * by the {@link #sort} method) prior to making this call.  If it
     * is not sorted, the results are undefined.  If the region contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the region to be searched.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the region;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the region: the index of the first
     *	       element greater than the key, or <tt>region.length()</tt>, if all
     *	       elements in the region are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     * @see #sort(ShortRegion)
     */
    public static int binarySearch(ShortRegion a, short key) {
        short[] arr = a.baseArray();
        int low = a.offset();
        int high = a.length() - 1;

        while (low <= high) {
            int mid =(low + high)/2;
            short midVal = arr[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified region of chars for the specified value using the
     * binary search algorithm.  The region <strong>must</strong> be sorted (as
     * by the {@link #sort} method) prior to making this call.  If it
     * is not sorted, the results are undefined.  If the region contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the region to be searched.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the region;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the region: the index of the first
     *	       element greater than the key, or <tt>region.length()</tt>, if all
     *	       elements in the region are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     * @see #sort(CharRegion)
     */
    public static int binarySearch(CharRegion a, char key) {
        char[] arr = a.baseArray();
        int low = a.offset();
        int high = a.length() - 1;

        while (low <= high) {
            int mid =(low + high)/2;
            char midVal = arr[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified region of ints for the specified value using the
     * binary search algorithm.  The region <strong>must</strong> be sorted (as
     * by the {@link #sort} method) prior to making this call.  If it
     * is not sorted, the results are undefined.  If the region contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the region to be searched.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the region;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the region: the index of the first
     *	       element greater than the key, or <tt>region.length()</tt>, if all
     *	       elements in the region are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     * @see #sort(IntRegion)
     */
    public static int binarySearch(IntRegion a, int key) {
        int[] arr = a.baseArray();
        int low = a.offset();
        int high = a.length() - 1;

        while (low <= high) {
            int mid =(low + high)/2;
            int midVal = arr[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified region of longs for the specified value using the
     * binary search algorithm.  The region <strong>must</strong> be sorted (as
     * by the {@link #sort} method) prior to making this call.  If it
     * is not sorted, the results are undefined.  If the region contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the region to be searched.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the region;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the region: the index of the first
     *	       element greater than the key, or <tt>region.length()</tt>, if all
     *	       elements in the region are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     * @see #sort(LongRegion)
     */
    public static int binarySearch(LongRegion a, long key) {
        long[] arr = a.baseArray();
        int low = a.offset();
        int high = a.length() - 1;

        while (low <= high) {
            int mid =(low + high)/2;
            long midVal = arr[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified region of floats for the specified value using the
     * binary search algorithm.  The region <strong>must</strong> be sorted (as
     * by the {@link #sort} method) prior to making this call.  If it
     * is not sorted, the results are undefined.  If the region contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the region to be searched.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the region;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the region: the index of the first
     *	       element greater than the key, or <tt>region.length()</tt>, if all
     *	       elements in the region are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     * @see #sort(FloatRegion)
     */
    public static int binarySearch(FloatRegion a, float key) {
        float[] arr = a.baseArray();
        int low = a.offset();
        int high = a.length() - 1;

        while (low <= high) {
            int mid =(low + high)/2;
            float midVal = arr[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified region of doubles for the specified value using the
     * binary search algorithm.  The region <strong>must</strong> be sorted (as
     * by the {@link #sort} method) prior to making this call.  If it
     * is not sorted, the results are undefined.  If the region contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the region to be searched.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the region;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the region: the index of the first
     *	       element greater than the key, or <tt>region.length()</tt>, if all
     *	       elements in the region are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     * @see #sort(DoubleRegion)
     */
    public static int binarySearch(DoubleRegion a, double key) {
        double[] arr = a.baseArray();
        int low = a.offset();
        int high = a.length() - 1;

        while (low <= high) {
            int mid =(low + high)/2;
            double midVal = arr[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Fills the specified region of booleans with a specific value,
     * using {@link Arrays#fill(boolean[], int, int, boolean)} method.
     *
     * @param a the region to be filled.
     */
    public static void fill(BooleanRegion a, boolean value) {
        Arrays.fill(a.baseArray(), a.startIdx(), a.endIdx(), value);
    }

    /**
     * Fills the specified region of bytes with a specific value,
     * using {@link Arrays#fill(byte[], int, int, byte)} method.
     *
     * @param a the region to be filled.
     */
    public static void fill(ByteRegion a, byte value) {
        Arrays.fill(a.baseArray(), a.startIdx(), a.endIdx(), value);
    }

    /**
     * Fills the specified region of shorts with a specific value,
     * using {@link Arrays#fill(short[], int, int, short)} method.
     *
     * @param a the region to be filled.
     */
    public static void fill(ShortRegion a, short value) {
        Arrays.fill(a.baseArray(), a.startIdx(), a.endIdx(), value);
    }

    /**
     * Fills the specified region of chars with a specific value,
     * using {@link Arrays#fill(char[], int, int, char)} method.
     *
     * @param a the region to be filled.
     */
    public static void fill(CharRegion a, char value) {
        Arrays.fill(a.baseArray(), a.startIdx(), a.endIdx(), value);
    }

    /**
     * Fills the specified region of integers with a specific value,
     * using {@link Arrays#fill(int[], int, int, int)} method.
     *
     * @param a the region to be filled.
     */
    public static void fill(IntRegion a, int value) {
        Arrays.fill(a.baseArray(), a.startIdx(), a.endIdx(), value);
    }

    /**
     * Fills the specified region of longs with a specific value,
     * using {@link Arrays#fill(long[], int, int, long)} method.
     *
     * @param a the region to be filled.
     */
    public static void fill(LongRegion a, long value) {
        Arrays.fill(a.baseArray(), a.startIdx(), a.endIdx(), value);
    }

    /**
     * Fills the specified region of floats with a specific value,
     * using {@link Arrays#fill(float[], int, int, float)} method.
     *
     * @param a the region to be filled.
     */
    public static void fill(FloatRegion a, float value) {
        Arrays.fill(a.baseArray(), a.startIdx(), a.endIdx(), value);
    }

    /**
     * Fills the specified region of doubles with a specific value,
     * using {@link Arrays#fill(double[], int, int, double)} method.
     *
     * @param a the region to be filled.
     */
    public static void fill(DoubleRegion a, double value) {
        Arrays.fill(a.baseArray(), a.startIdx(), a.endIdx(), value);
    }

    /**
     * Returns <tt>true</tt> if the two specified regions of booleans are
     * <i>equal</i> to one another.  Two regions are considered equal if both
     * regions contain the same number of elements, and all corresponding pairs
     * of elements in the two regions are equal.  In other words, two regions
     * are equal if they contain the same elements in the same order.  Also,
     * two region references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one region to be tested for equality.
     * @param a2 the other region to be tested for equality.
     * @return <tt>true</tt> if the two regions are equal.
     */
    public static boolean equals(BooleanRegion a, BooleanRegion a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;
        boolean[] arr1 = a.baseArray();
        boolean[] arr2 = a2.baseArray();
        if (arr1 == arr2)
            return true;

        int length = a.length();
        if (a2.length() != length)
            return false;

        for (int i=a.offset(), j=a2.offset(); length-- > 0; i++, j++)
            if (arr1[i] != arr2[i])
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified regions of bytes are
     * <i>equal</i> to one another.  Two regions are considered equal if both
     * regions contain the same number of elements, and all corresponding pairs
     * of elements in the two regions are equal.  In other words, two regions
     * are equal if they contain the same elements in the same order.  Also,
     * two region references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one region to be tested for equality.
     * @param a2 the other region to be tested for equality.
     * @return <tt>true</tt> if the two regions are equal.
     */
    public static boolean equals(ByteRegion a, ByteRegion a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;
        byte[] arr1 = a.baseArray();
        byte[] arr2 = a2.baseArray();
        if (arr1 == arr2)
            return true;

        int length = a.length();
        if (a2.length() != length)
            return false;

        for (int i=a.offset(), j=a2.offset(); length-- > 0; i++, j++)
            if (arr1[i] != arr2[i])
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified regions of shorts are
     * <i>equal</i> to one another.  Two regions are considered equal if both
     * regions contain the same number of elements, and all corresponding pairs
     * of elements in the two regions are equal.  In other words, two regions
     * are equal if they contain the same elements in the same order.  Also,
     * two region references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one region to be tested for equality.
     * @param a2 the other region to be tested for equality.
     * @return <tt>true</tt> if the two regions are equal.
     */
    public static boolean equals(ShortRegion a, ShortRegion a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;
        short[] arr1 = a.baseArray();
        short[] arr2 = a2.baseArray();
        if (arr1 == arr2)
            return true;

        int length = a.length();
        if (a2.length() != length)
            return false;

        for (int i=a.offset(), j=a2.offset(); length-- > 0; i++, j++)
            if (arr1[i] != arr2[i])
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified regions of chars are
     * <i>equal</i> to one another.  Two regions are considered equal if both
     * regions contain the same number of elements, and all corresponding pairs
     * of elements in the two regions are equal.  In other words, two regions
     * are equal if they contain the same elements in the same order.  Also,
     * two region references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one region to be tested for equality.
     * @param a2 the other region to be tested for equality.
     * @return <tt>true</tt> if the two regions are equal.
     */
    public static boolean equals(CharRegion a, CharRegion a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;
        char[] arr1 = a.baseArray();
        char[] arr2 = a2.baseArray();
        if (arr1 == arr2)
            return true;

        int length = a.length();
        if (a2.length() != length)
            return false;

        for (int i=a.offset(), j=a2.offset(); length-- > 0; i++, j++)
            if (arr1[i] != arr2[i])
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified regions of ints are
     * <i>equal</i> to one another.  Two regions are considered equal if both
     * regions contain the same number of elements, and all corresponding pairs
     * of elements in the two regions are equal.  In other words, two regions
     * are equal if they contain the same elements in the same order.  Also,
     * two region references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one region to be tested for equality.
     * @param a2 the other region to be tested for equality.
     * @return <tt>true</tt> if the two regions are equal.
     */
    public static boolean equals(IntRegion a, IntRegion a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;
        int[] arr1 = a.baseArray();
        int[] arr2 = a2.baseArray();
        if (arr1 == arr2)
            return true;

        int length = a.length();
        if (a2.length() != length)
            return false;

        for (int i=a.offset(), j=a2.offset(); length-- > 0; i++, j++)
            if (arr1[i] != arr2[i])
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified regions of longs are
     * <i>equal</i> to one another.  Two regions are considered equal if both
     * regions contain the same number of elements, and all corresponding pairs
     * of elements in the two regions are equal.  In other words, two regions
     * are equal if they contain the same elements in the same order.  Also,
     * two region references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one region to be tested for equality.
     * @param a2 the other region to be tested for equality.
     * @return <tt>true</tt> if the two regions are equal.
     */
    public static boolean equals(LongRegion a, LongRegion a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;
        long[] arr1 = a.baseArray();
        long[] arr2 = a2.baseArray();
        if (arr1 == arr2)
            return true;

        int length = a.length();
        if (a2.length() != length)
            return false;

        for (int i=a.offset(), j=a2.offset(); length-- > 0; i++, j++)
            if (arr1[i] != arr2[i])
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified regions of floats are
     * <i>equal</i> to one another.  Two regions are considered equal if both
     * regions contain the same number of elements, and all corresponding pairs
     * of elements in the two regions are equal.  In other words, two regions
     * are equal if they contain the same elements in the same order.  Also,
     * two region references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one region to be tested for equality.
     * @param a2 the other region to be tested for equality.
     * @return <tt>true</tt> if the two regions are equal.
     */
    public static boolean equals(FloatRegion a, FloatRegion a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;
        float[] arr1 = a.baseArray();
        float[] arr2 = a2.baseArray();
        if (arr1 == arr2)
            return true;

        int length = a.length();
        if (a2.length() != length)
            return false;

        for (int i=a.offset(), j=a2.offset(); length-- > 0; i++, j++)
            if (arr1[i] != arr2[i])
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified regions of doubles are
     * <i>equal</i> to one another.  Two regions are considered equal if both
     * regions contain the same number of elements, and all corresponding pairs
     * of elements in the two regions are equal.  In other words, two regions
     * are equal if they contain the same elements in the same order.  Also,
     * two region references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one region to be tested for equality.
     * @param a2 the other region to be tested for equality.
     * @return <tt>true</tt> if the two regions are equal.
     */
    public static boolean equals(DoubleRegion a, DoubleRegion a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;
        double[] arr1 = a.baseArray();
        double[] arr2 = a2.baseArray();
        if (arr1 == arr2)
            return true;

        int length = a.length();
        if (a2.length() != length)
            return false;

        for (int i=a.offset(), j=a2.offset(); length-- > 0; i++, j++)
            if (arr1[i] != arr2[i])
                return false;

        return true;
    }


}