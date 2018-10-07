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
 * Static library that provides all operations related with division and modular
 * arithmetic to {@link BigInteger}. Some methods are provided in both mutable
 * and immutable way. There are several variants provided listed below:
 *
 * <ul type="circle">
 * <li> <b>Division</b>
 * <ul type="circle">
 * <li>{@link BigInteger} division and remainder by {@link BigInteger}.</li>
 * <li>{@link BigInteger} division and remainder by {@code int}.</li>
 * <li><i>gcd</i> between {@link BigInteger} numbers.</li>
 * </ul>
 * </li>
 * <li> <b>Modular arithmetic </b>
 * <ul type="circle">
 * <li>Modular exponentiation between {@link BigInteger} numbers.</li>
 * <li>Modular inverse of a {@link BigInteger} numbers.</li>
 * </ul>
 * </li>
 *</ul>
 */
class Division {

    /**
     * Divides the array 'a' by the array 'b' and gets the quotient and the
     * remainder. Implements the Knuth's division algorithm. See D. Knuth, The
     * Art of Computer Programming, vol. 2. Steps D1-D8 correspond the steps in
     * the algorithm description.
     *
     * @param quot the quotient
     * @param quotLength the quotient's length
     * @param a the dividend
     * @param aLength the dividend's length
     * @param b the divisor
     * @param bLength the divisor's length
     * @return the remainder
     */
    static int[] divide(int quot[], int quotLength, int a[], int aLength,
                        int b[], int bLength) {

        int normA[] = new int[aLength + 1]; // the normalized dividend
        // an extra byte is needed for correct shift
        int normB[] = new int[bLength + 1]; // the normalized divisor;
        int normBLength = bLength;
        /*
         * Step D1: normalize a and b and put the results to a1 and b1 the
         * normalized divisor's first digit must be >= 2^31
         */
        int divisorShift = Integer.numberOfLeadingZeros(b[bLength - 1]);
        if (divisorShift != 0) {
            BitLevel.shiftLeft(normB, b, 0, divisorShift);
            BitLevel.shiftLeft(normA, a, 0, divisorShift);
        } else {
            System.arraycopy(a, 0, normA, 0, aLength);
            System.arraycopy(b, 0, normB, 0, bLength);
        }
        int firstDivisorDigit = normB[normBLength - 1];
        // Step D2: set the quotient index
        int i = quotLength - 1;
        int j = aLength;

        while (i >= 0) {
            // Step D3: calculate a guess digit guessDigit
            int guessDigit = 0;
            if (normA[j] == firstDivisorDigit) {
                // set guessDigit to the largest unsigned int value
                guessDigit = -1;
            } else {
                long product = (((normA[j] & 0xffffffffL) << 32) + (normA[j - 1] & 0xffffffffL));
                long res = Division.divideLongByInt(product, firstDivisorDigit);
                guessDigit = (int) res; // the quotient of divideLongByInt
                int rem = (int) (res >> 32); // the remainder of
                // divideLongByInt
                // decrease guessDigit by 1 while leftHand > rightHand
                if (guessDigit != 0) {
                    long leftHand = 0;
                    long rightHand = 0;
                    boolean rOverflowed = false;
                    guessDigit++; // to have the proper value in the loop
                    // below
                    do {
                        guessDigit--;
                        if (rOverflowed) {
                            break;
                        }
                        // leftHand always fits in an unsigned long
                        leftHand = (guessDigit & 0xffffffffL)
                                * (normB[normBLength - 2] & 0xffffffffL);
                        /*
                         * rightHand can overflow; in this case the loop
                         * condition will be true in the next step of the loop
                         */
                        rightHand = ((long) rem << 32)
                                + (normA[j - 2] & 0xffffffffL);
                        long longR = (rem & 0xffffffffL)
                                + (firstDivisorDigit & 0xffffffffL);
                        /*
                         * checks that longR does not fit in an unsigned int;
                         * this ensures that rightHand will overflow unsigned
                         * long in the next step
                         */
                        if (Integer.numberOfLeadingZeros((int) (longR >>> 32)) < 32) {
                            rOverflowed = true;
                        } else {
                            rem = (int) longR;
                        }
                    } while (((leftHand ^ 0x8000000000000000L) > (rightHand ^ 0x8000000000000000L)));
                }
            }
            // Step D4: multiply normB by guessDigit and subtract the production
            // from normA.
            if (guessDigit != 0) {
                int borrow = Division.multiplyAndSubtract(normA, j
                                - normBLength, normB, normBLength,
                        guessDigit);
                // Step D5: check the borrow
                if (borrow != 0) {
                    // Step D6: compensating addition
                    guessDigit--;
                    long carry = 0;
                    for (int k = 0; k < normBLength; k++) {
                        carry += (normA[j - normBLength + k] & 0xffffffffL)
                                + (normB[k] & 0xffffffffL);
                        normA[j - normBLength + k] = (int) carry;
                        carry >>>= 32;
                    }
                }
            }
            if (quot != null) {
                quot[i] = guessDigit;
            }
            // Step D7
            j--;
            i--;
        }
        /*
         * Step D8: we got the remainder in normA. Denormalize it id needed
         */
        if (divisorShift != 0) {
            // reuse normB
            BitLevel.shiftRight(normB, normBLength, normA, 0, divisorShift);
            return normB;
        }
        System.arraycopy(normA, 0, normB, 0, bLength);
        return normA;
    }

