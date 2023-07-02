package org.ksharp.ir.truffle.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.interop.TruffleObject;

public class FunctionObject implements TruffleObject {

    public final CallTarget callTarget;

    public FunctionObject(CallTarget callTarget) {
        this.callTarget = callTarget;
    }

    public Object execute(Object[] arguments) {
        return callTarget.call(arguments);
    }
}
