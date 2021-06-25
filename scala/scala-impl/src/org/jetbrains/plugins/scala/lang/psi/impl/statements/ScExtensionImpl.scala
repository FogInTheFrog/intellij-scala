package org.jetbrains.plugins.scala.lang.psi.impl.statements

import com.intellij.lang.ASTNode
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.{PsiClass, PsiElement, ResolveState}
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.scala.lang.parser.ScalaElementType
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.lang.psi.api.base.types.ScTypeElement
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.{ScParameter, ScParameterClause, ScParameters}
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScExtension, ScExtensionDefinitions, ScFunctionDefinition}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScTypeParametersOwner
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaStubBasedElementImpl
import org.jetbrains.plugins.scala.lang.psi.stubs.ScExtensionStub


class ScExtensionImpl(@Nullable stub: ScExtensionStub, @Nullable node: ASTNode)
    extends ScalaStubBasedElementImpl(stub, ScalaElementType.EXTENSION, node)
    with ScExtension
    with ScTypeParametersOwner {

  override def toString: String = "Extension on " + targetTypeElement.fold("<unknown>")(_.getText)

  override def targetTypeElement: Option[ScTypeElement] =
    parameters.headOption.flatMap(_.typeElement)

  override def extensionMethods: Seq[ScFunctionDefinition] =
    extensionBody.fold(Seq.empty[ScFunctionDefinition])(_.functions)

  override def parameters: Seq[ScParameter] =
    clauses.toSeq.flatMap(_.clauses.flatMap(_.parameters))

  override def clauses: Option[ScParameters]                 = findChild[ScParameters]
  override def extensionBody: Option[ScExtensionDefinitions] = findChild[ScExtensionDefinitions]

  override def getContainingClass: PsiClass = null

  override def hasModifierProperty(name: String): Boolean = false

  def effectiveParameterClauses: Seq[ScParameterClause] =
    allClauses ++ clauses.flatMap(
      ScalaPsiUtil.syntheticParamClause(this, _, isClassParameter = false)()
    )

  override def processDeclarations(
    processor:  PsiScopeProcessor,
    state:      ResolveState,
    lastParent: PsiElement,
    place:      PsiElement
  ): Boolean = {
    if (!super[ScTypeParametersOwner].processDeclarations(processor, state, lastParent, place))
      return false

    for {
      clause <- effectiveParameterClauses
      param  <- clause.effectiveParameters
    } {
      ProgressManager.checkCanceled()
      if (!processor.execute(param, state)) return false
    }

    true
  }
}

