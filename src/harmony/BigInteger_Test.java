package harmony;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BigInteger_Test {

    static final IBigInteger.Factory harmony = IBigInteger.harmony;

    @Test
    public void can_create() {
        assertNotNull(BigInteger.ONE);
    }

    @Test
    public void random_prime_10() {
        assertRandomPrime(10);
    }

    @Test
    public void random_prime_11() {
        assertRandomPrime(11);
    }

    @Test
    public void random_prime_100() {
        assertRandomPrime(100);
    }

    @Test
    public void random_prime_800() {
        assertRandomPrime(800);
    }

    @Test
    public void random_prime_801() {
        assertRandomPrime(801);
    }

    @Test
    public void random_prime_1000() {
        assertRandomPrime(1000);
    }

    @Test
    public void random_prime_2000() {
        assertRandomPrime(2000);
    }

    @Test
    public void random_prime_4000() {
        assertRandomPrime(4000);
    }

    void assertRandomPrime(int bitLength) {
        final IBigInteger x = harmony.prime(bitLength);
        assertTrue(x.isPrime());
    }

}