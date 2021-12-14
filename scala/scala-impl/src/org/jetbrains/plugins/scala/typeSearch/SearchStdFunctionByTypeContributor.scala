package org.jetbrains.plugins.scala.typeSearch

import com.intellij.ide.IdeBundle
import com.intellij.ide.actions.SearchEverywherePsiRenderer
import com.intellij.ide.actions.searcheverywhere.{AbstractGotoSEContributor, FileSearchEverywhereContributor, FoundItemDescriptor, PSIPresentationBgRendererWrapper, PersistentSearchEverywhereContributorFilter, SearchEverywhereCommandInfo, SearchEverywhereContributor, SearchEverywhereContributorFactory, SearchEverywhereDataKeys}
import com.intellij.ide.util.gotoByName.{FileTypeRef, FilteringGotoByModel, GotoFileConfiguration, GotoFileModel}
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, CommonDataKeys}
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.{PsiDirectory, PsiFile, PsiFileSystemItem}
import com.intellij.util.Processor

import javax.swing.ListCellRenderer
import javax.swing.JLabel
import javax.swing.JList
import java.awt.Color
import java.awt.Component
import java.util
import java.util.{ArrayList, List}
import scala.language.postfixOps


class MyCellRenderer() extends JLabel with ListCellRenderer[AnyRef] {
//  setOpaque(true)
  override def getListCellRendererComponent(list: JList[_ <: AnyRef], value: AnyRef, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
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

//class SearchStdFunctionByTypeContributor extends AbstractGotoSEContributor {
//  private val LOG = Logger.getInstance(classOf[SearchStdFunctionByTypeContributor])
//
////  val cellRenderer = new MyCellRenderer
//
//   def createModel: SearchStdFunctionByTypeModel = {
//    new SearchStdFunctionByTypeModel
//  }
//
//  override def getSearchProviderId: String = getClass.getSimpleName
//
//  override def getGroupName: String = "Std Functions by type"
//
//  override def getSortWeight: Int = 1000
//
//  override def isShownInSeparateTab: Boolean = true
//
//  override def showInFindResults(): Boolean = false
//
//  override def processSelectedItem(selected: StdFunction, modifiers: Int, searchText: String): Boolean = true
//
////  override def getElementsRenderer: ListCellRenderer[_ >: StdFunction] = cellRenderer
//
////  override def fetchElements(pattern: String, progressIndicator: ProgressIndicator, consumer: Processor[_ >: AnyRef]): Unit = {
////    // wzięte z AbstractGotoSEContributoa
////    // if (nie istnieje baza) return
////
////    // if (!isEmptyPatternSupported && pattern.isEmpty) return
////
////    val fetchRunnable: Unit = () => {
////      val model: SearchStdFunctionByTypeModel = createModel
////      val provider: SearchStdFunctionsByTypeItemProvider = model.getItemProvider
//////      val scope = Objects.requireNonNull(myScopeDescriptor.getScope).asInstanceOf[GlobalSearchScope]
//////      val everywhere = scope.isSearchInLibraries
//////      val viewModel = new AbstractGotoSEContributor.MyViewModel(myProject, model)
////      provider.filterElements(model.asInstanceOf[], pattern, everywhere, progressIndicator, (item: FoundItemDescriptor[AnyRef]) => processElement(progressIndicator, consumer, model, item.getItem, item.getWeight))
////    }
////
////
////
////
////    //    to jest jakieś pw trzeba rozkminić jak w scali
////    fetchRunnable()
////    //    val application = ApplicationManager.getApplication
////    //    if (application.isUnitTestMode && application.isDispatchThread) fetchRunnable()
////    //    else {
////    //      ProgressIndicatorUtils.yieldToPendingWriteActions()
////    //      ProgressIndicatorUtils.runInReadActionWithWriteActionPriority(fetchRunnable, progressIndicator)
////    //    }
//
////  }
////
////  private def processElement(progressIndicator: ProgressIndicator, consumer: Processor[_ >: FoundItemDescriptor[Any]], model: SearchStdFunctionByTypeModel, element: Any, degree: Int): Boolean = {
////    if (progressIndicator.isCanceled) {
////      return false
////    }
////    if (element == null) {
////      LOG.error("Null returned from " + model + " in " + this)
////      return true
////    }
////    consumer.process(new FoundItemDescriptor[Any](element, degree))
////  }
////
//////  override def getDataForItem(element: Any, dataId: String): AnyRef = element.asInstanceOf[StdFunction].getFunctionName
//
//  class Factory extends SearchEverywhereContributorFactory[AnyRef] {
//    override def createContributor(initEvent: AnActionEvent): SearchEverywhereContributor[AnyRef] = (new SearchStdFunctionByTypeContributor).asInstanceOf[SearchEverywhereContributor[AnyRef]]
//  }
//}

object SearchStdFunctionByTypeContributor {
  class Factory extends SearchEverywhereContributorFactory[AnyRef] {
    override def createContributor(initEvent: AnActionEvent): SearchEverywhereContributor[AnyRef] =
     new SearchStdFunctionByTypeContributor(initEvent)
  }

//  def createFileTypeFilter(project: Project): PersistentSearchEverywhereContributorFilter[FileTypeRef] = {
//    val items = new util.ArrayList[FileTypeRef](FileTypeRef.forAllFileTypes)
//    items.add(0, GotoFileModel.DIRECTORY_FILE_TYPE_REF)
//    new PersistentSearchEverywhereContributorFilter[FileTypeRef](items, GotoFileConfiguration.getInstance(project), FileTypeRef.getName, FileTypeRef.getIcon)
//  }
}

class SearchStdFunctionByTypeContributor(val event: AnActionEvent) extends AbstractGotoSEContributor(event) {
  val project: Project = event.getRequiredData(CommonDataKeys.PROJECT)
//  val myModelForRenderer: GotoFileModel = new GotoFileModel(project)
//  val myFilter: PersistentSearchEverywhereContributorFilter[FileTypeRef] = SearchStdFunctionByTypeContributor.createFileTypeFilter(project)
//  final private val myModelForRenderer = null
  final private val myFilter = null

  override def getGroupName: String = "Type Search"
  override def getSortWeight = 200

//  override protected def createModel(project: Project): FilteringGotoByModel[FileTypeRef] = {
//    val model = new GotoFileModel(project)
//    if (myFilter != null) model.setFilterItems(myFilter.getSelectedElements)
//    model
//  }

  override def getElementsRenderer: ListCellRenderer[AnyRef] = new MyCellRenderer

  override def processSelectedItem(selected: Any, modifiers: Int, searchText: String): Boolean = {
//    if (selected.isInstanceOf[PsiFile]) {
//      val file = selected.asInstanceOf[PsiFile].getVirtualFile
//      if (file != null && myProject != null) {
//        val pos = getLineAndColumn(searchText)
//        val descriptor = new OpenFileDescriptor(myProject, file, pos.first, pos.second)
//        if (descriptor.canNavigate) {
//          descriptor.navigate(true)
//          return true
//        }
//      }
//    }
//    super.processSelectedItem(selected, modifiers, searchText)
    true
  }

  override def getDataForItem(element: Any, dataId: String): Any = {
//    if (CommonDataKeys.PSI_FILE.is(dataId) && element.isInstanceOf[PsiFile]) return element
//    if (SearchEverywhereDataKeys.ITEM_STRING_DESCRIPTION.is(dataId) && (element.isInstanceOf[PsiFile] || element.isInstanceOf[PsiDirectory])) {
//      var path = element.asInstanceOf[PsiFileSystemItem].getVirtualFile.getPath
//      path = FileUtil.toSystemIndependentName(path)
//      if (myProject != null) {
//        val basePath = myProject.getBasePath
//        if (basePath != null) path = FileUtil.getRelativePath(basePath, path, '/')
//      }
//      return path
//    }
//    super.getDataForItem(element, dataId)
  }

  override def createModel(project: Project): FilteringGotoByModel[_] = (new SearchStdFunctionByTypeModel(project)).asInstanceOf[FilteringGotoByModel[_]]
}
