package org.jetbrains.plugins.scala.lang.dfa.controlFlow.invocations

import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import org.jetbrains.plugins.scala.lang.dfa.controlFlow.invocations.arguments.Argument
import org.jetbrains.plugins.scala.lang.dfa.controlFlow.transformations.ExpressionTransformer
import org.jetbrains.plugins.scala.lang.psi.api.expr.{MethodInvocation, ScExpression, ScReferenceExpression}

import scala.reflect.ClassTag

object InvocationExtractors {

  def extractInvocationUnderMarker(file: PsiFile, ranges: Seq[TextRange]): ScExpression = {
    val methodInvocationUnderMarker = extractElementOfType[MethodInvocation](file, ranges)
    val referenceExpressionUnderMarker = extractElementOfType[ScReferenceExpression](file, ranges)

    Option(methodInvocationUnderMarker)
      .orElse(Option(referenceExpressionUnderMarker))
      .getOrElse(throw new IllegalArgumentException("There is no invocation under the marker"))
  }

  def extractElementOfType[A <: PsiElement : ClassTag](actualFile: PsiFile, ranges: Seq[TextRange]): A = {
    val range = ranges.head
    val start = range.getStartOffset
    val end = range.getEndOffset
    val runtimeClass = implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]]

    if (start == end) PsiTreeUtil.getNonStrictParentOfType(actualFile.findElementAt(start), runtimeClass)
    else PsiTreeUtil.findElementOfClassAtRange(actualFile, start, end, runtimeClass)
  }

  def extractExpressionFromArgument(argument: Argument): ScExpression = argument.content match {
    case expressionTransformer: ExpressionTransformer => expressionTransformer.transformedExpression
    case _ => throw new IllegalArgumentException(s"Argument is not an expression: $argument")
  }
}
