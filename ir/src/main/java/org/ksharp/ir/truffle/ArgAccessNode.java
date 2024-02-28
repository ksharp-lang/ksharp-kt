package org.ksharp.ir.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.KValue;

import java.util.Map;

public class ArgAccessNode extends KSharpNode {

    private final int index;
    private final String captureName;

    public ArgAccessNode(int index, String captureName) {
        this.index = index;
        this.captureName = captureName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object execute(VirtualFrame frame) {
        if (captureName != null) {
            var context = (Map<String, Object>) KValue.value(frame.getObject(0));
            return context.get(captureName);
        }
        return frame.getArguments()[index];
    }
}
