package org.jetbrains.plugins.scala
package actions

import _root_.com.intellij.codeInsight.hint.{HintUtil, HintManager, HintManagerImpl}
import _root_.com.intellij.codeInsight.{TargetElementUtilBase}
import _root_.com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, PlatformDataKeys}
import _root_.com.intellij.psi._
import _root_.com.intellij.psi.util.PsiUtilBase
import _root_.com.intellij.util.ui.UIUtil
import _root_.java.awt.event.{MouseEvent, MouseMotionAdapter}
import _root_.com.intellij.openapi.ui.{MultiLineLabelUI}
import _root_.com.intellij.openapi.editor.{Editor}
import _root_.org.jetbrains.plugins.scala.lang.psi.types.result.TypingContext
import _root_.org.jetbrains.plugins.scala.ScalaBundle
import lang.psi.api.statements.{ScFunction, ScFunctionDeclaration, ScFunctionDefinition}
import lang.psi.api.statements.params.ScParameter
import lang.psi.api.base.patterns.{ScBindingPattern, ScReferencePattern}
import lang.psi.api.base.ScFieldId
import lang.psi.types.{ScType, ScSubstitutor}
import javax.swing.JLabel
import org.jetbrains.plugins.scala.extensions._
import lang.refactoring.util.ScalaRefactoringUtil
import lang.psi.api.expr.ScExpression
import java.awt.{Color, Point}
import com.intellij.openapi.util.JDOMUtil
import com.intellij.ui.{HintHint, LightweightHint}

/**
 * Pavel.Fatin, 16.04.2010
 */

class ShowTypeInfoAction extends AnAction(ScalaBundle.message("type.info")) {
  def actionPerformed(e: AnActionEvent) {
    val context = e.getDataContext
    val editor = PlatformDataKeys.EDITOR.getData(context)
    
    if(editor == null) return
    
    val file = PsiUtilBase.getPsiFileInEditor(editor, PlatformDataKeys.PROJECT.getData(context))

    val selectionModel = editor.getSelectionModel
    if (selectionModel.hasSelection) {
      import selectionModel._
      val a: Option[(ScExpression, ScType)] = ScalaRefactoringUtil.getExpression(file.getProject, editor, file, getSelectionStart, getSelectionEnd)
      a.foreach {
        case (expr, tpe) =>
          val tpeWithoutImplicits = expr.getTypeWithoutImplicits(TypingContext.empty).toOption
          val tpeWithoutImplicitsText = tpeWithoutImplicits.map(_.presentableText)

          val tpeText = tpe.presentableText
          val expectedTypeText = expr.expectedType.map(_.presentableText)

          val hint = (tpeWithoutImplicitsText, expectedTypeText) match {
            case (None | Some(`tpeText`), None) => tpeText
            case (Some(originalTypeText), None) =>
              """|Type:  %s
                 |Original Type: %s""".format(tpeText, originalTypeText).stripMargin
            case (Some(tpeWithoutImplicitsText), Some(expectedTypeText)) =>
              """|Type: %s
                 |Original Type: %s
                 |Expected Type: %s""".format(tpeText, tpeWithoutImplicitsText, expectedTypeText).stripMargin
          }

          showTypeHint(editor, hint)
      }
    } else {
      val offest = TargetElementUtilBase.adjustOffset(editor.getDocument,
        editor.logicalPositionToOffset(editor.getCaretModel.getLogicalPosition))

      val hint = file.findReferenceAt(offest) match {
        case Resolved(e, subst) => typeOf(e, subst)
        case _ => {
          file.findElementAt(offest) match {
            case Parent(p) => typeOf(p, ScSubstitutor.empty)
            case _ => None
          }
        }
      }

      hint.foreach(showTypeHint(editor, _))
    }
  }

  val typeOf: (PsiElement, ScSubstitutor) => Option[String] = {
    case (e: ScFunction, s) => e.returnType.toOption.map(s.subst(_)).map(_.presentableText)
    case (e: ScBindingPattern, s) => e.getType(TypingContext.empty).toOption.map(s.subst(_)).map(_ .presentableText)
    case (e: ScFieldId, s) => e.getType(TypingContext.empty).toOption.map(s.subst(_)).map(_ .presentableText)
    case (e: ScParameter, s) => e.getRealParameterType(TypingContext.empty).toOption.map(s.subst(_)).map(_ .presentableText)
    case (e: PsiMethod, s) => e.getReturnType.toOption.
      map(p => s.subst(ScType.create(p, e.getProject, e.getResolveScope))).map(_ presentableText)
    case (e: PsiVariable, s) => e.getType.toOption.
      map(p => s.subst(ScType.create(p, e.getProject, e.getResolveScope))).map(_ presentableText)
    case _ => None
  }



  def showTypeHint(editor: Editor, text: String) {
    val label = HintUtil.createInformationLabel(text)
    label.setFont(UIUtil.getLabelFont)

    val hint: LightweightHint = new LightweightHint(label)

    val hintManager: HintManagerImpl = HintManagerImpl.getInstanceImpl

    label.addMouseMotionListener(new MouseMotionAdapter {
      override def mouseMoved(e: MouseEvent) {
        hintManager.hideAllHints()
      }
    })

    val position = editor.getCaretModel.getLogicalPosition
    val p: Point = HintManagerImpl.getHintPosition(hint, editor, position, HintManager.ABOVE)

    hintManager.showEditorHint(hint, editor, p, 
      HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE | HintManager.HIDE_BY_SCROLLING, 0, false)
  }
}



