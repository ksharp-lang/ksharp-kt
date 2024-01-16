package org.ksharp.ir

import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute

val Set<Attribute>.returnIfConstant: Set<Attribute>
    get() =
        if (this.contains(CommonAttribute.Constant)) this
        else emptySet()
