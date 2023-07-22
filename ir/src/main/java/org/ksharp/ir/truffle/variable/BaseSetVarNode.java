package org.ksharp.ir.truffle.variable;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.FrameSlotKind;
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
        frame.getFrameDescriptor().setSlotKind(getSlot(), FrameSlotKind.Byte);
        frame.setByte(getSlot(), value);
        return value;
    }

    @Specialization
    protected short doShort(VirtualFrame frame, short value) {
        frame.getFrameDescriptor().setSlotKind(getSlot(), FrameSlotKind.Int);
        frame.setInt(getSlot(), value);
        return value;
    }

    @Specialization
    protected int doInt(VirtualFrame frame, int value) {
        frame.getFrameDescriptor().setSlotKind(getSlot(), FrameSlotKind.Int);
        frame.setInt(getSlot(), value);
        return value;
    }

    @Specialization
    protected long doLong(VirtualFrame frame, long value) {
        frame.getFrameDescriptor().setSlotKind(getSlot(), FrameSlotKind.Long);
        frame.setLong(getSlot(), value);
        return value;
    }

    @Specialization
    protected float doFloat(VirtualFrame frame, float value) {
        frame.getFrameDescriptor().setSlotKind(getSlot(), FrameSlotKind.Float);
        frame.setFloat(getSlot(), value);
        return value;
    }

    @Specialization
    protected double doDouble(VirtualFrame frame, double value) {
        frame.getFrameDescriptor().setSlotKind(getSlot(), FrameSlotKind.Double);
        frame.setDouble(getSlot(), value);
        return value;
    }

    @Specialization
    protected Boolean doBoolean(VirtualFrame frame, boolean value) {
        frame.getFrameDescriptor().setSlotKind(getSlot(), FrameSlotKind.Boolean);
        frame.setBoolean(getSlot(), value);
        return value;
    }

    @Specialization
    protected Object doObject(VirtualFrame frame, Object value) {
        frame.getFrameDescriptor().setSlotKind(getSlot(), FrameSlotKind.Object);
        frame.setObject(getSlot(), value);
        return value;
    }

}
