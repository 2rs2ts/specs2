package org.specs2
package matcher
import java.io.File
import io._

class FileMatchersSpec extends Specification with TestFiles with FileMatchers {
  private[specs2] override val fileSystem = new MockFileSystem {}

  def is = s2"""

 The FileMatchers trait provides matchers to check files and paths.

 In the following, okPath references a path which actually exists and missingPath, a path which doesn't.


 The PathMatchers trait provides matchers for paths
   beEqualToIgnoringSep checks if 2 paths are the same regardless of
   their separators
   ${ "c:\\temp\\hello" must beEqualToIgnoringSep("c:/temp/hello") }

   beAnExistingPath checks if a path exists
     `okPath must beAnExistingPath`                                                                      ${fs().e1}
     `missingPath must not be anExistingPath`                                                            ${fs().e2}

   beAReadablePath checks if a path is readable
      `setReadable(okPath) must beAReadablePath`                                                         ${fs().e3}
      `setNotReadable(okPath) must not be aReadablePath`                                                 ${fs().e4}

   beAWritablePath checks if a path is writable
     `setWritable(okPath) must beAWritablePath`                                                          ${fs().e5}
     `setNotWritable(okPath) must not be aWritablePath`                                                  ${fs().e6}

   beAnAbsolutePath checks if a path is absolute
   ${ "/tmp" must beAnAbsolutePath }
   ${ "./tmp" must not be anAbsolutePath }

   "beAHiddenPath checks if a path is hidden
   ${ ".tmp" must beAHiddenPath }
   ${ "/tmp" must not be aHiddenPath }

   beAFilePath checks if a path is a file
   ${ "c:/tmp.txt" must beAFilePath }
   ${ "c:/tmp" must not be aFilePath }

   beADirectorPath checks if a path is a directory
   ${ "c:/tmp" must beADirectoryPath }
   ${ "c:/tmp.txt" must not be aDirectoryPath }

   havePathName checks if a path has a given name
   ${ "c:/tmp/test.txt" must havePathName("test.txt") }
   ${ "c:/tmp/test.txt" must not have pathName("name.txt") }

   haveAsAbsolutePath checks if a path has a given absolute path
   ${ "c:/tmp/test.txt" must haveAsAbsolutePath("c:/tmp/test.txt") }

   haveAsCanonicalPath checks if a path has a given canonical path
   ${ "c:/tmp/../test.txt" must haveAsCanonicalPath("c:/test.txt") }

   haveParentPath checks if a path has a given parent path
   ${ "c:/tmp/dir/test.txt" must haveParentPath("c:/tmp/dir") }

   listPaths checks if a path has a given list of children                                               ${fs().e7}

 The FileMatchers trait provides similar matchers, but for files
   exist checks if a file exists                                                                         ${fs().e8}
   not exist checks if a file does not exist                                                             ${fs().e9}
   beReadable checks if a file is readable                                                               ${fs().e10}
   beWritable checks if a file is writable                                                               ${fs().e11}

   beAbsolute checks if a file is absolute
   ${ file("/tmp") must beAbsolute }

   beHidden checks if a file is hidden
   ${ file(".tmp") must beHidden }

   beAFile checks if a file is a file
   ${ file("c:/tmp.txt") must beAFile }

   beADirectory checks if a file is a directory
   ${ file("c:/tmp") must beADirectory }

   haveName checks if a file has a given name
   ${ file("c:/tmp/test.txt") must haveName("test.txt") }

   haveAbsolutePath checks if a file has a given absolute path
   ${ file("c:/tmp/test.txt") must haveAbsolutePath("c:/tmp/test.txt") }

   haveCanonicalPath checks if afile has a given canonical path
   ${ file("c:/tmp/dir/../test.txt") must haveCanonicalPath("c:/tmp/test.txt") }

   haveParent checks if a file has a given parent path
   ${ file("c:/tmp/dir/test.txt") must haveParent("c:/tmp/dir") }

   haveList checks if a file has a given list of children                                                ${fs().e12}
                                                                                                                        """
}

case class fs() extends MustMatchers with TestFiles with FileMatchers {
  private[specs2] override val fileSystem = new MockFileSystem {}
  import fileSystem._
  fileSystem.addFile(okPath, "")

  def e1 = okPath must beAnExistingPath
  def e2 = missingPath must not be anExistingPath
  def e3 = setReadable(okPath) must beAReadablePath
  def e4 = setNotReadable(okPath) must not be aReadablePath
  def e5 = setWritable(okPath) must beAWritablePath
  def e6 = setNotWritable(okPath) must not be aWritablePath
  def e7 = { addChild("c:/t/", "c:/t/test.txt")
             "c:/t/" must listPaths("c:/t/test.txt") }
  def e8 = file(okPath) must exist
  def e9 = file(missingPath) must not exist
  def e10 = file(setReadable(okPath)) must beReadable
  def e11 = file(setWritable(okPath)) must beWritable
  def e12 = { addChild("c:/t", "c:/t/tst.txt")
              file("c:/t/") must haveList("c:/t/tst.txt") }
}


trait TestFiles {
  val okPath = "path"
  val missingPath = "absent"

  def file(s: String) = new File(s)
}