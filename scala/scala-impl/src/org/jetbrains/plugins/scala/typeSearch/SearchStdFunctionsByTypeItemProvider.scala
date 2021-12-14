package org.jetbrains.plugins.scala.typeSearch

import com.intellij.ide.util.gotoByName.{ChooseByNameItemProvider, ChooseByNameViewModel}
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.util.Processor

import java.util
import scala.language.postfixOps
//import org.jetbrains.plugins.scala.typeSearch.SearchStdFunctionsByTypeItemProvider.getSortedResults

import java.util


import scala.jdk.CollectionConverters._
// nie pamiętam skąd jest ten companion object i jakie mam metody
//object SearchStdFunctionsByTypeItemProvider {
//  def getSortedResults: List[String] = List("funkcja pierwsza", "funkcja druga")
//
//  def processByNames(
//    base: ChooseByNameViewModel,
//    everywhere: Boolean,
//    indicator: ProgressIndicator,
//    consumer: Processor[Any],
//    namesList: List[String],
//    parameters: FindSymbolParameters
//  ): Boolean = {
//    val model = base.getModel
//    namesList.foreach(name => {
//      indicator.checkCanceled()
//      val elements = if (model.is[ContributorsBasedGotoByModel]) model.getElementsByName(name, parameters, indicator) else model.getElementsByName(name, everywhere, parameters.completePattern)
//      elements.foreach(consumer.process(_))
//    })
//    true
//  }
//}


// cała ta klasa jest zaimplementowana np tutaj "public class DefaultChooseByNameItemProvider implements ChooseByNameInScopeItemProvider {" całkiem sensownie chyba
class SearchStdFunctionsByTypeItemProvider extends ChooseByNameItemProvider  {

  override def filterNames(base: ChooseByNameViewModel, names: Array[String], pattern: String): util.List[String] = {
    val a = util.Arrays.asList("element1")
    names.map {
      (name: String) => a.add(name)
    }
    a
  }

//  private def createParameters(base: ChooseByNameViewModel, pattern: String, everywhere: Boolean) = {
//    val model = base.getModel
//    val idFilter = model match {
//      case model1: ContributorsBasedGotoByModel => IdFilter.getProjectIdFilter(model1.getProject, everywhere)
//      case _ => null
//    }
//    val searchScope = FindSymbolParameters.searchScopeFor(base.getProject, everywhere)
//    new FindSymbolParameters(pattern, getNamePattern(base, pattern), searchScope, idFilter)
//  }

//  private def getNamePattern(base: ChooseByNameViewModel, pattern: String) = {
//    val transformedPattern = base.transformPattern(pattern)
//    getNamePattern(base.getModel, transformedPattern)
//  }

//  private def getNamePattern(model: ChooseByNameModel, pattern: String) = {
//    val separators = model.getSeparators
//    var lastSeparatorOccurrence = 0
//    for (separator <- separators) {
//      var idx = pattern.lastIndexOf(separator)
//      if (idx == pattern.length - 1) { // avoid empty name
//        idx = pattern.lastIndexOf(separator, idx - 1)
//      }
//      lastSeparatorOccurrence = Math.max(lastSeparatorOccurrence, if (idx == -1) idx
//      else idx + separator.length)
//    }
//    pattern.substring(lastSeparatorOccurrence)
//  }

  override def filterElements(base: ChooseByNameViewModel, pattern: String, everywhere: Boolean, cancelled: ProgressIndicator, consumer: Processor[AnyRef]): Boolean = true
}


