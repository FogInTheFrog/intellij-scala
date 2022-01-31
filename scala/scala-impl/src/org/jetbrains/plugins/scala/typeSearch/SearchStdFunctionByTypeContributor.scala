package org.jetbrains.plugins.scala.typeSearch

import com.intellij.ide.actions.searcheverywhere.{FoundItemDescriptor, SearchEverywhereContributor, SearchEverywhereContributorFactory, WeightedSearchEverywhereContributor}
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.util.Processor
import org.jetbrains.plugins.scala.typeSearch.SearchStdFunctionByTypeContributor.inkuireService
import org.virtuslab.inkuire.engine.common.model.ITID

import java.awt.{Color, Component}
import java.io.File
import javax.swing.{JLabel, JList, ListCellRenderer}
import scala.language.postfixOps

class StdFunctionRef(val name: String, val packageName: String) {
}

class MyCellRenderer() extends JLabel with ListCellRenderer[StdFunctionRef] {
  setOpaque(true)

  override def getListCellRendererComponent(list: JList[_ <: StdFunctionRef], value: StdFunctionRef, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
    setText(value.name + ":   (" + value.packageName + ")")
    var background: Color = null
    var foreground: Color = null
    // check if this cell represents the current DnD drop location
    val dropLocation = list.getDropLocation
    if (dropLocation != null && !dropLocation.isInsert && dropLocation.getIndex == index) {
      background = Color.BLUE
      foreground = Color.WHITE
      // check if this cell is selected
    }
    else if (isSelected) {
      background = Color.PINK
      foreground = Color.DARK_GRAY
      // unselected, and not the DnD drop location
    }
    else {
      background = null
      foreground = Color.LIGHT_GRAY
    }

    setBackground(background)
    setForeground(foreground)
    this
  }
}

object SearchStdFunctionByTypeContributor {
  val file = new File("./scala/scala-impl/resources/inkuireTypeSearch")
  var inkuireService = new InkuireService(file.toURI.toURL.toString())

  class Factory extends SearchEverywhereContributorFactory[AnyRef] {
    override def createContributor(initEvent: AnActionEvent): SearchEverywhereContributor[AnyRef] =
      (new SearchStdFunctionByTypeContributor).asInstanceOf[SearchEverywhereContributor[AnyRef]]
    // TODO: discuss
    // override def getTab: SearchEverywhereTabDescriptor = SearchEverywhereTabDescriptor.IDE

  }
}

class SearchStdFunctionByTypeContributor extends WeightedSearchEverywhereContributor[StdFunctionRef] {
  private val LOG = Logger.getInstance(classOf[SearchStdFunctionByTypeContributor])
  val cellRenderer = new MyCellRenderer

  override def getElementsRenderer: ListCellRenderer[_ >: Any] = (new MyCellRenderer).asInstanceOf[ListCellRenderer[_ >: Any]]

  override def getSearchProviderId: String = getClass.getSimpleName

  override def getGroupName: String = "Type Search"

  override def getSortWeight = 1000

  // default is false
  override def isShownInSeparateTab: Boolean = true

  // default is true
  override def showInFindResults(): Boolean = false


  override def fetchWeightedElements(pattern: String, progressIndicator: ProgressIndicator, consumer: Processor[_ >: FoundItemDescriptor[StdFunctionRef]]): Unit = {
    println("read pattern is: " + pattern)
    val results = inkuireService.query(pattern)
    println("Results found: " + results.size)

    for (result <- results) {
      var arguments = ""
      for (signatureArgument <- result.signature.arguments) {
        val argument = if (signatureArgument.typ.itid.isDefined) {
          signatureArgument.typ.itid.get.uuid
        }
        else {
          ""
        }
        arguments += argument
      }

      val element: StdFunctionRef = new StdFunctionRef(result.name, arguments)
      val weight = 1
      val itemDescriptor = new FoundItemDescriptor[StdFunctionRef](element, weight)
      consumer.process(itemDescriptor)
    }

  }

  override def processSelectedItem(selected: StdFunctionRef, modifiers: Int, searchText: String): Boolean = true

  override def getDataForItem(element: StdFunctionRef, dataId: String): Option[Any] = null
}

