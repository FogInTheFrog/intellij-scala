package org.jetbrains.plugins.scala.lang.dfa.controlFlow.transformations

import com.intellij.codeInspection.dataFlow.java.inst.{BooleanBinaryInstruction, NumericBinaryInstruction}
import com.intellij.codeInspection.dataFlow.lang.ir.ControlFlow.DeferredOffset
import com.intellij.codeInspection.dataFlow.lang.ir._
import com.intellij.codeInspection.dataFlow.types.DfTypes
import com.intellij.codeInspection.dataFlow.value.RelationType
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.dfa.controlFlow.ScalaDfaControlFlowBuilder
import org.jetbrains.plugins.scala.lang.dfa.controlFlow.invocations.arguments.Argument
import org.jetbrains.plugins.scala.lang.dfa.controlFlow.invocations.{InvocationInfo, InvokedElement}
import org.jetbrains.plugins.scala.lang.dfa.framework.ScalaStatementAnchor
import org.jetbrains.plugins.scala.lang.dfa.utils.ScalaDfaTypeUtils.LogicalOperation
import org.jetbrains.plugins.scala.lang.dfa.utils.SpecialSupportUtils._
import org.jetbrains.plugins.scala.lang.psi.api.expr.{MethodInvocation, ScExpression, ScMethodCall, ScReferenceExpression}
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.synthetic.ScSyntheticFunction

