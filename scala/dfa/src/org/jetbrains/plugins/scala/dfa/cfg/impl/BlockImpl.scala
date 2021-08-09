package org.jetbrains.plugins.scala.dfa
package cfg
package impl

import scala.collection.SeqView

private final class BlockImpl[Info](override val name: String,
                                    override val index: Int,
                                    override val nodeBegin: Int,
                                    override val incoming: Seq[Block]) extends Block {
  override type SourceInfo = Info

  var _graph: Graph[Info] = _
  var _endIndex: Int = -1
  var _outgoing: Seq[Block] = Seq.empty

  override def graph: Graph[Info] = _graph

  override def nodeEnd: Int = _endIndex

  override def nodes: SeqView[Node] = graph.nodes.view.slice(nodeBegin, nodeEnd)

  override def outgoing: Seq[Block] = _outgoing

  override def nodeIndices: Range = nodeBegin until nodeEnd

  def sanityCheck(): Unit = {
    assert(_graph != null)
    assert(_endIndex >= 0)
    assert(_outgoing != null)
  }
}
