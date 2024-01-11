package org.ksharp.ir.truffle.arithmetic;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import org.ksharp.ir.truffle.BinaryOperationNode;

import java.math.BigDecimal;
import java.math.BigInteger;

@NodeField(name = "operations", type = NumericOperations.class)
public abstract class NumericNode extends BinaryOperationNode {

    protected abstract NumericOperations getOperations();

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
    protected float doFloat(float left, float right) {
        return getOperations().doFloat(left, right);
    }

    @Specialization
    protected double doDouble(double left, double right) {
        return getOperations().doDouble(left, right);
    }

    @Specialization
    protected BigInteger doBigInteger(BigInteger left, BigInteger right) {
        return getOperations().doBigInteger(left, right);
    }

    @Specialization
    protected BigDecimal doBigDecimal(BigDecimal left, BigDecimal right) {
        return getOperations().doBigDecimal(left, right);
    }

}
