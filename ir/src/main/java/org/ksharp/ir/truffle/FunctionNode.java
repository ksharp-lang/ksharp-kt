package org.ksharp.ir.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;

public class FunctionNode extends KSharpRootNode {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode expr;

    public FunctionNode(KSharpNode expr) {
        super(null);
        this.expr = expr;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return expr.execute(frame);
    }
}
