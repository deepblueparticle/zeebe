/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.msgpack;

import static io.zeebe.util.buffer.BufferUtil.bufferAsString;
import static io.zeebe.util.buffer.BufferUtil.wrapArray;

import io.zeebe.msgpack.spec.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class MsgPackUtil {

  public static MutableDirectBuffer encodeMsgPack(Consumer<MsgPackWriter> arg) {
    final UnsafeBuffer buffer = new UnsafeBuffer(new byte[1024 * 4]);
    encodeMsgPack(buffer, arg);
    return buffer;
  }

  private static void encodeMsgPack(MutableDirectBuffer buffer, Consumer<MsgPackWriter> arg) {
    final MsgPackWriter writer = new MsgPackWriter();
    writer.wrap(buffer, 0);
    arg.accept(writer);
    buffer.wrap(buffer, 0, writer.getOffset());
  }

  public static Map<String, Object> asMap(byte[] array) {
    return asMap(wrapArray(array));
  }

  public static Map<String, Object> asMap(DirectBuffer buffer) {
    return asMap(buffer, 0, buffer.capacity());
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> asMap(DirectBuffer buffer, int offset, int length) {
    final MsgPackReader reader = new MsgPackReader();
    reader.wrap(buffer, offset, length);
    return (Map<String, Object>) deserializeElement(reader);
  }

  private static Object deserializeElement(MsgPackReader reader) {

    final MsgPackToken token = reader.readToken();
    switch (token.getType()) {
      case INTEGER:
        return token.getIntegerValue();
      case FLOAT:
        return token.getFloatValue();
      case STRING:
        return bufferAsString(token.getValueBuffer());
      case BOOLEAN:
        return token.getBooleanValue();
      case BINARY:
        final DirectBuffer valueBuffer = token.getValueBuffer();
        final byte[] valueArray = new byte[valueBuffer.capacity()];
        valueBuffer.getBytes(0, valueArray);
        return valueArray;
      case MAP:
        final Map<String, Object> valueMap = new HashMap<>();
        final int mapSize = token.getSize();
        for (int i = 0; i < mapSize; i++) {
          final String key = (String) deserializeElement(reader);
          final Object value = deserializeElement(reader);
          valueMap.put(key, value);
        }
        return valueMap;
      case ARRAY:
        final int size = token.getSize();
        final Object[] arr = new Object[size];
        for (int i = 0; i < size; i++) {
          arr[i] = deserializeElement(reader);
        }
        return toString(arr);
      default:
        throw new RuntimeException("Not implemented yet");
    }
  }

  private static String toString(Object[] arr) {
    final StringBuilder buf = new StringBuilder("[");

    if (arr.length > 0) {
      buf.append(arr[0].toString());
      for (int i = 1; i < arr.length; i++) {
        buf.append(", ");
        buf.append(arr[i].toString());
      }
    }

    buf.append("]");

    return buf.toString();
  }
}
