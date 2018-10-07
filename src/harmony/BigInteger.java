// From https://github.com/apache/harmony/blob/trunk/classlib/modules/math/src/main/java/java/math/BigInteger.java
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

import java.util.Random;

import harmony.internal.nls.Messages;

/**
 * This class represents immutable integer numbers of arbitrary length. Large
 * numbers are typically used in security applications and therefore BigIntegers
 * offer dedicated functionality like the generation of large prime numbers or
 * the computation of modular inverse.
 * <p>
 * Since the class was modeled to offer all the functionality as the {@link Integer}
 * class does, it provides even methods that operate bitwise on a two's
 * complement representation of large integers. Note however that the
 * implementations favors an internal representation where magnitude and sign
 * are treated separately. Hence such operations are inefficient and should be
 * discouraged. In simple words: Do NOT implement any bit fields based on
 * BigInteger.
 */
public class BigInteger {

    /** This is the serialVersionUID used by the sun implementation. */
    private static final long serialVersionUID = -8287574255936472291L;

    /* Fields used for the internal representation. */

    /**
     * The magnitude of this big integer. This array holds unsigned little
     * endian digits. For example:
     *   {@code 13} is represented as [ 13 ]
     *   {@code -13} is represented as [ 13 ]
     *   {@code 2^32 + 13} is represented as [ 13, 1 ]
     *   {@code 2^64 + 13} is represented as [ 13, 0, 1 ]
     *   {@code 2^31} is represented as [ Integer.MIN_VALUE ]
     * The magnitude array may be longer than strictly necessary, which results
     * in additional trailing zeros.
     */
    transient int digits[];

    /** The length of this in measured in ints. Can be less than digits.length(). */
    transient int numberLength;

    /** The sign of this. */
    transient int sign;

    /**
     * The {@code BigInteger} constant 0.
     */
    public static final BigInteger ZERO = new BigInteger(0, 0);

    /**
     * The {@code BigInteger} constant 1.
     */
    public static final BigInteger ONE = new BigInteger(1, 1);

    /**
     * The {@code BigInteger} constant 10.
     */
    public static final BigInteger TEN = new BigInteger(1, 10);

    /** The {@code BigInteger} constant -1. */
    static final BigInteger MINUS_ONE = new BigInteger(-1, 1);

    /** The {@code BigInteger} constant 0 used for comparison. */
    static final int EQUALS = 0;

    /** The {@code BigInteger} constant 1 used for comparison. */
    static final int GREATER = 1;

    /** The {@code BigInteger} constant -1 used for comparison. */
    static final int LESS = -1;

    /** All the {@code BigInteger} numbers in the range [0,10] are cached. */
    static final BigInteger[] SMALL_VALUES = { ZERO, ONE, new BigInteger(1, 2),
            new BigInteger(1, 3), new BigInteger(1, 4), new BigInteger(1, 5),
            new BigInteger(1, 6), new BigInteger(1, 7), new BigInteger(1, 8),
            new BigInteger(1, 9), TEN };

    static final BigInteger[] TWO_POWS;

    static {
        TWO_POWS = new BigInteger[32];
        for(int i = 0; i < TWO_POWS.length; i++) {
            TWO_POWS[i] = BigInteger.valueOf(1L<<i);
        }
    }

    private transient int firstNonzeroDigit = -2;

    /** sign field, used for serialization. */
    private int signum;

    /** absolute value field, used for serialization */
    private byte[] magnitude;

    /** Cache for the hash code. */
    private transient int hashCode = 0;

    /**
     * Constructs a random non-negative {@code BigInteger} instance in the range
     * [0, 2^(numBits)-1].
     *
     * @param numBits
     *            maximum length of the new {@code BigInteger} in bits.
     * @param rnd
     *            is an optional random generator to be used.
     * @throws IllegalArgumentException
     *             if {@code numBits} < 0.
     */
    public BigInteger(int numBits, Random rnd) {
        if (numBits < 0) {
            // math.1B=numBits must be non-negative
            throw new IllegalArgumentException(Messages.getString("math.1B")); //$NON-NLS-1$
        }
        if (numBits == 0) {
            sign = 0;
            numberLength = 1;
            digits = new int[] { 0 };
        } else {
            sign = 1;
            numberLength = (numBits + 31) >> 5;
            digits = new int[numberLength];
            for (int i = 0; i < numberLength; i++) {
                digits[i] = rnd.nextInt();
            }
            // Using only the necessary bits
            digits[numberLength - 1] >>>= (-numBits) & 31;
            cutOffLeadingZeroes();
        }
    }

