package org.ksharp.ir.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.variable.CaptureVarNode;

import java.util.HashMap;

public class LambdaNode extends KSharpNode {

    @SuppressWarnings("FieldMayBeFinal")
    @Children
    private CaptureVarNode[] capturedContext;

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode expr;

    @SuppressWarnings("FieldMayBeFinal")
    private final int slots;

    public LambdaNode(int slots, CaptureVarNode[] capturedContext, KSharpNode expr) {
        this.slots = slots;
        this.expr = expr;
        this.capturedContext = capturedContext;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        var context = new HashMap<String, Object>();
        for (var entry : capturedContext) {
            context.put(entry.getCaptureName(), entry.execute(frame));
        }
        return new FunctionNode(slots, context, expr);
    }
}
