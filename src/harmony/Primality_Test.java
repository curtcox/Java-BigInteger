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
    public void prime_percent_20() {
        assertPercentPrime(20);
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
        assertTrue(diff<0.05,message);
    }

    static double ratio(long a, long b) {
        return (double)a / (double)b;
    }

    double harmonyPercentPrime(int bitLength) {
        int primes = 0;
        int sample = bitLength * bitLength * bitLength * bitLength;
        for (int i=0; i<sample; tick(i++)) {
            if (randomHarmony(bitLength).isProbablePrime(certainty)) {
                primes++;
            }
        }
        return ratio(primes,sample);
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
            if (randomJdk(bitLength).isProbablePrime(certainty)) {
                primes++;
            }
        }
        return ratio(primes,sample);
    }

}
