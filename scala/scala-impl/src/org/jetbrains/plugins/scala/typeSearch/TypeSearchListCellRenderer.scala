package org.jetbrains.plugins.scala.typeSearch

import java.awt.{Color, Component}
import javax.swing.{JLabel, JList, ListCellRenderer}


class TypeSearchListCellRenderer extends JLabel with ListCellRenderer[StdFunctionRef] {
  setOpaque(true)

  override def getListCellRendererComponent(list: JList[_ <: StdFunctionRef], value: StdFunctionRef, index: Int,
                                            isSelected: Boolean, cellHasFocus: Boolean): Component = {

    // Adjust printed text
    setText(value.getPrettyFQName + ": " + value.getPrettyContext)

    // Set colors
    var background: Color = null
    var foreground: Color = null
    val dropLocation = list.getDropLocation

    // check if this cell is selected
    if (dropLocation != null && !dropLocation.isInsert && dropLocation.getIndex == index) {
      background = Color.BLUE
      foreground = Color.WHITE

    }
    else if (isSelected) {
      background = Color.PINK
      foreground = Color.DARK_GRAY

    }
    else {
      background = null
      foreground = Color.LIGHT_GRAY
    }

    setBackground(background)
    setForeground(foreground)
    this
  }
}