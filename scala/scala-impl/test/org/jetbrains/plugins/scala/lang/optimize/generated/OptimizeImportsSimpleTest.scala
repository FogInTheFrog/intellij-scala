package org.jetbrains.plugins.scala.lang.optimize
package generated
import org.jetbrains.plugins.scala.ScalaVersion

class OptimizeImportsSimpleTestBase extends OptimizeImportsTestBase {
  //This class was generated by build script, please don't change this
  override def folderPath: String = super.folderPath + "simple/"

  protected override def sourceRootPath: String = folderPath

  def testFromRoot(): Unit = doTest()

  def testHasOverloads(): Unit = doTest()

  def testSorted(): Unit = doTest()

  def testSortedInPackage(): Unit = doTest()

  def testTwoExpressions(): Unit = doTest()

  def testDeleteBraces(): Unit = doTest()

  def testDontSaveNotResolved(): Unit = doTest()

  def testImportChainUsed(): Unit = doTest()

  def testLanguageFeatures(): Unit = doTest()

  def testNewLines(): Unit = doTest()

  def testOneImport(): Unit = doTest()

  def testScalaDoc(): Unit = doTest()

  def testSCL7275(): Unit = doTest()

  def testSomeTrait(): Unit = doTest()

  def testUnusedImportChain(): Unit = doTest()

  def testUnusedSelector(): Unit = doTest()

  def testUsedImport(): Unit = doTest()

  def testRelativeNameConflict(): Unit = doTest()

  def testNoReformattingComments(): Unit = doTest()

  def testRemoveImportsFromSamePackageAndDefaultPackages_NoNameClashes(): Unit = {
    getFixture.addFileToProject("org/example/declaration/all.scala",
      """package org.example.declaration.data
        |
        |class Random
        |class Qwe
        |""".stripMargin
    )
    doTest(
      """import java.lang.AbstractMethodError
        |import java.util.Properties
        |import scala.Predef.Manifest
        |import scala.Tuple1
        |import scala.util.Try
        |
        |object Usage {
        |  val a1: Manifest[_] = ???
        |  val a2: Tuple1[_] = ???
        |  val a3: AbstractMethodError = ???
        |  val a4: Properties = ???
        |  val a5: Try[_] = ???
        |}""".stripMargin,
      """import java.util.Properties
        |import scala.util.Try
        |
        |object Usage {
        |  val a1: Manifest[_] = ???
        |  val a2: Tuple1[_] = ???
        |  val a3: AbstractMethodError = ???
        |  val a4: Properties = ???
        |  val a5: Try[_] = ???
        |}""".stripMargin
    )
  }

  def testRemoveImportsFromSamePackageAndDefaultPackages_NoNameClashes_LocalImports(): Unit = {
    getFixture.addFileToProject("org/example/declaration/all.scala",
      """package org.example.declaration.data
        |
        |class Random
        |class Qwe
        |""".stripMargin
    )
    doTest(
      """object Usage {
        |  import java.lang.AbstractMethodError
        |  import java.util.Properties
        |  import scala.Predef.Manifest
        |  import scala.Tuple1
        |  import scala.util.Try
        |
        |  val a1: Manifest[_] = ???
        |  val a2: Tuple1[_] = ???
        |  val a3: AbstractMethodError = ???
        |  val a4: Properties = ???
        |  val a5: Try[_] = ???
        |}""".stripMargin,
      """object Usage {
        |  import java.util.Properties
        |  import scala.util.Try
        |
        |  val a1: Manifest[_] = ???
        |  val a2: Tuple1[_] = ???
        |  val a3: AbstractMethodError = ???
        |  val a4: Properties = ???
        |  val a5: Try[_] = ???
        |}""".stripMargin
    )
  }
}

class OptimizeImportsSimpleTest_2_12 extends OptimizeImportsSimpleTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean =
    version == ScalaVersion.Latest.Scala_2_12
}

class OptimizeImportsSimpleTest_2_13 extends OptimizeImportsSimpleTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean =
    version == ScalaVersion.Latest.Scala_2_13
}