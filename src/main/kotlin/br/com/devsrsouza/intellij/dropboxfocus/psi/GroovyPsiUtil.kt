package br.com.devsrsouza.intellij.dropboxfocus.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression

internal fun PsiFile.findGroovyHighOrderFunction(functionName: String): GrMethodCallExpression? =
    findDescendantOfType<GrMethodCallExpression> {
        it.isFunctionName(functionName)
    }

internal fun PsiElement.findGroovyMethodCall(
    functionName: String,
    callback: (arguments: Array<GroovyPsiElement>) -> Boolean = { true }
): GrMethodCall? {
    // This will cover both GrMethodCallExpression or GrApplicationStatement
    // that difference by it format, for example
    // id("something") -> id 'something'
    // we cant use `children` here because if the declaration is something like:
    // id("something") version "version", then the GrMethodCall that we want
    // will be in a lower tree
    return findDescendantOfType<GrMethodCall> {
        if (it.isFunctionName(functionName)) {
            val arguments = it.getFunctionArguments()

            if (arguments != null) {
                callback(arguments)
            } else false
        } else true
    }
}

internal fun GrMethodCall.getFunctionArguments(): Array<GroovyPsiElement>? =
    children.asSequence().filterIsInstance<GrArgumentList>()
        .firstOrNull()?.allArguments

internal fun PsiElement.forEachGroovyMethodCall(
    functionName: String,
    callback: (arguments: Array<GroovyPsiElement>) -> Unit
) {
    findGroovyMethodCall(functionName) {
        callback(it)
        false
    }
}

internal fun Array<GroovyPsiElement>.getFirstArgumentAsLiteralString(): String? =
    firstOrNull()?.findDescendantOfType<GrLiteral>()?.text
        ?.removeSurroundingQuotes()

internal fun GrMethodCall.isFunctionName(functionName: String): Boolean =
    children.asSequence().filterIsInstance<GrReferenceExpression>()
        .any { it.text == functionName }

internal fun String.removeSurroundingQuotes(): String = removeSurrounding("\"")
    .removeSurrounding("'")