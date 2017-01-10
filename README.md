# scala-SimpleBankAccount

Bank account.

Uses specs2 - to test, run sbt test

Currently: 
  - only one type of bank account 
  - transaction class has toAccount and fromAccount - for deposit and withdrawl they are the same account
  - basic test with only one test case  
  - Account transactions will return a transaction. The returned transaction inlcludes a new copy of the account with itself added to the account's transactions
  - Accounts are identified as unique by the ssn of the Owner
  - Uses Akka actor Transactor to deal with transactions

Future work:
 - implement a test using scala check generator
 - implement some rules surrounding different types of accounts - a savings account with interest etc
 - Add bank account number as a unique identifier, bank accounts with multiple owners
 - Use a library like Accord for validation
 - Add some sort of persistence - perhaps send to Kafka topic and track with Kafka Tables?
 - Improve actor model - currently just using an actor the do the work of creating a new transaction


