package org.jetbrains.plugins.scala.typeSearch

class OperatorCharacterDecoder() {
  val decodeMap = Map(
    "\\$eq" -> "=",
    "\\$greater" -> ">",
    "\\$less" -> "<",
    "\\$plus" -> "+",
    "\\$minus" -> "-",
    "\\$times" -> "*",
    "\\$div" -> "/",
    "\\$bslash" -> "\\",
    "\\$bar" -> "|",
    "\\$bang" -> "!",
    "\\$qmark" -> "?",
    "\\$colon" -> ":",
    "\\$up" -> "^",
    "\\$amp" -> "&",
    "\\$hash" -> "@",
    "\\$tilde" -> "~",
    "\\$at" -> "@",
    "\\$percent" -> "%")

  def decodeString(input: String): String = {
    var temp = input
    for (s <- decodeMap) {
      temp = temp.replaceAll(s._1, s._2)
    }

    temp
  }
}

class OperatorCharacterEncoder() {
  def encodeCharacter(x: Char): String = {
    x match {
      case '=' => "$eq"
      case '>' => "$greater"
      case '<' => "$less"
      case '+' => "$plus"
      case '-' => "$minus"
      case '*' => "$times"
      case '/' => "$div"
      case '\\' => "$bslash"
      case '|' => "$bar"
      case '!' => "$bang"
      case '?' => "$qmark"
      case ':' => "$colon"
      case '%' => "$percent"
      case '^' => "$up"
      case '&' => "$amp"
      case '@' => "$at"
      case '#' => "$hash"
      case '~' => "$tilde"
      case _ => x.toString
    }
  }

  def encodeString(s: String): String = {
    s.flatMap(c => encodeCharacter(c))
  }
}