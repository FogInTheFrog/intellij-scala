package org.jetbrains.plugins.scala.typeSearch

import com.intellij.ide.actions.searcheverywhere.{AbstractGotoSEContributor, FoundItemDescriptor, SearchEverywhereContributor, SearchEverywhereContributorFactory}
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.util.Processor

import javax.swing.ListCellRenderer
import javax.swing.JLabel
import javax.swing.JList
import java.awt.Color
import java.awt.Component
import scala.language.postfixOps

class StdFunction(name: String) {
  def getFunctionName: String = name
}

class MyCellRenderer() extends JLabel with ListCellRenderer[AnyRef]   {
  setOpaque(true)

  override def getListCellRendererComponent(list: JList[_ <: AnyRef], value: scala.Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
        setText(value.toString)
        var background: Color = null
        var foreground: Color = null
        // check if this cell represents the current DnD drop location
        val dropLocation: JList.DropLocation = list.getDropLocation
        if (dropLocation != null && !(dropLocation.isInsert) && dropLocation.getIndex == index) {
          background = Color.BLUE
          foreground = Color.WHITE
          // check if this cell is selected
        }
        else {
          if (isSelected) {
            background = Color.RED
            foreground = Color.WHITE
            // unselected, and not the DnD drop location
          }
          else {
            background = Color.WHITE
            foreground = Color.BLACK
          }
        }

        setBackground(background)
        setForeground(foreground)
        this
      }
}

class SearchStdFunctionByTypeContributor extends SearchEverywhereContributor[StdFunction] {
  private val LOG = Logger.getInstance(classOf[SearchStdFunctionByTypeContributor])

  val cellRenderer = new MyCellRenderer

   def createModel: SearchStdFunctionByTypeModel = {
    new SearchStdFunctionByTypeModel
  }

  override def getSearchProviderId: String = getClass.getSimpleName

  override def getGroupName: String = "Std Functions by type"

  override def getSortWeight: Int = 1000

  override def isShownInSeparateTab: Boolean = true

  override def showInFindResults(): Boolean = false

  override def processSelectedItem(selected: StdFunction, modifiers: Int, searchText: String): Boolean = true

  override def getElementsRenderer: ListCellRenderer[_ >: StdFunction] = cellRenderer

  override def fetchElements(pattern: String, progressIndicator: ProgressIndicator, consumer: Processor[_ >: AnyRef]): Unit = {
    // wzięte z AbstractGotoSEContributoa
    // if (nie istnieje baza) return

    // if (!isEmptyPatternSupported && pattern.isEmpty) return

    val fetchRunnable: Unit = () => {
      val model: SearchStdFunctionByTypeModel = createModel
      val provider: SearchStdFunctionsByTypeItemProvider = model.getItemProvider
//      val scope = Objects.requireNonNull(myScopeDescriptor.getScope).asInstanceOf[GlobalSearchScope]
//      val everywhere = scope.isSearchInLibraries
//      val viewModel = new AbstractGotoSEContributor.MyViewModel(myProject, model)
      provider.filterElements(viewModel, pattern, everywhere, progressIndicator, (item: FoundItemDescriptor[AnyRef]) => processElement(progressIndicator, consumer, model, item.getItem, item.getWeight))
    }




    //    to jest jakieś pw trzeba rozkminić jak w scali
    fetchRunnable()
    //    val application = ApplicationManager.getApplication
    //    if (application.isUnitTestMode && application.isDispatchThread) fetchRunnable()
    //    else {
    //      ProgressIndicatorUtils.yieldToPendingWriteActions()
    //      ProgressIndicatorUtils.runInReadActionWithWriteActionPriority(fetchRunnable, progressIndicator)
    //    }

  }

  private def processElement(progressIndicator: ProgressIndicator, consumer: Processor[_ >: FoundItemDescriptor[Any]], model: SearchStdFunctionByTypeModel, element: Any, degree: Int): Boolean = {
    if (progressIndicator.isCanceled) {
      return false
    }
    if (element == null) {
      LOG.error("Null returned from " + model + " in " + this)
      return true
    }
    consumer.process(new FoundItemDescriptor[Any](element, degree))
  }

//  override def getDataForItem(element: Any, dataId: String): AnyRef = element.asInstanceOf[StdFunction].getFunctionName

  class Factory extends SearchEverywhereContributorFactory[AnyRef] {
    override def createContributor(initEvent: AnActionEvent): SearchEverywhereContributor[AnyRef] = (new SearchStdFunctionByTypeContributor).asInstanceOf[SearchEverywhereContributor[AnyRef]]
  }
}



