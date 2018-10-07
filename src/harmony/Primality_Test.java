package harmony;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Random;

import static java.lang.Math.*;
import static org.junit.jupiter.api.Assertions.*;

public class Primality_Test {

    static final int certainty = 100;
    static final Random random = new SecureRandom();

    @Test
    public void prime_percent_10() {
        assertPercentPrime(10);
    }

    @Test
    public void prime_percent_11() {
        assertPercentPrime(11);
    }

    @Test
    public void prime_percent_20() {
        assertPercentPrime(20);
    }

    @Test
    public void prime_percent_21() {
        assertPercentPrime(21);
    }

    @Test
    public void prime_percent_40() {
        assertPercentPrime(40);
    }

    @Test
    public void prime_percent_80() {
        assertPercentPrime(80);
    }

    void assertPercentPrime(int bitLength) {
        double harmony = harmonyPercentPrime(bitLength);
        double jdk = jdkPercentPrime(bitLength);
        double ratio = harmony / jdk;
        double diff = abs(1.0-ratio);
        String message = "harmony = " + harmony + " jdk = " + jdk + " diff = " + diff;
        assertTrue(diff<0.1,message);
    }

    static double ratio(long a, long b) {
        return (double)a / (double)b;
    }

    double harmonyPercentPrime(int bitLength) {
        int primes = 0;
        int sample = bitLength * bitLength * bitLength * bitLength;
        for (int i=0; i<sample; tick(i++)) {
            BigInteger x = randomHarmony(bitLength);
            comparePrimality(x);
            if (x.isProbablePrime(certainty)) {
                primes++;
            }
        }
        return ratio(primes,sample);
    }

    void comparePrimality(BigInteger x) {
        java.math.BigInteger y = new java.math.BigInteger(x.toByteArray());
        assertEquals(x.isProbablePrime(certainty),y.isProbablePrime(certainty));
    }

    void comparePrimality(java.math.BigInteger x) {
        BigInteger y = new BigInteger(x.toByteArray());
        assertEquals(x.isProbablePrime(certainty),y.isProbablePrime(certainty));
    }

    static void tick(int i) {
        if (i%100==0) {
            System.out.print(".");
            if (i%101==0) {
                System.out.println();
            }
        }
    }

    private BigInteger randomHarmony(int bitLength) {
        return new BigInteger(bitLength,random);
    }

    private java.math.BigInteger randomJdk(int bitLength) {
        return new java.math.BigInteger(bitLength,random);
    }

    double jdkPercentPrime(int bitLength) {
        int primes = 0;
        int sample = bitLength * bitLength * bitLength * bitLength;
        for (int i=0; i<sample; tick(i++)) {
            java.math.BigInteger x = randomJdk(bitLength);
            comparePrimality(x);
            if (x.isProbablePrime(certainty)) {
                primes++;
            }
        }
        return ratio(primes,sample);
    }

}
