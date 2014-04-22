package http
package step3

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
import com.twitter.finagle.stats.SummarizingStatsReceiver
import com.twitter.finagle.stats.LoadedStatsReceiver

object App extends TwitterApp with step1.FetchUrl with step2.Filters {

  override val service =
    CachingFilter.Http(MemoryCache) andThen
    retrying[HttpRequest, HttpResponse]() andThen
    rateLimited() andThen
    timingout[HttpRequest, HttpResponse]() andThen
    FetchService
}