package org.ksharp.ir.truffle.variable;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

public class CaptureVarNode extends KSharpNode {
    private final String captureName;

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode expr;

    public CaptureVarNode(String captureName, KSharpNode expr) {
        this.captureName = captureName;
        this.expr = expr;
    }

    public String getCaptureName() {
        return captureName;
    }
    
    @Override
    public Object execute(VirtualFrame frame) {
        return expr.execute(frame);
    }
}
