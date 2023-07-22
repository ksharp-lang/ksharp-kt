package org.ksharp.ir.truffle.variable;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

public class LetNode extends KSharpNode {

    @Children
    private final KSharpNode[] arguments;

    public LetNode(KSharpNode[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object result = null;
        for (var expr : arguments) {
            result = expr.execute(frame);
        }
        return result;
    }
}
