package loomtest

import java.util.concurrent._



object MemoizedFunction {
  lazy val t0 = System.nanoTime()
  def tid = s"${Fiber.current().toString}(${TimeUnit.MILLISECONDS.convert(System.nanoTime() - t0, TimeUnit.NANOSECONDS)})"
  def memFunc[T](name: String)(f: ⇒ T)(implicit ex: Executor) = new MemoizedFunction0[T](name, f)(ex)
  def delay(ms: Long): Unit = {
    println(s"$tid about to sleep for $ms; ought to suspend")
    Thread.sleep(ms)
    println(s"$tid woke up after $ms")
  }

}

class MemoizedFunction0[T](name: String, f: ⇒T)(implicit ex: Executor) {
  import MemoizedFunction.tid
  private lazy val fiber = Fiber.schedule[T](ex, new Callable[T] {
    override def call(): T = {
      println(s"$tid launching $name")
      f
    }
  })
  def launch() = !fiber.isDone
  def apply() = fiber.get()


}


object NativeStackTest  extends App {
  import MemoizedFunction._

  implicit val ex = new ThreadPoolExecutor(1,1,0, TimeUnit.SECONDS,new LinkedBlockingQueue[Runnable]())

  //  System.setProperty("jdk.defaultScheduler.parallelism", "1")

  val nf1 = memFunc("nf1") {
    println(s"$tid in nf1 about to delay")
    delay(1000)
    println(s"$tid in node1 woke up")
    1
  }

  val dillyDally = memFunc("dillydally") {
    println(s"$tid Entering dillydally.  Should now park, with native stack.")
    delay(2000)
    println(s"$tid dillydallied.  Now calling nf1.")
    val ret = nf1()
    println(s"$tid dillydallier returning")
    ret
  }


  def nativeStack = {
    println(s"$tid Entering nativestack")
    val ret = dillyDally()
    println(s"$tid leaving nativestack")
    ret
  }


  val good = memFunc("good") {
    println(s"$tid goodNode about to call node1")
    val ret = nf1()
    println(s"$tid leaving goodnode")
    ret
  }

  val bad = memFunc("bad") {
    println(s"$tid badNode about to call nf1 via nativestack")
    nativeStack
  }

  val sad = memFunc("sad") {
    println(s"$tid About to launch bad directly")
    bad.launch()
    Thread.sleep(10)
    println(s"$tid About to launch good directly")
    good.launch()
    bad() + good()
  }

  println(sad())


}
