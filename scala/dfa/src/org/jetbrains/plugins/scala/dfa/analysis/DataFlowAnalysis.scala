package org.jetbrains.plugins.scala.dfa
package analysis

import org.jetbrains.plugins.scala.dfa.analysis.DataFlowAnalysis.{SpecialMethodProcessorFactories, WQItem}
import org.jetbrains.plugins.scala.dfa.analysis.impl.createNodeInstance
import org.jetbrains.plugins.scala.dfa.cfg.CallInfo

import scala.collection.immutable.ArraySeq
import scala.collection.mutable

final class DataFlowAnalysis[Info](val graph: cfg.Graph[Info],
                                   specialMethodProcessorFactories: SpecialMethodProcessorFactories) {
  private val items: ArraySeq[WQItem] =
    graph.nodes.iterator
      .zipWithIndex
      .map { case (node, index) => new WQItem(index, node) }
      .to(ArraySeq)
  private val workQueue = mutable.PriorityQueue.empty[WQItem](Ordering.by(_.index))
  private var endStates = List.empty[State]

  private var controller: NodeInstance.Controller = _

  locally {
    init()
  }

  private def addToQueue(to: Int, state: State): Unit = {
    val item = items(to)
    item.states ::= state
    if (!item.enqueued) {
      item.enqueued = true
      workQueue.enqueue(item)
    }
  }

  def hasFinished: Boolean = workQueue.isEmpty
  def finishedStates: Seq[State] = endStates

  def init(args: Seq[DfAny] = Seq.fill(graph.arguments.size)(DfAny.Top)): Unit = {
    assert(args.size == graph.arguments.size)

    workQueue.clear()
    endStates = Nil
    items.foreach(_.reset())

    controller = new NodeInstance.Controller {
      override def arguments: Seq[DfAny] = args
      override def enqueue(index: Int, state: State): Unit = addToQueue(index, state)
      override def addEndState(state: State): Unit = endStates ::= state
      override def specialMethodProcessor(callInfo: CallInfo, call: cfg.Call): Option[SpecialMethodProcessor] =
        specialMethodProcessorFactories.get(callInfo).map(_.apply(call))
    }

    addToQueue(0, State.from(graph))
  }

  def step(): Unit = {
    val item = workQueue.dequeue()
    item.enqueued = false

    val blockIdx = item.node.block.index
    val instance = item.instance
    for (state <- item.extractStates()) {
      state.blockActive(blockIdx) = true
      instance.process(state, controller)
    }
  }

  def run(): Unit =
    while (!hasFinished) step()

  def inspect(value: cfg.Value): DfAny = {
    val idx = value.valueId
    val blockIdx = value.block.index

    assert(hasFinished)
    assert(graph.values.lift(idx).contains(value))

    val valuesFromAllStates =
      finishedStates.iterator
        .filter(_.blockActive(blockIdx))
        .map(_.values(idx))

    join(valuesFromAllStates)
  }

  def result: DfaResult[Info] =
    DfaResult(graph, graph.values.map(inspect))
}

object DataFlowAnalysis {
  type SpecialMethodProcessorFactories = Map[CallInfo, cfg.Call => SpecialMethodProcessor]

  private class WQItem(val index: Int, val node: cfg.Node) {
    val shouldDispose: Boolean = true // node.block.incoming.sizeIs <= 1
    val instance: NodeInstance = createNodeInstance(node)
    var states: List[State] = Nil
    var enqueued: Boolean = false

    def extractStates(): Iterator[State] = {
      val it = states.iterator
      if (shouldDispose) {
        states = Nil
      }
      it
    }

    def reset(): Unit = {
      states = Nil
      enqueued = false
    }
  }
}