    /**
     * Divides an array by an integer value. Implements the Knuth's division
     * algorithm. See D. Knuth, The Art of Computer Programming, vol. 2.
     *
     * @param src the dividend
     * @param srcLength the length of the dividend
     * @param divisor the divisor
     * @return remainder
     */
    static int remainderArrayByInt(int src[], final int srcLength,
                                   final int divisor) {

        long result = 0;

        for (int i = srcLength - 1; i >= 0; i--) {
            long temp = (result << 32) + (src[i] & 0xffffffffL);
            long res = divideLongByInt(temp, divisor);
            result = (int) (res >> 32);
        }
        return (int) result;
    }

    /**
     * Divides an unsigned long a by an unsigned int b. It is supposed that the
     * most significant bit of b is set to 1, i.e. b < 0
     *
     * @param a the dividend
     * @param b the divisor
     * @return the long value containing the unsigned integer remainder in the
     *         left half and the unsigned integer quotient in the right half
     */
    static long divideLongByInt(long a, int b) {
        long quot;
        long rem;
        long bLong = b & 0xffffffffL;

        if (a >= 0) {
            quot = (a / bLong);
            rem = (a % bLong);
        } else {
            /*
             * Make the dividend positive shifting it right by 1 bit then get
             * the quotient an remainder and correct them properly
             */
            long aPos = a >>> 1;
            long bPos = b >>> 1;
            quot = aPos / bPos;
            rem = aPos % bPos;
            // double the remainder and add 1 if a is odd
            rem = (rem << 1) + (a & 1);
            if ((b & 1) != 0) { // the divisor is odd
                if (quot <= rem) {
                    rem -= quot;
                } else {
                    if (quot - rem <= bLong) {
                        rem += bLong - quot;
                        quot -= 1;
                    } else {
                        rem += (bLong << 1) - quot;
                        quot -= 2;
                    }
                }
            }
        }
        return (rem << 32) | (quot & 0xffffffffL);
    }

