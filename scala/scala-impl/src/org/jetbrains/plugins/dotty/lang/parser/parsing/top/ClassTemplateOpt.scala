package org.jetbrains.plugins.dotty.lang.parser.parsing.top

import org.jetbrains.plugins.dotty.lang.parser.parsing.top.template.TemplateBody

/**
  * @author adkozlov
  */
object ClassTemplateOpt extends org.jetbrains.plugins.scala.lang.parser.parsing.top.ClassTemplateOpt {
  override protected def templateBody = TemplateBody
  override protected def earlyDef = EarlyDef
}
