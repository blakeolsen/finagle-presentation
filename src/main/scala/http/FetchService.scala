package http

import org.jboss.netty.handler.codec.http.{ HttpRequest, HttpResponse }
import com.twitter.finagle.Service
import com.twitter.finagle.Http
import com.twitter.util.Future

object FetchService extends Service[HttpRequest, HttpResponse] {

  private[this] val PortForScheme = Map("http" -> 80, "https" -> 443)

  private[this] def serviceFor(req: HttpRequest): Future[Service[HttpRequest, HttpResponse]] = {
    val uri = new java.net.URI(req.getUri())
    val port = if(uri.getPort >= 0) uri.getPort else PortForScheme(uri.getScheme)
    Http.newClient(s"${uri.getHost}:$port", uri.getHost)()
  }

  def apply(req: HttpRequest): Future[HttpResponse] = {
    serviceFor(req)
      .flatMap { service =>
        service(req)
          .ensure(service.close())
      }
  }

}
