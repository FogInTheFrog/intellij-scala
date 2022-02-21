package org.jetbrains.plugins.scala.typeSearch

import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.ide.actions.SearchEverywherePsiRenderer
import com.intellij.openapi.wm.impl.ProjectWindowAction
import com.intellij.ui.{ColoredListCellRenderer, JBColor}
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.wm.impl.ProjectWindowAction
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JList
import java.awt.{Color, Component}
import javax.swing.{JLabel, JList, ListCellRenderer}

case class


class TypeSearchListCellRenderer extends SearchEverywherePsiRenderer(this) {
  override def customizeNonPsiElementLeftRenderer(renderer: ColoredListCellRenderer[_],
                                                  list: JList[_], value: Any,
                                                  index: Int, selected: Boolean, hasFocus: Boolean): Boolean = {
    if (renderer == null) {
      return false
    }
    if (list == null) {
      return false
    }
//    match(value) {
//      case ReopenProjectAction -> renderRecentProject(list, renderer, value)
//      case ProjectWindowAction -> renderOpenProject(list, renderer, value)
//    }
    true
  }

  private def renderProject (list: JList[StdFunctionRef], projectName: String, projectLocation: String,
                             renderer: ColoredListCellRenderer[Any]): Unit = {
    appendName(list, renderer, projectName)
    appendLocation(projectLocation, renderer)
    val projectOrAppIcon = RecentProjectsManagerBase.instanceEx.getProjectOrAppIcon(projectLocation)
    renderer.icon = projectOrAppIcon
  }

  private def appendName (list: JList[StdFunctionRef], renderer: ColoredListCellRenderer[Any], projectName: String) {
    val color = list.getForeground
    val nameAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, color)
    renderer.append(projectName, nameAttributes)
  }

  private def appendLocation (projectLocation: String, renderer: ColoredListCellRenderer[Any]) {
    if (!StringUtil.isEmpty(projectLocation)) {
      renderer.append(projectLocation, new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GRAY))
    }
  }

}