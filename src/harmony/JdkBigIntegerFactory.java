package harmony;

import java.security.SecureRandom;
import java.util.Random;

final class JdkBigIntegerFactory
        implements IBigInteger.Factory
{
    static final Random random = new SecureRandom();

    @Override
    public IBigInteger random(int numBits) {
        return new JdkAsIBigInteger(new java.math.BigInteger(numBits,random));
    }

    @Override
    public IBigInteger prime(int numBits) {
        int certainty = 100;
        return new JdkAsIBigInteger(new java.math.BigInteger(numBits,certainty,random));
    }
}
