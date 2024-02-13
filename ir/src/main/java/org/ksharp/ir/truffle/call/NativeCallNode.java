package org.ksharp.ir.truffle.call;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.CallNotFound;
import org.ksharp.ir.KValue;
import org.ksharp.ir.NativeCall;
import org.ksharp.ir.truffle.KSharpNode;
import org.ksharp.typesystem.types.Type;

import java.lang.reflect.InvocationTargetException;

public abstract class NativeCallNode extends BaseCallNode {

    private final String functionClass;
    private NativeCall call;

    protected NativeCallNode(String functionClass, KSharpNode[] arguments, Type returnType) {
        super(arguments, returnType);
        this.functionClass = functionClass;
    }

    public NativeCall getCall() {
        try {
            if (call == null) {
                call = (NativeCall) Class.forName(functionClass).getConstructor().newInstance();
                return call;
            }
            return call;
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new CallNotFound(e);
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        var argumentValues = getArguments(frame).getSecond();
        return KValue.wrap(getCall().execute(argumentValues), returnType);
    }
}
