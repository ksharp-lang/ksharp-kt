package org.ksharp.common.annotation

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class KoverIgnore(val reason: String)
