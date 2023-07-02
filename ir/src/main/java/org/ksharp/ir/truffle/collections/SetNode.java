package org.ksharp.ir.truffle.collections;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

import java.util.List;
import java.util.stream.Collectors;

public class SetNode extends KSharpNode {

    private final List<KSharpNode> nodes;

    public SetNode(List<KSharpNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return nodes.stream()
                .map(node -> node.execute(frame))
                .collect(Collectors.toSet());
    }
}