class InvocationTransformer(val wrappedInvocation: ScExpression)
  extends ExpressionTransformer(wrappedInvocation) {

  override def toString: String = s"InvocationTransformer: $wrappedInvocation"

  override def transform(builder: ScalaDfaControlFlowBuilder): Unit = {
    val invocationsInfo = wrappedInvocation match {
      case methodCall: ScMethodCall => InvocationInfo.fromMethodCall(methodCall)
      case methodInvocation: MethodInvocation => List(InvocationInfo.fromMethodInvocation(methodInvocation))
      case referenceExpression: ScReferenceExpression => List(InvocationInfo.fromReferenceExpression(referenceExpression))
      case _ => throw TransformationFailedException(wrappedInvocation,
        "Expression of this type cannot be transformed as an invocation.")
    }

    if (!tryTransformIntoSpecialRepresentation(invocationsInfo, builder)) {
      invocationsInfo.foreach(transformMethodInvocation(_, builder))
    }
  }

  // TODO extract later to a special IR instruction + add special support
  private def transformMethodInvocation(invocationInfo: InvocationInfo, builder: ScalaDfaControlFlowBuilder): Unit = {
    val args = invocationInfo.argListsInEvaluationOrder.flatten
    args.foreach(_.content.transform(builder))
    builder.popArguments(args.size)
    builder.pushUnknownValue()
    builder.pushInstruction(new FlushFieldsInstruction)
  }

  private def tryTransformIntoSpecialRepresentation(invocationsInfo: Seq[InvocationInfo], builder: ScalaDfaControlFlowBuilder): Boolean = {
    if (invocationsInfo.size > 1) return false
    val invocationInfo = invocationsInfo.head

    invocationInfo.invokedElement match {
      case Some(InvokedElement(psiElement)) => psiElement match {
        case function: ScSyntheticFunction => tryTransformSyntheticFunctionSpecially(function, invocationInfo, builder)
        case _ => false
      }
      case _ => false
    }
  }

  private def tryTransformSyntheticFunctionSpecially(function: ScSyntheticFunction, invocationInfo: InvocationInfo,
                                                     builder: ScalaDfaControlFlowBuilder): Boolean = {
    if (!verifyArgumentsForBinarySyntheticOperator(invocationInfo.argListsInEvaluationOrder)) false
    else if (tryTransformNumericOperations(function, invocationInfo, builder)) true
    else if (tryTransformRelationalExpressions(function, invocationInfo, builder)) true
    else if (tryTransformLogicalOperations(function, invocationInfo, builder)) true
    else false
  }

  private def matchesSignature(function: ScSyntheticFunction, functionName: String, returnedClass: String): Boolean = {
    val properReturnedClass = function.retType.extractClass match {
      case Some(returnClass) if returnClass.qualifiedName == returnedClass => true
      case _ => false
    }
    function.name == functionName && properReturnedClass
  }

  private def verifyArgumentsForBinarySyntheticOperator(arguments: List[List[Argument]]): Boolean = {
    arguments.size == 1 && arguments.head.size == 2
  }

  private def argumentsForBinarySyntheticOperator(invocationInfo: InvocationInfo): (Argument, Argument) = {
    val args = invocationInfo.argListsInEvaluationOrder
    assert(verifyArgumentsForBinarySyntheticOperator(args))
    val List(leftArg, rightArg) = args.head
    (leftArg, rightArg)
  }

  private def tryTransformNumericOperations(function: ScSyntheticFunction, invocationInfo: InvocationInfo,
                                            builder: ScalaDfaControlFlowBuilder): Boolean = {
    for (typeClass <- NumericTypeClasses; operationName <- NumericOperations.keys) {
      if (matchesSignature(function, operationName, typeClass)) {
        val operation = NumericOperations(operationName)
        val (leftArg, rightArg) = argumentsForBinarySyntheticOperator(invocationInfo)
        leftArg.content.transform(builder)
        // TODO check implicit conversions etc.
        // TODO check division by zero
        rightArg.content.transform(builder)
        builder.pushInstruction(new NumericBinaryInstruction(operation, ScalaStatementAnchor(wrappedInvocation)))
        return true
      }
    }

    false
  }

  private def tryTransformRelationalExpressions(function: ScSyntheticFunction, invocationInfo: InvocationInfo,
                                                builder: ScalaDfaControlFlowBuilder): Boolean = {
    for (operationName <- RelationalOperations.keys) {
      if (matchesSignature(function, operationName, BooleanTypeClass)) {
        val operation = RelationalOperations(operationName)
        val forceEqualityByContent = operation == RelationType.EQ || operation == RelationType.NE
        val (leftArg, rightArg) = argumentsForBinarySyntheticOperator(invocationInfo)
        leftArg.content.transform(builder)
        // TODO check implicit conversions etc.
        // TODO check division by zero
        rightArg.content.transform(builder)
        builder.pushInstruction(new BooleanBinaryInstruction(operation, forceEqualityByContent,
          ScalaStatementAnchor(wrappedInvocation)))
        return true
      }
    }

    false
  }

  private def tryTransformLogicalOperations(function: ScSyntheticFunction, invocationInfo: InvocationInfo,
                                            builder: ScalaDfaControlFlowBuilder): Boolean = {
    for (operationName <- LogicalOperations.keys) {
      if (matchesSignature(function, operationName, BooleanTypeClass)) {
        val operation = LogicalOperations(operationName)
        val (leftArg, rightArg) = argumentsForBinarySyntheticOperator(invocationInfo)

        val anchor = ScalaStatementAnchor(wrappedInvocation)
        val endOffset = new DeferredOffset
        val nextConditionOffset = new DeferredOffset

        leftArg.content.transform(builder)

        val valueNeededToContinue = operation == LogicalOperation.And
        builder.pushInstruction(new ConditionalGotoInstruction(nextConditionOffset,
          DfTypes.booleanValue(valueNeededToContinue)))
        builder.pushInstruction(new PushValueInstruction(DfTypes.booleanValue(!valueNeededToContinue), anchor))
        builder.pushInstruction(new GotoInstruction(endOffset))

        builder.setOffset(nextConditionOffset)
        builder.pushInstruction(new FinishElementInstruction(null))
        rightArg.content.transform(builder)
        builder.setOffset(endOffset)
        builder.pushInstruction(new ResultOfInstruction(anchor))
        return true
      }
    }

    false
  }
}
