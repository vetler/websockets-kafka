import java.nio.ByteBuffer

import org.msgpack.MessagePack
import org.msgpack.`type`.{RawValue, ValueFactory}


class MsgPack(buffer: ByteBuffer) {

  import MsgPack.msgPack

  val map = msgPack.read(buffer.array()).asMapValue()


  def get(key: String): RawValue = {
    map.get(ValueFactory.createRawValue(key)).asRawValue()
  }
}

object MsgPack {
  implicit def valueToString(v: RawValue) = v.getString

  val msgPack = new MessagePack
}
