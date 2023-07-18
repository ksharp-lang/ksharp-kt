package org.ksharp.ir.truffle.variable;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;
import org.ksharp.ir.truffle.PreludeTypeSystem;

@NodeField(name = "slot", type = int.class)
@NodeChild("value")
@TypeSystemReference(PreludeTypeSystem.class)
public abstract class BaseSetVarNode extends KSharpNode {

    protected abstract int getSlot();

    @Specialization
    protected byte doByte(VirtualFrame frame, byte value) {
        frame.setByte(getSlot(), value);
        return value;
    }

    @Specialization
    protected short doShort(VirtualFrame frame, short value) {
        frame.setInt(getSlot(), value);
        return value;
    }

    @Specialization
    protected int doInt(VirtualFrame frame, int value) {
        frame.setInt(getSlot(), value);
        return value;
    }

    @Specialization
    protected long doLong(VirtualFrame frame, long value) {
        frame.setLong(getSlot(), value);
        return value;
    }

    @Specialization
    protected float doFloat(VirtualFrame frame, float value) {
        frame.setFloat(getSlot(), value);
        return value;
    }

    @Specialization
    protected double doDouble(VirtualFrame frame, double value) {
        frame.setDouble(getSlot(), value);
        return value;
    }

    @Specialization
    protected Boolean doBoolean(VirtualFrame frame, boolean value) {
        frame.setBoolean(getSlot(), value);
        return value;
    }

    @Specialization
    protected Object doObject(VirtualFrame frame, Object value) {
        frame.setObject(getSlot(), value);
        return value;
    }

}
