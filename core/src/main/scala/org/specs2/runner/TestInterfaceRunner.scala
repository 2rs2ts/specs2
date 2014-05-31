package org.specs2
package runner

import _root_.org.scalatools.testing._
import main.Arguments
import control.Throwablex._
import reporter._
import specification._
import control.Exceptions._
import runner.TestInterfaceFingerprints._
import specification.ExecutedSpecification
import reflect.Classes
import io.ConsoleOutput
import scalaz.Scalaz._
import scala.reflect.ClassTag

/**
* Implementation of the Framework interface for the sbt tool.
* It declares the classes which can be executed by the specs2 library.
*/
class SpecsFramework extends Framework {
  def name = "specs2"
  def tests = Array[Fingerprint](fp1, fp2, fp3, fp4)
  def testRunner(classLoader: ClassLoader, loggers: Array[Logger]) = new TestInterfaceRunner(classLoader, loggers)
}

object TestInterfaceFingerprints {
  val fp1 =  new Specs2Fingerprint { def isModule = false }
  val fp2 =  new Specs2Fingerprint { def isModule = true  }
  val fp3 =  new FilesFingerprint { def isModule = false }
  val fp4 =  new FilesFingerprint { def isModule = true  }
}

trait Specs2Fingerprint extends TestFingerprint {
  def superClassName = "org.specs2.specification.SpecificationStructure"
}
trait FilesFingerprint extends TestFingerprint {
  def superClassName = "org.specs2.runner.FilesRunner"
}

/**
 * Runner for TestInterface.
 * It creates a Specification class with the given classLoader the classes which can be executed by the specs2 library.
 * 
 * Then it uses a NotifierRunner to notify the EventHandler of the test events.
 */
class TestInterfaceRunner(val loader: ClassLoader, val loggers: Array[Logger]) extends _root_.org.scalatools.testing.Runner
  with HandlerEvents with TestLoggers with Exporters {
  import reflect.Classes._

  def run(className: String, fingerprint: TestFingerprint, handler: EventHandler, args: Array[String]) =
    fingerprint match {
      case f if f.superClassName == fp3.superClassName => runFilesRunner(className, handler, args)
      case other                                       => runSpecification(className, handler, args)
    }

  def runSpecification(className: String, handler: EventHandler, args: Array[String]): Any = {
    implicit val commandLineArguments = Arguments(args:_*)
    SpecificationStructure.createSpecificationEither(className, loader) match {
      case Left(e)  => handleClassCreationError(className, handler, e)
      case Right(s) => reporter(handler)(args).report(s)(s.content.arguments.overrideWith(commandLineArguments))
    }
  }
  
  def runFilesRunner(className: String, handler: EventHandler, args: Array[String]) {
    toRun[FilesRunner](className, handler).right.toOption.toSeq.flatMap(_.run(args)).flatMap(_.issues).foreach { issue =>
      handler.handle(result(issue.s.raw)(issue.result))
    }
  }

  private def toRun[T <: AnyRef : ClassTag](className: String, handler: EventHandler): Either[Throwable, T] = {
    val runner: Either[Throwable, T] = create[T](className + "$", loader) match {
      case Right(s) => Right(s)
      case Left(e) => create[T](className, loader)
    }
    runner.left.map { e => handleClassCreationError(className, handler, e) }
    runner
  }

  /**
   * Notify sbt that the specification could not be created
   */
  private def handleClassCreationError(className: String, handler: EventHandler, e: Throwable) {
    handler.handle(error(className, e))
    logError("Could not create an instance of "+className+"\n")
    (e :: e.chainedExceptions) foreach { s =>
      logError("  caused by " + s.toString)
      s.getStackTrace.foreach(t => logError("  " + t.toString))
    }
  }

  protected def reporter(handler: EventHandler)(args: Array[String]): Reporter =
    new TestInterfaceConsoleReporter(consoleExporter(args, handler), (a: Arguments) => otherExporters(args, handler)(a))

  /** @return true if the console must report the results */
  private def isConsole(args: Array[String]) = !Seq("html", "junitxml", "markdown").exists(args.contains) || args.contains("console")
  private def consoleExporter(args: Array[String], handler: EventHandler) =
    exporter(isConsole(args))(new TestInterfaceReporter(handler, loggers))

  protected def finalExporter(handler: EventHandler) = FinalResultsReporter(handler, loggers)

  def otherExporters(args: Array[String], handler: EventHandler)(implicit arguments: Arguments): Seq[Exporting] = {
    val exportFinalStats = exporter(!isConsole(args))(finalExporter(handler))
    super.exporters(s  => args.filterNot(_ == "console").contains(s)) ++ exportFinalStats.toSeq
  }

  /** @return the list of all the exporters depending on the arguments passed on the command line */
  def exporters(args: Array[String], handler: EventHandler)(implicit arguments: Arguments): Seq[Exporting] = {
    consoleExporter(args, handler).toSeq ++ otherExporters(args, handler)
  }
}
/**
 * This reporter will just notify the test interface about test results for the end statistics
 *
 * It is only used if we are not using the Console exporter
 */
case class FinalResultsReporter(override val handler: EventHandler,
                                override val loggers: Array[Logger]) extends TestInterfaceReporter(handler, loggers) {
  override def export(implicit args: Arguments): ExecutingSpecification => ExecutedSpecification = (spec: ExecutingSpecification) => {
    val executed = spec.execute
    executed.fragments foreach handleFragment(args)
    executed
  }
}

class TestInterfaceConsoleReporter(consoleExporter: Option[Exporting], otherExporters: Arguments => Seq[Exporting]) extends ConsoleReporter with AllExporting {
  override def exporters(accept: String => Boolean)(implicit arguments: Arguments): Seq[Exporting] =
    otherExporters(arguments)

  override def exportConsole(accept: String => Boolean) (implicit arguments: Arguments) =
    consoleExporter

}

/**
 * This object can be used to debug the behavior of the TestInterfaceRunner
 */
object testInterface extends TestInterfaceRunner(Thread.currentThread().getContextClassLoader, Array(TestInterfaceConsoleLogger)) with Classes with SystemExit with ConsoleOutput {
  def main(arguments: Array[String]) {
    exitSystem(start(arguments:_*))
  }

  def start(arguments: String*): Option[ExecutedSpecification] = {
    if (arguments.length == 0)
      println("The first argument should at least be the specification class name")
    implicit val commandLineArgs = Arguments(arguments.drop(1):_*)
    val testInterfaceReporter = reporter(NullEventHandler)(arguments.toArray)
    execute(testInterfaceReporter, createSpecification(arguments(0))).headOption
  }

  private def execute(testInterfaceReporter: Reporter, specification: SpecificationStructure)(implicit args: Arguments = Arguments()): Option[ExecutedSpecification] = ???

  private def createSpecification(className: String)(implicit args: Arguments) = SpecificationStructure.createSpecification(className, loader)
}
object NullEventHandler extends EventHandler {
  def handle(event: Event) {}
}
object TestInterfaceConsoleLogger extends Logger {
  def ansiCodesSupported = false
  def error(message: String) = message.split("\n", -1).foreach(m => println("error: " + m))
  def info(message: String)  = message.split("\n", -1).foreach(m => println("info: "  + m))
  def warn(message: String)  = message.split("\n", -1).foreach(m => println("warn: "  + m))
  def debug(message: String) = message.split("\n", -1).foreach(m => println("debug: " + m))
  def trace(t: Throwable)    = println("trace: " + t)
}