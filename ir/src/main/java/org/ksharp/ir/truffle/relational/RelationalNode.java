package org.ksharp.ir.truffle.relational;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import org.ksharp.ir.truffle.BinaryOperationNode;

import java.math.BigDecimal;
import java.math.BigInteger;

@NodeField(name = "operations", type = RelationalOperations.class)
public abstract class RelationalNode extends BinaryOperationNode {

    protected abstract RelationalOperations getOperations();

    @Specialization
    protected boolean doByte(byte left, byte right) {
        return getOperations().doByte(left, right);
    }

    @Specialization
    protected boolean doShort(short left, short right) {
        return getOperations().doShort(left, right);
    }

    @Specialization
    protected boolean doInt(int left, int right) {
        return getOperations().doInt(left, right);
    }

    @Specialization
    protected boolean doLong(long left, long right) {
        return getOperations().doLong(left, right);
    }

    @Specialization
    protected boolean doFloat(float left, float right) {
        return getOperations().doFloat(left, right);
    }

    @Specialization
    protected boolean doDouble(double left, double right) {
        return getOperations().doDouble(left, right);
    }

    @Specialization
    protected boolean doBigInteger(BigInteger left, BigInteger right) {
        return getOperations().doBigInteger(left, right);
    }

    @Specialization
    protected boolean doBigDecimal(BigDecimal left, BigDecimal right) {
        return getOperations().doBigDecimal(left, right);
    }

}
