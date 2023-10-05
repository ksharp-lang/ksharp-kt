package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec

class ImplSemanticTest : StringSpec({
    "Trait not defined" {}
    "Not allow duplicate Impls" {}
    "Not allow duplicate methods in Impls" {}
    "Missing method implementation in Impls" {}
    "Valid Impl" {}
    "Impl with missing method, but it has default implementation in trait" {}
    "Error in method impl" {}
})
