package org.ksharp.ir.truffle.call;

import org.ksharp.typesystem.attributes.Attribute;

import java.util.Set;

public interface NativeCall {

    Set<Attribute> getAttributes(Set<Attribute> attributes);

    Object execute(Object... arguments);

}
