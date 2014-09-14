import java.nio.ByteBuffer

import org.msgpack.MessagePack
import org.msgpack.`type`.{ArrayValue, RawValue, Value, ValueFactory}

class MsgPack(buffer: ByteBuffer) {

    import MsgPack.msgPack

    val map = msgPack.read(buffer.array()).asMapValue()

    def get(key: String): Value = {
        map.get(ValueFactory.createRawValue(key))
    }
}

object MsgPack {
    implicit def valueToString(v: Value) = v match {
        case raw: RawValue => raw.getString
    }

    implicit def valueToList(v: Value): List[String] = v match {
        case array: ArrayValue => array.getElementArray.map(valueToString).toList
    }

    val msgPack = new MessagePack
}
