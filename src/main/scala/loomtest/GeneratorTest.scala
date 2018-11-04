package loomtest

  import java.util.concurrent.{Callable, ThreadPoolExecutor, TimeUnit, LinkedBlockingQueue}


class SillyGenerator[T](f: (T ⇒ Unit) ⇒ Unit) extends Iterator[T] {

  private val scope = new ContinuationScope("Bill")
  private val cont = new Continuation(scope, () ⇒ f(setCurrentAndYield(_)))

  private var current: T = _
  private var valid = false


  private def setCurrentAndYield(t: T): Unit = {
    current = t
    valid = true
    Continuation.`yield`(scope)
  }

  override def hasNext: Boolean = {
    if(!valid && !cont.isDone) cont.run()
    valid
  }

  override def next(): T = if(!hasNext) throw new NoSuchElementException else {
    valid = false
    current
  }
}

object SillyGenerator {
  def apply() = {
    val gen = new SillyGenerator[Int]({
      yld: (Int ⇒ Unit) ⇒
        for (i ← 0 until 10) {
          for (j ← 0 until 10) {
            println(s"about to yld $i $j")
            yld(i * j)
          }
        }
    })
    for (x ← gen) println(x)

  }
}



object GeneratorTest extends App {
  SillyGenerator()

}
