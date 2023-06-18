package org.ksharp.lsp.capabilities.semantic_tokens

import org.eclipse.lsp4j.SemanticTokenTypes
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.nodes.AnnotationNode
import org.ksharp.nodes.AnnotationNodeLocations
import org.ksharp.nodes.AttributeLocation

private fun List<AttributeLocation>.semanticTokens(encoder: TokenEncoder) {
    forEach {
        if (it.keyLocation != null) {
            encoder.register(it.keyLocation!!, SemanticTokenTypes.Parameter)
            encoder.register(it.operator!!, SemanticTokenTypes.Operator)
        }
        when (it.value) {
            String::class.java, java.lang.Boolean::class.java -> encoder.register(
                it.valueLocation.cast(),
                SemanticTokenTypes.String
            )

            List::class.java -> it.valueLocation.cast<List<AttributeLocation>>().semanticTokens(encoder)
            AnnotationNode::class.java -> it.valueLocation.cast<AnnotationNodeLocations>()
                .semanticTokens(encoder)

            else -> Unit
        }
    }
}

private fun AnnotationNodeLocations.semanticTokens(encoder: TokenEncoder) {
    val annotationLabelLocation =
        Location(
            altLocation.start,
            name.end
        )
    encoder.register(annotationLabelLocation, SemanticTokenTypes.Decorator)
    attrs.semanticTokens(encoder)
}


fun AnnotationNode.semanticTokens(encoder: TokenEncoder) {
    locations.semanticTokens(encoder)
}
