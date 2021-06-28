package org.jetbrains.plugins.scala.lang.typeInference

import com.intellij.openapi.util.registry.Registry
import org.jetbrains.plugins.scala.{LatestScalaVersions, ScalaVersion}
import org.jetbrains.plugins.scala.base.ScalaLightCodeInsightFixtureTestAdapter
import org.jetbrains.plugins.scala.externalHighlighters.ScalaHighlightingMode.ShowDotcErrorsKey

class Scala3ExtensionsTest extends ScalaLightCodeInsightFixtureTestAdapter {
  override def supportedIn(version: ScalaVersion): Boolean =
    version >= LatestScalaVersions.Scala_3_0

  override def setUp(): Unit = {
    super.setUp()
    Registry.get(ShowDotcErrorsKey).setValue(false, getTestRootDisposable)
  }

  def testSimpleExtension(): Unit = checkTextHasNoErrors(
    """
      |object A {
      |  case class Circle(x: Double, y: Double, radius: Double)
      |
      |  extension (c: Circle)
      |    def circumference: Double = c.radius * math.Pi * 2
      |
      |  val c: Circle = ???
      |  c.circumference
      |}
      |""".stripMargin
  )

  def testSimpleDesugaredInvocation(): Unit = checkTextHasNoErrors(
    """
      |object A {
      |  case class Circle(x: Double, y: Double, radius: Double)
      |
      |  extension (c: Circle)
      |    def circumference: Double = c.radius * math.Pi * 2
      |
      |  val c: Circle = ???
      |  circumference(c)
      |}
      |""".stripMargin
  )

  //@TODO: right-associative?
  def testOperators(): Unit = checkTextHasNoErrors(
    """
      |object A {
      |  extension (x: String)
      |    def < (y: String): Boolean = true
      |
      |  "123" < "4235"
      |}
      |""".stripMargin
  )

  def testGenericExtension(): Unit = checkTextHasNoErrors(
    """
      |object A {
      |  extension [T](xs: List[T])
      |    def second: T = ???
      |
      |  val xs: List[Int] = ???
      |  val x: Int = xs.second
      |}
      |""".stripMargin
  )

  def testCollectiveExtension(): Unit = checkTextHasNoErrors(
    """
      |object A {
      |  extension (ss: Seq[String])
      |    def longestStrings: Seq[String] = ???
      |    def longestString: String = ???
      |
      |  val xs: Seq[String] = ???
      |  val longest: Seq[String] = xs.longestStrings
      |  val singleLongest: String = xs.longestString
      |}
      |""".stripMargin

  )

  def testTwoTypeArgumentSectionsOnInvocation(): Unit = checkTextHasNoErrors(
    """
      |object A {
      |  extension [A](x: Int) { def method[B](y: Int) = () }
      |  method[Int](1)[Long](2)
      |}
      |""".stripMargin
  )

  def testPriorityOfVisibleExtensionOverVisibleConversion(): Unit = checkTextHasNoErrors(
    """
      |object A {
      |  extension (x: Int) { def foo: Int = 123 }
      |  implicit class IntOps(val x: Int) { def foo: Int = 123 }
      |
      |  123.foo
      |}
      |""".stripMargin
  )

  def testExtensionFromGivenInLexicalScope(): Unit = checkTextHasNoErrors(
    """
      |object A {
      |  trait F
      |  given F with {
      |    extension (x: Int) { def foo: Int = 123 }
      |  }
      |
      |  123.foo
      |}
      |""".stripMargin
  )

  def testExtensionFromImplicitScope(): Unit = checkTextHasNoErrors(
    """
      |trait List[T]
      |object List {
      |  extension [T, U](xs: List[T])(using t: Ordering[U])
      |    def foo(t: U): Int = ???
      |}
      |
      |object A {
      |  given Ordering[String] = ???
      |  val xs: List[Int] = ???
      |  val y: Int = xs.foo("123")
      |}
      |""".stripMargin
  )

  def testExtensionFromGivenInImplicitScope(): Unit = checkTextHasNoErrors(
    """
      |trait List[T]
      |object List {
      |  given Ordering[List[Int]] with {
      |    def compare(xs: List[Int], ys: List[Int]): Int = 1
      |
      |    extension [T, U](xs: List[T])(using t: Ordering[U])
      |      def foo(t: U): U = ???
      |  }
      |}
      |
      |object A {
      |  trait F
      |  given Ordering[F] = ???
      |  val xs: List[Int] = ???
      |  val f: F = ???
      |  val y: F = xs.foo(f)
      |}
      |""".stripMargin
  )

  def testAmbiguousExtensionAndConversion(): Unit = checkHasErrorAroundCaret(
    s"""
       |object A {
       |  trait F
       |  given F with {
       |    extension (x: Int) { def foo: Int = 123 }
       |  }
       |
       |  implicit class IntOps(val x: Int) { def foo: Int = 123 }
       |  123.fo${CARET}o
       |}
       |""".stripMargin
  )

  def testAmbiguousExtensionAndConversionImplicitScope(): Unit = checkHasErrorAroundCaret(
    s"""
       |trait List[T]
       |object List {
       |  extension [T](xs: List[T])
       |    def foo(u: String): Int = ???
       |
       |  implicit class ListOps[T](xs: List[T]) {
       |    def foo(t: String): Int = 123
       |  }
       |}
       |
       |object A {
       |  val xs: List[Int] = ???
       |  xs.fo${CARET}o("123")
       |}
       |""".stripMargin
  )

  def testResolveFromInsideExtension(): Unit = checkTextHasNoErrors(
    """
      |object A {
      |  extension (s: String)
      |    def position(ch: Char, n: Int): Int =
      |      if n < s.length && s(n) != ch then position(ch, n + 1)
      |      else n
      |
      |  extension [T](x: T)
      |    def f: Int = g
      |    def g: Int = 123
      |}
      |""".stripMargin
  )
}
