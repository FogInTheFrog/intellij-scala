package org.jetbrains.plugins.scala.typeSearch

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.openapi.extensions.ExtensionPointName

import java.util


object Extensions {
  private val EXTENSION_POINT_NAME = "org.jetbrains.plugins.scala.SeachStdFunctionByTypeContributor"
  private val extensionPoints = ExtensionPointName.create[ChooseByNameContributor](EXTENSION_POINT_NAME)

  def getExtensions: util.List[ChooseByNameContributor] = extensionPoints.getExtensionList
}