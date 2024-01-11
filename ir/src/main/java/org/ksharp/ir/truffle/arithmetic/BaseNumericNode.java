package org.ksharp.ir.truffle.arithmetic;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

public abstract class BaseNumericNode extends KSharpNode implements NumericOperations {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private NumericNode arithmeticNode;

    protected BaseNumericNode(KSharpNode left, KSharpNode right) {
        this.arithmeticNode = NumericNodeGen.create(left, right, this);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return arithmeticNode.execute(frame);
    }

}
