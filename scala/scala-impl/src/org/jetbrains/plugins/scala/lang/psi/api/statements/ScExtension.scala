package org.jetbrains.plugins.scala
package lang
package psi
package api
package statements

import org.jetbrains.plugins.scala.lang.psi.api.base.types.ScTypeElement
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScParameterClause
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScTypeParametersOwner
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScDocCommentOwner, ScMember}

trait ScExtension extends ScTypeParametersOwner
  with ScParameterOwner
  with ScDocCommentOwner
  with ScCommentOwner
  with ScMember {

  def targetTypeElement: Option[ScTypeElement]
  def extensionMethods: Seq[ScFunctionDefinition]
  def effectiveParameterClauses: Seq[ScParameterClause]
}
