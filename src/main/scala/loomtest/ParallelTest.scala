package loomtest

import java.util.concurrent._



object ParallelTest  extends App {
  import MemoizedFunction.schedule

    implicit val ex = new ThreadPoolExecutor(1,1,0, TimeUnit.SECONDS,new LinkedBlockingQueue[Runnable]())

  //  System.setProperty("jdk.defaultScheduler.parallelism", "1")

  def rando(i: Int) = schedule[Int] {
    val t = ThreadLocalRandom.current().nextInt(100, 500)
    val verbose = (i % 10000) == 0
    if(verbose) println(s"Starting $i; sleep for $t")
    Thread.sleep(t)
    if(verbose) println(s"Completing $i")
    i
  }

  val t0 = System.nanoTime()
  val futs = (0 to 100000).map(rando(_))
  val tot = futs.foldLeft(0)(_ + _.get())
  val t1 = System.nanoTime()
  println((t1-t0)/1.0e6)
  println(tot)

  ex.shutdown()


}
