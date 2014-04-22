package http
package step4

import com.twitter.finagle.Service
import com.twitter.finagle.redis.protocol._
import org.jboss.netty.buffer.ChannelBuffers

case class RedisCache(redis: Service[Command, Reply]) extends step3.Cache {

  def put(key: Array[Byte], value: Array[Byte]) = {
    val cmd = Set(ChannelBuffers.wrappedBuffer(key), ChannelBuffers.wrappedBuffer(value))
    redis(cmd)
      .map {
        case StatusReply(message) => ()
        case ErrorReply(message) => ()
        case _ => ()
      }
  }

  def get(key: Array[Byte]) = {
    val cmd = Get(ChannelBuffers.wrappedBuffer(key))
    redis(cmd)
      .map {
        case BulkReply(value) => Some {
          val buf = new Array[Byte](value.readableBytes)
          value.readBytes(buf)
          buf
        }
        case EmptyBulkReply() => None
        case ErrorReply(message) => None
        case _ => None
      }
  }

}