package org.jetbrains.plugins.scala.typeSearch

import com.intellij.ide.util.gotoByName.{ChooseByNameItemProvider, ChooseByNameViewModel}
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.util.Processor

import java.util
import scala.language.postfixOps


// cała ta klasa jest zaimplementowana np tutaj "public class DefaultChooseByNameItemProvider implements ChooseByNameInScopeItemProvider {" całkiem sensownie chyba
class SearchStdFunctionsByTypeItemProvider extends ChooseByNameItemProvider  {

  override def filterNames(base: ChooseByNameViewModel, names: Array[String], pattern: String): util.List[String] = {
    val a = util.Arrays.asList("element1")
    names.map {
      (name: String) => a.add(name)
    }
    a
  }

  override def filterElements(base: ChooseByNameViewModel, pattern: String, everywhere: Boolean, cancelled: ProgressIndicator, consumer: Processor[AnyRef]): Boolean = true
}


