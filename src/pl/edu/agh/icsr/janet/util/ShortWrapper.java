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
 * Mutable wrapper for short value. Since it is mutable, it may be used
 * for passing short values by reference (unlike java.lang.Short).
 */
public class ShortWrapper {

    /**
     * wrapped, mutable short value.
     */
    public short val;

    /**
     * Constructs a new wrapper with a zero value.
     */
    public ShortWrapper() {
        this((short)0);
    }

    /**
     * Constructs a new wrapper with given value.
     */
    public ShortWrapper(short val) {
        this.val = val;
    }
}
