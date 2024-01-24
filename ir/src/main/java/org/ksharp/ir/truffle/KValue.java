package org.ksharp.ir.truffle;

import org.ksharp.typesystem.types.Type;

public class KValue {
    private Object value;
    private Type type;

    public KValue(Object value, Type type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }
}
