package harmony;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BigInteger_Test {

    @Test
    public void test_can_create() {
        assertNotNull(BigInteger.ONE);
    }

    public static void main(String[] args) {
        System.out.println(BigInteger.ONE);
    }
}