package org.ksharp.ir.truffle.relational;

import com.oracle.truffle.api.nodes.NodeInfo;
import org.ksharp.ir.truffle.KSharpNode;

import java.math.BigDecimal;
import java.math.BigInteger;

@NodeInfo(shortName = ">")
public class GtNode extends BaseRelationalNode {
    protected GtNode(KSharpNode left, KSharpNode right) {
        super(left, right);
    }

    @Override
    public boolean doByte(byte left, byte right) {
        return left > right;
    }

    @Override
    public boolean doShort(short left, short right) {
        return left > right;
    }

    @Override
    public boolean doInt(int left, int right) {
        return left > right;
    }

    @Override
    public boolean doLong(long left, long right) {
        return left > right;
    }

    @Override
    public boolean doFloat(float left, float right) {
        return left > right;
    }

    @Override
    public boolean doDouble(double left, double right) {
        return left > right;
    }

    @Override
    public boolean doBigInteger(BigInteger left, BigInteger right) {
        return left.compareTo(right) > 0;
    }

    @Override
    public boolean doBigDecimal(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) > 0;
    }
}
