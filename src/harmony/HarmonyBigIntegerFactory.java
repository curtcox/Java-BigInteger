package harmony;

import java.security.SecureRandom;
import java.util.Random;

final class HarmonyBigIntegerFactory
    implements IBigInteger.Factory
{
    private Primality primality;
    static final Random random = new SecureRandom();

    static HarmonyBigIntegerFactory of() {
        HarmonyBigIntegerFactory factory = new HarmonyBigIntegerFactory();
        factory.primality = Primality.from(factory);
        return factory;
    }

    @Override
    public IBigInteger random(int numBits) {
        return new HarmonyAsIBigInteger(new BigInteger(numBits,random), primality);
    }

    @Override
    public IBigInteger prime(int numBits) {
        IBigInteger candidate = random(numBits);
        while (!primality.isProbablePrime(candidate)) {
            candidate = random(numBits);
        }
        return candidate;
    }

    @Override
    public IBigInteger valueOf(long number) {
        return new HarmonyAsIBigInteger(BigInteger.valueOf(number), primality);
    }

    @Override
    public IBigInteger from(byte[] bytes) {
        return new HarmonyAsIBigInteger(new BigInteger(bytes),primality);
    }
}
