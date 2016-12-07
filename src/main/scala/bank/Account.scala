package bank

import java.util.Date

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

import com.wix.accord.dsl._

//Validate Amount !< 0
//date in this lifetime?

//Validate account
//validate username
//validate account amount > 0


abstract class Transaction {
  type Amount = Double
  def date:Date
  def amount:Amount
}

case class Deposit(date: Date, amount: Double) extends Transaction
case class Withdraw(date: Date, amount: Double) extends Transaction
case class Transfer(date: Date, amount: Double, fromAccount: Account, direction:Boolean) extends Transaction

case class TransactionRequest(account:Account,transaction: Transaction)
case class TransactionResponse(account:Try[Account])


class Transactor extends Actor {
  type Amount = Double
  def receive = {
    case TransactionRequest(accnt,trans) => sender ! TransactionResponse( Try{
      if(trans.amount < 0) throw new ArithmeticException(s"Negative transactions not allowed (${trans.amount}).")
      trans match {
        case d: Deposit =>
          accnt.copy(transactions = trans #:: accnt.transactions)
        case w: Withdraw =>
          if (accnt.currentBalance >= w.amount)
            accnt.copy(transactions = w #:: accnt.transactions)
          else
            throw new ArithmeticException(s"Insufficient balance (${accnt.currentBalance}.")
        case t: Transfer =>
          if (accnt.currentBalance >= t.amount)
            accnt.copy(transactions = t #:: accnt.transactions)
          else
            throw new ArithmeticException(s"Insufficient balance (${accnt.currentBalance}).")
        }
      })
    case _ => sender ! TransactionResponse(throw new Exception("Unknown message type"))
  }
}


case class Person(first: String, last: String, ssn: String)

case class Account(opened: Date, owner: Person, transactions: Stream[Transaction],system:ActorSystem){
  type Amount = Double

  val actor = system.actorOf(Props(new Transactor()))
  implicit val timeout = Timeout(10.seconds)

  lazy val currentBalance = transactions.map(t => t match {
    case d: Deposit => d.amount
    case w: Withdraw => -w.amount
    case t:Transfer => if(t.direction) t.amount else -t.amount
    case _ => 0.0
  }).sum

  // Make a deposit, returning a new account instance with the new transaction stream.
  def deposit(amount: Amount):Try[Account] = {
    val future = actor ? TransactionRequest(this,Deposit(new Date(),amount))
    Await.result(future,timeout.duration).asInstanceOf[TransactionResponse].account
  }

  // Withdraw some money.
  def withdraw(amount: Amount): Try[Account] = {
    val future = actor ? TransactionRequest(this,Withdraw(new Date(),amount))
    Await.result(future,timeout.duration).asInstanceOf[TransactionResponse].account
  }

  def transfer(amount:Amount, toAccount:Account) : Try[(Try[Account],Try[Account])] = {
    Try{
      val future = actor ? TransactionRequest(this,Transfer(new Date(),amount,toAccount,direction = false))
      val future2 = actor ? TransactionRequest(toAccount,Transfer(new Date(),amount, this, direction = true))
      (Await.result(future,timeout.duration).asInstanceOf[TransactionResponse].account,
        Await.result(future2,timeout.duration).asInstanceOf[TransactionResponse].account)
    }
  }

  // Show me the transactions!!!
  override def toString() = transactions map (t => s"${t.date} - ${t.getClass.getSimpleName} amount ${t.amount}") mkString("\n")
}

object Account {
  // Improve the API, make it possible to create a new account with just a person instance.
  lazy val system = ActorSystem("System")
  def apply(owner: Person) = new Account(new Date(), owner, Stream[Transaction](),system)
  def end() = if(!this.system.isTerminated) {
    this.system.shutdown()
    this.system.awaitTermination()
  }
}
