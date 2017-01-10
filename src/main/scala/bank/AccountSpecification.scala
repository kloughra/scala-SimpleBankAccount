package bank
import akka.actor.ActorSystem
import org.specs2.{Specification,ScalaCheck}
import akka.actor.Status.{Success, Failure}
import scala.util.Try

/**
  * Created by katherine.loughran on 1/6/17.
  */
class AccountSpecification extends Specification{
    def is = s2"""

 This is my first specification $testok
   it is working
   really working!              $create
                                 """

    def testok = true mustEqual(true)
    val system = ActorSystem("System")

    //Three test users
    val person1 = Person("Harry", "Potter", "77-123-4500")
    val person2 = Person("Hermoine", "Granger", "33-412-5224")
    val person3 = person2.copy(first = "Hermon", ssn = "44-123-6453") //different person same last name

    //All have the same system
    val account1 = Account(person1,system)
    val account2 = account1.copy(owner = person2)
    val account3 = account1.copy(owner = person3)

    val transactions:Stream[Transaction] = Stream.empty[Transaction]

    val trans1 = account1 deposit 1000.00  //accnt1 1000
    val trans2 = account1 withdraw 10.00   //accnt1 990

    val trans3 = account2 deposit 5000.00  //accnt2 5000
    val trans4 = account2 transfer(500, account1) //accnt1 1490 accnt2 4500

    /*
   val j = trans4 match {
           case Success(t) => Some((t.asInstanceOf[Transfer].fromAccount,t.asInstanceOf[Transfer].toAccount))
           case Failure(e) => None
       }

   println(j.get._1)

*/
    def create = trans4 must_== Try(450.00)

}
