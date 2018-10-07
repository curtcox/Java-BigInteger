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

import harmony.internal.nls.Messages;

/**
 * Static library that provides all multiplication of {@link BigInteger} methods.
 */
class Multiplication {

    /** Just to denote that this class can't be instantiated. */
    private Multiplication() {}

    /**
     * Break point in digits (number of {@code int} elements)
     * between Karatsuba and Pencil and Paper multiply.
     */
    static final int whenUseKaratsuba = 63; // an heuristic value

    /**
     * An array with powers of ten that fit in the type {@code int}.
     * ({@code 10^0,10^1,...,10^9})
     */
    static final int tenPows[] = {
            1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000
    };

    /**
     * An array with powers of five that fit in the type {@code int}.
     * ({@code 5^0,5^1,...,5^13})
     */
    static final int fivePows[] = {
            1, 5, 25, 125, 625, 3125, 15625, 78125, 390625,
            1953125, 9765625, 48828125, 244140625, 1220703125
    };

    /**
     * An array with the first powers of ten in {@code BigInteger} version.
     * ({@code 10^0,10^1,...,10^31})
     */
    static final BigInteger[] bigTenPows = new BigInteger[32];

    /**
     * An array with the first powers of five in {@code BigInteger} version.
     * ({@code 5^0,5^1,...,5^31})
     */
    static final BigInteger bigFivePows[] = new BigInteger[32];



    static {
        int i;
        long fivePow = 1L;

        for (i = 0; i <= 18; i++) {
            bigFivePows[i] = BigInteger.valueOf(fivePow);
            bigTenPows[i] = BigInteger.valueOf(fivePow << i);
            fivePow *= 5;
        }
        for (; i < bigTenPows.length; i++) {
            bigFivePows[i] = bigFivePows[i - 1].multiply(bigFivePows[1]);
            bigTenPows[i] = bigTenPows[i - 1].multiply(BigInteger.TEN);
        }
    }

    /**
     * Performs a multiplication of two BigInteger and hides the algorithm used.
     * @see BigInteger#multiply(BigInteger)
     */
    static BigInteger multiply(BigInteger x, BigInteger y) {
        return karatsuba(x, y);
    }

    /**
     * Performs the multiplication with the Karatsuba's algorithm.
     * <b>Karatsuba's algorithm:</b>
     *<tt>
     *             u = u<sub>1</sub> * B + u<sub>0</sub><br>
     *             v = v<sub>1</sub> * B + v<sub>0</sub><br>
     *
     *
     *  u*v = (u<sub>1</sub> * v<sub>1</sub>) * B<sub>2</sub> + ((u<sub>1</sub> - u<sub>0</sub>) * (v<sub>0</sub> - v<sub>1</sub>) + u<sub>1</sub> * v<sub>1</sub> +
     *  u<sub>0</sub> * v<sub>0</sub> ) * B + u<sub>0</sub> * v<sub>0</sub><br>
     *</tt>
     * @param op1 first factor of the product
     * @param op2 second factor of the product
     * @return {@code op1 * op2}
     * @see #multiply(BigInteger, BigInteger)
     */
    static BigInteger karatsuba(BigInteger op1, BigInteger op2) {
        BigInteger temp;
        if (op2.numberLength > op1.numberLength) {
            temp = op1;
            op1 = op2;
            op2 = temp;
        }
        if (op2.numberLength < whenUseKaratsuba) {
            return multiplyPAP(op1, op2);
        }
        /*  Karatsuba:  u = u1*B + u0
         *              v = v1*B + v0
         *  u*v = (u1*v1)*B^2 + ((u1-u0)*(v0-v1) + u1*v1 + u0*v0)*B + u0*v0
         */
        // ndiv2 = (op1.numberLength / 2) * 32
        int ndiv2 = (op1.numberLength & 0xFFFFFFFE) << 4;
        BigInteger upperOp1 = op1.shiftRight(ndiv2);
        BigInteger upperOp2 = op2.shiftRight(ndiv2);
        BigInteger lowerOp1 = op1.subtract(upperOp1.shiftLeft(ndiv2));
        BigInteger lowerOp2 = op2.subtract(upperOp2.shiftLeft(ndiv2));

        BigInteger upper = karatsuba(upperOp1, upperOp2);
        BigInteger lower = karatsuba(lowerOp1, lowerOp2);
        BigInteger middle = karatsuba( upperOp1.subtract(lowerOp1),
                lowerOp2.subtract(upperOp2));
        middle = middle.add(upper).add(lower);
        middle = middle.shiftLeft(ndiv2);
        upper = upper.shiftLeft(ndiv2 << 1);

        return upper.add(middle).add(lower);
    }

