package org.ksharp.ir.truffle.call;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;
import org.ksharp.ir.truffle.runtime.FunctionObject;
import org.ksharp.typesystem.types.Type;

public abstract class CallNode extends BaseCallNode {

    private FunctionObject functionTarget;

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private FunctionDispatchNode dispatchNode;

    protected CallNode(KSharpNode[] arguments) {
        super(arguments);
        this.dispatchNode = FunctionDispatchNodeGen.create();
    }


    public abstract CallTarget getCallTarget(Type firstArgument);

    @Override
    public Object execute(VirtualFrame frame) {
        var argumentValues = getArguments(frame);
        if (functionTarget == null) {
            var rootNode = getCallTarget(argumentValues.getFirst());
            functionTarget = new FunctionObject(rootNode);
        }
        return dispatchNode.executeDispatch(functionTarget, argumentValues.getSecond());
    }
}
