package http
package step4

import com.twitter.app.{ App => TwitterApp }
import com.twitter.finagle.Redis
import com.twitter.finagle.redis.protocol.{ Command, Reply }
import org.jboss.netty.handler.codec.http.{ HttpRequest, HttpResponse }
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.redis.{ Redis => RedisCodec }
import com.twitter.finagle.stats.DefaultStatsReceiver
import com.twitter.finagle.stats.ClientStatsReceiver
import com.twitter.finagle.tracing.DefaultTracer
import com.twitter.finagle.zipkin.thrift.SamplingTracer
import com.twitter.finagle.zipkin.thrift.RawZipkinTracer

trait RedisService extends step2.Filters {
  
  val stats = ClientStatsReceiver

  lazy val redis =
    retrying[Command, Reply]() andThen
    timingout[Command, Reply]() andThen
    ClientBuilder()
      .name("redis")
      .codec(RedisCodec(stats))
      .reportTo(stats)
      .tracer(new SamplingTracer(RawZipkinTracer(), 1.0f))
      .hostConnectionLimit(1)
      .hosts("localhost:6379")
      .build
// Builder-style necessary because tracing is not activated properly otherwise.
//    Redis.newClient("localhost:6379", "redis").toService

}

object App extends TwitterApp with step1.FetchUrl with RedisService with step2.Filters {

  override val service =
    step3.CachingFilter.Http(RedisCache(redis)) andThen
    retrying[HttpRequest, HttpResponse]() andThen
    rateLimited() andThen
    timingout[HttpRequest, HttpResponse]() andThen
    FetchService
}