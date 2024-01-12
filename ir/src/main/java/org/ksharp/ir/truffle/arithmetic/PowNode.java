package org.ksharp.ir.truffle.arithmetic;

import com.oracle.truffle.api.nodes.NodeInfo;
import org.ksharp.ir.truffle.KSharpNode;

import java.math.BigDecimal;
import java.math.BigInteger;

@NodeInfo(shortName = "**")
public class PowNode extends BaseArithmeticNode {

    public PowNode(KSharpNode left, KSharpNode right) {
        super(left, right);
    }

    @Override
    public byte doByte(byte left, byte right) {
        return (byte) Math.pow(left, right);
    }

    @Override
    public short doShort(short left, short right) {
        return (short) Math.pow(left, right);
    }

    @Override
    public int doInt(int left, int right) {
        return (int) Math.pow(left, right);
    }

    @Override
    public long doLong(long left, long right) {
        return (long) Math.pow(left, right);
    }

    @Override
    public float doFloat(float left, float right) {
        return (float) Math.pow(left, right);
    }

    @Override
    public double doDouble(double left, double right) {
        return Math.pow(left, right);
    }

    @Override
    public BigInteger doBigInteger(BigInteger left, BigInteger right) {
        return left.pow(right.intValue());
    }

    @Override
    public BigDecimal doBigDecimal(BigDecimal left, BigDecimal right) {
        return left.pow(right.intValue());
    }
}
