package org.ksharp.ir.truffle.numeric;

import com.oracle.truffle.api.dsl.Specialization;

import java.math.BigDecimal;

public abstract class NumericNode extends IntegerNode {

    @Specialization
    protected float doFloat(float left, float right) {
        return ((NumericOperations) getOperations()).doFloat(left, right);
    }

    @Specialization
    protected double doDouble(double left, double right) {
        return ((NumericOperations) getOperations()).doDouble(left, right);
    }

    @Specialization
    protected BigDecimal doBigDecimal(BigDecimal left, BigDecimal right) {
        return ((NumericOperations) getOperations()).doBigDecimal(left, right);
    }

}
