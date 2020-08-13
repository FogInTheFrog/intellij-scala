package org.jetbrains.plugins.scala.dfa.lattice

import org.jetbrains.plugins.scala.dfa.{JoinSemiLattice, MeetSemiLattice}

import scala.annotation.tailrec


/**
 * Lattice type class implementation for binary lattices
 *
 * A binary lattice has only a Top and a Bottom
 *
 *     Top
 *      |
 *      |
 *    Bottom
 *
 */
final class BinaryLattice[T](override val top: T, override val bottom: T)
  extends JoinSemiLattice[T] with MeetSemiLattice[T]
{
  override def <=(subSet: T, superSet: T): Boolean =
    superSet == top || subSet == bottom

  override def intersects(lhs: T, rhs: T): Boolean =
    lhs == top && rhs == top

  override def join(lhs: T, rhs: T): T =
    if (lhs == rhs) lhs else top

  override def joinAll(first: T, others: TraversableOnce[T]): T =
    if (first == top || others.exists(_ == top)) top else bottom

  override def meet(lhs: T, rhs: T): T =
    if (lhs == rhs) lhs else bottom

  override def meetAll(first: T, others: TraversableOnce[T]): T =
    if (first == top && others.forall(_ == top)) top else bottom
}