package org.ksharp.ir.truffle.call;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;
import org.ksharp.ir.truffle.runtime.FunctionObject;

public abstract class CallNode extends KSharpNode {

    private FunctionObject functionTarget;

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private FunctionDispatchNode dispatchNode;

    @Children
    private final KSharpNode[] arguments;

    public CallNode(KSharpNode[] arguments) {
        this.arguments = arguments;
        this.dispatchNode = FunctionDispatchNodeGen.create();
    }


    public abstract CallTarget getCallTarget();

    @Override
    public Object execute(VirtualFrame frame) {
        if (functionTarget == null) {
            var rootNode = getCallTarget();
            functionTarget = new FunctionObject(rootNode);
        }

        var argumentValues = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            argumentValues[i] = arguments[i].execute(frame);
        }

        return dispatchNode.executeDispatch(functionTarget, argumentValues);
    }
}
