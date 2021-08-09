package org.jetbrains.plugins.scala.dfa.lattice
package specific

/**
 * Lattice type class implementation for powersets
 *
 * In this lattice every element can be combined with all other elements.
 * Bottom is then the empty set, namely no element at all.
 * Because the height of the lattice is proportional to the amount of possible elements,
 * it is important to keep the amount of possible elements finite.
 * Otherwise the data flow analysis might not hal!.
 *
 * Here a lattice on 4 elements (many lattice elements are omitted)
 *
 * {{{
 *                    Top
 *                   /  \
 *                  /    \
 *          [A, B, C]   [B, C, D]
 *           /   \       /  \
 *         /      \     /    \
 *     [A, B]     [B, C]   [B, D]
 *      |   \    /         /
 *      |    \  /         /
 *     [A]    [B]        /
 *      \      |        /
 *       \     |       /
 *        \    |      /
 *        [] (= Bottom)
 * }}}
 */
abstract class PowerSetLattice[T](override val top: T, override val bottom: T)
  extends JoinSemiLattice[T] with MeetSemiLattice[T] {

  override def <=(subSet: T, superSet: T): Boolean = (subSet, superSet) match {
    case (`bottom`, _) => true
    case (_, `bottom`) => false
    case (_, `top`) => true
    case (`top`, _) => false
    case (PowerSetBase(subSet), PowerSetBase(superSet)) => subSet.subsetOf(superSet)
    case (PowerSetBase(_), _) => false
    case (sub, PowerSetBase(sup)) => sup.contains(sub)
    case (sub, sup) => sub == sup
  }

  override def intersects(lhs: T, rhs: T): Boolean = (lhs, rhs) match {
    case (`bottom`, _) => false
    case (_, `bottom`) => false
    case (_, `top`) => true
    case (`top`, _) => true
    case (PowerSetBase(lhs), PowerSetBase(rhs)) => lhs.exists(rhs.contains)
    case (PowerSetBase(lhs), rhs) => lhs.contains(rhs)
    case (lhs, PowerSetBase(rhs)) => rhs.contains(lhs)
    case (sub, sup) => sub == sup
  }

  override def join(lhs: T, rhs: T): T = (lhs, rhs) match {
    case (`bottom`, rhs) => rhs
    case (lhs, `bottom`) => lhs
    case (_, `top`) => top
    case (`top`, _) => top
    case (PowerSetBase(lhs), PowerSetBase(rhs)) => createSet(lhs union rhs)
    case (PowerSetBase(lhs), rhs) => createSet(lhs + rhs)
    case (lhs, PowerSetBase(rhs)) => createSet(rhs + lhs)
    case (sub, sup) => createSet(Set(sub, sup))
  }

  override def meet(lhs: T, rhs: T): T = (lhs, rhs) match {
    case (`bottom`, _) => bottom
    case (_, `bottom`) => bottom
    case (lhs, `top`) => lhs
    case (`top`, rhs) => rhs
    case (PowerSetBase(lhs), PowerSetBase(rhs)) => createSet(lhs intersect rhs)
    case (PowerSetBase(lhs), rhs) => meetSetAndValue(lhs, rhs)
    case (lhs, PowerSetBase(rhs)) => meetSetAndValue(rhs, lhs)
    case (sub, sup) => if (sub == sup) sub else bottom
  }

  private def meetSetAndValue(set: Set[T], other: T): T =
    if (set.contains(other)) other else bottom

  protected def createSet(elements: Set[T]): T

  protected trait PowerSetBase {
    def elements: Set[T]

    // we need this to have a reference to outer which can be checked in pattern matching
    def _lattice: PowerSetLattice[T] = PowerSetLattice.this
  }

  private final object PowerSetBase {
    def unapply(powerSet: PowerSetBase): Some[Set[T]] = Some(powerSet.elements)
  }
}
