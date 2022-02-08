package org.jetbrains.plugins.scala.typeSearch

import com.intellij.ide.actions.searcheverywhere.{FoundItemDescriptor, SearchEverywhereContributor, SearchEverywhereContributorFactory, WeightedSearchEverywhereContributor}
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.{Processor, ThrowableRunnable}
import com.intellij.util.SlowOperations.allowSlowOperations
import org.jetbrains.plugins.scala.caches.ScalaShortNamesCacheManager
import org.jetbrains.plugins.scala.inReadAction
import org.jetbrains.plugins.scala.typeSearch.SearchStdFunctionByTypeContributor.inkuireService
import org.virtuslab.inkuire.engine.common.model.ExternalSignature
import org.virtuslab.inkuire.engine.common.service.ScalaExternalSignaturePrettifier

import java.awt.{Color, Component}
import java.io.File
import javax.swing.{JLabel, JList, ListCellRenderer}
import scala.language.postfixOps

class StdFunctionRef(val externalSignature: ExternalSignature) {
}

class MyCellRenderer() extends JLabel with ListCellRenderer[StdFunctionRef] {
  setOpaque(true)

  override def getListCellRendererComponent(list: JList[_ <: StdFunctionRef], value: StdFunctionRef, index: Int,
                                            isSelected: Boolean, cellHasFocus: Boolean): Component = {

    // Adjust printed text
    val prettifier = new ScalaExternalSignaturePrettifier
    val functionName = value.externalSignature.name
    val prettyContext = prettifier.prettify(value.externalSignature)

    setText(functionName + ": " + prettyContext)

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

  class Factory extends SearchEverywhereContributorFactory[AnyRef] {
    override def createContributor(initEvent: AnActionEvent): SearchEverywhereContributor[AnyRef] =
      (new SearchStdFunctionByTypeContributor).asInstanceOf[SearchEverywhereContributor[AnyRef]]
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
    import org.jetbrains.plugins.scala.extensions

    class MyThread extends ThrowableRunnable[Throwable] {
      override def run = {

        val name = selected.externalSignature.name
        val project = ProjectManager.getInstance().getOpenProjects.apply(0)
        val modules = ModuleManager.getInstance(project).getModules
        for (module <- modules) {
          println(module.getName + ": " + module.getModuleFilePath)
        }
        //    val scope: GlobalSearchScope = GlobalSearchScope.moduleWithLibrariesScope(modules.apply(0)) // looks awful
        val ssncm = ScalaShortNamesCacheManager.getInstance(project)
        val otherProjectScope = GlobalSearchScope.allScope(project)

        val psiMethods: Iterable[PsiMethod] = inReadAction {
          ssncm.methodsByName(name)(otherProjectScope)
        }

        for (p <- psiMethods) {
          println(p.getName, "::= ", p.getContainingFile.getVirtualFile.getPath)
        }

        var done = false
        for (p <- psiMethods) {
          if (p != null && !done) {
            //        new OpenFileDescriptor(p.getProject, p.getContainingFile.getVirtualFile, 0, 0).navigate(true)

            extensions.invokeAndWait {
              p.navigate(true)

            }

            done = true
          }
        }
      }
    }

    allowSlowOperations(new MyThread)

//    extensions.executeOnPooledThread {
//      val name = selected.externalSignature.name
//      val project = ProjectManager.getInstance().getOpenProjects.apply(0)
//      val modules = ModuleManager.getInstance(project).getModules
//      for (module <- modules) {
//        println(module.getName + ": " + module.getModuleFilePath)
//      }
//      //    val scope: GlobalSearchScope = GlobalSearchScope.moduleWithLibrariesScope(modules.apply(0)) // looks awful
//      val ssncm = ScalaShortNamesCacheManager.getInstance(project)
//      val otherProjectScope = GlobalSearchScope.allScope(project)
//
//      val psiMethods: Iterable[PsiMethod] = inReadAction {
//        ssncm.methodsByName(name)(otherProjectScope)
//      }
//
//      for (p <- psiMethods) {
//        println(p.getName, "::= ", p.getContainingFile.getVirtualFile.getPath)
//      }
//
//      var done = false
//      for (p <- psiMethods) {
//        if (p != null && !done) {
//  //        new OpenFileDescriptor(p.getProject, p.getContainingFile.getVirtualFile, 0, 0).navigate(true)
//
//          extensions.invokeAndWait {
//            p.navigate(true)
//
//          }
////          p.navigati
//          done = true
//        }
//      }
//    }

    true
  }

  override def getDataForItem(element: StdFunctionRef, dataId: String): Option[Any] = null
}


