package org.ksharp.ir.truffle;

import com.oracle.truffle.api.TruffleLanguage;

@TruffleLanguage.Registration(id = KSharpLanguage.ID, name = "KSharp")
public class KSharpLanguage extends TruffleLanguage<Void> {
    public static final String ID = "ksharp";

    @Override
    protected Void createContext(Env env) {
        return null;
    }

}
