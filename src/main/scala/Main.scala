import akka.actor.SupervisorStrategy.{Restart, Resume}
import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props}


object AkkaSupervisionExample extends App {

    case object CreatChild
    case class SignalChild(n : Int)
    case class PrintSignal(order : Int)
    case class DevideNumber( n : Int , d: Int)
    case object BadStuf
  class ParentActor extends Actor {

    private var number = 0
    private val children = collection.mutable.Buffer[ActorRef]()
    override def receive: Receive =  {

      case CreatChild =>
        children += context.actorOf(Props[ChildActor] , "child"+number)
        number +=1
      case SignalChild(n) =>
        children.foreach(_ ! PrintSignal(n))
    }

    override  val supervisorStrategy = OneForOneStrategy(loggingEnabled =  false){

      case ae: ArithmeticException => Resume
      case _:Exception => Restart

    }
    }
  class ChildActor extends Actor {
    println("child created.")

    override def receive: Receive = {

      case PrintSignal(n) =>
        println(n+" "+self)
      case  DevideNumber(n , d) =>
        println(n/d)
      case BadStuf => throw  new RuntimeException("Bad STUF Happened")

    }


    override def preStart() = {
      super.preStart()
      println("preStart")
    }

    override def postStop()  = {
      super.postStop()
      println("postStop")
    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      super.preRestart(reason, message)
      println("preRestart")

    }


    override def postRestart(reason: Throwable): Unit = {
      super.postRestart(reason)
      println("postRestart")

    }


  }
  val system = ActorSystem("SUPERVISION")
  val actor1 = system.actorOf(Props[ParentActor] , "Parent1")
  val actor2 = system.actorOf(Props[ParentActor] , "Parent2")

  actor1 ! CreatChild
  val child1 = system.actorSelection("akka://SUPERVISION/user/Parent1/child0")
  child1 ! DevideNumber(10 , 0)
  child1 ! DevideNumber(10 , 2)
  actor1 ! SignalChild(1)
  child1 ! BadStuf

  Thread.sleep(1000)
  system.terminate()
}