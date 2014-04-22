package http
package step6

import com.twitter.server.TwitterServer
import com.twitter.util.Await
import com.twitter.finagle.Http
import com.twitter.finagle.tracing.DefaultTracer
import com.twitter.finagle.zipkin.thrift.SamplingTracer
import com.twitter.finagle.zipkin.thrift.RawZipkinTracer
import com.twitter.util.Closable

object Server extends TwitterServer with step5.FetchUrlService {

  def main {
    val server = Http.serve(":*", fetchService)

    server.announce("zk!localhost:2181!/org/example/services/fetcher!0")
      .onFailure { t =>
        println(s"Failed to announce service: ${t.getMessage}. Failing fast.")
        System.exit(-1)
      }

    closeOnExit(Closable.all(server, redis))

    Await.ready(server)
  }
}