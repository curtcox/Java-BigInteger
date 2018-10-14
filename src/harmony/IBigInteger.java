package harmony;

/**
 * The minimal methods needed by Primality.
 */
interface IBigInteger {

    int certainty = 100;

    interface Factory {
        IBigInteger random(int bits);
        IBigInteger prime(int bits);
        IBigInteger valueOf(long number);
        IBigInteger from(byte[] bytes);
    }

    IBigInteger.Factory harmony = HarmonyBigIntegerFactory.of();
    IBigInteger.Factory jdk = JdkBigIntegerFactory.of();

    IBigInteger multiply(IBigInteger val);
    IBigInteger mod(IBigInteger val);
    IBigInteger modPow(IBigInteger exponent, IBigInteger m);
    IBigInteger subtract_1();
    boolean isZero();
    boolean isOne();
    boolean isTwo();
    boolean isEven();
    boolean isPrime();
    boolean lessThanOrEqualTo(IBigInteger value);
    IBigInteger shiftRight(int n);
    int bitLength();
    int getLowestSetBit();

    // This is only needed by the tests
    byte[] toByteArray();

}

