package br.com.devsrsouza.intellij.dropboxfocus.psi

import br.com.devsrsouza.intellij.dropboxfocus.services.GRADLE_PROPERTY_SET_FUNCTION_NAME
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType

internal fun PsiElement.findKotlinFunction(
    functionName: String,
    predicate: (KtCallExpression, arguments: List<KtValueArgument>?) -> Boolean = { _, _ -> true }
): KtCallExpression? = findDescendantOfType<KtCallExpression> {
    val arguments = it.children.asSequence().filterIsInstance<KtValueArgumentList>()
        .firstOrNull()?.arguments
    it.isFunctionName(functionName) && predicate(it, arguments)
}

internal fun PsiElement.forEachKotlinFunction(
    functionName: String,
    predicate: (KtCallExpression, arguments: List<KtValueArgument>?) -> Unit = { _, _ -> }
) {
    findKotlinFunction(functionName) { it, argument ->
        predicate(it, argument)
        false
    }
}

internal fun KtCallExpression.isFunctionName(functionName: String): Boolean {
    return children.asSequence().filterIsInstance<KtReferenceExpression>()
        .any { it.text == functionName }
}

internal fun KtCallExpression.findGradlePropertySetValueOnCallback(
    propertyName: String
): String? {
    var result: String? = null
    // search for expression with a Dot ex: property.set()
    findDescendantOfType<KtDotQualifiedExpression> {
        // checks if the property name at the left is [propertyName]
        val isThePropertyWeAreLookingFor = it.children.asSequence().filterIsInstance<KtReferenceExpression>()
            .any { it.text == propertyName }

        if (isThePropertyWeAreLookingFor) {
            // search for the set("") function call expression
            it.findKotlinFunction(GRADLE_PROPERTY_SET_FUNCTION_NAME) { it, arguments ->
                // If it is, we will get the first parameter value
                // ex: set("something") => something
                result = arguments?.getFirstArgumentAsLiteralString()

                true
            } != null
        } else false
    }

    return result
}

internal fun List<KtValueArgument>.getFirstArgumentAsLiteralString(): String? =
    firstOrNull()?.text?.removeSurrounding("\"")