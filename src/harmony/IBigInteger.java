package harmony;

public interface IBigInteger {

    IBigInteger subtract(IBigInteger val);
    IBigInteger multiply(IBigInteger val);
    IBigInteger mod(IBigInteger val);
    boolean isOne();
    IBigInteger shiftRight(int n);
    int getLowestSetBit();
    IBigInteger modPow(IBigInteger exponent, IBigInteger m);
}

