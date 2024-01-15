package org.ksharp.ir.truffle.call;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

import java.lang.reflect.InvocationTargetException;

public abstract class NativeCallNode extends BaseCallNode {

    private static class NativeCallNotFound extends RuntimeException {
        public NativeCallNotFound(Exception exception) {
            super(exception);
        }
    }

    private final String functionClass;
    private NativeCall call;

    protected NativeCallNode(String functionClass, KSharpNode[] arguments) {
        super(arguments);
        this.functionClass = functionClass;
    }

    public NativeCall getNativeCall() {
        try {
            if (call == null) {
                call = (NativeCall) Class.forName(functionClass).getConstructor().newInstance();
                return call;
            }
            return call;
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new NativeCallNotFound(e);
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        var argumentValues = getArguments(frame);
        return getNativeCall().execute(argumentValues);
    }
}
