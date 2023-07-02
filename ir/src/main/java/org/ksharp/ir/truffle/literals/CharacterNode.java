package org.ksharp.ir.truffle.literals;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

public class CharacterNode extends KSharpNode {
    
    private final char value;

    public CharacterNode(char value) {
        this.value = value;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return value;
    }
}
