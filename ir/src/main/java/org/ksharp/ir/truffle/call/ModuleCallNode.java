package org.ksharp.ir.truffle.call;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.Call;
import org.ksharp.ir.KValue;
import org.ksharp.ir.truffle.KSharpNode;
import org.ksharp.typesystem.types.Type;

public abstract class ModuleCallNode extends BaseCallNode {

    private Call call;

    protected ModuleCallNode(KSharpNode[] arguments, Type returnType) {
        super(arguments, returnType);
    }

    protected abstract Call getCall();

    @Override
    public Object execute(VirtualFrame frame) {
        if (call == null) {
            call = getCall();
        }
        var argumentValues = getArguments(frame).getSecond();
        return KValue.wrap(call.execute(argumentValues), returnType);
    }
}
