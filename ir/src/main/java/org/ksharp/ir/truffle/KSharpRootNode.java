package org.ksharp.ir.truffle;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.nodes.RootNode;

public abstract class KSharpRootNode extends RootNode {

    protected KSharpRootNode(TruffleLanguage<?> language, int slots) {
        super(language, createFrameDescriptor(slots));
    }

    private static FrameDescriptor createFrameDescriptor(int slots) {
        final var frameDescriptor = FrameDescriptor.newBuilder(slots);
        if (slots > 0)
            frameDescriptor.addSlots(slots, FrameSlotKind.Illegal);
        return frameDescriptor.build();
    }

}
