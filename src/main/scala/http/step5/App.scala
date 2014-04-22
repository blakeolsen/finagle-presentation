package http
package step5

import com.twitter.server.TwitterServer
import com.twitter.util.{ Await, Future }
import com.twitter.finagle.{ Http, SimpleFilter, Service }
import com.twitter.finagle.http.{ Request, RequestBuilder }
import org.jboss.netty.handler.codec.http._
import com.twitter.finagle.HttpServer
import com.twitter.finagle.tracing.Trace
import com.twitter.finagle.tracing.DefaultTracer
import com.twitter.finagle.zipkin.thrift.SamplingTracer
import com.twitter.finagle.zipkin.thrift.RawZipkinTracer
import com.twitter.util.Closable
import com.twitter.server.Closer

trait FetchUrlService extends step4.RedisService with step2.Filters {
  
  val BadRequest = Future.value(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST))

  val UrlFromParam = new SimpleFilter[HttpRequest, HttpResponse] {
    def apply(req: HttpRequest, service: Service[HttpRequest, HttpResponse]) = {
      Option(Request(req).getParam("url"))
        .map { url =>
          service(
            RequestBuilder
              .create
              .url(url)
              .proxied
              .buildGet
          )
        }.getOrElse(BadRequest)
    }
  }

  lazy val fetchService =
    UrlFromParam andThen
    step3.CachingFilter.Http(step4.RedisCache(redis)) andThen
    retrying[HttpRequest, HttpResponse]() andThen
    rateLimited() andThen
    timingout[HttpRequest, HttpResponse]() andThen
    FetchService

}

object App extends TwitterServer with FetchUrlService {
  
  def main {
    val server = Http.serve(":8080", fetchService)

    closeOnExit(Closable.all(server, redis))

    Await.ready(server)
  }
}