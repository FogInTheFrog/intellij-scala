package org.jetbrains.plugins.scala.lang.dfa.cfg.transformations

import com.intellij.psi.PsiElement

case class TransformationFailedException(element: PsiElement, reason: String = "")
  extends Exception(s"Transformation failed for element $element. $reason")
