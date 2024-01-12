package org.ksharp.ir.truffle.bitwise;

import java.math.BigInteger;

public interface BitwiseOperations {

    byte doByte(byte left, byte right);

    short doShort(short left, short right);

    int doInt(int left, int right);

    long doLong(long left, long right);

    BigInteger doBigInteger(BigInteger left, BigInteger right);
}
