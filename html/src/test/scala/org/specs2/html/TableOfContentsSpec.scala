package org.specs2
package html

import TableOfContents._
import io._
import scala.xml.NodeSeq
import specification._
import matcher.XmlMatchers

class TableOfContentsSpec extends script.Specification with HtmlDocuments with Grouped with XmlMatchers { def is = s2"""

  Creating a table of content for a html document                                               
    creates an unordered list from the html headers                                             
      + as nested <li/> lists corresponding to the hierarchy of the document headers
      each <li/> element has
        + the header text as text
        + an url+anchor referencing the header name
        + an id attribute with the spec id. the id attribute is expected by jstree
    + injects sub-table of contents where there are <subtoc/> tags in the original document
                                                                                                  """

  "toc" - new g1 {
    e1 := addToc(aBodyWithHeaders) must \\("li") \\ ("ul") \ ("li")

    //    <li><a href="http://specs2.org/#title_123456">title</a>
    //      <ul><li><a href="http://specs2.org/#a+header_123456">a header</a></li>
    //      </ul>
    //    </li>
    e2 := addToc(aBodyWithHeaders) must \\ ("li") \ ("a") \> "title"
    e3 := addToc(aBodyWithHeaders) must \\ ("li") \ ("a", "href" -> "../guide/UserGuide.html#title.*")
    e4 := addToc(aBodyWithHeaders) must \\ ("li", "id")
    e5 := {
      val subtoc = <a href="http://specs2.org/#other" />
      addToc(aBodyWithHeadersAndASubtoc, Map(SpecId("123") -> subtoc)) must \\ ("li") \\ ("a", "href" -> "http://specs2.org/#other")
    }

  }

  def addToc(body: NodeSeq, subtocs: Map[SpecId, NodeSeq] = Map()) =
    tocItemList(body, DirectoryPath.unsafe("guide"), "guide/UserGuide.html", SpecId("specName"), subtocs)

}