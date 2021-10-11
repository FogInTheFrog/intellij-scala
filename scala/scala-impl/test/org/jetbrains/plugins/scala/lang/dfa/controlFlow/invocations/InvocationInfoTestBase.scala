package org.jetbrains.plugins.scala.lang.dfa.controlFlow.invocations

import org.jetbrains.plugins.scala.AssertionMatchers
import org.jetbrains.plugins.scala.base.{ScalaLightCodeInsightFixtureTestAdapter, SharedTestProjectToken}
import org.jetbrains.plugins.scala.extensions.ObjectExt
import org.jetbrains.plugins.scala.lang.dfa.controlFlow.invocations.InvocationExtractors.{extractExpressionFromArgument, extractInvocationUnderMarker}
import org.jetbrains.plugins.scala.lang.dfa.controlFlow.invocations.arguments.Argument
import org.jetbrains.plugins.scala.lang.dfa.controlFlow.invocations.arguments.Argument.{PassingMechanism, ProperArgument, ThisArgument}
import org.jetbrains.plugins.scala.lang.psi.api.expr.{MethodInvocation, ScMethodCall, ScReferenceExpression}
import org.jetbrains.plugins.scala.util.MarkersUtils
import org.junit.Assert.assertTrue

abstract class InvocationInfoTestBase extends ScalaLightCodeInsightFixtureTestAdapter with AssertionMatchers {

  override protected def sharedProjectToken: SharedTestProjectToken = SharedTestProjectToken(classOf[InvocationInfo])

  protected def markerStart: String = MarkersUtils.start()

  protected def markerEnd: String = MarkersUtils.end()

  protected def generateInvocationInfoFor(code: String, assertSingleInvocation: Boolean = true): InvocationInfo = {
    val (codeWithoutMarkers, ranges) = MarkersUtils.extractNumberedMarkers(code.strip)
    val actualFile = configureFromFileText(codeWithoutMarkers)

    extractInvocationUnderMarker(actualFile, ranges) match {
      case methodCall: ScMethodCall => val invocationsInfo = InvocationInfo.fromMethodCall(methodCall)
        if (assertSingleInvocation) invocationsInfo.size shouldBe 1
        invocationsInfo.head
      case methodInvocation: MethodInvocation => InvocationInfo.fromMethodInvocation(methodInvocation)
      case referenceExpression: ScReferenceExpression => InvocationInfo.fromReferenceExpression(referenceExpression)
    }
  }

  protected def verifyInvokedElement(invocationInfo: InvocationInfo, expectedText: String): Unit = {
    val actualText = invocationInfo.invokedElement.get.toString
    actualText shouldBe expectedText
  }

  protected def verifyArgumentsWithSingleArgList(invocationInfo: InvocationInfo, expectedArgCount: Int,
                                                 expectedProperArgsInText: List[String],
                                                 expectedMappedParamNames: List[String],
                                                 expectedPassingMechanisms: List[PassingMechanism],
                                                 expectedParamToArgMapping: List[Int],
                                                 isRightAssociative: Boolean = false): Unit = {
    invocationInfo.argListsInEvaluationOrder.size shouldBe 1
    val args = invocationInfo.argListsInEvaluationOrder.head
    val properArgs = invocationInfo.properArguments.flatten

    args.size shouldBe expectedArgCount
    args.count(_.kind == ThisArgument) shouldBe 1
    convertArgsToText(properArgs) shouldBe expectedProperArgsInText
    properArgs.map(_.kind.asInstanceOf[ProperArgument].parameterMapping.name) shouldBe expectedMappedParamNames
    args.map(_.passingMechanism) shouldBe expectedPassingMechanisms
    invocationInfo.paramToArgMapping.map(_.get) shouldBe expectedParamToArgMapping

    if (isRightAssociative) {
      assertTrue("In a right associative call, the first argument should be a proper argument", args.head.kind.is[ProperArgument])
      args(1).kind shouldBe ThisArgument
      args.head +: args.tail.tail shouldBe properArgs
    } else {
      args.head.kind shouldBe ThisArgument
      args.tail shouldBe properArgs
    }
  }

  protected def verifyArgumentsWithMultipleArgLists(invocationInfo: InvocationInfo, expectedArgCount: List[Int],
                                                    expectedProperArgsInText: List[List[String]],
                                                    expectedMappedParamNames: List[List[String]],
                                                    expectedPassingMechanisms: List[List[PassingMechanism]],
                                                    expectedParamToArgMapping: List[Int],
                                                    isRightAssociative: Boolean = false): Unit = {
    invocationInfo.argListsInEvaluationOrder.size shouldBe expectedArgCount.size
    val args = invocationInfo.argListsInEvaluationOrder
    val properArgs = invocationInfo.properArguments

    args.map(_.size) shouldBe expectedArgCount
    args.head.count(_.kind == ThisArgument) shouldBe 1
    assertTrue("\"This\" argument should only be present in the first argument list",
      args.tail.forall(_.count(_.kind == ThisArgument) == 0))
    properArgs.map(convertArgsToText) shouldBe expectedProperArgsInText
    properArgs.map(_.map(_.kind.asInstanceOf[ProperArgument].parameterMapping.name)) shouldBe expectedMappedParamNames
    args.map(_.map(_.passingMechanism)) shouldBe expectedPassingMechanisms
    invocationInfo.paramToArgMapping.map(_.get) shouldBe expectedParamToArgMapping

    if (isRightAssociative) {
      assertTrue("In a right associative call, the first argument should be a proper argument",
        args.head.head.kind.is[ProperArgument])
      args.head(1).kind shouldBe ThisArgument
    } else {
      args.head.head.kind shouldBe ThisArgument
    }
  }

  protected def verifyArgumentsInInvalidInvocation(invocationInfo: InvocationInfo, expectedArgCount: Int,
                                                   expectedProperArgsInText: List[String]): Unit = {
    invocationInfo.argListsInEvaluationOrder.size shouldBe 1
    val args = invocationInfo.argListsInEvaluationOrder.head
    val properArgs = invocationInfo.properArguments.flatten

    args.size shouldBe expectedArgCount
    args.count(_.kind == ThisArgument) shouldBe 1
    convertArgsToText(properArgs) shouldBe expectedProperArgsInText

    args.head.kind shouldBe ThisArgument
    args.tail shouldBe properArgs
  }

  protected def verifyThisExpression(invocationInfo: InvocationInfo, expectedExpressionInText: String): Unit = {
    val thisArgument = invocationInfo.thisArgument.get
    val thisExpression = extractExpressionFromArgument(thisArgument)
    thisExpression.getText shouldBe expectedExpressionInText
  }

  private def convertArgsToText(args: List[Argument]): List[String] = args.map(extractExpressionFromArgument).map(_.getText)
}
