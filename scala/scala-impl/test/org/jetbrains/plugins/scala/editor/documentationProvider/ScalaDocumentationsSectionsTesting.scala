package org.jetbrains.plugins.scala.editor.documentationProvider

import com.intellij.lang.documentation.DocumentationMarkup
import org.junit.Assert._

trait ScalaDocumentationsSectionsTesting {
  self: DocumentationTesting =>

  protected val DocStart = "<html><body>"
  protected val DocEnd   = "</body></html>"
  protected val DefinitionStart: String = DocumentationMarkup.DEFINITION_START
  protected val DefinitionEnd  : String = DocumentationMarkup.DEFINITION_END
  protected val ContentStart   : String = DocumentationMarkup.CONTENT_START
  protected val ContentEnd     : String = DocumentationMarkup.CONTENT_END
  protected val SectionsStart  : String = DocumentationMarkup.SECTIONS_START
  protected val SectionsEnd    : String = DocumentationMarkup.SECTIONS_END

  protected def doGenerateDocBodyTest(fileContent: String, expectedDoc: String): Unit = {
    val actualDoc = generateDoc(fileContent)
    assertDocHtml(DocStart + expectedDoc + DocEnd, actualDoc)
  }

  protected def doGenerateDocDefinitionTest(fileContent: String, expectedDefinition: String): Unit = {
    val actualDoc = generateDoc(fileContent)
    val actualSections = extractSection(actualDoc, "definition", DefinitionStart, DefinitionEnd)
    assertDocHtml(DefinitionStart + expectedDefinition + DefinitionEnd, actualSections)
  }

  /** NOTE: doesn't support inner <div> tags, see [[extractSection]] */
  protected def doGenerateDocContentTest(fileContent: String, expectedContent: String): Unit = {
    val actualDoc = generateDoc(fileContent)
    val actualSections = extractSection(actualDoc, "content", ContentStart, ContentEnd)
    assertDocHtml(ContentStart + expectedContent + ContentEnd, actualSections)
  }

  /** The same as [[doGenerateDocContentTest]] but accepts a comment without any definition */
  protected def doGenerateDocContentDanglingTest(commentContent: String, expectedContent: String): Unit =
    doGenerateDocContentTest(
      s"""$commentContent
         |class A""".stripMargin,
      expectedContent
    )

  protected def doGenerateDocSectionsTest(fileContent: String, expectedDoSections: String): Unit = {
    val actualDoc = generateDoc(fileContent)
    val actualSections = extractSection(actualDoc, "sections", SectionsStart, SectionsEnd)
    assertDocHtml(SectionsStart + expectedDoSections + SectionsEnd, actualSections)
  }

  // NOTE: doesn't support nested tags,
  // so will not detect "<div>a <div> b </div></div>"
  // will find first closing tag: "<div>a <div> b </div>"
  private def extractSection(doc: String, sectionName: String, tagStart: String, tagEnd: String): String =
    doc.indexOf(tagStart) match {
      case -1 => fail(s"no '$sectionName' section found\n$doc").asInstanceOf[Nothing]
      case idx  => doc.substring(idx, doc.indexOf(tagEnd, idx)) + tagEnd
    }
}

