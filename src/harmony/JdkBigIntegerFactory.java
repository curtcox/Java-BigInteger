package harmony;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

final class JdkBigIntegerFactory
        implements IBigInteger.Factory
{
    static final Random random = new SecureRandom();
    static final int certainty = IBigInteger.certainty;

    static JdkBigIntegerFactory of() {
        JdkBigIntegerFactory factory = new JdkBigIntegerFactory();
        return factory;
    }

    @Override
    public IBigInteger random(int numBits) {
        return new JdkAsIBigInteger(new java.math.BigInteger(numBits,random));
    }

    @Override
    public IBigInteger prime(int numBits) {
        return new JdkAsIBigInteger(new java.math.BigInteger(numBits,certainty,random));
    }

    @Override
    public IBigInteger valueOf(long number) {
        return new JdkAsIBigInteger(java.math.BigInteger.valueOf(number));
    }

    @Override
    public IBigInteger from(byte[] bytes) {
        return new JdkAsIBigInteger(new BigInteger(bytes));
    }
}
