package org.jetbrains.plugins.scala
package lang
package psi
package impl
package toplevel
package typedef

import com.intellij.lang.ASTNode
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.psi.{PsiElement, PsiNamedElement}
import org.jetbrains.plugins.scala.caches.ModTracker

import javax.swing.Icon
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.icons.Icons
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenType
import org.jetbrains.plugins.scala.lang.parser.ScalaElementType
import org.jetbrains.plugins.scala.lang.psi.api.base.types.ScTypeElement
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.{ScParameter, ScParameters}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScGivenDefinition, ScMember}
import org.jetbrains.plugins.scala.lang.psi.stubs.ScTemplateDefinitionStub
import org.jetbrains.plugins.scala.lang.psi.stubs.elements.ScTemplateDefinitionElementType
import org.jetbrains.plugins.scala.macroAnnotations.{Cached, CachedInUserData}

class ScGivenDefinitionImpl(
  stub:      ScTemplateDefinitionStub[ScGivenDefinition],
  nodeType:  ScTemplateDefinitionElementType[ScGivenDefinition],
  node:      ASTNode,
  debugName: String
) extends ScTypeDefinitionImpl(stub, nodeType, node, debugName)
    with ScGivenImpl
    with ScGivenDefinition {

  override protected def baseIcon: Icon = Icons.CLASS // todo: better icon ?

  override protected def targetTokenType: ScalaTokenType = ScalaTokenType.GivenKeyword

  override def declaredElements: Seq[PsiNamedElement] = Seq(this)

  override def isObject: Boolean = typeParametersClause.isEmpty && parameters.isEmpty

  override def nameId: PsiElement = nameElement.getOrElse(extendsBlock)

  override def nameInner: String = {
    val explicitName = nameElement.map(_.getText)
    val typeElements = extendsBlock.templateParents.toSeq.flatMap(_.typeElements)

    explicitName
      .getOrElse(ScalaPsiUtil.generateGivenOrExtensionName(typeElements: _*))
  }

  @Cached(ModTracker.anyScalaPsiChange, this)
  override def clauses: Option[ScParameters] =
    findChild[ScParameters]

  override def parameters: Seq[ScParameter] =
    clauses.fold(Seq.empty[ScParameter])(_.params)

  @CachedInUserData(this, ModTracker.libraryAware(this))
  override def desugaredDefinitions: Seq[ScMember] =
    try {
      val supersText = extendsBlock.templateParents.fold("")(_.getText)

      if (isObject) {
        val text = s"implicit object $name extends $supersText"
        val obj  = ScalaPsiElementFactory.createTypeDefinitionWithContext(text, this.getContext, this)
        obj.originalGivenElement       = this
        obj.syntheticNavigationElement = this
        Seq(obj)
      } else {
        val typeParametersText = typeParametersClause.fold("")(_.getTextByStub)
        val parametersText     = clauses.fold("")(_.getText)

        val clsText        = s"class $name$typeParametersText$parametersText extends $supersText"
        val conversionText = s"implicit def $name$typeParametersText$parametersText: $name$typeParametersText = ???"

        val cls            = ScalaPsiElementFactory.createTypeDefinitionWithContext(clsText, this.getContext, this)
        cls.originalGivenElement       = this
        cls.syntheticNavigationElement = this

        val conversion     = ScalaPsiElementFactory.createDefinitionWithContext(conversionText, this.getContext, this)
        conversion.originalGivenElement       = this
        conversion.syntheticNavigationElement = this

        Seq(cls, conversion)
      }
    } catch {
      case p: ProcessCanceledException => throw p
      case _: Exception                => Seq.empty
    }
}
