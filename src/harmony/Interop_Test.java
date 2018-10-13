package harmony;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Interop_Test {

    static final int positive = 1;
    static final Random random = new SecureRandom();
    static final int samples = 100;
    static final IBigInteger.Factory harmony = IBigInteger.harmony;
    static final IBigInteger.Factory jdk = IBigInteger.jdk;

    @Test
    public void equality_0() {
        assertEquals(BigInteger.ZERO,BigInteger.ZERO);
    }

    @Test
    public void equality_1() {
        assertEquals(BigInteger.ONE,BigInteger.ONE);
    }

    @Test
    public void interop_0() {
        assertInterop(harmony.valueOf(0));
    }

    @Test
    public void interop_1() {
        assertInterop(harmony.valueOf(1));
    }

    @Test
    public void random_10() {
        assertInteropBitLength(10);
    }

    @Test
    public void random_100() {
        assertInteropBitLength(100);
    }

    @Test
    public void random_1000() {
        assertInteropBitLength(1000);
    }

    @Test
    public void random_10000() {
        assertInteropBitLength(10000);
    }

    void assertInteropBitLength(int bitLength) {
        Set<IBigInteger> set = new HashSet();
        Set<IBigInteger> ref = new HashSet();
        for (int i=0; i<samples; i++) {
            IBigInteger x = harmony.random(bitLength);
            ref.add(assertInterop(x));
            set.add(x);
        }
        assertEquals(ref.size(),set.size());
    }

    IBigInteger assertInterop(IBigInteger x) {
        assertEquals(x,x);
        IBigInteger harmonyCopy = harmony.from(x.toByteArray());
        assertEquals(x,harmonyCopy);
        assertEquals(harmonyCopy,x);
        assertEquals(harmonyCopy,harmonyCopy);
        IBigInteger ref = jdk.from(x.toByteArray());
        assertEquals(x.isPrime(),ref.isPrime());
        IBigInteger thruRef = harmony.from(ref.toByteArray());
        assertEquals(x,thruRef);
        assertEquals(thruRef,x);
        assertEquals(thruRef,thruRef);
        return ref;
    }

}
