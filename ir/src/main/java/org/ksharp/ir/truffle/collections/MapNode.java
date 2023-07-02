package org.ksharp.ir.truffle.collections;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.IrPair;
import org.ksharp.ir.truffle.KSharpNode;

import java.util.HashMap;
import java.util.List;

public class MapNode extends KSharpNode {

    private final List<IrPair> nodes;

    public MapNode(List<IrPair> nodes) {
        this.nodes = nodes;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        var result = new HashMap<>();
        for (var entry : nodes) {
            result.put(((KSharpNode) entry.getFirst()).execute(frame), ((KSharpNode) entry.getSecond()).execute(frame));
        }
        return result;
    }
}
