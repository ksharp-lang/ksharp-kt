package org.ksharp.ir.truffle.collections;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

import java.util.List;

public class ArrayNode extends KSharpNode {

    private final List<KSharpNode> nodes;

    public ArrayNode(List<KSharpNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return nodes.stream()
                .map(node -> node.execute(frame))
                .toArray();
    }
}
