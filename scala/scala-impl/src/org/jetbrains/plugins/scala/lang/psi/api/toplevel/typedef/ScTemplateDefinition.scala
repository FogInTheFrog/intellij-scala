package org.jetbrains.plugins.scala
package lang
package psi
package api
package toplevel
package typedef

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.Key
import com.intellij.psi._
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.parser.ScalaElementType
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil.isLineTerminator
import org.jetbrains.plugins.scala.lang.psi.adapters.PsiClassAdapter
import org.jetbrains.plugins.scala.lang.psi.api.base.types.ScSelfTypeElement
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScNewTemplateDefinition
import org.jetbrains.plugins.scala.lang.psi.api.statements._
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScClassParameter
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.templates.ScExtendsBlock
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScTemplateDefinition._
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory._
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.synthetic.ScSyntheticClass
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef.TypeDefinitionMembers
import org.jetbrains.plugins.scala.lang.psi.types._
import org.jetbrains.plugins.scala.lang.psi.types.result._
import org.jetbrains.plugins.scala.macroAnnotations.{Cached, CachedInUserData, ModCount}
import org.jetbrains.plugins.scala.project.ProjectContext

/**
 * @author ven
 */
trait ScTemplateDefinition extends ScNamedElement with PsiClassAdapter with Typeable {

  def qualifiedName: String = null

  def originalElement: Option[ScTemplateDefinition] = Option(getUserData(originalElemKey))
  def setDesugared(actualElement: ScTypeDefinition): ScTemplateDefinition = {
    putUserData(originalElemKey, actualElement)
    members.foreach { member =>
      member.syntheticNavigationElement = actualElement
      member.syntheticContainingClass = actualElement
    }
    this
  }
  // designates that this very element has been created as a result of macro transform
  // do not confuse with desugaredElement
  def isDesugared: Boolean = originalElement.isDefined

  def desugaredElement: Option[ScTemplateDefinition] = None

  @Cached(ModCount.anyScalaPsiModificationCount, this)
  def physicalExtendsBlock: ScExtendsBlock = this.stubOrPsiChild(ScalaElementType.EXTENDS_BLOCK).orNull

  def extendsBlock: ScExtendsBlock = desugaredElement.map(_.extendsBlock).getOrElse(physicalExtendsBlock)

  def innerExtendsListTypes: Array[PsiClassType] = {
    val eb = extendsBlock
    if (eb != null) {
      val tp = eb.templateParents

      implicit val elementScope: ElementScope = ElementScope(getProject)
      tp match {
        case Some(tp1) => (for (te <- tp1.allTypeElements;
                                t = te.`type`().getOrAny;
                                asPsi = t.toPsiType
                                if asPsi.isInstanceOf[PsiClassType]) yield asPsi.asInstanceOf[PsiClassType]).toArray[PsiClassType]
        case _ => PsiClassType.EMPTY_ARRAY
      }
    } else PsiClassType.EMPTY_ARRAY
  }

  def showAsInheritor: Boolean = extendsBlock.templateBody.isDefined

  def getTypeWithProjections(thisProjections: Boolean = false): TypeResult

  def functions: Seq[ScFunction] = extendsBlock.functions

  def aliases: Seq[ScTypeAlias] = extendsBlock.aliases

  def members: Seq[ScMember] = extendsBlock.members

  @CachedInUserData(this, ModCount.getBlockModificationCount)
  def syntheticMethods: Seq[ScFunction] = syntheticMethodsImpl

  protected def syntheticMethodsImpl: Seq[ScFunction] = Seq.empty

  def typeDefinitions: Seq[ScTypeDefinition] = extendsBlock.typeDefinitions

  @CachedInUserData(this, ModCount.getBlockModificationCount)
  def syntheticTypeDefinitions: Seq[ScTypeDefinition] = syntheticTypeDefinitionsImpl

  protected def syntheticTypeDefinitionsImpl: Seq[ScTypeDefinition] = Seq.empty

  @CachedInUserData(this, ModCount.getBlockModificationCount)
  def syntheticMembers: Seq[ScMember] = syntheticMembersImpl

  protected def syntheticMembersImpl: Seq[ScMember] = Seq.empty

