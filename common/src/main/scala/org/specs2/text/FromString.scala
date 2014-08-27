package org.specs2
package text

import control.Exceptions._

/**
 * This typeclass is used to describe any instance which can be decoded from a String.
 *
 * It can be used to pass string properties in SystemProperties and decode them in a specific way
 */
trait FromString[T] {
  def fromString(s: String): Option[T]
}

object FromString {

  implicit def StringFromString = new FromString[String] {
    def fromString(s: String): Option[String] = Option(s)
  }

  implicit def IntFromString = new FromString[Int] {
    def fromString(s: String): Option[Int] = tryo(s.toInt)
  }

  implicit def BooleanFromString = new FromString[Boolean] {
    def fromString(s: String): Option[Boolean] =
      if ("false".equals(s))     Some(false)
      else if ("true".equals(s)) Some(true)
      else                       None
  }
}