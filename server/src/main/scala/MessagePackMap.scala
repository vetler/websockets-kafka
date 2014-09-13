import java.nio.ByteBuffer

import org.msgpack.MessagePack
import org.msgpack.`type`.{RawValue, ValueFactory}


class MessagePackMap(buffer: ByteBuffer) {

  import MessagePackMap.msgPack

  val map = msgPack.read(buffer.array()).asMapValue()

  def get(key: String): RawValue = {
    map.get(ValueFactory.createRawValue(key)).asRawValue()
  }
}

object MessagePackMap {
  val msgPack = new MessagePack
}
