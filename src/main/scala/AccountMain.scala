import bank._
import akka.actor.ActorSystem
object AccountMain extends App{

  val system = ActorSystem("System")
  val me = Person("Zaphod", "Beeblebrox", "77-123-4500")
  val maybeZaphodsAccount = Account(me,system) deposit 1000.00
  val maybeZaphodsAccount2 = maybeZaphodsAccount.get.toAccount withdraw 10

  println(maybeZaphodsAccount2 map(_.toAccount currentBalance))

  println(maybeZaphodsAccount2)


  // Now a different account.
  val you = Person("Ford", "Prefect", "00-000-0001")
  val maybeFordsAccount = Account(you,system) deposit 5000.00
  maybeFordsAccount map (_.toAccount currentBalance)

  //Set up

  // Let's do a transfer.
  val zaphodsAccount = maybeZaphodsAccount2.get.toAccount // Don't do this in the real world. Use getOrElse.
  val fordsAccount = maybeFordsAccount.get.fromAccount // Nope. Never do it.
  val maybeFords2Zaphods = fordsAccount transfer(500, zaphodsAccount)
  maybeFords2Zaphods map (_.toAccount currentBalance)
  val maybeFordsAccount2 = maybeFordsAccount

  maybeFords2Zaphods map {trans =>
    println("Fords:")
    println("Transaction history:")
    println(s"Current balance: ${trans.fromAccount.currentBalance}")
    println(trans.fromAccount)
  }


  maybeFords2Zaphods map { trans =>
    println("Zaphods:")
    println("Transaction history:")
    println(s"Current balance: ${trans.toAccount.currentBalance}")
    println(trans.toAccount)
  }

  system.shutdown()
}
