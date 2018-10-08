package harmony;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Multiplication_Test {

    static final BigInteger one = BigInteger.ONE;
    static final BigInteger ten = BigInteger.TEN;
    static final Random random = new Random();

    @Test
    public void _1_x_1_is_1() {
        assertEquals(one,mult(one,one));
    }

    @Test
    public void _10_x_1_is_10() {
        assertEquals(ten,mult(ten,one));
    }

    @Test
    public void _1_x_10_is_10() {
        assertEquals(ten,mult(one,ten));
    }

    @Test
    public void random_times_one() {
        BigInteger x = new BigInteger(100,random);
        assertEquals(x,mult(x,one));
    }

    @Test
    public void random_times_random() {
        BigInteger x = new BigInteger(100,random);
        BigInteger y = new BigInteger(100,random);
        assertEquals(mult(y,x),mult(x,y));
    }

    BigInteger mult(BigInteger a, BigInteger b) {
        return Multiplication.multiply(a,b);
    }
}
