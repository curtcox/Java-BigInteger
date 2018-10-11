package harmony;

import java.security.SecureRandom;
import java.util.Random;

final class HarmonyBigIntegerFactory
    implements IBigInteger.Factory
{
    static final Random random = new SecureRandom();

    @Override
    public IBigInteger random(int numBits) {
        return new HarmonyAsIBigIntger(new BigInteger(numBits,random));
    }

    @Override
    public IBigInteger prime(int numBits) {
        return null;
    }
}
