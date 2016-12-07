import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

val smallInteger = Gen.choose(0, 100)

val test = forAll(smallInteger){
  n =>
    (n >= 0 && n <= 100) == true
}

test.check

import org.scalacheck.Properties
import org.scalacheck.Prop.{BooleanOperators, forAll}

class ZeroSpecification extends Properties("Zero") {

  property("addition property") = forAll { n: Int => (n != 0) ==> (n + 0 == n) }

  property("additive inverse property") = forAll { n: Int => (n != 0) ==> (n + (-n) == 0) }

  property("multiplication property") = forAll { n: Int => (n != 0) ==> (n * 0 == 0) }

}

val test2 = new ZeroSpecification

val myGen = for {
  n <- Gen.choose(10, 20)
  m <- Gen.choose(2 * n, 500)
} yield (n, m)

val c = myGen.sample

import bank._


/*check{
  forAll(smallInteger) { n =>
    (n >= 0 && n <= 100) ==
      true

  }
}
*/