package org.ksharp.ir.truffle.numeric;

import java.math.BigDecimal;

public interface NumericOperations extends IntegerOperations {

    float doFloat(float left, float right);

    double doDouble(double left, double right);

    BigDecimal doBigDecimal(BigDecimal left, BigDecimal right);
}
