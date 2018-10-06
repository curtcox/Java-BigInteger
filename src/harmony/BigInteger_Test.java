package harmony;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class BigInteger_Test {

    static final Random random = new Random();
    static final int certainty = 100;

    @Test
    public void can_create() {
        assertNotNull(BigInteger.ONE);
    }

    @Test
    public void random_prime_10() {
        assertRandomPrime(10);
    }

    @Test
    public void random_prime_100() {
        assertRandomPrime(100);
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
        final BigInteger x = new BigInteger(bitLength,certainty,random);
        assertTrue(x.isProbablePrime(certainty));
    }

}