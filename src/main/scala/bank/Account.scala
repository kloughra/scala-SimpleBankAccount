package bank

import java.util.Date

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try


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
        case d: Deposit => accnt.copy(transactions = trans #:: accnt.transactions, currentBalance = Some(accnt.currentBalance.getOrElse(0.0) + d.amount))
        case w: Withdraw =>
          if (accnt.currentBalance.getOrElse(0.0) >= w.amount)
            accnt.copy(transactions = w #:: accnt.transactions, currentBalance = Some(accnt.currentBalance.getOrElse(0.0) - w.amount))
          else
            throw new ArithmeticException(s"Insufficient balance (${accnt.currentBalance.getOrElse(0.0)}).")
        case t: Transfer =>
          if (accnt.currentBalance.getOrElse(0.0) >= t.amount)
          {accnt.copy(transactions = t #:: accnt.transactions,
            currentBalance = if(t.direction) Some(accnt.currentBalance.getOrElse(0.0) + t.amount)
            else Some(accnt.currentBalance.getOrElse(0.0) - t.amount))}
          else
            throw new ArithmeticException(s"Insufficient balance (${accnt.currentBalance.getOrElse(0.0)}).")
        }
      })
    case _ => sender ! TransactionResponse(throw new Exception("Unknown message type"))
  }
}


case class Person(first: String, last: String, ssn: String)

case class Account(opened: Date, owner: Person, transactions: Stream[Transaction],currentBalance:Option[Double],system:ActorSystem){
  type Amount = Double

  val actor = system.actorOf(Props(new Transactor()))
  implicit val timeout = Timeout(10.seconds)

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

  def transfer(amount:Amount, toAccount:Account) : (Try[Account],Try[Account]) = {
    val future = actor ? TransactionRequest(this,Transfer(new Date(),amount,toAccount,direction = false))
    val future2 = actor ? TransactionRequest(toAccount,Transfer(new Date(),amount, this, direction = true))
    (Await.result(future,timeout.duration).asInstanceOf[TransactionResponse].account,
      Await.result(future2,timeout.duration).asInstanceOf[TransactionResponse].account)
  }

  // Show me the transactions!!!
  override def toString() = transactions map (t => s"${t.date} - ${t.getClass.getSimpleName} amount ${t.amount}") mkString("\n")
}

object Account {
  // Improve the API, make it possible to create a new account with just a person instance.
  def apply(owner: Person, system:ActorSystem) = new Account(new Date(), owner, Stream[Transaction](),None,system)
}
