import com.twitter.app.{ App => TwitterApp }
import com.twitter.finagle.tracing.DefaultTracer
import com.twitter.finagle.zipkin.thrift.SamplingTracer
import com.twitter.finagle.zipkin.thrift.RawZipkinTracer

import com.twitter.util.Bijection
import org.jboss.netty.handler.codec.http.{DefaultHttpResponse, HttpRequest, HttpResponse, HttpResponseStatus, HttpVersion}
import com.google.common.base.Charsets
import com.google.common.io.ByteStreams
import org.jboss.netty.buffer.ChannelBuffers

package object http {
  
  val HttpResponseToByteArray = {
    import scala.collection.JavaConverters._
    new Bijection[HttpResponse, Array[Byte]] {
      def apply(a: HttpResponse): Array[Byte] = {
        val out = ByteStreams.newDataOutput()
        out.writeInt(a.getStatus.getCode)
        out.writeInt(a.headers.entries.size)
        a.headers().asScala.foreach { e =>
          out.writeUTF(e.getKey)
          out.writeUTF(e.getValue)
        }
        val buf = new Array[Byte](a.getContent.readableBytes)
        a.getContent.duplicate.readBytes(buf)
        out.writeInt(buf.size)
        out.write(buf)
        out.toByteArray
      }

      def invert(b: Array[Byte]): HttpResponse = {
        val in = ByteStreams.newDataInput(b, 0)
        val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(in.readInt))
        (0 until in.readInt).foreach { _ =>
          response.headers.add(in.readUTF(), in.readUTF)
        }
        val buf = new Array[Byte](in.readInt)
        in.readFully(buf)
        response.setContent(ChannelBuffers.wrappedBuffer(buf))
        response
      }
    }
  }

  trait DemoApp { self: TwitterApp =>
  	DefaultTracer.self = new SamplingTracer(RawZipkinTracer(), 1.0f)
  }

}