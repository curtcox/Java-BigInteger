package harmony;

class Benchmark {

    static final int samples = 10000;
    static final IBigInteger.Factory harmony = IBigInteger.harmony;
    static final IBigInteger.Factory jdk = IBigInteger.jdk;

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
        return factory(jdk,bitLength);
    }

    static Times harmony(int bitLength) {
        return factory(harmony,bitLength);
    }

    static void tick(int i) {
        if (i%(samples/100)==0) {
            System.out.print(".");
        }
    }

    static Times factory(IBigInteger.Factory factory,int bitLength) {
        Times times = new Times();
        for (int i=0; i< samples; tick(i++)) {
            long t0 = now();
            IBigInteger x = factory.prime(bitLength);
            long t1 = now();
            x.isPrime();
            long t2 = now();
            times.create += (t1 - t0);
            times.test += (t2 - t1);
        }
        print("");
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
