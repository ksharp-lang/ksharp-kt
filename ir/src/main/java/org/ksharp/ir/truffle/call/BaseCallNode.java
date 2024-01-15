package org.ksharp.ir.truffle.call;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.strings.TruffleString;
import org.ksharp.ir.truffle.KSharpNode;

public abstract class BaseCallNode extends KSharpNode {
    @Children
    private final KSharpNode[] arguments;
    private final TruffleString.ToJavaStringNode toJavaStringNode;

    protected BaseCallNode(KSharpNode[] arguments) {
        this.arguments = arguments;
        toJavaStringNode = TruffleString.ToJavaStringNode.create();
    }

    public Object[] getArguments(VirtualFrame frame) {
        var argumentValues = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            var argument = arguments[i].execute(frame);
            if (argument instanceof TruffleString truffleStringArgument) {
                argumentValues[i] = toJavaStringNode.execute(truffleStringArgument);
            } else argumentValues[i] = argument;
        }
        return argumentValues;
    }
}
