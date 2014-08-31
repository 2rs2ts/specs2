package org.specs2
package execute

import matcher.ResultMatchers
import matcher.MatchersImplicits._

class ResultLogicalCombinatorsSpec extends Spec with ResultMatchers { def is = s2"""

 Multiple results can be or-ed
 ${ success or success }
 ${ success or failure }
 ${ failure or success }
 ${ (failure or failure) must beFailing }
 ${ success or failure or failure }
 ${ failure or success or failure }
 ${ failure or failure or success }
 ${ success or success or failure }
 ${ success or failure or success }
 ${ success or failure or failure }
 ${ failure or success or success }
 ${ success or success or success }
 ${ (failure or failure or failure) must beFailing }
 ${ (success or Error("")) must beSuccessful  }

 Even when some of them throw FailureExceptions
 ${ (success or failure1) must beSuccessful }
 ${ (failure1 or success) must beSuccessful }
 ${ (failure1 or failure1) must beFailing }
 ${ (success or failure1 or failure1) must beSuccessful }
 ${ (failure1 or success or failure1) must beSuccessful }
 ${ (failure1 or failure1 or success) must beSuccessful }
 ${ (success or success or failure1) must beSuccessful }
 ${ (success or failure1 or success) must beSuccessful }
 ${ (failure1 or success or success) must beSuccessful }

 And results
 ${ (pending1 and pending2) === pending1 }
 """

  def failure1 = { throw new FailureException(Failure("ko")); success }
  def pending1 = Pending("1")
  def pending2 = Pending("2")
}
