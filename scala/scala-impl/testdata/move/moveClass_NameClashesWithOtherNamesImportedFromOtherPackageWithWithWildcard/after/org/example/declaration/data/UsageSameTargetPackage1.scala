package org.example.declaration.data

import scala.util._

object UsageSameTargetPackage1 {
  def main(args: Array[String]): Unit = {
    println(this.getClass)
    println(classOf[Random])
    println(Properties.versionString)
    println()

    val x: X = ???
  }
}