package org.ksharp.ir.truffle.variable;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

@NodeField(name = "slot", type = int.class)
public abstract class BaseVarAccessNode extends KSharpNode {
    protected abstract int getSlot();

    @Specialization(guards = "frame.isInt(getSlot())")
    protected int doInt(VirtualFrame frame) {
        return frame.getInt(getSlot());
    }

    @Specialization(guards = "frame.isByte(getSlot())")
    protected byte doByte(VirtualFrame frame) {
        return frame.getByte(getSlot());
    }

    @Specialization(guards = "frame.isLong(getSlot())")
    protected long doLong(VirtualFrame frame) {
        return frame.getLong(getSlot());
    }

    @Specialization(guards = "frame.isFloat(getSlot())")
    protected float doFloat(VirtualFrame frame) {
        return frame.getFloat(getSlot());
    }

    @Specialization(guards = "frame.isDouble(getSlot())")
    protected double doDouble(VirtualFrame frame) {
        return frame.getDouble(getSlot());
    }

    @Specialization(guards = "frame.isBoolean(getSlot())")
    protected boolean doBoolean(VirtualFrame frame) {
        return frame.getBoolean(getSlot());
    }

    @Specialization(replaces = {"doInt", "doByte", "doLong", "doFloat", "doDouble", "doBoolean"})
    protected Object doObject(VirtualFrame frame) {
        return frame.getObject(getSlot());
    }

}