  def selfTypeElement: Option[ScSelfTypeElement] = {
    val qual = qualifiedName
    if (qual != null && (qual == "scala.Predef" || qual == "scala")) return None
    extendsBlock.selfTypeElement
  }

  def selfType: Option[ScType] = extendsBlock.selfType

  def superTypes: List[ScType] = extendsBlock.superTypes
  def supers: Seq[PsiClass] = extendsBlock.supers

  def allTypeSignatures: Iterator[TypeSignature] =
    TypeDefinitionMembers.getTypes(this).allSignatures

  def allTypeSignaturesIncludingSelfType: Iterator[TypeSignature] = {
    selfType match {
      case Some(selfType) =>
        val clazzType = getTypeWithProjections().getOrAny
        selfType.glb(clazzType) match {
          case c: ScCompoundType =>
            TypeDefinitionMembers.getTypes(c, Some(clazzType)).allSignatures
          case _ =>
            allTypeSignatures
        }
      case _ =>
        allTypeSignatures
    }
  }

  private def isValSignature(s: TermSignature): Boolean = s match {
    case _: PhysicalMethodSignature => false
    case _ => s.namedElement.nameContext match {
      case _: ScValueOrVariable | _: ScClassParameter => s.namedElement.name == s.name
      case _: PsiField => true
      case _ => false
    }
  }

  def allVals: Iterator[TermSignature] = {
    TypeDefinitionMembers.getSignatures(this).allSignatures.filter(isValSignature)
  }

  def allValsIncludingSelfType: Iterator[TermSignature] = {
    selfType match {
      case Some(selfType) =>
        val clazzType = getTypeWithProjections().getOrAny
        selfType.glb(clazzType) match {
          case c: ScCompoundType =>
            TypeDefinitionMembers.getSignatures(c, Some(clazzType))
              .allSignatures
              .filter(isValSignature)
          case _ =>
            allVals
        }
      case _ =>
        allVals
    }
  }

  def allMethods: Iterator[PhysicalMethodSignature] =
    TypeDefinitionMembers.getSignatures(this)
      .allSignatures
      .collect {
        case p: PhysicalMethodSignature => p
      }

  def allMethodsIncludingSelfType: Iterator[PhysicalMethodSignature] = {
    selfType match {
      case Some(selfType) =>
        val clazzType = getTypeWithProjections().getOrAny
        selfType.glb(clazzType) match {
          case c: ScCompoundType =>
            TypeDefinitionMembers.getSignatures(c, Some(clazzType))
              .allSignatures
              .collect {
                case p: PhysicalMethodSignature => p
              }
          case _ =>
            allMethods
        }
      case _ =>
        allMethods
    }
  }

  def allSignatures: Iterator[TermSignature] =
    TypeDefinitionMembers.getSignatures(this).allSignatures

  def allSignaturesIncludingSelfType: Iterator[TermSignature] = {
    selfType match {
      case Some(selfType) =>
        val clazzType = getTypeWithProjections().getOrAny
        selfType.glb(clazzType) match {
          case c: ScCompoundType =>
            TypeDefinitionMembers.getSignatures(c, Some(clazzType)).allSignatures
          case _ =>
            allSignatures
        }
      case _ =>
       allSignatures
    }
  }

  def isScriptFileClass: Boolean = getContainingFile match {
    case file: ScalaFile => file.isScriptFile
    case _ => false
  }

  def addMember(member: ScMember, anchor: Option[PsiElement]): ScMember = {
    implicit val projectContext: ProjectContext = member.projectContext
    extendsBlock.templateBody.map {
      _.getNode
    }.map { node =>
      val beforeNode = anchor.map {
        _.getNode
      }.getOrElse {
        val last = node.getLastChildNode
        last.getTreePrev match {
          case result if isLineTerminator(result.getPsi) => result
          case _ => last
        }
      }

      val before = beforeNode.getPsi
      if (isLineTerminator(before))
        node.addChild(createNewLineNode(), beforeNode)
      node.addChild(member.getNode, beforeNode)

      val newLineNode = createNewLineNode()
      if (isLineTerminator(before)) {
        node.replaceChild(beforeNode, newLineNode)
      } else {
        node.addChild(newLineNode, beforeNode)
      }

      member
    }.getOrElse {
      val node = extendsBlock.getNode
      node.addChild(createWhitespace.getNode)
      node.addChild(createBodyFromMember(member.getText).getNode)
      members.head
    }
  }

