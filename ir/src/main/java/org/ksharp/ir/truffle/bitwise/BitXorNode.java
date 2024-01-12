package org.ksharp.ir.truffle.bitwise;

import com.oracle.truffle.api.nodes.NodeInfo;
import org.ksharp.ir.truffle.KSharpNode;

import java.math.BigInteger;

@NodeInfo(shortName = "^")
public class BitXorNode extends BaseBitwiseNode {
    protected BitXorNode(KSharpNode left, KSharpNode right) {
        super(left, right);
    }

    @Override
    public byte doByte(byte left, byte right) {
        return (byte) (left ^ right);
    }

    @Override
    public short doShort(short left, short right) {
        return (short) (left ^ right);
    }

    @Override
    public int doInt(int left, int right) {
        return (left ^ right);
    }

    @Override
    public long doLong(long left, long right) {
        return (left ^ right);
    }

    @Override
    public BigInteger doBigInteger(BigInteger left, BigInteger right) {
        return left.xor(right);
    }
}