    /**
     * Multiplies two BigIntegers.
     * Implements traditional scholar algorithm described by Knuth.
     *
     * <br><tt>
     *         <table border="0">
     * <tbody>
     *
     *
     * <tr>
     * <td align="center">A=</td>
     * <td>a<sub>3</sub></td>
     * <td>a<sub>2</sub></td>
     * <td>a<sub>1</sub></td>
     * <td>a<sub>0</sub></td>
     * <td></td>
     * <td></td>
     * </tr>
     *
     *<tr>
     * <td align="center">B=</td>
     * <td></td>
     * <td>b<sub>2</sub></td>
     * <td>b<sub>1</sub></td>
     * <td>b<sub>1</sub></td>
     * <td></td>
     * <td></td>
     * </tr>
     *
     * <tr>
     * <td></td>
     * <td></td>
     * <td></td>
     * <td>b<sub>0</sub>*a<sub>3</sub></td>
     * <td>b<sub>0</sub>*a<sub>2</sub></td>
     * <td>b<sub>0</sub>*a<sub>1</sub></td>
     * <td>b<sub>0</sub>*a<sub>0</sub></td>
     * </tr>
     *
     * <tr>
     * <td></td>
     * <td></td>
     * <td>b<sub>1</sub>*a<sub>3</sub></td>
     * <td>b<sub>1</sub>*a<sub>2</sub></td>
     * <td>b<sub>1</sub>*a1</td>
     * <td>b<sub>1</sub>*a0</td>
     * </tr>
     *
     * <tr>
     * <td>+</td>
     * <td>b<sub>2</sub>*a<sub>3</sub></td>
     * <td>b<sub>2</sub>*a<sub>2</sub></td>
     * <td>b<sub>2</sub>*a<sub>1</sub></td>
     * <td>b<sub>2</sub>*a<sub>0</sub></td>
     * </tr>
     *
     *<tr>
     * <td></td>
     *<td>______</td>
     * <td>______</td>
     * <td>______</td>
     * <td>______</td>
     * <td>______</td>
     * <td>______</td>
     *</tr>
     *
     * <tr>
     *
     * <td align="center">A*B=R=</td>
     * <td align="center">r<sub>5</sub></td>
     * <td align="center">r<sub>4</sub></td>
     * <td align="center">r<sub>3</sub></td>
     * <td align="center">r<sub>2</sub></td>
     * <td align="center">r<sub>1</sub></td>
     * <td align="center">r<sub>0</sub></td>
     * <td></td>
     * </tr>
     *
     * </tbody>
     * </table>
     *
     *</tt>
     *
     * @return a {@code BigInteger} of value {@code  op1 * op2}
     */
    static BigInteger multiplyPAP(BigInteger a, BigInteger b) {
        // PRE: a >= b
        int aLen = a.numberLength;
        int bLen = b.numberLength;
        int resLength = aLen + bLen;
        int resSign = (a.sign != b.sign) ? -1 : 1;
        // A special case when both numbers don't exceed int
        if (resLength == 2) {
            long val = unsignedMultAddAdd(a.digits[0], b.digits[0], 0, 0);
            int valueLo = (int)val;
            int valueHi = (int)(val >>> 32);
            return ((valueHi == 0)
                    ? new BigInteger(resSign, valueLo)
                    : new BigInteger(resSign, 2, new int[]{valueLo, valueHi}));
        }
        int[] aDigits = a.digits;
        int[] bDigits = b.digits;
        int resDigits[] = new int[resLength];
        // Common case
        multArraysPAP(aDigits, aLen, bDigits, bLen, resDigits);
        BigInteger result = new BigInteger(resSign, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }

    static void multArraysPAP(int[] aDigits, int aLen, int[] bDigits, int bLen, int[] resDigits) {
        if(aLen == 0 || bLen == 0) return;

        if(aLen == 1) {
            resDigits[bLen] = multiplyByInt(resDigits, bDigits, bLen, aDigits[0]);
        } else if(bLen == 1) {
            resDigits[aLen] = multiplyByInt(resDigits, aDigits, aLen, bDigits[0]);
        } else {
            multPAP(aDigits, bDigits, resDigits, aLen, bLen);
        }
    }

    static void multPAP(int a[], int b[], int t[], int aLen, int bLen) {
        if(a == b && aLen == bLen) {
            square(a, aLen, t);
            return;
        }

        for(int i = 0; i < aLen; i++){
            long carry = 0;
            int aI = a[i];
            for (int j = 0; j < bLen; j++){
                carry = unsignedMultAddAdd(aI, b[j], t[i+j], (int)carry);
                t[i+j] = (int) carry;
                carry >>>= 32;
            }
            t[i+bLen] = (int) carry;
        }
    }

    /**
     * Multiplies an array of integers by an integer value
     * and saves the result in {@code res}.
     * @param a the array of integers
     * @param aSize the number of elements of intArray to be multiplied
     * @param factor the multiplier
     * @return the top digit of production
     */
    private static int multiplyByInt(int res[], int a[], final int aSize, final int factor) {
        long carry = 0;
        for (int i = 0; i < aSize; i++) {
            carry = unsignedMultAddAdd(a[i], factor, (int)carry, 0);
            res[i] = (int)carry;
            carry >>>= 32;
        }
        return (int)carry;
    }

    
    /**
     *  Performs a<sup>2</sup>
     *  @param a The number to square.
     *  @param aLen The length of the number to square.
     */
    static int[] square(int[] a, int aLen, int[] res) {
        long carry;

        for(int i = 0; i < aLen; i++){
            carry = 0;
            for (int j = i+1; j < aLen; j++){
                carry = unsignedMultAddAdd(a[i], a[j], res[i+j], (int)carry);
                res[i+j] = (int) carry;
                carry >>>= 32;
            }
            res[i+aLen] = (int) carry;
        }

        BitLevel.shiftLeftOneBit(res, res, aLen << 1);

        carry = 0;
        for(int i = 0, index = 0; i < aLen; i++, index++){
            carry = unsignedMultAddAdd(a[i], a[i], res[index],(int)carry);
            res[index] = (int) carry;
            carry >>>= 32;
            index++;
            carry += res[index] & 0xFFFFFFFFL;
            res[index] = (int)carry;
            carry >>>= 32;
        }
        return res;
    }

    /**
     * Computes the value unsigned ((uint)a*(uint)b + (uint)c + (uint)d). This
     * method could improve the readability and performance of the code.
     *
     * @param a
     *            parameter 1
     * @param b
     *            parameter 2
     * @param c
     *            parameter 3
     * @param d
     *            parameter 4
     * @return value of expression
     */
    static long unsignedMultAddAdd(int a, int b, int c, int d) {
        return (a & 0xFFFFFFFFL) * (b & 0xFFFFFFFFL) + (c & 0xFFFFFFFFL) + (d & 0xFFFFFFFFL);
    }

}