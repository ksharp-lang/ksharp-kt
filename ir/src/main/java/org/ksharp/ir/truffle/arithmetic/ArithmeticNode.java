package org.ksharp.ir.truffle.arithmetic;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import org.ksharp.ir.truffle.BinaryOperationNode;
import org.ksharp.ir.truffle.PreludeTypeSystemGen;

import java.math.BigDecimal;
import java.math.BigInteger;

@NodeField(name = "operations", type = ArithmeticOperations.class)
public abstract class ArithmeticNode extends BinaryOperationNode {

    protected abstract ArithmeticOperations getOperations();

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

    public byte executeByte(VirtualFrame frame) throws UnexpectedResultException {
        return PreludeTypeSystemGen.expectByte(execute(frame));
    }

    public short executeShort(VirtualFrame frame) throws UnexpectedResultException {
        return PreludeTypeSystemGen.expectShort(execute(frame));
    }

    public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
        return PreludeTypeSystemGen.expectInteger(execute(frame));
    }

    public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
        return PreludeTypeSystemGen.expectLong(execute(frame));
    }

    public float executeFloat(VirtualFrame frame) throws UnexpectedResultException {
        return PreludeTypeSystemGen.expectFloat(execute(frame));
    }

    public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
        return PreludeTypeSystemGen.expectDouble(execute(frame));
    }

    public BigInteger executeBigInteger(VirtualFrame frame) throws UnexpectedResultException {
        return PreludeTypeSystemGen.expectBigInteger(execute(frame));
    }

    public BigDecimal executeBigDecimal(VirtualFrame frame) throws UnexpectedResultException {
        return PreludeTypeSystemGen.expectBigDecimal(execute(frame));
    }

}
