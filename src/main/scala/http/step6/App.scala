package http
package step6

import com.twitter.app.{ App => TwitterApp }
import com.twitter.finagle.Http
import com.twitter.finagle.tracing.DefaultTracer
import com.twitter.finagle.zipkin.thrift.SamplingTracer
import com.twitter.finagle.zipkin.thrift.RawZipkinTracer
import com.twitter.finagle.http.RequestBuilder
import com.twitter.util.Await
import com.twitter.finagle.tracing.Trace
import com.twitter.finagle.tracing.Flags

object App extends TwitterApp with step1.FetchUrl {
  
  override lazy val service = Http.newClient("zk!localhost:2181!/org/example/services/fetcher", "fetcher").toService

}