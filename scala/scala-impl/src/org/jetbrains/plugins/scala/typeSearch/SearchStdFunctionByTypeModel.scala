//package org.jetbrains.plugins.scala.typeSearch
//
//
//import com.intellij.history.integration.ui.views.RevisionsList.MyCellRenderer
//import com.intellij.ide.util.gotoByName.ChooseByNameModel
//import com.intellij.openapi.progress.ProgressIndicator
//import com.intellij.openapi.util.NlsContexts
//import com.intellij.util.indexing.FindSymbolParameters
//import org.jetbrains.annotations.Nls
//
//import java.awt.{Color, Component}
//import javax.swing.{JLabel, JList, ListCellRenderer}
//
//class SearchStdFunctionByTypeModel extends ChooseByNameModel {
//
//  class MyCellRenderer1 extends JLabel with ListCellRenderer[StdFunctionRef] {
//    setOpaque(true)
//
//    override def getListCellRendererComponent(list: JList[_ <: StdFunctionRef], value: StdFunctionRef, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
//      setText(value.name)
//      var background: Color = null
//      var foreground: Color = null
//      // check if this cell represents the current DnD drop location
//      val dropLocation = list.getDropLocation
//      if (dropLocation != null && !dropLocation.isInsert && dropLocation.getIndex == index) {
//        background = Color.BLUE
//        foreground = Color.WHITE
//        // check if this cell is selected
//      }
//      else if (isSelected) {
//        background = Color.RED
//        foreground = Color.WHITE
//        // unselected, and not the DnD drop location
//      }
//      else {
//        background = Color.WHITE
//        foreground = Color.BLACK
//      }
//
//      setBackground(background)
//      setForeground(foreground)
//      this
//    }
//  }
//
////  def getItemProvider: SearchStdFunctionsByTypeItemProvider = {
////    new SearchStdFunctionsByTypeItemProvider
////  }
//
//  @Nls(capitalization = Nls.Capitalization.Sentence)
//  override def getPromptText = "Enter the type"
//
//  @NlsContexts.Label
//  override def getNotInMessage = "No matches found"
//
//  @NlsContexts.Label
//  override def getNotFoundMessage = "Type not found"
//
//  @NlsContexts.Label
//  override def getCheckBoxName: String = null
//
//  override def loadInitialCheckBoxState = false
//
//  override def saveInitialCheckBoxState(state: Boolean): Unit = {
//  }
//
//  override def getSeparators = new Array[String](0)
//
//  override def getFullName(element: Any): String = element.toString
//
//  override def willOpenEditor = false
//
//  override def useMiddleMatching(): Boolean = true
//
////  override def getListCellRenderer: ListCellRenderer[_] = new MyCellRenderer
//
//  override def getNames(checkBoxState: Boolean): Array[String] = Array("elementy", "ekement")
//
////  override def getElementsByName(name: String, parameters: FindSymbolParameters, canceled: ProgressIndicator): Array[StdFunctionRef] = {
////    Array("mockupFunctionA", "mockupFunctionB", "mockupFunctionC")
////  }
//
//  override def getHelpId: String = "HELP ID" // todo
//
//  override def getElementsByName(name: String, checkBoxState: Boolean, pattern: String): Array[AnyRef] =
//    Array()
//
//  override def getElementName(element: Any): String = "elementx"
//
//  override def getListCellRenderer: ListCellRenderer[StdFunctionRef] = new MyCellRenderer1
//}
//
//
//// to jest model w kotlinie z restful helpera zaimplementowany w scali
////def this (project: Project, contributors: List[ChooseByNameContributor] ) {
////  this ()
////  super (project, contributors)
////  }
////
////  override protected def filterValueFor (item: NavigationItem): String = {
////  return null
////  }
////
////  @Nls (capitalization = Nls.Capitalization.Sentence) override def getPromptText: String = {
////  return "Enter the type"
////  }
////
////  @NlsContexts.Label override def getNotInMessage: String = {
////  return "No matches found"
////  }
////
////  @NlsContexts.Label override def getNotFoundMessage: String = {
////  return "Type not found"
////  }
////
////  @NlsContexts.Label override def getCheckBoxName: String = {
////  return null
////  }
////
////  override def loadInitialCheckBoxState: Boolean = {
////  return false
////  }
////
////  override def saveInitialCheckBoxState (state: Boolean): Unit = {
////  }
////
////  override def getSeparators: Array[String] = {
////  return new Array[String] (0)
////  }
////
////  override def getFullName (element: Any): String = {
////  return null
////  }
////
////  override def willOpenEditor: Boolean = {
////  return false
////  }