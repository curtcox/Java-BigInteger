package harmony;

final class HarmonyAsIBigInteger implements IBigInteger {

    private final BigInteger bigInteger;
    private final Primality primality;
    private static final BigInteger zero = BigInteger.ZERO;
    private static final BigInteger one = BigInteger.ONE;

    HarmonyAsIBigInteger(BigInteger bigInteger, Primality primality) {
        this.bigInteger = bigInteger;
        this.primality = primality;
    }

    static private BigInteger i(IBigInteger val) {
        return ((HarmonyAsIBigInteger)val).bigInteger;
    }

    private IBigInteger o(BigInteger val) {
        return new HarmonyAsIBigInteger(val,primality);
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
        return bigInteger.isOne();
    }

    @Override
    public boolean isTwo() {
        return (bigInteger.numberLength() == 1) && (bigInteger.getDigit(0) == 2);
    }

    @Override
    public boolean isEven() {
        return !bigInteger.testBit(0);
    }

    @Override
    public boolean isPrime() {
        return primality.isProbablePrime(this);
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
        HarmonyAsIBigInteger that = (HarmonyAsIBigInteger) other;
        return bigInteger.equals(that.bigInteger);
    }
}
