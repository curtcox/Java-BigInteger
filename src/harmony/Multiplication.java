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
 * Static library that provides all multiplication of {@link BigInteger} methods.
 */
class Multiplication {

    /**
     * Break point in digits (number of {@code int} elements)
     * between Karatsuba and Pencil and Paper multiply.
     */
    private static final int whenUseKaratsuba = 63; // an heuristic value

    /**
     * Performs a multiplication of two BigInteger and hides the algorithm used.
     //* @see BigInteger#multiply(BigInteger)
     */
    static BigInteger multiply(BigInteger x, BigInteger y) {
        return karatsuba(x, y);
    }

    private static BigInteger karatsuba(BigInteger op1, BigInteger op2) {
        if (op2.numberLength() > op1.numberLength()) {
            throw new IllegalArgumentException();
        }
        if (op2.numberLength() < whenUseKaratsuba) {
            return multiplyPAP(op1, op2);
        }
        int ndiv2 = (op1.numberLength() & 0xFFFFFFFE) << 4;
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

    private static BigInteger multiplyPAP(BigInteger a, BigInteger b) {
        // PRE: a >= b
        int aLen = a.numberLength();
        int bLen = b.numberLength();
        int resLength = aLen + bLen;
        int resSign = (a.sign != b.sign) ? -1 : 1;
        // A special case when both numbers don't exceed int
        if (resLength == 2) {
            long val = unsignedMultAddAdd(a.getDigit(0), b.getDigit(0), 0, 0);
            int valueLo = (int)val;
            int valueHi = (int)(val >>> 32);
            return ((valueHi == 0)
                    ? new BigInteger(resSign, valueLo)
                    : new BigInteger(resSign, 2, new int[]{valueLo, valueHi}));
        }
        int[] aDigits = a.digits();
        int[] bDigits = b.digits();
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

    private static void multPAP(int a[], int b[], int t[], int aLen, int bLen) {
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

    private static int multiplyByInt(int res[], int a[], final int aSize, final int factor) {
        long carry = 0;
        for (int i = 0; i < aSize; i++) {
            carry = unsignedMultAddAdd(a[i], factor, (int)carry, 0);
            res[i] = (int)carry;
            carry >>>= 32;
        }
        return (int)carry;
    }

    private static int[] square(int[] a, int aLen, int[] res) {
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

    static long unsignedMultAddAdd(int a, int b, int c, int d) {
        return (a & 0xFFFFFFFFL) * (b & 0xFFFFFFFFL) + (c & 0xFFFFFFFFL) + (d & 0xFFFFFFFFL);
    }

}