package org.ksharp.common.annotation

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class KoverIgnore(val reason: String)
