package harmony;

import java.math.BigInteger;

final class JdkAsIBigInteger implements IBigInteger {

    private final BigInteger bigInteger;
    private static final BigInteger zero = BigInteger.ZERO;
    private static final BigInteger one = BigInteger.ONE;
    private static final BigInteger two = BigInteger.TWO;

    JdkAsIBigInteger(BigInteger bigInteger) {
        this.bigInteger = bigInteger;
    }

    static private BigInteger i(IBigInteger val) {
        return ((JdkAsIBigInteger)val).bigInteger;
    }

    static private IBigInteger o(BigInteger val) {
        return new JdkAsIBigInteger(val);
    }

    @Override
    public IBigInteger multiply(IBigInteger val) {
        return o(this.bigInteger.multiply(i(val)));
    }

    @Override
    public IBigInteger mod(IBigInteger val) {
        return o(this.bigInteger.mod(i(val)));
    }

    @Override
    public IBigInteger modPow(IBigInteger exponent, IBigInteger m) {
        return o(this.bigInteger.modPow(i(exponent),i(m)));
    }

    @Override
    public IBigInteger subtract_1() {
        return o(bigInteger.subtract(one));
    }

    @Override
    public boolean isZero() {
        return bigInteger.equals(zero);
    }

    @Override
    public boolean isOne() {
        return bigInteger.equals(one);
    }

    @Override
    public boolean isTwo() {
        return bigInteger.equals(two);
    }

    @Override
    public boolean isEven() {
        return bigInteger.mod(two).equals(zero);
    }

    @Override
    public boolean isPrime() {
        return bigInteger.isProbablePrime(certainty);
    }

    @Override
    public boolean lessThanOrEqualTo(IBigInteger value) {
        return bigInteger.compareTo(i(value)) <= 0;
    }

    @Override
    public IBigInteger shiftRight(int n) {
        return o(bigInteger.shiftRight(n));
    }

    @Override
    public int bitLength() {
        return bigInteger.bitLength();
    }

    @Override
    public int getLowestSetBit() {
        return bigInteger.getLowestSetBit();
    }

    @Override
    public byte[] toByteArray() {
        return bigInteger.toByteArray();
    }

    @Override
    public int hashCode() {
        return bigInteger.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        JdkAsIBigInteger that = (JdkAsIBigInteger) other;
        return bigInteger.equals(that.bigInteger);
    }

}
