/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package harmony;

/**
 * Static library that provides all the <b>bit level</b> operations for
 * {@link BigInteger}. The operations are:
 * <ul type="circle">
 * <li>Left Shifting</li>
 * <li>Right Shifting</li>
 * <li>Bit clearing</li>
 * <li>Bit setting</li>
 * <li>Bit counting</li>
 * <li>Bit testing</li>
 * <li>Getting of the lowest bit set</li>
 * </ul>
 * All operations are provided in immutable way, and some in both mutable and
 * immutable.
 */
class BitLevel {

    /** @see BigInteger#bitLength() */
    static int bitLength(BigInteger val) {
        int bLength = (val.numberLength() << 5);
        int highDigit = val.getDigit(val.numberLength() - 1);

        // Subtracting all sign bits
        bLength -= Integer.numberOfLeadingZeros(highDigit);
        return bLength;
    }

    /**
     * Performs a fast bit testing for positive numbers. The bit to to be tested
     * must be in the range {@code [0, val.bitLength()-1]}
     */
    static boolean testBit(BigInteger val, int n) {
        // PRE: 0 <= n < val.bitLength()
        return ((val.getDigit(n >> 5) & (1 << (n & 31))) != 0);
    }

    /** @see BigInteger#shiftLeft(int) */
    static BigInteger shiftLeft(BigInteger source, int count) {
        int intCount = count >> 5;
        count &= 31; // %= 32
        int resLength = source.numberLength() + intCount
                + ( ( count == 0 ) ? 0 : 1 );
        int resDigits[] = new int[resLength];

        shiftLeft(resDigits, source.digits(), intCount, count);
        BigInteger result = new BigInteger(source.sign, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }

    /**
     * Abstractly shifts left an array of integers in little endian (i.e. shift
     * it right). Total shift distance in bits is intCount * 32 + count
     *
     * @param result the destination array
     * @param source the source array
     * @param intCount the shift distance in integers
     * @param count an additional shift distance in bits
     */
    static void shiftLeft(int result[], int source[], int intCount, int count) {
        if (count == 0) {
            System.arraycopy(source, 0, result, intCount, result.length
                    - intCount);
        } else {
            int rightShiftCount = 32 - count;

            result[result.length - 1] = 0;
            for (int i = result.length - 1; i > intCount; i--) {
                result[i] |= source[i - intCount - 1] >>> rightShiftCount;
                result[i - 1] = source[i - intCount - 1] << count;
            }
        }

        for (int i = 0; i < intCount; i++) {
            result[i] = 0;
        }
    }

    /**
     * Shifts the source digits left one bit, creating a value whose magnitude
     * is doubled.
     *
     * @param result an array of digits that will hold the computed result when
     *      this method returns. The size of this array is {@code srcLen + 1},
     *      and the format is the same as {@link BigInteger#digits}.
     * @param source the array of digits to shift left, in the same format as
     *      {@link BigInteger#digits}.
     * @param srcLen the length of {@code source}; may be less than {@code
     *      source.length}
     */
    static void shiftLeftOneBit(int result[], int source[], int srcLen) {
        int carry = 0;
        for (int i = 0; i < srcLen; i++) {
            int val = source[i];
            result[i] = (val << 1) | carry;
            carry = val >>> 31;
        }
    }

    /** @see BigInteger#shiftRight(int) */
    static BigInteger shiftRight(BigInteger source, int count) {
        int intCount = count >> 5; // count of integers
        count &= 31; // count of remaining bits
        int resLength = source.numberLength() - intCount;
        int resDigits[] = new int[resLength + 1];

        shiftRight(resDigits, resLength, source.digits(), intCount, count);
        BigInteger result = new BigInteger(source.sign, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }

    /**
     * Shifts right an array of integers. Total shift distance in bits is
     * intCount * 32 + count.
     *
     * @param result
     *            the destination array
     * @param resultLen
     *            the destination array's length
     * @param source
     *            the source array
     * @param intCount
     *            the number of elements to be shifted
     * @param count
     *            the number of bits to be shifted
     * @return dropped bit's are all zero (i.e. remainder is zero)
     */
    static boolean shiftRight(int result[], int resultLen, int source[], int intCount, int count) {
        int i;
        boolean allZero = true;
        for (i = 0; i < intCount; i++)
            allZero &= source[i] == 0;
        if (count == 0) {
            System.arraycopy(source, intCount, result, 0, resultLen);
            i = resultLen;
        } else {
            int leftShiftCount = 32 - count;

            allZero &= ( source[i] << leftShiftCount ) == 0;
            for (i = 0; i < resultLen - 1; i++) {
                result[i] = ( source[i + intCount] >>> count )
                        | ( source[i + intCount + 1] << leftShiftCount );
            }
            result[i] = ( source[i + intCount] >>> count );
            i++;
        }

        return allZero;
    }

}