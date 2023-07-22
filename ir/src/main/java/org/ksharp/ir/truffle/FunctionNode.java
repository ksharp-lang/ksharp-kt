package org.ksharp.ir.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;

public class FunctionNode extends KSharpRootNode {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode expr;

    public FunctionNode(int slots, KSharpNode expr) {
        super(null, slots);
        this.expr = expr;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return expr.execute(frame);
    }
}
