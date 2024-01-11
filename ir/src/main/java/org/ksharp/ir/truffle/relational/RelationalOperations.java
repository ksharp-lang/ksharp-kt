package org.ksharp.ir.truffle.relational;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface RelationalOperations {

    boolean doByte(byte left, byte right);

    boolean doShort(short left, short right);

    boolean doInt(int left, int right);

    boolean doLong(long left, long right);

    boolean doFloat(float left, float right);

    boolean doDouble(double left, double right);

    boolean doBigInteger(BigInteger left, BigInteger right);

    boolean doBigDecimal(BigDecimal left, BigDecimal right);
}