  def deleteMember(member: ScMember) {
    member.getParent.getNode.removeChild(member.getNode)
  }

  def allFunctionsByName(name: String): Iterator[PsiMethod] = {
    TypeDefinitionMembers.getSignatures(this).forName(name)
      .iterator
      .collect {
        case p: PhysicalMethodSignature => p.method
      }
  }

  override def isInheritor(baseClass: PsiClass, deep: Boolean): Boolean = {
    val basePath = Path.of(baseClass)

    // These doesn't appear in the superTypes at the moment, so special case required.
    if (basePath == Path.javaObject) return true

    if (basePath.kind.isFinal) return false

    if (deep) superPathsDeep.contains(basePath)
    else superPaths.contains(basePath)
  }

  @Cached(ModCount.getModificationCount, this)
  def cachedPath: Path = {
    val kind = this match {
      case _: ScTrait => Kind.ScTrait
      case _: ScClass => Kind.ScClass
      case _: ScObject => Kind.ScObject
      case _: ScNewTemplateDefinition => Kind.ScNewTd
      case s: ScSyntheticClass if s.className != "AnyRef" && s.className != "AnyVal" => Kind.SyntheticFinal
      case _ => Kind.NonScala
    }
    Path(name, Option(qualifiedName), kind)
  }

  @Cached(ModCount.getModificationCount, this)
  private def superPaths: Set[Path] = {
    if (DumbService.getInstance(getProject).isDumb) return Set.empty //to prevent failing during indexes

    supers.map(Path.of).toSet
  }

  @Cached(ModCount.getModificationCount, this)
  private def superPathsDeep: Set[Path] = {
    if (DumbService.getInstance(getProject).isDumb) return Set.empty //to prevent failing during indexes

    var collected = Set[Path]()

    def addForClass(c: PsiClass): Unit = {
      val path = c match {
        case td: ScTemplateDefinition => td.cachedPath
        case _ => Path.of(c)
      }
      if (!collected.contains(path)) {
        collected += path
        c match {
          case td: ScTemplateDefinition =>
            val supersIterator = td.supers.iterator
            while (supersIterator.hasNext) {
              addForClass(supersIterator.next())
            }
          case other =>
            val supersIterator = other.getSuperTypes.iterator
            while (supersIterator.hasNext) {
              val psiT = supersIterator.next()
              val next = psiT.resolveGenerics.getElement
              if (next != null) {
                addForClass(next)
              }
            }
        }
      }
    }
    addForClass(this)

    collected - cachedPath
  }
}

object ScTemplateDefinition {
  object ExtendsBlock {
    def unapply(definition: ScTemplateDefinition): Some[ScExtendsBlock] = Some(definition.extendsBlock)
  }

  sealed abstract class Kind(val isFinal: Boolean)
  object Kind {
    object ScClass extends Kind(false)
    object ScTrait extends Kind(false)
    object ScObject extends Kind(true)
    object ScNewTd extends Kind(true)
    object SyntheticFinal extends Kind(true)
    object NonScala extends Kind(false)
  }

  case class Path(name: String, qName: Option[String], kind: Kind)

  object Path {
    def of(c: PsiClass): Path = {
      c match {
        case td: ScTemplateDefinition =>
          td.cachedPath
        case s: ScSyntheticClass if s.className != "AnyRef" && s.className != "AnyVal" =>
          Path(c.name, Option(c.qualifiedName), Kind.SyntheticFinal)
        case s: ScSyntheticClass =>
          Path(c.name, Option(c.qualifiedName), Kind.ScClass)
        case _ =>
          Path(c.name, Option(c.qualifiedName), Kind.NonScala)
      }
    }

    val javaObject = Path("Object", Some("java.lang.Object"), Kind.NonScala)
  }

  private val originalElemKey: Key[ScTemplateDefinition] = Key.create("ScTemplateDefinition.originalElem")

  implicit class SyntheticMembersExt(private val td: ScTemplateDefinition) extends AnyVal {
    //this method is not in the ScTemplateDefinition trait to avoid binary incompatible change
    def membersWithSynthetic: Seq[ScMember] =
      td.members ++ td.syntheticMembers ++ td.syntheticMethods ++ td.syntheticTypeDefinitions

  }

}
