package org.ksharp.ir.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;

import java.util.Map;

public class FunctionNode extends KSharpRootNode {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode expr;

    @SuppressWarnings("FieldMayBeFinal")
    private Map<String, Object> capturedContext;

    public FunctionNode(int slots, Map<String, Object> capturedContext, KSharpNode expr) {
        super(null, slots);
        this.expr = expr;
        this.capturedContext = capturedContext;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (capturedContext != null) {
            frame.setObject(0, capturedContext);
        }
        return expr.execute(frame);
    }
}
