package org.jetbrains.plugins.scala.failed.annotator

import org.jetbrains.plugins.scala.base.ScalaLightCodeInsightFixtureTestAdapter

/**
  * Created by Anton Yalyshev on 10/01/18.
  */

class ScalaAnnotationTest extends ScalaLightCodeInsightFixtureTestAdapter {

  override protected def shouldPass: Boolean = false

  def testSCL10352(): Unit = {
    checkTextHasNoErrors(
      """
        |class someAnnotation extends scala.annotation.StaticAnnotation
        |
        |class BlockAnnotationExample {
        |  def hello: String = {
        |    {
        |      println("Something")
        |    }: @someAnnotation
        |
        |    "Hello world"
        |  }
        |}
      """.stripMargin)
  }
}
