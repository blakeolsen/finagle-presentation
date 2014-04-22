package http
package step2

import com.twitter.app.{ App => TwitterApp }
import com.twitter.finagle.http.RequestBuilder
import com.twitter.util.{ Await, JavaTimer, Try, Throw }
import com.twitter.finagle.service.{ Backoff, RetryingFilter, RetryPolicy }
import com.twitter.conversions.time._
import org.jboss.netty.channel.ConnectTimeoutException
import com.twitter.finagle.service.TimeoutFilter
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.HttpResponse
import com.twitter.finagle.service.RateLimitingFilter
import com.twitter.finagle.service.LocalRateLimitingStrategy
import com.twitter.util.Duration

trait Filters {

  private[this] implicit val timer = new JavaTimer(true)

  val shouldRetry: PartialFunction[Try[Nothing], Boolean] = 
    RetryPolicy.ChannelClosedExceptionsOnly orElse
    RetryPolicy.TimeoutAndWriteExceptionsOnly orElse {
      case Throw(_: ConnectTimeoutException) => true
    }

  def retrying[Req, Rep]() = {
    RetryingFilter[Req, Rep](
      Backoff.exponential(1.second, 2).take(3)
    )(shouldRetry)
  }

  def rateLimited() = {
    def domain(req: HttpRequest) = new java.net.URI(req.getUri).getHost
    new RateLimitingFilter[HttpRequest, HttpResponse](
      new LocalRateLimitingStrategy(domain, 1.second, 10)
    )
  }

  def timingout[Req, Rep](timeout: Duration = 5.seconds) = {
    new TimeoutFilter[Req, Rep](timeout, timer)
  }

}

object App extends TwitterApp with step1.FetchUrl with Filters {

  override val service =
    retrying() andThen
    rateLimited() andThen
    timingout[HttpRequest, HttpResponse]() andThen
    FetchService

}