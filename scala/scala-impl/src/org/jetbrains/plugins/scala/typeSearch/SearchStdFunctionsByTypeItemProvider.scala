package org.jetbrains.plugins.scala.typeSearch

import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor
import com.intellij.ide.util.gotoByName.{ChooseByNameInScopeItemProvider, ChooseByNameItemProvider, ChooseByNameModel, ChooseByNameViewModel, ChooseByNameWeightedItemProvider, ContributorsBasedGotoByModel}
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager}
import com.intellij.psi.{PsiElement, SmartPsiElementPointer}
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.indexing.{FindSymbolParameters, IdFilter}
import org.jetbrains.plugins.scala.extensions.ObjectExt
import org.jetbrains.plugins.scala.typeSearch.SearchStdFunctionsByTypeItemProvider.getSortedResults

import scala.collection.JavaConverters._
import java.util

// nie pamiętam skąd jest ten companion object i jakie mam metody
object SearchStdFunctionsByTypeItemProvider {
  def getSortedResults: List[String] = List("funkcja pierwsza", "funkcja druga")

  def processByNames(
    base: ChooseByNameViewModel,
    everywhere: Boolean,
    indicator: ProgressIndicator,
    consumer: Processor[Any],
    namesList: List[String],
    parameters: FindSymbolParameters
  ): Boolean = {
    val model = base.getModel
    namesList.foreach(name => {
      indicator.checkCanceled()
      val elements = if (model.is[ContributorsBasedGotoByModel]) model.getElementsByName(name, parameters, indicator) else model.getElementsByName(name, everywhere, parameters.completePattern)
      elements.foreach(consumer.process(_))
    })
    true
  }
}


// cała ta klasa jest zaimplementowana np tutaj "public class DefaultChooseByNameItemProvider implements ChooseByNameInScopeItemProvider {" całkiem sensownie chyba
class SearchStdFunctionsByTypeItemProvider extends ChooseByNameInScopeItemProvider  {

  private val myContext: SmartPsiElementPointer[PsiElement] = null
//  override def filterNames(base: ChooseByNameViewModel, names: Array[String], pattern: String): util.List[String] =  List[String]().asJava
//
//
//  override def filterElements(  base: ChooseByNameViewModel,
//                               pattern: String, everywhere: Boolean,
//                               cancelled: ProgressIndicator,
//                               consumer: Processor[Any]
//                             ): Boolean =  {
//
//    val namesList = getSortedResults
//    true
//
//
//  }

  override def filterNames(base: ChooseByNameViewModel, names: Array[String], pattern: String): util.List[String] = List[String]().asJava


  // do obu niżej dodane asInstanceOf AnyRef
  override def filterElements(base: ChooseByNameViewModel, pattern: String, everywhere: Boolean, indicator: ProgressIndicator, consumer: Processor[AnyRef]): Boolean = {
    filterElementsWithWeights(base, createParameters(base, pattern, everywhere), indicator, (res: FoundItemDescriptor[_]) => consumer.process(res.getItem.asInstanceOf[AnyRef]))
  }

  override def filterElements(base: ChooseByNameViewModel, parameters: FindSymbolParameters, indicator: ProgressIndicator, consumer: Processor[AnyRef]): Boolean = {
    filterElementsWithWeights(base, parameters, indicator, (res: FoundItemDescriptor[_]) => consumer.process(res.getItem.asInstanceOf[AnyRef]))
  }

  private def createParameters(base: ChooseByNameViewModel, pattern: String, everywhere: Boolean) = {
    val model = base.getModel
    val idFilter = model match {
      case model1: ContributorsBasedGotoByModel => IdFilter.getProjectIdFilter(model1.getProject, everywhere)
      case _ => null
    }
    val searchScope = FindSymbolParameters.searchScopeFor(base.getProject, everywhere)
    new FindSymbolParameters(pattern, getNamePattern(base, pattern), searchScope, idFilter)
  }

  private def getNamePattern(base: ChooseByNameViewModel, pattern: String) = {
    val transformedPattern = base.transformPattern(pattern)
    getNamePattern(base.getModel, transformedPattern)
  }

  private def getNamePattern(model: ChooseByNameModel, pattern: String) = {
    val separators = model.getSeparators
    var lastSeparatorOccurrence = 0
    for (separator <- separators) {
      var idx = pattern.lastIndexOf(separator)
      if (idx == pattern.length - 1) { // avoid empty name
        idx = pattern.lastIndexOf(separator, idx - 1)
      }
      lastSeparatorOccurrence = Math.max(lastSeparatorOccurrence, if (idx == -1) idx
      else idx + separator.length)
    }
    pattern.substring(lastSeparatorOccurrence)
  }



  override def filterElementsWithWeights(base: ChooseByNameViewModel, pattern: String, everywhere: Boolean, indicator: ProgressIndicator, consumer: Processor[_ >: FoundItemDescriptor[_]]): Boolean = {
    filterElementsWithWeights(base, createParameters(base, pattern, everywhere), indicator, consumer)
  }

  override def filterElementsWithWeights(base: ChooseByNameViewModel, parameters: FindSymbolParameters, indicator: ProgressIndicator, consumer: Processor[_ >: FoundItemDescriptor[_]]): Boolean = {
    ProgressManager.getInstance.computePrioritized(() => filterElements(base, indicator, if (myContext == null) null
    else myContext.getElement, () => base.getModel.getNames(parameters.isSearchInLibraries), consumer, parameters))
  }

}


