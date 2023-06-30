package org.ksharp.ir.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

public abstract class KSharpNode extends Node {
    public abstract Object execute(VirtualFrame frame);

}
