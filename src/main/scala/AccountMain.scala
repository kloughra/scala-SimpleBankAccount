import bank._
import akka.actor.ActorSystem
object AccountMain extends App{

  val system = ActorSystem("System")
  val me = Person("Zaphod", "Beeblebrox", "77-123-4500")
  val maybeZaphodsAccount = Account(me,system) deposit 1000.00
  val maybeZaphodsAccount2 = maybeZaphodsAccount.get withdraw 10

  println(maybeZaphodsAccount2 map(_.currentBalance))

  println(maybeZaphodsAccount2)


  // Now a different account.
  val you = Person("Ford", "Prefect", "00-000-0001")
  val maybeFordsAccount = Account(you,system) deposit 5000.00
  maybeFordsAccount map (_.currentBalance)

  //Set up

  // Let's do a transfer.
  val zaphodsAccount = maybeZaphodsAccount2.get // Don't do this in the real world. Use getOrElse.
  val fordsAccount = maybeFordsAccount.get // Nope. Never do it.
  val maybeFords2Zaphods = fordsAccount transfer(500, zaphodsAccount)
  maybeFords2Zaphods._1 map (_.currentBalance)
  val maybeFordsAccount2 = maybeFordsAccount

  maybeFords2Zaphods._1 map {account =>
    println("Fords:")
    println("Transaction history:")
    println(s"Current balance: ${account.currentBalance}")
    println(account)
  }


  maybeFords2Zaphods._2 map { account =>
    println("Zaphods:")
    println("Transaction history:")
    println(s"Current balance: ${account.currentBalance}")
    println(account)
  }

  system.shutdown()
}
