package org.jetbrains.plugins.scala.typeSearch

import com.intellij.ide.actions.searcheverywhere.{FoundItemDescriptor, SearchEverywhereContributor, SearchEverywhereContributorFactory, WeightedSearchEverywhereContributor}
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.{Processor, ThrowableRunnable}
import com.intellij.util.SlowOperations.allowSlowOperations
import org.jetbrains.plugins.scala.caches.ScalaShortNamesCacheManager
import org.jetbrains.plugins.scala.typeSearch.SearchStdFunctionByTypeContributor.inkuireService
import org.virtuslab.inkuire.engine.common.model.ExternalSignature
import org.virtuslab.inkuire.engine.common.service.ScalaExternalSignaturePrettifier

import java.awt.{Color, Component}
import java.io.File
import javax.swing.{JLabel, JList, ListCellRenderer}
import scala.language.postfixOps

class StdFunctionRef(val externalSignature: ExternalSignature) {
  // TODO: poor naming convention? I should consider using def here
  val getName: String = externalSignature.name
  val getFQName: String = externalSignature.packageName + "#" + getName
  val getPrettyName: String = externalSignature.name.split("\\$").apply(0)
  val getPrettyFQName: String = externalSignature.packageName + "#" + getPrettyName
  val getPrettyContext: String = (new ScalaExternalSignaturePrettifier).prettify(externalSignature)
}

class MyCellRenderer() extends JLabel with ListCellRenderer[StdFunctionRef] {
  setOpaque(true)

  override def getListCellRendererComponent(list: JList[_ <: StdFunctionRef], value: StdFunctionRef, index: Int,
                                            isSelected: Boolean, cellHasFocus: Boolean): Component = {

    // Adjust printed text
    setText(value.getPrettyFQName + ": " + value.getPrettyContext)

    // Set colors
    var background: Color = null
    var foreground: Color = null
    val dropLocation = list.getDropLocation

    // check if this cell is selected
    if (dropLocation != null && !dropLocation.isInsert && dropLocation.getIndex == index) {
      background = Color.BLUE
      foreground = Color.WHITE

    }
    else if (isSelected) {
      background = Color.PINK
      foreground = Color.DARK_GRAY

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
  var inkuireService = new InkuireService(file.toURI.toURL.toString)
  var project: Option[Project] = None

  class Factory extends SearchEverywhereContributorFactory[AnyRef] {
    override def createContributor(initEvent: AnActionEvent): SearchEverywhereContributor[AnyRef] = {
      project = Some(initEvent.getProject)
      (new SearchStdFunctionByTypeContributor).asInstanceOf[SearchEverywhereContributor[AnyRef]]
    }
  }
}

class SearchStdFunctionByTypeContributor extends WeightedSearchEverywhereContributor[StdFunctionRef] {
  val cellRenderer = new MyCellRenderer

  override def getElementsRenderer: ListCellRenderer[_ >: Any] = (new MyCellRenderer).asInstanceOf[ListCellRenderer[_ >: Any]]

  override def getSearchProviderId: String = getClass.getSimpleName

  override def getGroupName: String = "Type Search"

  override def getSortWeight = 1000

  // default is false
  override def isShownInSeparateTab: Boolean = true

  // default is true
  override def showInFindResults(): Boolean = false

  // Simple accuracy measure
  def calculateWeightOfMatch(pattern: String, element: StdFunctionRef): Int = {
    val patternParameters = pattern.split("=>").map(s => s.trim)
    val prettifier = new ScalaExternalSignaturePrettifier
    val elementParametersStringified = prettifier.prettify(element.externalSignature)

    val weight = {
      if (elementParametersStringified == pattern) {
        100
      }
      else {
        var ctr = 0
        for (parameter <- patternParameters) {
          val isMatch = if (elementParametersStringified.contains(parameter)) 1 else 0
          ctr += isMatch
        }
        ctr
      }
    }

    weight
  }

  override def fetchWeightedElements(pattern: String, progressIndicator: ProgressIndicator, consumer: Processor[_ >: FoundItemDescriptor[StdFunctionRef]]): Unit = {
    val results = inkuireService.query(pattern)

    for (result <- results) {
      val element: StdFunctionRef = new StdFunctionRef(result)
      val weight = calculateWeightOfMatch(pattern, element)
      val itemDescriptor = new FoundItemDescriptor[StdFunctionRef](element, weight)

      consumer.process(itemDescriptor)
    }
  }

  override def processSelectedItem(selected: StdFunctionRef, modifiers: Int, searchText: String): Boolean = {

    class MyThread extends ThrowableRunnable[Throwable] {
      val FQClassName: String = selected.externalSignature.packageName
      val FQName: String = selected.externalSignature.name.split("\\$").apply(0)

      def findMethodByFQN(scalaShortNamesCacheManager: ScalaShortNamesCacheManager,
                          projectWithLibrariesScope: GlobalSearchScope): Option[PsiMethod] = {
        try {
          scalaShortNamesCacheManager
            .getClassByFQName(FQClassName, projectWithLibrariesScope)
            .getAllMethods
            .find(_.getName == FQName)
        }
        catch {
          case _: NullPointerException => None
        }
      }

      override def run(): Unit = {
       val projectEvent = SearchStdFunctionByTypeContributor.project.get
        val scalaShortNamesCacheManager = ScalaShortNamesCacheManager.getInstance(projectEvent)
        val otherProjectScope = GlobalSearchScope.allScope(projectEvent)

        val psiMethodToNavigate: PsiMethod = findMethodByFQN(scalaShortNamesCacheManager, otherProjectScope).orNull

        psiMethodToNavigate match {
          case null => println("psiMethodToNavigate is null")
          case _ => psiMethodToNavigate.navigate(true)
        }
      }
    }
    allowSlowOperations(new MyThread)

    true // close SEWindow
  }

  override def getDataForItem(element: StdFunctionRef, dataId: String): Option[Any] = null
}


