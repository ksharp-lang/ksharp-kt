package org.ksharp.ir.truffle.call;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.KValue;
import org.ksharp.ir.truffle.FunctionNode;
import org.ksharp.ir.truffle.KSharpNode;
import org.ksharp.ir.truffle.runtime.FunctionObject;
import org.ksharp.typesystem.types.Type;

public class LambdaCallNode extends BaseCallNode {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private FunctionDispatchNode dispatchNode;

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode lambda;

    private FunctionObject functionTarget;

    public LambdaCallNode(KSharpNode lambda, KSharpNode[] arguments, Type returnType) {
        super(arguments, returnType);
        this.dispatchNode = FunctionDispatchNodeGen.create();
        this.lambda = lambda;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        var argumentValues = getArguments(frame);
        if (functionTarget == null) {
            var funNode = (FunctionNode) KValue.value(lambda.execute(frame));
            functionTarget = new FunctionObject(funNode.getCallTarget());
        }
        return KValue.wrap(
                dispatchNode.executeDispatch(functionTarget, argumentValues.getSecond()),
                returnType
        );
    }
}
