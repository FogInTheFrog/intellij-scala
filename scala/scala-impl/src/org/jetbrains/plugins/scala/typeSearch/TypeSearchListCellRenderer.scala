package org.jetbrains.plugins.scala.typeSearch

import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.psi.{PsiMethod, PsiSubstitutor}
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef.ScClassImpl
import org.jetbrains.plugins.scala.lang.psi.light.ScFunctionWrapper
import org.jetbrains.plugins.scala.lang.psi.types.TypePresentationContext


class TypeSearchListCellRenderer() extends PsiElementListCellRenderer[PsiMethod] {

  def renderMethodTypes(element: PsiMethod): String = {
    try {
      "(".concat(
        element
          .asInstanceOf[ScFunctionWrapper]
          .delegate
          .parameters
          .map(
            x => x.typeElement.get.`type`().getOrNothing
          )
          .mkString(", "))
        .concat(")")
        .concat(" => ")
        .concat(
          element.asInstanceOf[ScFunctionWrapper]
            .delegate
            .returnType
            .get
            .presentableText(TypePresentationContext.emptyContext)
        )
    }
    catch {
      case _: Throwable => "(" + element.getContainingClass.getName + " => ".concat(element
        .getHierarchicalMethodSignature
        .getParameterTypes.map(x => x.getPresentableText + " => ").mkString.concat(element.getReturnType
        .getPresentableText)) + ")"
    }
  }

  override def getElementText(element: PsiMethod): String = element.getName + renderMethodTypes(element)

  override def getContainerText(element: PsiMethod, name: String): String = {
    // element.asInstanceOf[ScFunctionWrapper]
    // element.asInstanceOf[ScFunctionWrapper].delegate.returnType.get.presentableText(TypePresentationContext.emptyContext)
    // element.asInstanceOf[ScFunctionWrapper].delegate.parameters.map(x => x.typeElement.toString)
    // element.asInstanceOf[ScFunctionWrapper].delegate.parameters.map(x => x.typeElement.get.`type`())
    try {
      element.asInstanceOf[ScFunctionWrapper].delegate.getContainingClass.asInstanceOf[ScClassImpl].getPath
    }
    catch {
      case _: Throwable => element.getContainingClass.getName
    }
    // element.asInstanceOf[ScFunctionWrapper].delegate.parameters.map(x => x.typeElement.get.`type`().get.toString)

//    renderMethodTypes(element)


    //      element.asInstanceOf[ScFunctionWrapper].delegate.parameters.map(x => x.typeElement.get.`type`().getOrNothing)
    //        .mkString(", ") +
    //      " => " +
    //      element.asInstanceOf[ScFunctionWrapper].delegate.returnType.get.presentableText(TypePresentationContext.emptyContext)

    //    element
    //      .getSignature(PsiSubstitutor.EMPTY)
    //      .getParameterTypes
    //      .map(x => x.getPresentableText + " => ")
    //      .mkString
    //      .concat(
    //        element
    //          .getReturnType
    //          .getPresentableText
    //      )

//        element
//          .getContainingClass
//          .getName + " => "
//          .concat(
//            element
//              .getHierarchicalMethodSignature
//              .getParameterTypes
//              .map(x => x.getPresentableText + " => ")
//              .mkString
//              .concat(
//                element
//                  .getReturnType
//                  .getPresentableText
//              )
//          )
  }
}