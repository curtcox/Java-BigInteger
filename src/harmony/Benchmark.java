package harmony;

import java.security.SecureRandom;
import java.util.Random;

class Benchmark {

    static final Random random = new SecureRandom();
    static final int certainty = 100;

    public static void main(String[] args) {
        for (int i=1; i<10; i++) {
            compare(10 * i);
        }
        for (int i=1; i<10; i++) {
            compare(100 * i);
        }
        for (int i=1; i<10; i++) {
            compare(1000 * i);
        }
        for (int i=1; i<10; i++) {
            compare(10000 * i);
        }
    }

    static class Times {
        long create;
        long test;
        public String toString() {
            return "create=" +create + " test="+test;
        }
    }

    static void compare(int bitLength) {
        print("Bit length = " + bitLength);
        Times harmony = harmony(bitLength);
        Times jdk = jdk(bitLength);
        compare(harmony,jdk);
    }

    static Times jdk(int bitLength) {
        Times times = new Times();
        for (int i=0; i< 10000; i++) {
            long t0 = now();
            java.math.BigInteger x = new java.math.BigInteger(bitLength,certainty,random);
            long t1 = now();
            x.isProbablePrime(certainty);
            long t2 = now();
            times.create += (t1 - t0);
            times.test += (t2 - t1);

        }
        return times;
    }

    static Times harmony(int bitLength) {
        Times times = new Times();
        for (int i=0; i< 1000; i++) {
            long t0 = now();
            BigInteger x = new BigInteger(bitLength,certainty,random);
            long t1 = now();
            x.isProbablePrime(certainty);
            long t2 = now();
            times.create += (t1 - t0);
            times.test += (t2 - t1);
        }
        return times;
    }

    static long now() {
        return System.currentTimeMillis();
    }

    static void compare(Times harmony, Times jdk) {
        print("harmony " + harmony);
        print("jdk     " + jdk);
        print("harmony / jdk");
        print("create  " + ratio(harmony.create ,jdk.create));
        print("test    " + ratio(harmony.test   ,jdk.test));
    }

    static double ratio(long a, long b) {
        return (double)a / (double)b;
    }

    static void print(Object o) {
        System.out.println(o);
    }

}
