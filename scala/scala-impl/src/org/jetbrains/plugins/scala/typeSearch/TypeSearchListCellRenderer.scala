package org.jetbrains.plugins.scala.typeSearch

import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.psi.PsiMethod
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef.ScClassImpl
import org.jetbrains.plugins.scala.lang.psi.light.ScFunctionWrapper
import org.jetbrains.plugins.scala.lang.psi.types.TypePresentationContext


class TypeSearchListCellRenderer() extends PsiElementListCellRenderer[PsiMethod] {

  def wrapInBrackets(s: String): String = {
    "(" + s + ")"
  }

  def getScalaParameters(element: PsiMethod): String = {
    element
      .asInstanceOf[ScFunctionWrapper]
      .delegate
      .parameters
      .map(x => x.typeElement.get.`type`().getOrNothing)
      .mkString(", ")
  }

  def getScalaReturnType(element: PsiMethod): String = {
    element.asInstanceOf[ScFunctionWrapper]
      .delegate
      .returnType
      .get
      .presentableText(TypePresentationContext.emptyContext)
  }

  def renderMethodTypes(element: PsiMethod): String = {
    try {
      wrapInBrackets(getScalaParameters(element))
        .concat(" => ")
        .concat(getScalaReturnType(element))
    }
    catch {
      case _: Throwable =>
        wrapInBrackets(
          element
            .getHierarchicalMethodSignature
            .getParameterTypes
            .map(x => x.getPresentableText)
            .mkString(", ")
        )
          .concat(" => ")
          .concat(
            element
              .getReturnType
              .getPresentableText
          )
    }
  }

  override def getElementText(element: PsiMethod): String = element.getName + renderMethodTypes(element)

  override def getContainerText(element: PsiMethod, name: String): String = {
    try {
      element
        .asInstanceOf[ScFunctionWrapper]
        .delegate
        .getContainingClass
        .asInstanceOf[ScClassImpl]
        .getPath
    }
    catch {
      case _: Throwable => element.getContainingClass.getName
    }
  }
}