package org.ksharp.ir.truffle;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.nodes.RootNode;

public abstract class KSharpRootNode extends RootNode {

    protected KSharpRootNode(TruffleLanguage<?> language) {
        super(language);
    }
    
}
