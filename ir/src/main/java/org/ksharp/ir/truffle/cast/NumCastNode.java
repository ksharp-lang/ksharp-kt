package org.ksharp.ir.truffle.cast;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.CastType;
import org.ksharp.ir.truffle.KSharpNode;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumCastNode extends KSharpNode {

    private final CastType castType;

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode expression;

    public NumCastNode(CastType castType, KSharpNode expression) {
        this.castType = castType;
        this.expression = expression;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        var value = (Number) expression.execute(frame);
        if (castType == CastType.Byte) {
            return value.byteValue();
        }
        if (castType == CastType.Short) {
            return value.shortValue();
        }
        if (castType == CastType.Int) {
            return value.intValue();
        }
        if (castType == CastType.Long) {
            return value.longValue();
        }
        if (castType == CastType.BigInt) {
            if (value instanceof BigInteger) {
                return value;
            }
            if (value instanceof BigDecimal bigDecimal) {
                return bigDecimal.toBigInteger();
            }
            return BigInteger.valueOf(value.longValue());
        }
        if (castType == CastType.Float) {
            return value.floatValue();
        }
        if (castType == CastType.Double) {
            return value.doubleValue();
        }
        if (castType == CastType.BigDecimal) {
            if (value instanceof BigDecimal) {
                return value;
            }
            return BigDecimal.valueOf(value.doubleValue());
        }
        return value;
    }
}