    /**
     * Constructs a random {@code BigInteger} instance in the range [0,
     * 2^(bitLength)-1] which is probably prime. The probability that the
     * returned {@code BigInteger} is prime is beyond (1-1/2^certainty).
     *
     * @param bitLength
     *            length of the new {@code BigInteger} in bits.
     * @param certainty
     *            tolerated primality uncertainty.
     * @param rnd
     *            is an optional random generator to be used.
     * @throws ArithmeticException
     *             if {@code bitLength} < 2.
     */
    public BigInteger(int bitLength, int certainty, Random rnd) {
        if (bitLength < 2) {
            // math.1C=bitLength < 2
            throw new ArithmeticException(Messages.getString("math.1C")); //$NON-NLS-1$
        }
        BigInteger me = Primality.consBigInteger(bitLength, certainty, rnd);
        sign = me.sign;
        numberLength = me.numberLength;
        digits = me.digits;
    }

    /**
     * Constructs a new {@code BigInteger} instance from the string
     * representation. The string representation consists of an optional minus
     * sign followed by a non-empty sequence of decimal digits.
     *
     * @param val
     *            string representation of the new {@code BigInteger}.
     * @throws NullPointerException
     *             if {@code val == null}.
     * @throws NumberFormatException
     *             if {@code val} is not a valid representation of a {@code
     *             BigInteger}.
     */
    public BigInteger(String val) {
        this(val, 10);
    }

