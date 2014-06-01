package org.specs2
package specification

import script._

class GWTSpec extends script.Specification with GWT with Grouped with StandardDelimitedStepParsers { def is = s2"""

 Given / When / Then is a style of specification where there are a number of steps setting up values to setup a context (given steps), then some steps to trigger some actions (when steps) and finally some checks (then steps).

Extractors steps
================

 Combinations with delimited extractors

  + given/when/then
  + given/given/when/then
  + given/given/when/when/then
  + given/when/then/then
  + given/given/when/then and seq of given
  + given/when/when/then and seq of when
  + given/when/then with no extractors
  + when/then with no extractors and actions for when
  + given/given/when/when/then where the order for type parameters is significant

Stripping text
==============
 Extractors must extract values and return the resulting string

  + with delimited extractors
  + with regex extractors

Execution
=========

 If there are errors, the rest of the sequence must be skipped,
 but if there is a failure in a then step, the other then steps must still be executed

  + in a given step
  + in a when step - extraction error
  + in a when step - mapping error
  + in a then step - extraction error
  + in a then step - verification error
  + in a then step - verification failure
  + in a then step - verification failure in 2nd step

 It is possible to intercalate other variables in the gwt steps
  + with a simple variable

Templates
=========
 + Templates can be used to define which lines should be used
                                                                 """

  "extractors" - new g1 {
    e1 := {
      val steps = Scenario("e1").
        given(groupAs("\\d").and((_:String).toInt)).
        when(aString) { case op :: i :: _ => -i }.
        andThen(anInt) { case e :: a :: _ => a === e }

      executeExamplesResult {
        s2""" ${steps.start}
          given {1}
          when {-}
          then {-1}       ${steps.end}
        """
      }
    }

    e2 := {
      val steps = Scenario("e2").
        given(anInt).
        given(anInt).
        when(aString) { case op :: i :: j :: _ => i + j }.
        andThen(anInt) { case e :: a :: _ => a === e }

      executeExamplesResult {
        s2""" ${steps.start}
          given {1}
          given {2}
          when {+}
          then {3}       ${steps.end}
        """
      }
    }

    e3 := {
      val steps = Scenario("e3").
        given(anInt).
        given(anInt).
        when(aString) { case op :: i :: j :: _ => i + j }.
        when(aString) { case op :: _           => ((i:Int) => -i) }.
        andThen(anInt) { case e :: f :: a :: _ => f(a) === e }

      executeExamplesResult {
        s2""" ${steps.start}
          given {1}
          given {2}
          when {+}
          when {-}
          then {-3}       ${steps.end}
        """
      }
    }

    e4 := {
      val steps = Scenario("e4").
        given(anInt).
        when(aString) { case op :: i :: _ => -i }.
        andThen(anInt) { case e :: a :: _ => a === e }.
        andThen(anInt) { case e :: a :: _ => a must be_>(e) }

      executeExamplesResult {
        s2"""              ${steps.start}
          given {1}
          when {-}
          then {-1}
          then {-10}       ${steps.end}
        """
      }
    }

    e5 := {
      val steps = Scenario("e5").
        given(anInt).
        given(anInt).
        when(aString).collect { case (op, numbers) => numbers.sum }.
        andThen(anInt) { case e :: a :: _ => a === e }

      executeExamplesResult {
        s2""" ${steps.start}
          given {1}
          given {2}
          when {+}
          then {3}       ${steps.end}
        """
      }
    }

    e6 := {
      val steps = Scenario("e6").
        given(anInt).
        when(aString) { case op :: i :: _ => i }.
        when(aString) { case op :: j :: _ => j }.
        andThen(anInt).collect { case (e, numbers) => numbers.sum === e }

      executeExamplesResult { s2""" ${steps.start}
          given {1}
          when {+}
          when {+}
          then {2}                  ${steps.end}
        """
      }
    }

    e7 := {
      val steps = Scenario("e7").
        given().
        when() { case op :: i :: _ => i.size }.
        when() { case op :: j :: _ => j.size }.
        andThen().collect { case (e, numbers) => e.size must be_<(numbers.sum) }

      executeExamplesResult { s2""" ${steps.start}
          given {1}
          when {+}
          when {+}
          then {2}                  ${steps.end}
        """
      }
    }

    e8 := {
      val steps = Scenario("e8").
        when("get value 1").
        when("get value 2").
        andThen().collect { case (e, values) =>
        (e +: values) must contain(allOf(=~("the values are"), =~("value 1"), =~("value 2")))
      }

      executeExamplesResult { s2""" ${steps.start}
          when we do 1
          when we do 2
          then the values are       ${steps.end}
        """
      }
    }

    e9 := {
      val steps = Scenario("e9").
        given(anInt).
        given(aString).
        when(aString) { case op :: s :: i :: _ => (op.size + s.size + i).toString }.
        when(anInt)   { case i :: s :: j :: _ => i + s.size + j }.
        andThen(anInt) { case e :: i :: j :: _ => e === i + j.toInt }

      executeExamplesResult { s2""" ${steps.start}
          given {1}
          given {ab}
          when {cde}
          when {2}
          then {11}                 ${steps.end}
        """
      }
    }
  }

