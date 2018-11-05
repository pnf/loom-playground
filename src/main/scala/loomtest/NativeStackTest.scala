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

/*
Optional[Fiber@32377cb5[pool-1-thread-1,main]](0) launching sad
Optional[Fiber@32377cb5[pool-1-thread-1,main]](3) About to launch bad directly
Optional[Fiber@634875b3[pool-1-thread-1,main]](7) launching bad
Optional[Fiber@634875b3[pool-1-thread-1,main]](7) badNode about to call nf1 via nativestack
Optional[Fiber@634875b3[pool-1-thread-1,main]](7) Entering nativestack
Optional[Fiber@c28765e[pool-1-thread-1,main]](8) launching dillydally
Optional[Fiber@c28765e[pool-1-thread-1,main]](8) Entering dillydally.  Should now park, with native stack.
Optional[Fiber@c28765e[pool-1-thread-1,main]](8) about to sleep for 2000; ought to suspend
Optional[Fiber@32377cb5[pool-1-thread-1,main]](17) About to launch good directly
Optional[Fiber@6ac5436c[pool-1-thread-1,main]](18) launching good
Optional[Fiber@6ac5436c[pool-1-thread-1,main]](18) goodNode about to call node1
Optional[Fiber@c61467a[pool-1-thread-1,main]](19) launching nf1
Optional[Fiber@c61467a[pool-1-thread-1,main]](19) in nf1 about to delay
Optional[Fiber@c61467a[pool-1-thread-1,main]](19) about to sleep for 1000; ought to suspend
Optional[Fiber@c61467a[pool-1-thread-1,main]](1021) woke up after 1000
Optional[Fiber@c61467a[pool-1-thread-1,main]](1021) in node1 woke up
Optional[Fiber@6ac5436c[pool-1-thread-1,main]](1022) leaving goodnode
Optional[Fiber@c28765e[pool-1-thread-1,main]](2012) woke up after 2000
Optional[Fiber@c28765e[pool-1-thread-1,main]](2012) dillydallied.  Now calling nf1.
Optional[Fiber@c28765e[pool-1-thread-1,main]](2012) dillydallier returning
Optional[Fiber@634875b3[pool-1-thread-1,main]](2013) leaving nativestack
2

 */
