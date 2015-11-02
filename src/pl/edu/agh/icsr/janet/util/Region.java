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
 * Represents a contiguous region in an array of some primitive type.
 * The region may be empty, or it may refer to the whole array. This class may
 * be used for passing "pointers into the middle of arrays" to native methods.
 *
 * @author Dawid Kurzyniec
 * @version 1.0, 2002/01/02
 */
public interface Region extends Cloneable, java.io.Serializable {

    /**
     * Returns the base array as object.
     *
     * @return the base array as object
     */
    public Object baseArrayAsObject();

    /**
     * Returns the offset in the base array where the region starts.
     *
     * @return the offset in the base array where the region starts
     */
    public int offset();

    /**
     * Returns the length of the region.
     *
     * @return the length of the region
     */
    public int length();

    /**
     * The same as {@link #offset()}.
     *
     * @return the offset in the base array where the region starts
     */
    public int startIdx();

    /**
     * Returns the offset in the base array where the region ends.
     *
     * @return the offset in the base array where the region ends
     */
    public int endIdx();

    /**
     * Tests if the region is empty (has a length of 0).
     *
     * @return  <code>true</code> if the region is empty;
     *          <code>false</code> otherwise.
     */
    public boolean isEmpty();

}