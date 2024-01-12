package org.ksharp.ir.truffle.arithmetic;

import com.oracle.truffle.api.nodes.NodeInfo;
import org.ksharp.ir.truffle.KSharpNode;

import java.math.BigDecimal;
import java.math.BigInteger;

@NodeInfo(shortName = "+")
public class SumNode extends BaseArithmeticNode {

    public SumNode(KSharpNode left, KSharpNode right) {
        super(left, right);
    }

    @Override
    public byte doByte(byte left, byte right) {
        return (byte) (left + right);
    }

    @Override
    public short doShort(short left, short right) {
        return (short) (left + right);
    }

    @Override
    public int doInt(int left, int right) {
        return left + right;
    }

    @Override
    public long doLong(long left, long right) {
        return left + right;
    }

    @Override
    public float doFloat(float left, float right) {
        return left + right;
    }

    @Override
    public double doDouble(double left, double right) {
        return left + right;
    }

    @Override
    public BigInteger doBigInteger(BigInteger left, BigInteger right) {
        return left.add(right);
    }

    @Override
    public BigDecimal doBigDecimal(BigDecimal left, BigDecimal right) {
        return left.add(right);
    }
}
