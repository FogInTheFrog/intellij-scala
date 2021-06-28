package org.jetbrains.plugins.scala.annotator

import com.intellij.openapi.util.registry.Registry
import org.jetbrains.plugins.scala.base.ScalaLightCodeInsightFixtureTestAdapter
import org.jetbrains.plugins.scala.{LatestScalaVersions, ScalaVersion}
import org.jetbrains.plugins.scala.externalHighlighters.ScalaHighlightingMode.ShowDotcErrorsKey


class Scala3AliasedTypeLambdaConformanceTest extends ScalaLightCodeInsightFixtureTestAdapter {
  override protected def supportedIn(version: ScalaVersion): Boolean =
    version >= LatestScalaVersions.Scala_3_0

  override def setUp(): Unit = {
    super.setUp()
    Registry.get(ShowDotcErrorsKey).setValue(false, getTestRootDisposable)
  }

  private val context: String =
    """
      |trait Bar[A]
      |val xs: Bar[String] = ???
      |def foo[F[_], A](fa: F[A]): F[A] = fa
      |""".stripMargin

  private def doTest(code: String): Unit =
    checkTextHasNoErrors(
      s"""
         |object Test {
         |$context
         |$code
         |}
         |""".stripMargin)

  def testSimpleLambda(): Unit = doTest(
    """
      |type TL = [A] =>> Bar[A]
      |foo[TL, String](xs)
      |""".stripMargin
  )

  def testMultiListLambda(): Unit = doTest(
    """
      |type TL2 = [A] =>> [B] =>> Bar[A]
      |foo[TL2[String], Int](xs)
      |""".stripMargin
  )

  def testAliasAndLambdaBothHaveTypeParameters(): Unit = doTest(
    """
      |type Foo[A] = [B] =>> [C] =>> Bar[B]
      |foo[Foo[Int][String], Double](xs)
      |""".stripMargin
  )

  def testChainedAlias(): Unit = doTest(
    """
      |type TL3 = [A] =>> [B] =>> Int
      |type C = TL3
      |foo[C[Int], String](1)
      |""".stripMargin
  )

  def testTypeLambdaNeg(): Unit = checkHasErrorAroundCaret(
    s"""
       |object Test {
       |  $context
       |  type TL4 = [A] =>> [B] =>> Bar[B]
       |  foo[TL4[String], Int](x${CARET}s)
       |}
       |""".stripMargin
  )
}
