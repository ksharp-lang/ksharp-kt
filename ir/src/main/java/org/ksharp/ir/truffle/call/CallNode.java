package org.ksharp.ir.truffle.call;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;
import org.ksharp.ir.truffle.runtime.FunctionObject;

public abstract class CallNode extends BaseCallNode {

    private FunctionObject functionTarget;

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private FunctionDispatchNode dispatchNode;

    protected CallNode(KSharpNode[] arguments) {
        super(arguments);
        this.dispatchNode = FunctionDispatchNodeGen.create();
    }


    public abstract CallTarget getCallTarget();

    @Override
    public Object execute(VirtualFrame frame) {
        if (functionTarget == null) {
            var rootNode = getCallTarget();
            functionTarget = new FunctionObject(rootNode);
        }
        var argumentValues = getArguments(frame);
        return dispatchNode.executeDispatch(functionTarget, argumentValues);
    }
}
