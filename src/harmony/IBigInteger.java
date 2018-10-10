package harmony;

/**
 * The minimal methods needed by Primality.
 */
interface IBigInteger {
    IBigInteger subtract(IBigInteger val);
    IBigInteger multiply(IBigInteger val);
    IBigInteger mod(IBigInteger val);
    IBigInteger modPow(IBigInteger exponent, IBigInteger m);
    boolean isOne();
    IBigInteger shiftRight(int n);
    int getDigit(int i);
    void setDigit(int i, int value);
    int[] digits();
    int numberLength();
    int getLowestSetBit();

//    public int getLowestSetBit() {
//        // (sign != 0) implies that exists some non zero digit
//        int i = getFirstNonzeroDigit();
//        return ((i << 5) + Integer.numberOfTrailingZeros(digits[i]));
//    }
//
//    int getFirstNonzeroDigit() {
//        if (firstNonzeroDigit == -2) {
//            int i;
//            for (i = 0; digits[i] == 0; i++) {}
//            firstNonzeroDigit = i;
//        }
//        return firstNonzeroDigit;
//    }
//    /**
//     * Returns the number of zero bits following the lowest-order ("rightmost")
//     * one-bit in the two's complement binary representation of the specified
//     * {@code int} value.  Returns 32 if the specified value has no
//     * one-bits in its two's complement representation, in other words if it is
//     * equal to zero.
//     *
//     * @param i the value whose number of trailing zeros is to be computed
//     * @return the number of zero bits following the lowest-order ("rightmost")
//     *     one-bit in the two's complement binary representation of the
//     *     specified {@code int} value, or 32 if the value is equal
//     *     to zero.
//     * @since 1.5
//     */
//    public static int numberOfTrailingZeros(int i) {
//        // HD, Figure 5-14
//        int y;
//        if (i == 0) return 32;
//        int n = 31;
//        y = i <<16; if (y != 0) { n = n -16; i = y; }
//        y = i << 8; if (y != 0) { n = n - 8; i = y; }
//        y = i << 4; if (y != 0) { n = n - 4; i = y; }
//        y = i << 2; if (y != 0) { n = n - 2; i = y; }
//        return n - ((i << 1) >>> 31);
//    }
}

