package org.jetbrains.plugins.scala.lang.psi.implicits

import com.intellij.psi.{PsiElement, PsiNamedElement, ResolveState}
import org.jetbrains.plugins.scala.lang.resolve.ScalaResolveState.ResolveStateExt
import org.jetbrains.plugins.scala.lang.resolve.processor.ResolveProcessor
import org.jetbrains.plugins.scala.lang.resolve.{ResolveUtils, ScalaResolveResult, StdKinds}

final class ExtensionProcessor(place: PsiElement, name: String)
    extends ResolveProcessor(StdKinds.methodsOnly, place, name) {

  override protected def execute(
    namedElement: PsiNamedElement
  )(implicit
    state: ResolveState
  ): Boolean = {
    if (nameMatches(namedElement) && ResolveUtils.isExtensionMethod(namedElement)) {
      addResult(
        new ScalaResolveResult(
          namedElement,
          substitutor         = state.substitutor,
          importsUsed         = state.importsUsed,
          implicitConversion  = state.implicitConversion,
          implicitType        = state.implicitType,
          implicitScopeObject = state.implicitScopeObject,
          isExtension         = true
        )
      )
    }

    true
  }
}