    /**
     * Multiplies an array by int and subtracts it from a subarray of another
     * array.
     *
     * @param a the array to subtract from
     * @param start the start element of the subarray of a
     * @param b the array to be multiplied and subtracted
     * @param bLen the length of b
     * @param c the multiplier of b
     * @return the carry element of subtraction
     */
    static int multiplyAndSubtract(int a[], int start, int b[], int bLen, int c) {
        long carry0 = 0;
        long carry1 = 0;

        for (int i = 0; i < bLen; i++) {
            carry0 = Multiplication.unsignedMultAddAdd(b[i], c, (int)carry0, 0);
            carry1 = (a[start+i] & 0xffffffffL) - (carry0 & 0xffffffffL) + carry1;
            a[start+i] = (int)carry1;
            carry1 >>=  32; // -1 or 0
            carry0 >>>= 32;
        }

        carry1 = (a[start + bLen] & 0xffffffffL) - carry0 + carry1;
        a[start + bLen] = (int)carry1;
        return (int)(carry1 >> 32); // -1 or 0
    }

    /**
     * Calculate the first digit of the inverse
     */
    private static int calcN(BigInteger a) {
        long m0 = a.digits[0] & 0xFFFFFFFFL;
        long n2 = 1L; // this is a'[0]
        long powerOfTwo = 2L;
        do {
            if (((m0 * n2) & powerOfTwo) != 0) {
                n2 |= powerOfTwo;
            }
            powerOfTwo <<= 1;
        } while (powerOfTwo < 0x100000000L);
        n2 = -n2;
        return (int)(n2 & 0xFFFFFFFFL);
    }

    static BigInteger squareAndMultiply(BigInteger x2, BigInteger a2, BigInteger exponent,BigInteger modulus, int n2  ){
        BigInteger res = x2;
        for (int i = exponent.bitLength() - 1; i >= 0; i--) {
            res = monPro(res,res,modulus, n2);
            if (BitLevel.testBit(exponent, i)) {
                res = monPro(res, a2, modulus, n2);
            }
        }
        return res;
    }

    /*Implements the Montgomery modular exponentiation based in <i>The sliding windows algorithm and the Mongomery
     *Reduction</i>.
     *@ar.org.fitc.ref "A. Menezes,P. van Oorschot, S. Vanstone - Handbook of Applied Cryptography";
     *@see #oddModPow(BigInteger, BigInteger,
     *                           BigInteger)
     */
    static BigInteger slidingWindow(BigInteger x2, BigInteger a2, BigInteger exponent,BigInteger modulus, int n2){
        // fill odd low pows of a2
        BigInteger pows[] = new BigInteger[8];
        BigInteger res = x2;
        int lowexp;
        BigInteger x3;
        int acc3;
        pows[0] = a2;

        x3 = monPro(a2,a2,modulus,n2);
        for (int i = 1; i <= 7; i++){
            pows[i] = monPro(pows[i-1],x3,modulus,n2) ;
        }

        for (int i = exponent.bitLength()-1; i>=0;i--){
            if( BitLevel.testBit(exponent,i) ) {
                lowexp = 1;
                acc3 = i;

                for(int j = Math.max(i-3,0);j <= i-1 ;j++) {
                    if (BitLevel.testBit(exponent,j)) {
                        if (j<acc3) {
                            acc3 = j;
                            lowexp = (lowexp << (i-j))^1;
                        } else {
                            lowexp = lowexp^(1<<(j-acc3));
                        }
                    }
                }

                for(int j = acc3; j <= i; j++) {
                    res = monPro(res,res,modulus,n2);
                }
                res = monPro(pows[(lowexp-1)>>1], res, modulus,n2);
                i = acc3 ;
            }else{
                res = monPro(res, res, modulus, n2) ;
            }
        }
        return res;
    }

