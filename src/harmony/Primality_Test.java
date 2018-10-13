package harmony;

import org.junit.jupiter.api.Test;

import static java.lang.Math.*;
import static org.junit.jupiter.api.Assertions.*;

public class Primality_Test {

    static final IBigInteger.Factory harmony = IBigInteger.harmony;
    static final IBigInteger.Factory jdk = IBigInteger.jdk;

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

    @Test
    public void numberOfRequiredIterations() {
        assertEquals(50,Primality.numberOfRequiredIterations(1));
        assertEquals(50,Primality.numberOfRequiredIterations(10));
        assertEquals(47,Primality.numberOfRequiredIterations(50));
        assertEquals(37,Primality.numberOfRequiredIterations(100));
        assertEquals(22,Primality.numberOfRequiredIterations(200));
        assertEquals(14,Primality.numberOfRequiredIterations(300));
        assertEquals(10,Primality.numberOfRequiredIterations(400));
        assertEquals( 8,Primality.numberOfRequiredIterations(500));
        assertEquals(7,Primality.numberOfRequiredIterations(600));
        assertEquals(6,Primality.numberOfRequiredIterations(700));
        assertEquals(5,Primality.numberOfRequiredIterations(800));
        assertEquals(5,Primality.numberOfRequiredIterations(900));
        assertEquals(4,Primality.numberOfRequiredIterations(1000));
        assertEquals(2,Primality.numberOfRequiredIterations(5000));
        assertEquals(2,Primality.numberOfRequiredIterations(10000));
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
        return percentPrime(harmony,bitLength);
    }

    void comparePrimality(IBigInteger x) {
        assertEquals(x.isPrime(),other(x).isPrime());
    }

    static IBigInteger other(IBigInteger x) {
        if (x instanceof HarmonyAsIBigInteger) {
            return jdk.from(x.toByteArray());
        } else {
            return harmony.from(x.toByteArray());
        }
    }

    static void tick(int i) {
        if (i%100==0) {
            System.out.print(".");
            if (i%101==0) {
                System.out.println();
            }
        }
    }

    double jdkPercentPrime(int bitLength) {
        return percentPrime(jdk,bitLength);
    }

    double percentPrime(IBigInteger.Factory factory,int bitLength) {
        int primes = 0;
        int sample = bitLength * bitLength * bitLength * bitLength;
        for (int i=0; i<sample; tick(i++)) {
            IBigInteger x = factory.random(bitLength);
            comparePrimality(x);
            if (x.isPrime()) {
                primes++;
            }
        }
        return ratio(primes,sample);
    }

}
