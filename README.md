# Java-BigInteger
For comparing different Java BigInteger implementations.

My original thought was to compare the algorithms from Apache Harmony, Android, and the JDK
(via whatever JRE you run this on). Harmony was pretty trivial to "port", but Android looks like it
will be much tougher due to all of the native code. Plus, I'm not certain which version I should use.

https://android.googlesource.com/platform/libcore/+/master/luni/src/main/java/java/math
https://android.googlesource.com/platform/libcore2/+/master/luni/src/main/java/java/math

I'm interested in the algorithms concerning primes, so that is what I benchmarked.
