package org.jetbrains.plugins.scala.lang.dfa.controlFlow.transformations.tests

import org.jetbrains.plugins.scala.lang.dfa.controlFlow.transformations.ScalaDfaControlFlowBuilderTestBase

class ReferenceExpressionControlFlowTest extends ScalaDfaControlFlowBuilderTestBase {

  def testReferencesToMethodArgs(): Unit = test(codeFromMethodBody(returnType = "Int") {
    """
      |val x = 15
      |val y = 2193 + 2
      |arg1 + x * arg2
      |x + y
      |x
      |""".stripMargin
  }) {
    """
      |0: PUSH_VAL 15
      |1: ASSIGN_TO x
      |2: POP
      |3: PUSH_VAL 2193
      |4: PUSH_VAL 2
      |5: NUMERIC_OP +
      |6: ASSIGN_TO y
      |7: POP
      |8: PUSH arg1
      |9: PUSH x
      |10: PUSH arg2
      |11: NUMERIC_OP *
      |12: NUMERIC_OP +
      |13: POP
      |14: PUSH x
      |15: PUSH y
      |16: NUMERIC_OP +
      |17: POP
      |18: PUSH x
      |19: FINISH BlockExpression
      |20: RETURN
      |21: POP
      |22: RETURN
      |""".stripMargin
  }

  def testUnknownReferences(): Unit = test(codeFromMethodBody(returnType = "Int") {
    """
      |val x = 15
      |val y = x + `k` * anotherUnknown
      |y + 2
      |""".stripMargin
  }) {
    """
      |0: PUSH_VAL 15
      |1: ASSIGN_TO x
      |2: POP
      |3: PUSH x
      |4: PUSH_VAL TOP
      |5: FLUSH_ALL_FIELDS
      |6: PUSH_VAL TOP
      |7: FLUSH_ALL_FIELDS
      |8: CALL <unknown>
      |9: NUMERIC_OP +
      |10: ASSIGN_TO y
      |11: POP
      |12: PUSH y
      |13: PUSH_VAL 2
      |14: NUMERIC_OP +
      |15: FINISH BlockExpression
      |16: RETURN
      |17: POP
      |18: RETURN
      |""".stripMargin
  }

  def testLiteralIdentifierReferences(): Unit = test(codeFromMethodBody(returnType = "Int") {
    """
      |val `some strange name` = if (3 > 2) 5 else 9
      |val `another strange name!` = 3 == 3
      |if (`another strange name!`) `some strange name`
      |else 3
      |""".stripMargin
  }) {
    """
      |0: PUSH_VAL 3
      |1: PUSH_VAL 2
      |2: BOOLEAN_OP >
      |3: IF_EQ false 7
      |4: FINISH
      |5: PUSH_VAL 5
      |6: GOTO 9
      |7: FINISH
      |8: PUSH_VAL 9
      |9: FINISH IfStatement
      |10: ASSIGN_TO `some strange name`
      |11: POP
      |12: PUSH_VAL 3
      |13: PUSH_VAL 3
      |14: BOOLEAN_OP ==
      |15: ASSIGN_TO `another strange name!`
      |16: POP
      |17: PUSH `another strange name!`
      |18: IF_EQ false 22
      |19: FINISH
      |20: PUSH `some strange name`
      |21: GOTO 24
      |22: FINISH
      |23: PUSH_VAL 3
      |24: FINISH IfStatement; flushing [`some strange name`]
      |25: FINISH BlockExpression
      |26: RETURN
      |27: POP
      |28: RETURN
      |""".stripMargin
  }
}
