package loomtest

//  import java.util.concurrent.{Callable, ThreadPoolExecutor, TimeUnit, LinkedBlockingQueue}


class PythonStyleGenerator[T](setNextAndYield: (T ⇒ Unit) ⇒ Unit) extends Iterator[T] {

  private val scope = new ContinuationScope("Bill")
  private val cont = new Continuation(scope, () ⇒ setNextAndYield { e ⇒
    elem = e
    stale = false
    Continuation.`yield`(scope)
  })

  private var elem: T = _
  private var stale = true

  override def hasNext: Boolean = {
    if(stale && !cont.isDone) cont.run()
    !stale
  }

  override def next(): T = if(!hasNext) throw new NoSuchElementException else {
    stale = true
    elem
  }
}

object PythonStyleGenerator {
  def apply[T](setNextAndYield: (T ⇒ Unit) ⇒ Unit) = new PythonStyleGenerator[T](setNextAndYield)
}

object GeneratorTest extends App {
  val gen = PythonStyleGenerator[Int] {
    setNextAndYield ⇒
      for (i ← 0 until 10) {
        for (j ← 0 until 10) {
          println(s"about to yield $i $j")
          setNextAndYield(i * j)
        }
      }
  }
  for (x ← gen) println(x)

}
