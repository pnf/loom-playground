package loomtest

import java.util.{Timer, TimerTask}

object Interruption extends App {
  private val scope = new ContinuationScope("Scuse")
  val epochal = BigInt(1000000)
  @volatile var x = BigInt(0)
  val cont = new Continuation(scope, () ⇒
    while(true) {
      if((x % epochal) == 0) println(x)
      x = x + 1
    }
  )
  val cur = Thread.currentThread()

  val timer = new Timer
  timer.schedule(new TimerTask {
    override def run(): Unit = cont.tryPreempt(cur)
    }, 1000l)
  cont.run()
  println(x)

}
