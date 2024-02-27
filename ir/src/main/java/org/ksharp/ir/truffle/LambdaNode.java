package org.ksharp.ir.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;

public class LambdaNode extends KSharpNode {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private FunctionNode lambda;

    public LambdaNode(int slots, KSharpNode expr) {
        this.lambda = new FunctionNode(slots, expr);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return this.lambda;
    }
}
