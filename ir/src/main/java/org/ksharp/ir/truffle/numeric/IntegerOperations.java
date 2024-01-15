package org.ksharp.ir.truffle.numeric;

import java.math.BigInteger;

public interface IntegerOperations {

    byte doByte(byte left, byte right);

    short doShort(short left, short right);

    int doInt(int left, int right);

    long doLong(long left, long right);

    BigInteger doBigInteger(BigInteger left, BigInteger right);
}
