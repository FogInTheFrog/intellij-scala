package org.jetbrains.plugins.scala.typeSearch

import com.intellij.ide.util.gotoByName.ChooseByNameModel
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.NlsContexts
import org.jetbrains.annotations.Nls
import com.intellij.util.indexing.FindSymbolParameters

import javax.swing.ListCellRenderer

class SearchStdFunctionByTypeModel extends ChooseByNameModel{

  def getItemProvider: SearchStdFunctionsByTypeItemProvider = {
    new SearchStdFunctionsByTypeItemProvider
  }


  @Nls(capitalization = Nls.Capitalization.Sentence)
  override def getPromptText = "Enter the type"

  @NlsContexts.Label
  override def getNotInMessage = "No matches found"

  @NlsContexts.Label
  override def getNotFoundMessage = "Type not found"

  @NlsContexts.Label
  override def getCheckBoxName: String = null

  override def loadInitialCheckBoxState = false

  override def saveInitialCheckBoxState(state: Boolean): Unit = {
  }

  override def getSeparators = new Array[String](0)

  override def getFullName(element: Any): String = null

  override def willOpenEditor = false

  override def getListCellRenderer: ListCellRenderer[_] = getListCellRenderer

  override def getNames(checkBoxState: Boolean): Array[String] = ???

  def getElementsByName(name: String, parameters: FindSymbolParameters, canceled: ProgressIndicator): Array[AnyRef] = {
    Array[AnyRef]
  }

  override def getElementName(element: Any): String = element match {
    case  =>
  }

  override def getHelpId: String = ???

  override def useMiddleMatching(): Boolean = ???
}

// to jest model w kotlinie z restful helpera zaimplementowany w scali
//def this (project: Project, contributors: List[ChooseByNameContributor] ) {
//  this ()
//  super (project, contributors)
//  }
//
//  override protected def filterValueFor (item: NavigationItem): String = {
//  return null
//  }
//
//  @Nls (capitalization = Nls.Capitalization.Sentence) override def getPromptText: String = {
//  return "Enter the type"
//  }
//
//  @NlsContexts.Label override def getNotInMessage: String = {
//  return "No matches found"
//  }
//
//  @NlsContexts.Label override def getNotFoundMessage: String = {
//  return "Type not found"
//  }
//
//  @NlsContexts.Label override def getCheckBoxName: String = {
//  return null
//  }
//
//  override def loadInitialCheckBoxState: Boolean = {
//  return false
//  }
//
//  override def saveInitialCheckBoxState (state: Boolean): Unit = {
//  }
//
//  override def getSeparators: Array[String] = {
//  return new Array[String] (0)
//  }
//
//  override def getFullName (element: Any): String = {
//  return null
//  }
//
//  override def willOpenEditor: Boolean = {
//  return false
//  }