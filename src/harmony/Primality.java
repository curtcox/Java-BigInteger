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

import java.util.Arrays;
import java.util.Random;

/**
 * Provides primality probabilistic methods.
 */
class Primality {

    /** All prime numbers with bit length lesser than 10 bits. */
    private static final int primes[] = { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29,
            31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101,
            103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167,
            173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239,
            241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313,
            317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397,
            401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461, 463, 467,
            479, 487, 491, 499, 503, 509, 521, 523, 541, 547, 557, 563, 569,
            571, 577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643,
            647, 653, 659, 661, 673, 677, 683, 691, 701, 709, 719, 727, 733,
            739, 743, 751, 757, 761, 769, 773, 787, 797, 809, 811, 821, 823,
            827, 829, 839, 853, 857, 859, 863, 877, 881, 883, 887, 907, 911,
            919, 929, 937, 941, 947, 953, 967, 971, 977, 983, 991, 997, 1009,
            1013, 1019, 1021 };

    /** All {@code BigInteger} prime numbers with bit length lesser than 8 bits. */
    private static final IBigInteger BIprimes[] = new IBigInteger[primes.length];

    /**
     * It encodes how many iterations of Miller-Rabin test are need to get an
     * error bound not greater than {@code 2<sup>(-100)</sup>}. For example:
     * for a {@code 1000}-bit number we need {@code 4} iterations, since
     * {@code BITS[3] < 1000 <= BITS[4]}.
     */
    private static final int[] BITS = { 0, 0, 1854, 1233, 927, 747, 627, 543,
            480, 431, 393, 361, 335, 314, 295, 279, 265, 253, 242, 232, 223,
            216, 181, 169, 158, 150, 145, 140, 136, 132, 127, 123, 119, 114,
            110, 105, 101, 96, 92, 87, 83, 78, 73, 69, 64, 59, 54, 49, 44, 38,
            32, 26, 1 };

    /**
     * It encodes how many i-bit primes there are in the table for
     * {@code i=2,...,10}. For example {@code offsetPrimes[6]} says that from
     * index {@code 11} exists {@code 7} consecutive {@code 6}-bit prime
     * numbers in the array.
     */
    private static final int[][] offsetPrimes = { null, null, { 0, 2 },
            { 2, 2 }, { 4, 2 }, { 6, 5 }, { 11, 7 }, { 18, 13 }, { 31, 23 },
            { 54, 43 }, { 97, 75 } };

    static {// To initialize the dual table of BigInteger primes
        for (int i = 0; i < primes.length; i++) {
            BIprimes[i] = BigInteger.valueOf(primes[i]);
        }
    }

    /**
     * A random number is generated until a probable prime number is found.
     *
     * @see BigInteger#BigInteger(int,int,Random)
     * @see BigInteger#probablePrime(int,Random)
     //* @see #isProbablePrime(BigInteger, int)
     */
    static IBigInteger consBigInteger(int bitLength, int certainty, Random rnd) {
        // PRE: bitLength >= 2;
        // For small numbers get a random prime from the prime table
        if (bitLength <= 10) {
            int rp[] = offsetPrimes[bitLength];
            return BIprimes[rp[0] + rnd.nextInt(rp[1])];
        }
        IBigInteger candidate = new BigInteger(bitLength,rnd);
        while (!isProbablePrime(candidate, certainty)) {
            candidate = new BigInteger(bitLength,rnd);
        }
        return candidate;
     }

    static boolean isTwo(IBigInteger n) {
        return (n.numberLength() == 1) && (n.getDigit(0) == 2);
    }

    static boolean isEven(IBigInteger n) {
        return !n.testBit(0);
    }
    /**
     * @see BigInteger#isProbablePrime(int)
     // @see #millerRabin(BigInteger, int)
     * @ar.org.fitc.ref Optimizations: "A. Menezes - Handbook of applied
     *                  Cryptography, Chapter 4".
     */
    static boolean isProbablePrime(IBigInteger n, int certainty) {
        // PRE: n >= 0;
        if (certainty <= 0) {
            return true;
        }
        if (isTwo(n)) {
            return true;
        }
        // To discard all even numbers
        if (isEven(n)) {
            return false;
        }
        // To check if 'n' exists in the table (it fit in 10 bits)
        if ((n.numberLength() == 1) && ((n.getDigit(0) & 0XFFFFFC00) == 0)) {
            return (Arrays.binarySearch(primes, n.getDigit(0)) >= 0);
        }
        // To check if 'n' is divisible by some prime of the table
        for (int i = 1; i < primes.length; i++) {
            if (n.mod(BIprimes[i]).equals(BigInteger.ZERO)) {
                return false;
            }
        }

        return millerRabin(n, numberOfRequiredIterations(n,certainty));
    }

    static int numberOfRequiredIterations(IBigInteger n, int certainty) {
        // To set the number of iterations necessary for Miller-Rabin test
        int i;
        int bitLength = n.bitLength();

        for (i = 2; bitLength < BITS[i]; i++) {
            ;
        }
        return Math.min(i, 1 + ((certainty - 1) >> 1));
    }

    /**
     * The Miller-Rabin primality test.
     *
     * @param n the input number to be tested.
     * @param t the number of trials.
     * @return {@code false} if the number is definitely compose, otherwise
     *         {@code true} with probability {@code 1 - 4<sup>(-t)</sup>}.
     * @ar.org.fitc.ref "D. Knuth, The Art of Computer Programming Vo.2, Section
     *                  4.5.4., Algorithm P"
     */
    private static boolean millerRabin(IBigInteger n, int t) {
        // PRE: n >= 0, t >= 0
        final IBigInteger n_minus_1 = n.subtract(BigInteger.ONE); // n-1
        // (q,k) such that: n-1 = q * 2^k and q is odd
        final int k = n_minus_1.getLowestSetBit();
        final IBigInteger q = n_minus_1.shiftRight(k);

        IBigInteger x; // x := UNIFORM{2...n-1}
        IBigInteger y; // y := x^(q * 2^j) mod n
        for (int i = 0; i < t; i++) {
            // To generate a witness 'x', first it use the primes of table
            if (i < primes.length) {
                x = BIprimes[i];
            } else {
                throw new UnsupportedOperationException();
            }
            y = x.modPow(q, n);
            if (y.isOne() || y.equals(n_minus_1)) {
                continue;
            }
            for (int j = 1; j < k; j++) {
                if (y.equals(n_minus_1)) {
                    continue;
                }
                y = y.multiply(y).mod(n);
                if (y.isOne()) {
                    return false;
                }
            }
            if (!y.equals(n_minus_1)) {
                return false;
            }
        }
        return true;
    }

}