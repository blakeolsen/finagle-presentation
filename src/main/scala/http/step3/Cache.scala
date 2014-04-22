package http
package step3

import com.twitter.util.{ Future, FuturePool, JavaTimer, NonFatal }
import com.twitter.finagle.{ SimpleFilter, Service }
import com.twitter.conversions.time._
import scala.collection.concurrent.{ Map, TrieMap }
import com.twitter.util.Bijection
import org.jboss.netty.handler.codec.http.{ HttpRequest, HttpResponse }
import com.google.common.base.Charsets
import com.twitter.finagle.stats.{ StatsReceiver, DefaultStatsReceiver }

trait Cache {

  def put(key: Array[Byte], value: Array[Byte]): Future[Unit]

  def get(key: Array[Byte]): Future[Option[Array[Byte]]]

}

object MemoryCache extends Cache {

  private[this] val cache: Map[IndexedSeq[Any], Array[Byte]] = TrieMap.empty

  def put(key: Array[Byte], value: Array[Byte]) = {
    cache.put(key.deep, value)
    Future.Done
  }

  def get(key: Array[Byte]) = {
    Future.value(cache.get(key.deep))
  }

}

case class CachingFilter[Req, Rep](
  cache: Cache,
  kf: (Req) => Array[Byte],
  vb: Bijection[Rep, Array[Byte]],
  stats: StatsReceiver = DefaultStatsReceiver) extends SimpleFilter[Req, Rep] {
  
  private[this] implicit val timer = new JavaTimer(true)

  private[this] val hit = stats.counter("cache", "hit")
  private[this] val miss = stats.counter("cache", "miss")
  private[this] val fail = stats.counter("cache", "miss", "fail")

  def apply(req: Req, service: Service[Req, Rep]) = {
    val cacheKey = kf(req)
    cache.get(cacheKey)
      .handle {
        case NonFatal(e) => {
          fail.incr
          None
        }
      }
      .flatMap {
        case Some(value) => {
          hit.incr
          Future.value(vb.invert(value))
        }
        case None => {
          miss.incr
          service(req)
            .onSuccess { r => cache.put(cacheKey, vb(r)).within(1.second) }
        }
      }
  }
}

object CachingFilter {

  def Http(cache: Cache) = {
    def kf(req: HttpRequest) = s"${req.getMethod} ${req.getUri}".getBytes(Charsets.UTF_8)
    val vb: Bijection[HttpResponse, Array[Byte]] = HttpResponseToByteArray
    CachingFilter[HttpRequest, HttpResponse](cache, kf, vb)
  }
}