  "extractors" - new g2 {
    e1 := {
      val steps = Scenario("e1").
        given(anInt).
        when(aString) { case op :: i :: _ => -i }.
        andThen(anInt) { case e :: a :: _ => a === e }

      toText { nocolor ^
        s2"""             ${steps.start}
          given {1}
          when {-}
          then {-1}       ${steps.end}
        """
      } must contain("given 1") and contain("when -") and contain("+ then -1")
    }

    e2 := {
      val anInt = groupAs("\\-?\\d+").and((_:String).toInt)
      val lastString = groupAs("\\w+").and((ss: Seq[String]) => ss.last)

      val steps = Scenario("e2").
        given(anInt).
        when(lastString) { case op :: i :: _ => -i }.
        andThen(anInt)   { case e :: a :: _ => a === e }

      toText { nocolor ^
        s2""" ${steps.start}
          given 1
          when -
          then -1       ${steps.end}
        """
      } must contain("given 1") and contain("when -") and contain("+ then -1")

    }
  }

  "errors" - new g3 {
    e1 := {
      val steps = Scenario("e1").
        given(twoInts).
        when(aString) { case op :: (i, j) :: _ => -i }.
        andThen(anInt) { case e :: a :: _ => a === e }

      toText { nocolor ^
        s2"""             ${steps.start}
          given {1}
          when {-}
          then {-1}       ${steps.end}
        """
      } must contain("! step error") and contain("o skipped step") and contain("o then")
    }

    e2 := {
      val steps = Scenario("e2").
        given(anInt).
        when(twoStrings) { case (o, p) :: i :: _ => -i }.
        andThen(anInt) { case e :: a :: _ => a === e }

      toText { nocolor ^
        s2"""             ${steps.start}
          given {1}
          when {-}
          then {-1}       ${steps.end}
        """
      } must contain("given 1") and contain("! step error") and contain("o then")
    }

    e3 := {
      val steps = Scenario("e3").
        given(anInt).
        when(aString) { case op :: i :: _ => op.toInt; -i }.
        andThen(anInt) { case e :: a :: _ => a === e }

      toText { nocolor ^
        s2"""             ${steps.start}
          given {1}
          when {-}
          then {-1}       ${steps.end}
        """
      } must contain("given 1") and contain("! step error") and contain("o then")
    }

    e4 := {
      val steps = Scenario("e4").
        given(anInt).
        when(aString) { case op :: i :: _ => -i }.
        andThen(twoInts) { case e :: a :: _ => a === e }

      toText { nocolor ^
        s2"""             ${steps.start}
          given {1}
          when {-}
          then {-1}       ${steps.end}
        """
      } must contain("given 1") and contain("when -") and contain("! then")
    }

    e5 := {
      val steps = Scenario("e5").
        given(anInt).
        when(aString) { case op :: i :: _ => -i }.
        andThen(anInt) { case e :: a :: _ => "".toInt; a === e }

      toText { nocolor ^
        s2"""             ${steps.start}
          given {1}
          when {-}
          then {-1}       ${steps.end}
        """
      } must contain("given 1") and contain("when -") and contain("! then")
    }

    e6 := {
      val steps = Scenario("e6").
        given(anInt).
        when(aString) { case op :: i :: _ => -i }.
        andThen(anInt) { case e :: a :: _ => (a + 1) === e }

      toText { nocolor ^
        s2"""             ${steps.start}
          given {1}
          when {-}
          then {-1}       ${steps.end}
        """
      } must contain("given 1") and contain("when -") and contain("x then")
    }

    e7 := {
      val steps = Scenario("e7").
        given(anInt).
        when(aString) { case op :: i :: _ => -i }.
        andThen(anInt) { case e :: a :: _ => (a + 1) === e }.
        andThen(anInt) { case e :: a :: _ => a === e }

      toText { nocolor ^
        s2"""             ${steps.start}
          given {1}
          when {-}
          then {-1}
          then {-1}       ${steps.end}
        """
      } must contain("given 1") and contain("when -") and contain("x then") and contain ("+ then")
    }

    e8 := {
      val steps = Scenario("e1").
        given(anInt).
        when(aString) { case op :: i :: _ => -i }.
        andThen(anInt) { case e :: a :: _ => a === e }

      val sign = "-"
      toText { nocolor ^
        s2"""             ${steps.start}
          given {1}
          when {$sign}
          then {-1}       ${steps.end}
        """
      } must contain("+ then")
    }
  }

  "templates" - new g4 with StandardRegexStepParsers with GWT with FragmentsBuilder {
    e1 := {
      implicit val bulletTemplate: ScriptTemplate[Scenario, GivenWhenThenLines] = BulletTemplate()
      val steps = Scenario("e1").
        given(anInt).
        when(anInt)    { case i :: j :: _ => i + j }.
        andThen(anInt) { case e :: a :: _ => a === e }


      toText { nocolor ^
        s2"""                ${steps.start}
          These are the steps for an addition
           * given 1
           * when 2
           * then 3          ${steps.end}
        """
      } must contain("+ then")
    }
  }

  def toText(fs: Fragments) = (new TextRunner)(fs).replace("\n", "")

  lazy val addition = Scenario("addition").
    given(anInt).
    given(anInt).
    when(aString) { case operator :: a :: b:: _ => a + b }.
    andThen(anInt) { case expected :: sum :: _ => sum === expected }

}