    /**
     * Constructs a new {@code BigInteger} instance from the string
     * representation. The string representation consists of an optional minus
     * sign followed by a non-empty sequence of digits in the specified radix.
     * For the conversion the method {@code Character.digit(char, radix)} is
     * used.
     *
     * @param val
     *            string representation of the new {@code BigInteger}.
     * @param radix
     *            the base to be used for the conversion.
     * @throws NullPointerException
     *             if {@code val == null}.
     * @throws NumberFormatException
     *             if {@code val} is not a valid representation of a {@code
     *             BigInteger} or if {@code radix < Character.MIN_RADIX} or
     *             {@code radix > Character.MAX_RADIX}.
     */
    public BigInteger(String val, int radix) {
        if (val == null) {
            throw new NullPointerException();
        }
        if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX)) {
            // math.11=Radix out of range
            throw new NumberFormatException(Messages.getString("math.11")); //$NON-NLS-1$
        }
        if (val.length() == 0) {
            // math.12=Zero length BigInteger
            throw new NumberFormatException(Messages.getString("math.12")); //$NON-NLS-1$
        }
        setFromString(this, val, radix);
    }

    /**
     * Constructs a new {@code BigInteger} instance with the given sign and the
     * given magnitude. The sign is given as an integer (-1 for negative, 0 for
     * zero, 1 for positive). The magnitude is specified as a byte array. The
     * most significant byte is the entry at index 0.
     *
     * @param signum
     *            sign of the new {@code BigInteger} (-1 for negative, 0 for
     *            zero, 1 for positive).
     * @param magnitude
     *            magnitude of the new {@code BigInteger} with the most
     *            significant byte first.
     * @throws NullPointerException
     *             if {@code magnitude == null}.
     * @throws NumberFormatException
     *             if the sign is not one of -1, 0, 1 or if the sign is zero and
     *             the magnitude contains non-zero entries.
     */
    public BigInteger(int signum, byte[] magnitude) {
        if (magnitude == null) {
            throw new NullPointerException();
        }
        if ((signum < -1) || (signum > 1)) {
            // math.13=Invalid signum value
            throw new NumberFormatException(Messages.getString("math.13")); //$NON-NLS-1$
        }
        if (signum == 0) {
            for (byte element : magnitude) {
                if (element != 0) {
                    // math.14=signum-magnitude mismatch
                    throw new NumberFormatException(Messages
                            .getString("math.14")); //$NON-NLS-1$
                }
            }
        }
        if (magnitude.length == 0) {
            sign = 0;
            numberLength = 1;
            digits = new int[] { 0 };
        } else {
            sign = signum;
            putBytesPositiveToIntegers(magnitude);
            cutOffLeadingZeroes();
        }
    }

    /**
     * Constructs a new {@code BigInteger} from the given two's complement
     * representation. The most significant byte is the entry at index 0. The
     * most significant bit of this entry determines the sign of the new {@code
     * BigInteger} instance. The given array must not be empty.
     *
     * @param val
     *            two's complement representation of the new {@code BigInteger}.
     * @throws NullPointerException
     *             if {@code val == null}.
     * @throws NumberFormatException
     *             if the length of {@code val} is zero.
     */
    public BigInteger(byte[] val) {
        if (val.length == 0) {
            // math.12=Zero length BigInteger
            throw new NumberFormatException(Messages.getString("math.12")); //$NON-NLS-1$
        }
        if (val[0] < 0) {
            sign = -1;
            putBytesNegativeToIntegers(val);
        } else {
            sign = 1;
            putBytesPositiveToIntegers(val);
        }
        cutOffLeadingZeroes();
    }

    /**
     * Constructs a number which array is of size 1.
     *
     * @param sign
     *            the sign of the number
     * @param value
     *            the only one digit of array
     */
    BigInteger(int sign, int value) {
        this.sign = sign;
        numberLength = 1;
        digits = new int[] { value };
    }

    /**
     * Constructs a number without to create new space. This construct should be
     * used only if the three fields of representation are known.
     *
     * @param sign
     *            the sign of the number
     * @param numberLength
     *            the length of the internal array
     * @param digits
     *            a reference of some array created before
     */
    BigInteger(int sign, int numberLength, int[] digits) {
        this.sign = sign;
        this.numberLength = numberLength;
        this.digits = digits;
    }

    /**
     * Creates a new {@code BigInteger} whose value is equal to the specified
     * {@code long}.
     *
     * @param sign
     *            the sign of the number
     * @param val
     *            the value of the new {@code BigInteger}.
     */
    BigInteger(int sign, long val) {
        // PRE: (val >= 0) && (sign >= -1) && (sign <= 1)
        this.sign = sign;
        if ((val & 0xFFFFFFFF00000000L) == 0) {
            // It fits in one 'int'
            numberLength = 1;
            digits = new int[] { (int) val };
        } else {
            numberLength = 2;
            digits = new int[] { (int) val, (int) (val >> 32) };
        }
    }

    /**
     * Creates a new {@code BigInteger} with the given sign and magnitude. This
     * constructor does not create a copy, so any changes to the reference will
     * affect the new number.
     *
     * @param signum
     *            The sign of the number represented by {@code digits}
     * @param digits
     *            The magnitude of the number
     */
    BigInteger(int signum, int digits[]) {
        if (digits.length == 0) {
            sign = 0;
            numberLength = 1;
            this.digits = new int[] { 0 };
        } else {
            sign = signum;
            numberLength = digits.length;
            this.digits = digits;
            cutOffLeadingZeroes();
        }
    }

    public static BigInteger valueOf(long val) {
        if (val < 0) {
            if (val != -1) {
                return new BigInteger(-1, -val);
            }
            return MINUS_ONE;
        } else if (val <= 10) {
            return SMALL_VALUES[(int) val];
        } else {// (val > 10)
            return new BigInteger(1, val);
        }
    }

    /**
     * Returns the two's complement representation of this BigInteger in a byte
     * array.
     *
     * @return two's complement representation of {@code this}.
     */
    public byte[] toByteArray() {
        if (this.sign == 0) {
            return new byte[] { 0 };
        }
        BigInteger temp = this;
        int bitLen = bitLength();
        int iThis = getFirstNonzeroDigit();
        int bytesLen = (bitLen >> 3) + 1;
        /*
         * Puts the little-endian int array representing the magnitude of this
         * BigInteger into the big-endian byte array.
         */
        byte[] bytes = new byte[bytesLen];
        int firstByteNumber = 0;
        int highBytes;
        int digitIndex = 0;
        int bytesInInteger = 4;
        int digit;
        int hB;

        if (bytesLen - (numberLength << 2) == 1) {
            bytes[0] = (byte) ((sign < 0) ? -1 : 0);
            highBytes = 4;
            firstByteNumber++;
        } else {
            hB = bytesLen & 3;
            highBytes = (hB == 0) ? 4 : hB;
        }

        digitIndex = iThis;
        bytesLen -= iThis << 2;

        if (sign < 0) {
            digit = -temp.digits[digitIndex];
            digitIndex++;
            if (digitIndex == numberLength) {
                bytesInInteger = highBytes;
            }
            for (int i = 0; i < bytesInInteger; i++, digit >>= 8) {
                bytes[--bytesLen] = (byte) digit;
            }
            while (bytesLen > firstByteNumber) {
                digit = ~temp.digits[digitIndex];
                digitIndex++;
                if (digitIndex == numberLength) {
                    bytesInInteger = highBytes;
                }
                for (int i = 0; i < bytesInInteger; i++, digit >>= 8) {
                    bytes[--bytesLen] = (byte) digit;
                }
            }
        } else {
            while (bytesLen > firstByteNumber) {
                digit = temp.digits[digitIndex];
                digitIndex++;
                if (digitIndex == numberLength) {
                    bytesInInteger = highBytes;
                }
                for (int i = 0; i < bytesInInteger; i++, digit >>= 8) {
                    bytes[--bytesLen] = (byte) digit;
                }
            }
        }
        return bytes;
    }

    /** @see BigInteger#BigInteger(String, int) */
    private static void setFromString(BigInteger bi, String val, int radix) {
    }

    /**
     * Returns a (new) {@code BigInteger} whose value is the absolute value of
     * {@code this}.
     *
     * @return {@code abs(this)}.
     */
    public BigInteger abs() {
        return ((sign < 0) ? new BigInteger(1, numberLength, digits) : this);
    }

    /**
     * Returns a new {@code BigInteger} whose value is the {@code -this}.
     *
     * @return {@code -this}.
     */
    public BigInteger negate() {
        return ((sign == 0) ? this
                : new BigInteger(-sign, numberLength, digits));
    }

    /**
     * Returns a new {@code BigInteger} whose value is {@code this + val}.
     *
     * @param val
     *            value to be added to {@code this}.
     * @return {@code this + val}.
     * @throws NullPointerException
     *             if {@code val == null}.
     */
    public BigInteger add(BigInteger val) {
        return Elementary.add(this, val);
    }

    /**
     * Returns a new {@code BigInteger} whose value is {@code this - val}.
     *
     * @param val
     *            value to be subtracted from {@code this}.
     * @return {@code this - val}.
     * @throws NullPointerException
     *             if {@code val == null}.
     */
    public BigInteger subtract(BigInteger val) {
        return Elementary.subtract(this, val);
    }

    /**
     * Returns the sign of this {@code BigInteger}.
     *
     * @return {@code -1} if {@code this < 0},
     *         {@code 0} if {@code this == 0},
     *         {@code 1} if {@code this > 0}.
     */
    public int signum() {
        return sign;
    }

    /**
     * Returns a new {@code BigInteger} whose value is {@code this >> n}. For
     * negative arguments, the result is also negative. The shift distance may
     * be negative which means that {@code this} is shifted left.
     * <p>
     * <b>Implementation Note:</b> Usage of this method on negative values is
     * not recommended as the current implementation is not efficient.
     *
     * @param n
     *            shift distance
     * @return {@code this >> n} if {@code n >= 0}; {@code this << (-n)}
     *         otherwise
     */
    public BigInteger shiftRight(int n) {
        if ((n == 0) || (sign == 0)) {
            return this;
        }
        return ((n > 0) ? BitLevel.shiftRight(this, n) : BitLevel.shiftLeft(
                this, -n));
    }

    /**
     * Returns a new {@code BigInteger} whose value is {@code this << n}. The
     * result is equivalent to {@code this * 2^n} if n >= 0. The shift distance
     * may be negative which means that {@code this} is shifted right. The
     * result then corresponds to {@code floor(this / 2^(-n))}.
     * <p>
     * <b>Implementation Note:</b> Usage of this method on negative values is
     * not recommended as the current implementation is not efficient.
     *
     * @param n
     *            shift distance.
     * @return {@code this << n} if {@code n >= 0}; {@code this >> (-n)}.
     *         otherwise
     */
    public BigInteger shiftLeft(int n) {
        if ((n == 0) || (sign == 0)) {
            return this;
        }
        return ((n > 0) ? BitLevel.shiftLeft(this, n) : BitLevel.shiftRight(
                this, -n));
    }

    /**
     * Returns the length of the value's two's complement representation without
     * leading zeros for positive numbers / without leading ones for negative
     * values.
     * <p>
     * The two's complement representation of {@code this} will be at least
     * {@code bitLength() + 1} bits long.
     * <p>
     * The value will fit into an {@code int} if {@code bitLength() < 32} or
     * into a {@code long} if {@code bitLength() < 64}.
     *
     * @return the length of the minimal two's complement representation for
     *         {@code this} without the sign bit.
     */
    public int bitLength() {
        return BitLevel.bitLength(this);
    }

    /**
     * Tests whether the bit at position n in {@code this} is set. The result is
     * equivalent to {@code this & (2^n) != 0}.
     * <p>
     * <b>Implementation Note:</b> Usage of this method is not recommended as
     * the current implementation is not efficient.
     *
     * @param n
     *            position where the bit in {@code this} has to be inspected.
     * @return {@code this & (2^n) != 0}.
     * @throws ArithmeticException
     *             if {@code n < 0}.
     */
    public boolean testBit(int n) {
        if (n == 0) {
            return ((digits[0] & 1) != 0);
        }
        if (n < 0) {
            // math.15=Negative bit address
            throw new ArithmeticException(Messages.getString("math.15")); //$NON-NLS-1$
        }
        int intCount = n >> 5;
        if (intCount >= numberLength) {
            return (sign < 0);
        }
        int digit = digits[intCount];
        n = (1 << (n & 31)); // int with 1 set to the needed position
        if (sign < 0) {
            int firstNonZeroDigit = getFirstNonzeroDigit();
            if (intCount < firstNonZeroDigit) {
                return false;
            } else if (firstNonZeroDigit == intCount) {
                digit = -digit;
            } else {
                digit = ~digit;
            }
        }
        return ((digit & n) != 0);
    }

    /**
     * Returns the position of the lowest set bit in the two's complement
     * representation of this {@code BigInteger}. If all bits are zero (this=0)
     * then -1 is returned as result.
     * <p>
     * <b>Implementation Note:</b> Usage of this method is not recommended as
     * the current implementation is not efficient.
     *
     * @return position of lowest bit if {@code this != 0}, {@code -1} otherwise
     */
    public int getLowestSetBit() {
        if (sign == 0) {
            return -1;
        }
        // (sign != 0) implies that exists some non zero digit
        int i = getFirstNonzeroDigit();
        return ((i << 5) + Integer.numberOfTrailingZeros(digits[i]));
    }

    /**
     * Compares this {@code BigInteger} with {@code val}. Returns one of the
     * three values 1, 0, or -1.
     *
     * @param val
     *            value to be compared with {@code this}.
     * @return {@code 1} if {@code this > val}, {@code -1} if {@code this < val}
     *         , {@code 0} if {@code this == val}.
     * @throws NullPointerException
     *             if {@code val == null}.
     */
    public int compareTo(BigInteger val) {
        if (sign > val.sign) {
            return GREATER;
        }
        if (sign < val.sign) {
            return LESS;
        }
        if (numberLength > val.numberLength) {
            return sign;
        }
        if (numberLength < val.numberLength) {
            return -val.sign;
        }
        // Equal sign and equal numberLength
        return (sign * Elementary.compareArrays(digits, val.digits,
                numberLength));
    }

    /**
     * Returns the minimum of this {@code BigInteger} and {@code val}.
     *
     * @param val
     *            value to be used to compute the minimum with {@code this}.
     * @return {@code min(this, val)}.
     * @throws NullPointerException
     *             if {@code val == null}.
     */
    public BigInteger min(BigInteger val) {
        return ((this.compareTo(val) == LESS) ? this : val);
    }

    /**
     * Returns the maximum of this {@code BigInteger} and {@code val}.
     *
     * @param val
     *            value to be used to compute the maximum with {@code this}
     * @return {@code max(this, val)}
     * @throws NullPointerException
     *             if {@code val == null}
     */
    public BigInteger max(BigInteger val) {
        return ((this.compareTo(val) == GREATER) ? this : val);
    }

    /**
     * Returns a hash code for this {@code BigInteger}.
     *
     * @return hash code for {@code this}.
     */
    @Override
    public int hashCode() {
        if (hashCode != 0) {
            return hashCode;
        }
        for (int i = 0; i < digits.length; i++) {
            hashCode = (hashCode * 33 + (digits[i] & 0xffffffff));
        }
        hashCode = hashCode * sign;
        return hashCode;
    }

    /**
     * Returns {@code true} if {@code x} is a BigInteger instance and if this
     * instance is equal to this {@code BigInteger}.
     *
     * @param x
     *            object to be compared with {@code this}.
     * @return true if {@code x} is a BigInteger and {@code this == x},
     *          {@code false} otherwise.
     */
    @Override
    public boolean equals(Object x) {
        if (this == x) {
            return true;
        }
        if (x instanceof BigInteger) {
            BigInteger x1 = (BigInteger) x;
            return sign == x1.sign && numberLength == x1.numberLength
                    && equalsArrays(x1.digits);
        }
        return false;
    }

    boolean equalsArrays(final int[] b) {
        int i;
        for (i = numberLength - 1; (i >= 0) && (digits[i] == b[i]); i--) {
            // Empty
        }
        return i < 0;
    }

    /**
     * Returns a new {@code BigInteger} whose value is {@code this * val}.
     *
     * @param val
     *            value to be multiplied with {@code this}.
     * @return {@code this * val}.
     * @throws NullPointerException
     *             if {@code val == null}.
     */
    public BigInteger multiply(BigInteger val) {
        // This let us to throw NullPointerException when val == null
        if (val.sign == 0) {
            return ZERO;
        }
        if (sign == 0) {
            return ZERO;
        }
        return Multiplication.multiply(this, val);
    }

    /**
     * Returns a new {@code BigInteger} whose value is {@code this % divisor}.
     * Regarding signs this methods has the same behavior as the % operator on
     * int's, i.e. the sign of the remainder is the same as the sign of this.
     *
     * @param divisor
     *            value by which {@code this} is divided.
     * @return {@code this % divisor}.
     * @throws NullPointerException
     *             if {@code divisor == null}.
     * @throws ArithmeticException
     *             if {@code divisor == 0}.
     */
    public BigInteger remainder(BigInteger divisor) {
        if (divisor.sign == 0) {
            // math.17=BigInteger divide by zero
            throw new ArithmeticException(Messages.getString("math.17")); //$NON-NLS-1$
        }
        int thisLen = numberLength;
        int divisorLen = divisor.numberLength;
        if (((thisLen != divisorLen) ? ((thisLen > divisorLen) ? 1 : -1)
                : Elementary.compareArrays(digits, divisor.digits, thisLen)) == LESS) {
            return this;
        }
        int resLength = divisorLen;
        int resDigits[] = new int[resLength];
        if (resLength == 1) {
            resDigits[0] = Division.remainderArrayByInt(digits, thisLen,
                    divisor.digits[0]);
        } else {
            int qLen = thisLen - divisorLen + 1;
            resDigits = Division.divide(null, qLen, digits, thisLen,
                    divisor.digits, divisorLen);
        }
        BigInteger result = new BigInteger(sign, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }

    /**
     * Returns a new {@code BigInteger} whose value is {@code this^exponent mod
     * m}. The modulus {@code m} must be positive. The result is guaranteed to
     * be in the interval {@code [0, m)} (0 inclusive, m exclusive). If the
     * exponent is negative, then {@code this.modInverse(m)^(-exponent) mod m)}
     * is computed. The inverse of this only exists if {@code this} is
     * relatively prime to m, otherwise an exception is thrown.
     *
     * @param exponent
     *            the exponent.
     * @param m
     *            the modulus.
     * @return {@code this^exponent mod val}.
     * @throws NullPointerException
     *             if {@code m == null} or {@code exponent == null}.
     * @throws ArithmeticException
     *             if {@code m < 0} or if {@code exponent<0} and this is not
     *             relatively prime to {@code m}.
     */
    public BigInteger modPow(BigInteger exponent, BigInteger m) {
        if (m.sign <= 0) {
            // math.18=BigInteger: modulus not positive
            throw new ArithmeticException(Messages.getString("math.18")); //$NON-NLS-1$
        }
        BigInteger base = this;

        if (m.isOne() | (exponent.sign > 0 & base.sign == 0)) {
            return BigInteger.ZERO;
        }
        if (exponent.sign == 0) {
            return BigInteger.ONE.mod(m);
        }
        // From now on: (m > 0) and (exponent >= 0)
        boolean odd = m.testBit(0);
        BigInteger res = odd
                ? Division.oddModPow(base.abs(), exponent, m)
                : Division.evenModPow(base.abs(), exponent, m);
        if ((base.sign < 0) && exponent.testBit(0)) {
            // -b^e mod m == ((-1 mod m) * (b^e mod m)) mod m
            res = m.subtract(BigInteger.ONE).multiply(res).mod(m);
        }
        // else exponent is even, so base^exp is positive
        return res;
    }

    /**
     * Returns a new {@code BigInteger} whose value is {@code this mod m}. The
     * modulus {@code m} must be positive. The result is guaranteed to be in the
     * interval {@code [0, m)} (0 inclusive, m exclusive). The behavior of this
     * function is not equivalent to the behavior of the % operator defined for
     * the built-in {@code int}'s.
     *
     * @param m
     *            the modulus.
     * @return {@code this mod m}.
     * @throws NullPointerException
     *             if {@code m == null}.
     * @throws ArithmeticException
     *             if {@code m < 0}.
     */
    public BigInteger mod(BigInteger m) {
        if (m.sign <= 0) {
            // math.18=BigInteger: modulus not positive
            throw new ArithmeticException(Messages.getString("math.18")); //$NON-NLS-1$
        }
        BigInteger rem = remainder(m);
        return ((rem.sign < 0) ? rem.add(m) : rem);
    }

    /**
     * Tests whether this {@code BigInteger} is probably prime. If {@code true}
     * is returned, then this is prime with a probability beyond
     * (1-1/2^certainty). If {@code false} is returned, then this is definitely
     * composite. If the argument {@code certainty} <= 0, then this method
     * returns true.
     *
     * @param certainty
     *            tolerated primality uncertainty.
     * @return {@code true}, if {@code this} is probably prime, {@code false}
     *         otherwise.
     */
    public boolean isProbablePrime(int certainty) {
        return Primality.isProbablePrime(abs(), certainty);
    }

    /**
     * Returns a random positive {@code BigInteger} instance in the range [0,
     * 2^(bitLength)-1] which is probably prime. The probability that the
     * returned {@code BigInteger} is prime is beyond (1-1/2^80).
     * <p>
     * <b>Implementation Note:</b> Currently {@code rnd} is ignored.
     *
     * @param bitLength
     *            length of the new {@code BigInteger} in bits.
     * @param rnd
     *            random generator used to generate the new {@code BigInteger}.
     * @return probably prime random {@code BigInteger} instance.
     * @throws IllegalArgumentException
     *             if {@code bitLength < 2}.
     */
    public static BigInteger probablePrime(int bitLength, Random rnd) {
        return new BigInteger(bitLength, 100, rnd);
    }

    /* Private Methods */

    /** Decreases {@code numberLength} if there are zero high elements. */
    final void cutOffLeadingZeroes() {
        while ((numberLength > 0) && (digits[--numberLength] == 0)) {
            // Empty
        }
        if (digits[numberLength++] == 0) {
            sign = 0;
        }
    }

    /** Tests if {@code this.abs()} is equals to {@code ONE} */
    boolean isOne() {
        return ((numberLength == 1) && (digits[0] == 1));
    }

    /**
     * Puts a big-endian byte array into a little-endian int array.
     */
    private void putBytesPositiveToIntegers(byte[] byteValues) {
        int bytesLen = byteValues.length;
        int highBytes = bytesLen & 3;
        numberLength = (bytesLen >> 2) + ((highBytes == 0) ? 0 : 1);
        digits = new int[numberLength];
        int i = 0;
        // Put bytes to the int array starting from the end of the byte array
        while (bytesLen > highBytes) {
            digits[i++] = (byteValues[--bytesLen] & 0xFF)
                    | (byteValues[--bytesLen] & 0xFF) << 8
                    | (byteValues[--bytesLen] & 0xFF) << 16
                    | (byteValues[--bytesLen] & 0xFF) << 24;
        }
        // Put the first bytes in the highest element of the int array
        for (int j = 0; j < bytesLen; j++) {
            digits[i] = (digits[i] << 8) | (byteValues[j] & 0xFF);
        }
    }

    /**
     * Puts a big-endian byte array into a little-endian applying two
     * complement.
     */
    private void putBytesNegativeToIntegers(byte[] byteValues) {
        int bytesLen = byteValues.length;
        int highBytes = bytesLen & 3;
        numberLength = (bytesLen >> 2) + ((highBytes == 0) ? 0 : 1);
        digits = new int[numberLength];
        int i = 0;
        // Setting the sign
        digits[numberLength - 1] = -1;
        // Put bytes to the int array starting from the end of the byte array
        while (bytesLen > highBytes) {
            digits[i] = (byteValues[--bytesLen] & 0xFF)
                    | (byteValues[--bytesLen] & 0xFF) << 8
                    | (byteValues[--bytesLen] & 0xFF) << 16
                    | (byteValues[--bytesLen] & 0xFF) << 24;
            if (digits[i] != 0) {
                digits[i] = -digits[i];
                firstNonzeroDigit = i;
                i++;
                while (bytesLen > highBytes) {
                    digits[i] = (byteValues[--bytesLen] & 0xFF)
                            | (byteValues[--bytesLen] & 0xFF) << 8
                            | (byteValues[--bytesLen] & 0xFF) << 16
                            | (byteValues[--bytesLen] & 0xFF) << 24;
                    digits[i] = ~digits[i];
                    i++;
                }
                break;
            }
            i++;
        }
        if (highBytes != 0) {
            // Put the first bytes in the highest element of the int array
            if (firstNonzeroDigit != -2) {
                for (int j = 0; j < bytesLen; j++) {
                    digits[i] = (digits[i] << 8) | (byteValues[j] & 0xFF);
                }
                digits[i] = ~digits[i];
            } else {
                for (int j = 0; j < bytesLen; j++) {
                    digits[i] = (digits[i] << 8) | (byteValues[j] & 0xFF);
                }
                digits[i] = -digits[i];
            }
        }
    }

    int getFirstNonzeroDigit() {
        if (firstNonzeroDigit == -2) {
            int i;
            if (this.sign == 0) {
                i = -1;
            } else {
                for (i = 0; digits[i] == 0; i++) {
                    // Empty
                }
            }
            firstNonzeroDigit = i;
        }
        return firstNonzeroDigit;
    }

    void unCache() {
        firstNonzeroDigit = -2;
    }

    static BigInteger getPowerOfTwo(int exp) {
        if(exp < TWO_POWS.length) {
            return TWO_POWS[exp];
        }
        int intCount = exp >> 5;
        int bitN = exp & 31;
        int resDigits[] = new int[intCount+1];
        resDigits[intCount] = 1 << bitN;
        return new BigInteger(1, intCount+1, resDigits);
    }
}
