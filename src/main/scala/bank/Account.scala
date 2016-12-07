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
  def account:Account
}

case class Deposit(date: Date, amount: Double, account:Account) extends Transaction
case class Withdraw(date: Date, amount: Double, account:Account) extends Transaction
case class Transfer(date: Date, amount: Double, account:Account, fromAccount: Account, direction:Boolean) extends Transaction
case class TransactionRequest(transaction: Transaction)
case class TransactionResponse(tansactionSuccess:Try[String])

object TransactionQueue{
    var queue:Stream[Transaction] = Stream.empty[Transaction]
}

class Transactor extends Actor {
  type Amount = Double
  def receive = {
    case TransactionRequest(trans) => sender ! TransactionResponse( Try{
      if(trans.amount < 0) throw new ArithmeticException(s"Negative transactions not allowed (${trans.amount}).")
      trans match {
          case d: Deposit =>{
              TransactionQueue.queue = trans #:: TransactionQueue.queue
              "Success"
          }
          //d.copy(trans.account.transactions = trans #:: trans.account.transactions)
          case w: Withdraw =>
              if (trans.account.currentBalance >= w.amount) {
                    TransactionQueue.queue = trans #:: TransactionQueue.queue
                    "Success"
                }
              //w.copy(trans.account.transactions = w #:: trans.account.transactions)
          else
            throw new ArithmeticException(s"Insufficient balance (${trans.account.currentBalance}.")
        case t: Transfer =>
          if (trans.account.currentBalance >= t.amount){
              TransactionQueue.queue = trans #:: TransactionQueue.queue
              "Success"
          }
              //t.copy(trans.account.transactions = t #:: t.account.transactions)
          else
            throw new ArithmeticException(s"Insufficient balance (${trans.account.currentBalance}).")
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

  lazy val currentBalance = TransactionQueue.queue.filter(_.account.owner == owner).map(tr => tr match {
    case d: Deposit => d.amount
    case w: Withdraw => -w.amount
    case t:Transfer => if(t.direction) t.amount else -t.amount
    case _ => 0.0
  }).sum


  // Make a deposit, returning a new account instance with the new transaction stream.
  def deposit(amount: Amount):Try[String] = {
    val future = actor ? TransactionRequest(Deposit(new Date(),amount,this))
    Await.result(future,timeout.duration).asInstanceOf[TransactionResponse].tansactionSuccess
  }

  // Withdraw some money.
  def withdraw(amount: Amount): Try[String] = {
    val future = actor ? TransactionRequest(Withdraw(new Date(),amount,this))
    Await.result(future,timeout.duration).asInstanceOf[TransactionResponse].tansactionSuccess
  }

  def transfer(amount:Amount, toAccount:Account) : Try[(Try[String],Try[String])] = {
    Try{
      val future = actor ? TransactionRequest(Transfer(new Date(),amount,this,toAccount,direction = false))
      val future2 = actor ? TransactionRequest(Transfer(new Date(),amount,this, this, direction = true))
      (Await.result(future,timeout.duration).asInstanceOf[TransactionResponse].tansactionSuccess,
        Await.result(future2,timeout.duration).asInstanceOf[TransactionResponse].tansactionSuccess)
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
