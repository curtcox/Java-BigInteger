package harmony;

final class HarmonyAsIBigIntger implements IBigInteger {

    private BigInteger bigInteger;

    HarmonyAsIBigIntger(BigInteger bigInteger) {
        this.bigInteger = bigInteger;
    }

    @Override
    public IBigInteger subtract(IBigInteger val) {
        return null;
    }

    @Override
    public IBigInteger multiply(IBigInteger val) {
        return null;
    }

    @Override
    public IBigInteger mod(IBigInteger val) {
        return null;
    }

    @Override
    public IBigInteger modPow(IBigInteger exponent, IBigInteger m) {
        return null;
    }

    @Override
    public boolean isOne() {
        return false;
    }

    @Override
    public IBigInteger shiftRight(int n) {
        return null;
    }

    @Override
    public int getDigit(int i) {
        return 0;
    }

    @Override
    public int[] digits() {
        return new int[0];
    }

    @Override
    public int numberLength() {
        return 0;
    }

    @Override
    public int bitLength() {
        return 0;
    }

    @Override
    public boolean testBit(int n) {
        return false;
    }

    @Override
    public int getLowestSetBit() {
        return 0;
    }
}
