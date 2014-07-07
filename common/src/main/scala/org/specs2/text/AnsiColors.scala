package org.specs2
package text

import NotNullStrings._

/**
 * This trait provides AnsiColors codes for the OutputReporter
 * @see http://en.wikipedia.org/wiki/ANSI_escape_code
 */
trait AnsiColors {
  lazy val black   = "\033[30m"
  lazy val red     = "\033[31m"
  lazy val green   = "\033[32m"
  lazy val yellow  = "\033[33m"
  lazy val blue    = "\033[34m"
  lazy val magenta = "\033[35m"
  lazy val cyan    = "\033[36m"
  lazy val white   = "\033[37m"
    
  lazy val reset   = "\033[0m"
    
  lazy val all = Seq(black, red, green, yellow, blue, magenta, cyan, white, reset)

  /** @return a string with no color codes */
  def removeColors(s: String, doIt: Boolean = true): String = {
    if (doIt) all.foldLeft (s.notNull) { (res, cur) => res.replace(cur, "") }
    else      s.notNull
  }

  /**
   * @return a colored string (if args.color == true)
   * color markers are inserted at the beginning and end of each line so that newlines are preserved
   */
  def color(s: String, color: String, doIt: Boolean = true) = {
    if (doIt) {
      val colored = s.foldLeft(color) { (res, cur) =>
        if (cur == '\n') res + reset + cur + color
        else             res + cur
      } + reset
      colored
    }
    else      removeColors(s, true)
  }

  override def toString = all.mkString("AnsiColors(",",",")")
}
object AnsiColors extends AnsiColors

