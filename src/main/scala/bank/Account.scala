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
  def toAccount: Account
  def fromAccount: Account
}

case class Deposit(date: Date, amount: Double, toAccount:Account, fromAccount:Account) extends Transaction
case class Withdraw(date: Date, amount: Double, toAccount:Account, fromAccount:Account) extends Transaction
case class Transfer(date: Date, amount: Double, toAccount:Account, fromAccount:Account) extends Transaction
//case class Transfer(date: Date, amount: Double, fromAccount: Account, direction:Boolean) extends Transaction

case class TransactionRequest(transaction: Transaction)
case class TransactionResponse(transaction: Try[Transaction])


class Transactor extends Actor {
  type Amount = Double
  def receive = {
    case TransactionRequest(trans) => sender ! TransactionResponse( Try{
      if(trans.amount < 0) throw new ArithmeticException(s"Negative transactions not allowed (${trans.amount}).")
      trans match {
          case d: Deposit =>
              //return the a new transaction with new account - including that new transaction
              val newAccount = d.toAccount.copy(transactions = d #:: d.toAccount.transactions)
              d.copy(toAccount = newAccount, fromAccount = newAccount)
          //accnt.copy(transactions = trans #:: accnt.transactions)

          case w: Withdraw =>
              if (w.fromAccount.currentBalance >= w.amount) {
                val newAccount = w.toAccount.copy(transactions = w #:: w.toAccount.transactions)
                w.copy(toAccount = newAccount, fromAccount = newAccount)
              }
          else
            throw new ArithmeticException(s"Insufficient balance (${w.fromAccount.currentBalance}.")
        case t: Transfer =>
          if (t.fromAccount.currentBalance >= t.amount)
            t.copy(toAccount = t.toAccount.copy(transactions = t #:: t.toAccount.transactions),
                   fromAccount = t.fromAccount.copy(transactions = t #:: t.fromAccount.transactions))
          else
            throw new ArithmeticException(s"Insufficient balance (${t.fromAccount.currentBalance}).")
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
    case t:Transfer => if(t.toAccount.owner.ssn == this.owner.ssn) t.amount else -t.amount
    case _ => 0.0
  }).sum


  // Make a deposit, returning a new account instance with the new transaction stream.
  def deposit(amount: Amount):Try[Transaction] = {
    val future = actor ? TransactionRequest(Deposit(new Date(),amount,this,this))
    Await.result(future,timeout.duration).asInstanceOf[TransactionResponse].transaction
  }

  // Withdraw some money.
  def withdraw(amount: Amount): Try[Transaction] = {
    val future = actor ? TransactionRequest(Withdraw(new Date(),amount,this,this))
    Await.result(future,timeout.duration).asInstanceOf[TransactionResponse].transaction
  }

  def transfer(amount:Amount, toAccount:Account) : Try[Transaction] = {
    val future = actor ? TransactionRequest(Transfer(new Date(),amount,toAccount,this))
    Await.result(future,timeout.duration).asInstanceOf[TransactionResponse].transaction

  }

  // Show me the transactions!!!
  override def toString() = transactions map (t => s"${t.date} :: ${t.getClass.getSimpleName} amount ${directionString(t)} ${t.amount}") mkString("\n")

  private def directionString(trans:Transaction) = {
       trans match{
           case d:Deposit => " +"
           case w:Withdraw => " -"
           case t:Transfer => if(t.toAccount.owner.ssn == this.owner.ssn) " +" else " -"
           case _ => ""
       }
  }
}

object Account {
  // Improve the API, make it possible to create a new account with just a person instance.
  def apply(owner: Person, system:ActorSystem) = new Account(new Date(), owner, Stream[Transaction](),system)
}
