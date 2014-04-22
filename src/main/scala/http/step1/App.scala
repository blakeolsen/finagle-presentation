package http
package step1

import com.twitter.app.{ App => TwitterApp }
import com.twitter.finagle.http.RequestBuilder
import com.twitter.util.{ Await, Future }
import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http.{ HttpRequest, HttpResponse }

trait FetchUrl { self: TwitterApp =>

  val urls = flag("url", Seq.empty[String], "The URL(s) to fetch")

  val service: Service[HttpRequest, HttpResponse]

  def main() {
    urls().map { url =>
      Await.result {
        service(
          RequestBuilder
            .create
            .url(url)
            .proxied
            .buildGet
        ).map { response =>
          println(response.getHeaders())
        }
      }
    }
  }
}

object App extends TwitterApp with FetchUrl {

  override val service = FetchService

}