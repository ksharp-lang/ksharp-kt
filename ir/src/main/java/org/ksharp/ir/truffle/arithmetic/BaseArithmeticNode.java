package org.ksharp.ir.truffle.arithmetic;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;
import org.ksharp.ir.truffle.numeric.NumericNode;
import org.ksharp.ir.truffle.numeric.NumericNodeGen;
import org.ksharp.ir.truffle.numeric.NumericOperations;

public abstract class BaseArithmeticNode extends KSharpNode implements NumericOperations {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private NumericNode numericNode;

    protected BaseArithmeticNode(KSharpNode left, KSharpNode right) {
        this.numericNode = NumericNodeGen.create(left, right, this);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return numericNode.execute(frame);
    }

}
