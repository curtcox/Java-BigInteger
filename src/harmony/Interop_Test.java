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
        assertInterop(BigInteger.ZERO);
    }

    @Test
    public void interop_1() {
        assertInterop(BigInteger.ONE);
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

    @Test
    public void random_100000() {
        assertInteropBitLength(100000);
    }

    void assertInteropBitLength(int bitLength) {
        Set<BigInteger> harmony = new HashSet();
        Set<java.math.BigInteger> jdk = new HashSet();
        for (int i=0; i<samples; i++) {
            BigInteger x = new BigInteger(bitLength,random);
            jdk.add(assertInterop(x));
            harmony.add(x);
        }
        assertEquals(jdk.size(),harmony.size());
    }

    java.math.BigInteger assertInterop(BigInteger x) {
        assertEquals(x,x);
        int[] digits = x.digits;
        BigInteger harmonyCopy = new BigInteger(positive,digits);
        assertEquals(x,harmonyCopy);
        assertEquals(harmonyCopy,x);
        assertEquals(harmonyCopy,harmonyCopy);
        java.math.BigInteger jdk = new java.math.BigInteger(x.toByteArray());
        BigInteger thruJdk = new BigInteger(jdk.toByteArray());
        assertEquals(x,thruJdk);
        assertEquals(thruJdk,x);
        assertEquals(thruJdk,thruJdk);
        return jdk;
    }

}
