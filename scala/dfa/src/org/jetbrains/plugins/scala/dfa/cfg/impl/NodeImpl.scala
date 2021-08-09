package org.jetbrains.plugins.scala.dfa
package cfg
package impl

private abstract class NodeImpl { this: cfg.Node =>

  final var _block: BlockImpl = _
  final var _index: Int = -1

  override final def index: Int = _index.ensuring(_ >= 0)

  override final def block: Block = _block
  override final def graph: Graph[_] = _block.graph

  override final def labelString: String = s".${block.name}[${block.index}]"

  override def toString: String = asmString()

  override def asmString(showIndex: Boolean = false, showLabel: Boolean = false, indent: Boolean = false, maxIndexHint: Int = 99): String = {
    val builder = new StringBuilder

    val indexPrefix =
      if (showIndex) s"%0${maxIndexHint.toString.length}d ".format(index)
      else ""

    if (showLabel) {
      builder ++= indexPrefix.map(_ => ' ')
      builder ++= labelString
      builder ++= ":\n"
    }

    builder ++= indexPrefix

    if (indent) {
      builder ++= "  "
    }

    builder ++= asmString

    builder.result()
  }

  protected def asmString: String

  def sanityCheck(): Unit = {
    assert(_block != null)
    assert(_index >= 0)
  }
}
