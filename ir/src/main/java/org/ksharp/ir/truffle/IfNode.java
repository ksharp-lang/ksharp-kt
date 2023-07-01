package org.ksharp.ir.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;

public class IfNode extends KSharpNode {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode condition;

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode thenExpr;

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode elseExpr;

    public IfNode(KSharpNode condition, KSharpNode thenExpr, KSharpNode elseExpr) {
        this.condition = condition;
        this.thenExpr = thenExpr;
        this.elseExpr = elseExpr;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if ((boolean) condition.execute(frame)) {
            return thenExpr.execute(frame);
        } else {
            return elseExpr.execute(frame);
        }
    }
}
