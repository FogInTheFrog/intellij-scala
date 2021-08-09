package org.jetbrains.plugins.scala.dfa
package analysis

import org.jetbrains.plugins.scala.dfa.analysis.NodeInstance.Controller
import org.jetbrains.plugins.scala.dfa.cfg.CallInfo

abstract class NodeInstance extends Cloneable {
  def node: cfg.Node
  def process(state: State, controller: Controller): Unit
  def reset(): Unit = ()

  @inline
  final protected def nextNodeIndex: Int = node.index + 1
}



object NodeInstance {
  abstract class Controller {
    def arguments: Seq[DfAny]
    def enqueue(index: Int, state: State): Unit
    def addEndState(state: State): Unit

    def specialMethodProcessor(callInfo: CallInfo, call: cfg.Call): Option[SpecialMethodProcessor]
  }
}