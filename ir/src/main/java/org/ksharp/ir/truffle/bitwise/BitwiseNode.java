package org.ksharp.ir.truffle.bitwise;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import org.ksharp.ir.truffle.BinaryOperationNode;

import java.math.BigInteger;

@NodeField(name = "operations", type = BitwiseOperations.class)
public abstract class BitwiseNode extends BinaryOperationNode {

    protected abstract BitwiseOperations getOperations();

    @Specialization
    protected byte doByte(byte left, byte right) {
        return getOperations().doByte(left, right);
    }

    @Specialization
    protected short doShort(short left, short right) {
        return getOperations().doShort(left, right);
    }

    @Specialization
    protected int doInt(int left, int right) {
        return getOperations().doInt(left, right);
    }

    @Specialization
    protected long doLong(long left, long right) {
        return getOperations().doLong(left, right);
    }

    @Specialization
    protected BigInteger doBigInteger(BigInteger left, BigInteger right) {
        return getOperations().doBigInteger(left, right);
    }


}
