package org.specs2
package runner

import reflect._
import io._
import main.Arguments
import control.Exceptions._
import specification._
import reporter._

/**
 * This class can be used to executed a Specification in the Console
 * by specifying its name as the first argument on the command line
 * 
 * @see specs2.run
 * @see org.specs2.main.Arguments for other command line options
 */
class ClassRunner extends Classes with ConsoleOutput with SystemExit {
  lazy val reporter: Reporter = new ConsoleReporter {}

  protected val errorHandler = ClassRunner.errorHandler
  
  def main(arguments: Array[String]) {
    exitSystem(start(arguments:_*))
  }

  def start(arguments: String*): Option[ExecutedSpecification] = {
    if (arguments.length == 0)
      println("The first argument should at least be the specification class name")
    implicit val commandLineArgs = Arguments(arguments.drop(1):_*)
    apply(createSpecification(arguments(0))).headOption
  }

  /**
   * This method can be called directly from the console with the specs2.run object:
   *
   *     > specs2.run(spec1, spec2)
   * or  > import specs2._
   *     > run(spec1, spec2)
   *
   * If you want to pass specific arguments you can pass:
   *
   *    > import specs2.args._
   *    > specs2.run(spec1)(nocolor)
   *
   * Or you can set specific default with an implicit value:
   *
   *    > import specs2.args._
   *    > implicit val myargs = nocolor
   *    > specs2.run(spec1)
   */
  def apply(specifications: SpecificationStructure*)(implicit args: Arguments = Arguments()): Seq[ExecutedSpecification] = ???

  protected def createSpecification(className: String, classLoader: ClassLoader = Thread.currentThread.getContextClassLoader)
                                   (implicit args: Arguments = Arguments()) =
    SpecificationStructure.createSpecification(className, classLoader)
}

object ClassRunner {
  val errorHandler: PartialFunction[Throwable, Unit] = {  case e =>
    println(s"""|\nAn error occurred: ${e.getMessage} during the creation of the specification.
                |
                |This might be cause by failing variable initializations. If this is the case you should consider transforming those
                |variables into `lazy val`s instead of `val`s. 
                |
                |Otherwise please create an issue on http://github.com/etorreborre/specs2/issues with the stacktrace below. Thanks.""".stripMargin)
    e.printStackTrace
  }
}

