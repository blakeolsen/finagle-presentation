package http.step2

import com.twitter.app.{ App => TwitterApp }
import com.twitter.finagle.Http
import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpVersion
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import com.twitter.util.Await
import com.twitter.util.JavaTimer
import scala.util.Random
import com.twitter.conversions.time._
import com.twitter.util.Promise
import java.util.concurrent.atomic.AtomicInteger

object Bad extends TwitterApp {

  val timer = new JavaTimer(true)
  
  val random = new Random
  
  val seq = new AtomicInteger(0)

  val bad = new Service[HttpRequest, HttpResponse] {
    def apply(req: HttpRequest) = {
      val wait = random.nextInt(10)
      val id = seq.getAndIncrement
      timer.doLater(wait.seconds) {
        val ok = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
        ok.setHeader("X-Seq", id)
        ok.setHeader("X-Waited", wait)
        ok
      }
    }
  }

  def main() {
    Await.ready(Http.serve(":8976", bad))
  }
  
}