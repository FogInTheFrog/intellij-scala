package org.jetbrains.plugins.scala
package lang
package parser
package parsing
package statements

import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.parser.parsing.builder.ScalaPsiBuilder
import org.jetbrains.plugins.scala.lang.parser.parsing.expressions.{Block, ConstrExprInIndentationRegion, ExprInIndentationRegion}
import org.jetbrains.plugins.scala.lang.parser.parsing.params.{FunTypeParamClause, ParamClauses, Params}
import org.jetbrains.plugins.scala.lang.parser.parsing.types.Type

/**
* @author Alexander Podkhalyuzin
* Date: 13.02.2008
*/

/*
 * FunDef ::= FunSig [':' Type] '=' Expr
 *          | FunSig [nl] '{' Block '}'
 *          | 'this' ParamClause ParamClauses
 *            ('=' ConstrExpr | [nl] ConstrBlock)
 */
object FunDef extends ParsingRule {

  override def apply()(implicit builder: ScalaPsiBuilder): Boolean = {
    val faultMarker = builder.mark
    builder.getTokenType match {
      case ScalaTokenTypes.kDEF => builder.advanceLexer()
      case _ =>
        faultMarker.drop()
        return false
    }

    builder.getTokenType match {
      case ScalaTokenTypes.tIDENTIFIER =>
        FunSig()
        builder.getTokenType match {
          case ScalaTokenTypes.tCOLON =>
            builder.advanceLexer() //Ate :
            if (Type.parse(builder)) {
              builder.getTokenType match {
                case ScalaTokenTypes.tASSIGN =>
                  builder.advanceLexer() //Ate =
                  if (ExprInIndentationRegion()) {
                    faultMarker.drop()
                    true
                  }
                  else {
                    builder error ScalaBundle.message("wrong.expression")
                    faultMarker.drop()
                    true
                  }
                case _ =>
                  faultMarker.rollbackTo()
                  false
              }
            }
            else {
              faultMarker.rollbackTo()
              false
            }
          case ScalaTokenTypes.tASSIGN =>
            builder.advanceLexer() //Ate =
            builder.skipExternalToken()

            if (ExprInIndentationRegion()) {
              faultMarker.drop()
              true
            }
            else {
              builder error ScalaBundle.message("wrong.expression")
              faultMarker.drop()
              true
            }
          case ScalaTokenTypes.tLBRACE =>
            if (builder.twoNewlinesBeforeCurrentToken) {
              faultMarker.rollbackTo()
              return false
            }
            Block.parse(builder, hasBrace = true)
            faultMarker.drop()
            true
          case _ =>
            faultMarker.rollbackTo()
            false
        }
      case ScalaTokenTypes.kTHIS =>
        builder.advanceLexer() //Ate this
        ParamClauses parse(builder, expectAtLeastOneClause = true)

        // just parse a type annotation here, even though it is not correct
        if (builder.getTokenType == ScalaTokenTypes.tCOLON) {
          val wrongTypeMarker = builder.mark()
          builder.advanceLexer() // Ate :
          Type.parse(builder)
          wrongTypeMarker error ScalaBundle.message("auxiliary.constructor.may.not.have.a.type.annotation")
        }

        builder.getTokenType match {
          case ScalaTokenTypes.tASSIGN =>
            builder.advanceLexer() //Ate =
            if (!ConstrExprInIndentationRegion()) {
              builder error ScalaBundle.message("wrong.constr.expression")
            }
            faultMarker.drop()
            true
          case _ =>
            if (builder.twoNewlinesBeforeCurrentToken || !ConstrBlock()) {
              builder error ScalaBundle.message("auxiliary.constructor.definition.expected")
            }
            faultMarker.drop()
            true
        }
      case _ =>
        faultMarker.rollbackTo()
        false
    }
  }
}