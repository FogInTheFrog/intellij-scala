package org.jetbrains.plugins.scala.dfa
package analysis

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DataFlowAnalysisSpec extends AnyFunSuite with Matchers {
  class Source
  private lazy val (arg, argSource, const, constSource, graph) = {
    val builder = cfg.Builder.newBuilder[Source]()

    val argSource = new Source
    val constSource = new Source

    val (_, argValue) = builder.addArgument("arg", new AnyRef)
    builder.addSourceInfo(argValue, argSource)

    val constValue = builder.constant(DfInt(10))
    builder.addSourceInfo(constValue, constSource)

    val graph = builder.finish()
    (
      graph.valueForSource(argSource),
      argSource,
      graph.valueForSource(constSource),
      constSource,
      graph
    )
  }

  test("run without initializing") {
    val dfa = TestDfa(graph)
    dfa.run()

    dfa.hasFinished shouldBe true

    val results = dfa.finishedStates
    results.size shouldBe 1

    dfa.inspect(arg) shouldBe DfAny.Top
    dfa.inspect(const) shouldBe DfInt(10)
  }

  test("run with args") {
    val dfa = TestDfa(graph)
    dfa.init(Seq(DfInt(1)))
    dfa.run()

    dfa.inspect(arg) shouldBe DfInt(1)
  }

  test("wrong numbers of args") {
    val dfa = TestDfa(graph)

    an [AssertionError] should be thrownBy
      dfa.init(Seq(DfBool.True, DfBool.False))
  }

  test("dfa results") {
    val dfa = TestDfa(graph)
    dfa.run()

    val result = dfa.result
    result.valueOf(argSource) shouldBe DfAny.Top
    result.valueOf(constSource) shouldBe DfInt(10)
  }
}
