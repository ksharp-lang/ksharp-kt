package org.ksharp.ir.truffle.arithmetic;

import java.math.BigDecimal;
import java.math.BigInteger;

interface ArithmeticOperations {

    byte doByte(byte left, byte right);

    short doShort(short left, short right);

    int doInt(int left, int right);

    long doLong(long left, long right);

    float doFloat(float left, float right);

    double doDouble(double left, double right);

    BigInteger doBigInteger(BigInteger left, BigInteger right);

    BigDecimal doBigDecimal(BigDecimal left, BigDecimal right);
}
