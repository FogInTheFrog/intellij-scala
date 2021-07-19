package org.jetbrains.plugins.scala
package lang
package psi
package api
package toplevel
package typedef

import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunction

/**
* @author Alexander Podkhalyuzin
* Date: 20.02.2008
*/
trait ScClass extends ScTypeDefinition with ScConstructorOwner {
  def tooBigForUnapply: Boolean = constructor.exists(_.parameters.length > 22)

  def getSyntheticImplicitMethod: Option[ScFunction]
}
