package loomtest

import java.util.concurrent._

object MemoizedFunction {
  lazy val t0 = System.nanoTime()
  def tid = s"${Fiber.current().toString}(${TimeUnit.MILLISECONDS.convert(System.nanoTime() - t0, TimeUnit.NANOSECONDS)})"

  var debug = false

  def memFunc[T](name: String)(f: ⇒ T)(implicit ex: Executor) = new MemoizedFunction0[T](name, f)(ex)
  def memFunc1[T1,T](name: String)(f: T1 ⇒ T)(implicit  ex: Executor) = new MemoizedFunction1[T1,T](name, f)(ex)

  def delay(ms: Long): Unit = {
    if(debug) println(s"$tid about to sleep for $ms; ought to suspend")
    Thread.sleep(ms)
    if(debug) (s"$tid woke up after $ms")
  }

  val cache = new ConcurrentHashMap[List[Any],Fiber[_]]

  def schedule[T](f: ⇒ T)(implicit ex: Executor) = Fiber.schedule[T](new Callable[T] {
    override def call(): T = f
  })


}


class MemoizedFunction0[T](name: String, f: ⇒T)(implicit ex: Executor) {
  import MemoizedFunction._
  private lazy val fiber = Fiber.schedule[T](ex, new Callable[T] {
    override def call(): T = {
      if (debug) println(s"$tid launching $name")
      f
    }
  })

  def launch() = fiber.isAlive
  def apply() = fiber.join()
}

class MemoizedFunction1[T1,T](name: String, f: T1 ⇒ T)(implicit ex: Executor) {
  import MemoizedFunction._

  def fiber(x1: T1) = {
    val key = name :: x1 :: Nil
    cache.computeIfAbsent(key, _ ⇒ Fiber.schedule(ex, new Callable[T] { override def call = f(x1) }))
  }

  def launch(x1: T1) = fiber(x1).isAlive
  def apply(x1: T1) = fiber(x1).join()

}