    /**
     * Performs modular exponentiation using the Montgomery Reduction. It
     * requires that all parameters be positive and the modulus be odd. >
     *
     * @see BigInteger#modPow(BigInteger, BigInteger)
     * @see #monPro(BigInteger, BigInteger, BigInteger, int)
     * @see #slidingWindow(BigInteger, BigInteger, BigInteger, BigInteger,
     *                      int)
     * @see #squareAndMultiply(BigInteger, BigInteger, BigInteger, BigInteger,
     *                      int)
     */
    static BigInteger oddModPow(BigInteger base, BigInteger exponent,
                                BigInteger modulus) {
        // PRE: (base > 0), (exponent > 0), (modulus > 0) and (odd modulus)
        int k = (modulus.numberLength << 5); // r = 2^k
        // n-residue of base [base * r (mod modulus)]
        BigInteger a2 = base.shiftLeft(k).mod(modulus);
        // n-residue of base [1 * r (mod modulus)]
        BigInteger x2 = BigInteger.getPowerOfTwo(k).mod(modulus);
        BigInteger res;
        // Compute (modulus[0]^(-1)) (mod 2^32) for odd modulus

        int n2 = calcN(modulus);
        if( modulus.numberLength == 1 ){
            res = squareAndMultiply(x2,a2, exponent, modulus,n2);
        } else {
            res = slidingWindow(x2, a2, exponent, modulus, n2);
        }

        return monPro(res, BigInteger.ONE, modulus, n2);
    }

    private static void monReduction(int[] res, BigInteger modulus, int n2) {

        /* res + m*modulus_digits */
        int[] modulus_digits = modulus.digits;
        int modulusLen = modulus.numberLength;
        long outerCarry = 0;

        for (int i = 0; i < modulusLen; i++){
            long innnerCarry = 0;
            int m = (int) Multiplication.unsignedMultAddAdd(res[i],n2,0,0);
            for(int j = 0; j < modulusLen; j++){
                innnerCarry =  Multiplication.unsignedMultAddAdd(m, modulus_digits[j], res[i+j], (int)innnerCarry);
                res[i+j] = (int) innnerCarry;
                innnerCarry >>>= 32;
            }

            outerCarry += (res[i+modulusLen] & 0xFFFFFFFFL) + innnerCarry;
            res[i+modulusLen] = (int) outerCarry;
            outerCarry >>>= 32;
        }

        res[modulusLen << 1] = (int) outerCarry;

        /* res / r  */
        for(int j = 0; j < modulusLen+1; j++){
            res[j] = res[j+modulusLen];
        }
    }

    /**
     * Implements the Montgomery Product of two integers represented by
     * {@code int} arrays. The arrays are supposed in <i>little
     * endian</i> notation.
     *
     * @param a The first factor of the product.
     * @param b The second factor of the product.
     * @param modulus The modulus of the operations. Z<sub>modulus</sub>.
     * @param n2 The digit modulus'[0].
     * @ar.org.fitc.ref "C. K. Koc - Analyzing and Comparing Montgomery
     *                  Multiplication Algorithms"
     */
    static BigInteger monPro(BigInteger a, BigInteger b, BigInteger modulus, int n2) {
        int modulusLen = modulus.numberLength;
        int res[] = new int[(modulusLen << 1) + 1];
        Multiplication.multArraysPAP(a.digits, Math.min(modulusLen, a.numberLength),
                b.digits, Math.min(modulusLen, b.numberLength), res);
        monReduction(res,modulus,n2);
        return finalSubtraction(res, modulus);

    }

    /**
     * Performs the final reduction of the Montgomery algorithm.
     */
    static BigInteger finalSubtraction(int res[], BigInteger modulus){

        // skipping leading zeros
        int modulusLen = modulus.numberLength;
        boolean doSub = res[modulusLen]!=0;
        if(!doSub) {
            int modulusDigits[] = modulus.digits;
            doSub = true;
            for(int i = modulusLen - 1; i >= 0; i--) {
                if(res[i] != modulusDigits[i]) {
                    doSub = (res[i] != 0) && ((res[i] & 0xFFFFFFFFL) > (modulusDigits[i] & 0xFFFFFFFFL));
                    break;
                }
            }
        }

        BigInteger result = new BigInteger(1, modulusLen+1, res);

        // if (res >= modulusDigits) compute (res - modulusDigits)
        if (doSub) {
            Elementary.inplaceSubtract(result, modulus);
        }

        result.cutOffLeadingZeroes();
        return result;
    }

}