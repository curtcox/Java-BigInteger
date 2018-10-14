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

import java.util.ArrayList;
import java.util.List;

/**
 * Provides primality probabilistic methods.
 */
final class Primality {

    /** All {@code BigInteger} prime numbers with bit length lesser than 8 bits. */
    private final List<IBigInteger> BIprimes;

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

    static final int certainty = IBigInteger.certainty;

    private Primality(List<IBigInteger> BIprimes) {
        this.BIprimes = BIprimes;
    }

    static Primality from(IBigInteger.Factory factory) {
        List<IBigInteger> big = new ArrayList<>();
        for (int i = 0; i < primes.length; i++) {
            big.add(factory.valueOf(primes[i]));
        }
        return new Primality(big);
    }

    /**
     //* @see BigInteger#isProbablePrime(int)
     // @see #millerRabin(BigInteger, int)
     * @ar.org.fitc.ref Optimizations: "A. Menezes - Handbook of applied
     *                  Cryptography, Chapter 4".
     */
    boolean isProbablePrime(IBigInteger n) {
        // PRE: n >= 0;
        if (certainty <= 0) {
            return true;
        }
        if (n.isTwo()) {
            return true;
        }
        // To discard all even numbers
        if (n.isEven()) {
            return false;
        }
        if (isSmallEnoughToBeInTable(n)) {
            return isInTable(n);
        }
        // To check if 'n' is divisible by some prime of the table
        for (int i = 1; i < primes.length; i++) {
            if (n.mod(BIprimes.get(i)).isZero()) {
                return false;
            }
        }

        return millerRabin(n, numberOfRequiredIterations(n.bitLength()));
    }

    boolean isInTable(IBigInteger n) {
        return BIprimes.contains(n);
    }

    boolean isSmallEnoughToBeInTable(IBigInteger n) {
        return n.lessThanOrEqualTo(largestTablePrime());
    }

    private IBigInteger largestTablePrime() {
        return BIprimes.get(BIprimes.size()-1);
    }

    static int numberOfRequiredIterations(int bitLength) {
        // To set the number of iterations necessary for Miller-Rabin test
        int i;

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
    private boolean millerRabin(IBigInteger n, int t) {
        // PRE: n >= 0, t >= 0
        final IBigInteger n_minus_1 = n.subtract_1(); // n-1
        // (q,k) such that: n-1 = q * 2^k and q is odd
        final int k = n_minus_1.getLowestSetBit();
        final IBigInteger q = n_minus_1.shiftRight(k);

        IBigInteger x; // x := UNIFORM{2...n-1}
        IBigInteger y; // y := x^(q * 2^j) mod n
        for (int i = 0; i < t; i++) {
            // To generate a witness 'x', first it use the primes of table
            if (i < primes.length) {
                x = BIprimes.get(i);
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