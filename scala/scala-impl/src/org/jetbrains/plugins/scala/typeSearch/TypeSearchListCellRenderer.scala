package org.jetbrains.plugins.scala.typeSearch

import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.psi.PsiMethod
import org.virtuslab.inkuire.engine.common.model.ExternalSignature


class TypeSearchListCellRenderer() extends PsiElementListCellRenderer[PsiMethod] {

  override def getElementText(element: PsiMethod): String = element.getName

  override def getContainerText(element: PsiMethod, name: String): String = {
    element.
      getContainingClass
      .getName + " => "
      .concat(
        element
          .getHierarchicalMethodSignature
          .getParameterTypes
          .map(x => x.getPresentableText + " => ")
          .mkString
          .concat(
            element
              .getReturnType
              .getPresentableText
          )
      )
  }
}