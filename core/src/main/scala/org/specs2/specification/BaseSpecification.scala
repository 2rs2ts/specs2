package org.specs2
package specification

import scalaz._
import Scalaz._
import collection.Seqx._
import Fragments._
import reflect.Classes._
import main.{CommandLineArguments, Arguments}
/**
 * A Base specification contains the minimum elements for a Specification
 * 
 * - a Seq of Fragments, available through the SpecificationStructure trait
 * - methods for creating Fragments from the FragmentsBuilder trait
 * - methods to include other specifications
 *
 */
trait BaseSpecification extends SpecificationStructure with FragmentsBuilder with SpecificationInclusion

/**
 * additional methods to include other specifications or Fragments
 */
trait SpecificationInclusion { this: FragmentsBuilder =>
  def include(f: Fragments): FragmentsFragment = fragmentsFragments(f)
  def include(f: Fragments, fs: Fragments*): FragmentsFragment = include((f +: fs).sumr)
  implicit def include(s: SpecificationStructure): FragmentsFragment = include(s.content)
  def include(s: SpecificationStructure, ss: SpecificationStructure*): FragmentsFragment = include(s.content, ss.map(_.content):_*)
  def include(args: Arguments, s: SpecificationStructure): FragmentsFragment = include(args, s.applyArguments(args).content)
  def include(args: Arguments, s: SpecificationStructure, ss: SpecificationStructure*): FragmentsFragment = include(args, s.applyArguments(args).content, ss.map(_.applyArguments(args).content):_*)
  def include(args: Arguments, f: Fragments): FragmentsFragment = include(f.overrideArgs(args))
  def include(args: Arguments, f: Fragments, fs: Fragments*): FragmentsFragment = include((f +: fs).sumr.overrideArgs(args))

  /** add the fragments of another specification without start and end */
  def inline(specs: SpecificationStructure*): Fragments = Fragments.createList(specs.flatMap(s => s.map(s.is).middle):_*)
}

/**
 * The structure of a Specification is simply defined as a sequence of fragments
 */
trait SpecificationStructure extends DefaultFragmentsFormatting {
  /** declaration of Fragments from the user */
  def is: Fragments
  /** this method can be overridden to map additional behavior in the user-defined fragments */
  def map(fs: =>Fragments): Fragments = fs
  /** specName provides useful information identifying the specification: title, className, url... */
  def identification: SpecIdentification = content.specName
  /** automatically convert a specification to its identification */
  implicit def identifySpecificationStructure(s: SpecificationStructure): SpecIdentification = s.identification
  /** 
   * this "cached" version of the Fragments is kept hidden from the user to avoid polluting
   * the Specification namespace.
   * SpecStart and SpecEnd fragments are added if the user haven't inserted any
   *
   * A creation path is possibly set on Examples and Actions if they haven't any
   */
  private[specs2] lazy val content: Fragments = formatFragments(map(Fragments.withCreationPaths(Fragments.withSpecName(is, this))))

  /**
   * empty fragments with just the specification name (and without the possible title specified in the fragments).
   * This is used to create 'see' links and avoid infinite loops of a specification referencing itself
   */
  private[specs2] lazy val emptyContent: Fragments = Fragments.withSpecName(Fragments.create(is.specStart), this)

  /** apply command-line arguments if there are any */
  private[specs2] def applyArguments(implicit args: Arguments) =
    this match {
      case withCommandLineArguments : CommandLineArguments => withCommandLineArguments.set(args); this
      case other                                           => this
    }

}

/**
 * methods for creating SpecificationStructure instances from fragments
 */
object SpecificationStructure {
  import collection.Iterablex._
  
  def apply(fs: Fragments): SpecificationStructure = new SpecificationStructure {
    def is = content
    override lazy val content = fs.fragments match {
      case SpecStart(n,a,l,_) +: middle :+ SpecEnd(_,_,_) => Fragments(Some(n), middle, a, l)
      case other                                          => fs
    }
  }
  def apply(fs: Seq[Fragment]): SpecificationStructure = apply(Fragments.create(fs:_*))

  def apply(spec: SpecificationStructure, arguments: Arguments): SpecificationStructure = apply(spec.content add arguments)

  /**
   * create a SpecificationStructure from a className, throwing an Error if that's not possible
   */
  def createSpecification(className: String, classLoader: ClassLoader = Thread.currentThread.getContextClassLoader)
                         (implicit args: Arguments = Arguments()): SpecificationStructure = {
    createSpecificationOption(className, classLoader) match {
      case Some(s) => s
      case None    => sys.error("can not create specification: "+className)
    }
  }

  /**
   * create a SpecificationStructure from a className, returning None if that's not possible
   */
  def createSpecificationOption(className: String, classLoader: ClassLoader = Thread.currentThread.getContextClassLoader)
                               (implicit args: Arguments = Arguments()) : Option[SpecificationStructure] = ???
  /**
   * create a SpecificationStructure from a className, returning an Exception if that's not possible
   */
  def createSpecificationEither(className: String, classLoader: ClassLoader = Thread.currentThread.getContextClassLoader)
                               (implicit args: Arguments = Arguments()) : Either[Throwable, SpecificationStructure] = ???

  private def createSpecificationFromClassOrObject(className: String,
                                                   classLoader: ClassLoader = Thread.currentThread.getContextClassLoader)
                                                  (implicit args: Arguments = Arguments()) : Option[SpecificationStructure] = ???

  /**
   * store the command-line arguments in the CommandLineArguments trait if necessary
   */
  private def applyCommandLineArguments(implicit args: Arguments) = (spec: SpecificationStructure) => spec.applyArguments(args)
}