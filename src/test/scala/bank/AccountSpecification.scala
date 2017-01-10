package bank

import akka.actor.ActorSystem
import org.specs2.Specification
import org.specs2.mutable.{Before,After}
import scala.util.{Success,Failure}

import scala.util.Try

/**
  * Created by katherine.loughran on 1/6/17.
  */
class AccountSpecification extends Specification {

    def is = s2"""
    | Simple transaction pattern returns the correct results.$testSimpleTransaction
     """

    def testSimpleTransaction = {
        val system = ActorSystem("System")
        //Three test users
        val person1 = Person("Harry", "Potter", "77-123-4500")
        val person2 = Person("Hermoine", "Granger", "33-412-5224")
        val person3 = person2.copy(first = "Hermon", ssn = "44-123-6453") //different person same last name

        //All have the same system
        val account1 = Account(person1)
        val account2 = account1.copy(owner = person2)
        val account3 = account1.copy(owner = person3)

        //val transactions:Stream[Transaction] = Stream.empty[Transaction]

        //First Account - test deposit and withdrawl
        val trans1 = account1 deposit 1000.00  //accnt1 1000
        val account1b = trans1 map{trans => trans.toAccount}
        val trans2 = account1b.get withdraw 10.00   //accnt1 990
        val account1c = trans2 map{trans => trans.toAccount}


        //Second Account - depost and transfer money to first account
        val trans3 = account2 deposit 5000.00  //accnt2 5000
        val account2b = trans3 map{trans => trans.fromAccount}
        val trans4 = account2b.get transfer(500, account1c.get) //accnt1 1490 accnt2 4500

        //Print Transaction Histories
       trans4 map {trans =>
            println("~Harry~")
            println("Transaction history:")
            println(s"Current balance: ${trans.toAccount.currentBalance}")
            println(trans.toAccount)
        }
        trans4 map {trans =>
            println("~Hermoine~")
            println("Transaction history:")
            println(s"Current balance: ${trans.fromAccount.currentBalance}")
            println(trans.fromAccount)
        }
        Account.end()

        //Check Balances of Account
        val res = trans2 map { trans =>
            trans.fromAccount.currentBalance
        }
        res mustEqual Success(990.0)

        val res2 = trans4 map { trans =>
            (trans.fromAccount.currentBalance,trans.toAccount.currentBalance)
        }
        res2 mustEqual Success(4500.00,1490.00)
    }

}
