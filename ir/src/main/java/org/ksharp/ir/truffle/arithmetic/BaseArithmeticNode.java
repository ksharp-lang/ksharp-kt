package org.ksharp.ir.truffle.arithmetic;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

public abstract class BaseArithmeticNode extends KSharpNode implements ArithmeticOperations {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private ArithmeticNode arithmeticNode;

    protected BaseArithmeticNode(KSharpNode left, KSharpNode right) {
        this.arithmeticNode = ArithmeticNodeGen.create(left, right, this);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return arithmeticNode.execute(frame);
    }

}
