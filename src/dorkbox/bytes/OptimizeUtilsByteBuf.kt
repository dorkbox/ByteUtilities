/*
 * Copyright 2023 dorkbox, llc
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
@file:Suppress("NAME_SHADOWING")

package dorkbox.bytes

import io.netty.buffer.ByteBuf

@Suppress("unused")
object OptimizeUtilsByteBuf {
    // int
    /**
     * FROM KRYO
     *
     *
     * Returns the number of bytes that would be written with [.writeInt].
     *
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (5
     * bytes).  This ultimately means that it will use fewer bytes for positive numbers.
     */
    fun intLength(value: Int, optimizePositive: Boolean): Int {
        @Suppress("NAME_SHADOWING")
        var value = value
        if (!optimizePositive) {
            value = value shl 1 xor (value shr 31)
        }
        if (value ushr 7 == 0) {
            return 1
        }
        if (value ushr 14 == 0) {
            return 2
        }
        if (value ushr 21 == 0) {
            return 3
        }
        return if (value ushr 28 == 0) {
            4
        } else 5
    }

    /**
     * FROM KRYO
     *
     *
     * look at buffer, and see if we can read the length of the int off of it. (from the reader index)
     *
     * @return 0 if we could not read anything, >0 for the number of bytes for the int on the buffer
     */
    fun canReadInt(buffer: ByteBuf): Int {
        val startIndex = buffer.readerIndex()
        return try {
            var remaining = buffer.readableBytes()
            var offset = 0
            var count = 1
            while (offset < 32 && remaining > 0) {
                val b = buffer.readByte().toInt()
                if (b and 0x80 == 0) {
                    return count
                }
                offset += 7
                remaining--
                count++
            }
            0
        } finally {
            buffer.readerIndex(startIndex)
        }
    }

    fun canReadVarInt(buffer: ByteBuf): Boolean {
        val startIndex = buffer.readerIndex()
        try {
            var remaining = buffer.readableBytes()

            if (remaining >= 5) return true

            var p = startIndex
            if (buffer.getByte(p++).toInt() and 0x80 == 0) return true
            if (p == remaining) return false

            if (buffer.getByte(p++).toInt() and 0x80 == 0) return true
            if (p == remaining) return false
            if (buffer.getByte(p++).toInt() and 0x80 == 0) return true
            if (p == remaining) return false
            if (buffer.getByte(p++).toInt() and 0x80 == 0) return true
            return if (p == remaining) false else true
        } finally {
            buffer.readerIndex(startIndex)
        }
    }




    /**
     * FROM KRYO
     *
     *
     * Reads an int from the buffer that was optimized.
     *
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (5
     * bytes). This ultimately means that it will use fewer bytes for positive numbers.
     *
     * @return the number of bytes written.
     */
    fun readInt(buffer: ByteBuf, optimizePositive: Boolean = false): Int {
        var b = buffer.readByte().toInt()
        var result = b and 0x7F
        if (b and 0x80 != 0) {
            b = buffer.readByte().toInt()
            result = result or (b and 0x7F shl 7)
            if (b and 0x80 != 0) {
                b = buffer.readByte().toInt()
                result = result or (b and 0x7F shl 14)
                if (b and 0x80 != 0) {
                    b = buffer.readByte().toInt()
                    result = result or (b and 0x7F shl 21)
                    if (b and 0x80 != 0) {
                        b = buffer.readByte().toInt()
                        result = result or (b and 0x7F shl 28)
                    }
                }
            }
        }
        return if (optimizePositive) result else result ushr 1 xor -(result and 1)
    }

    /**
     * FROM KRYO
     *
     *
     * Writes the specified int to the buffer using 1 to 5 bytes, depending on the size of the number.
     *
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (5
     * bytes). This ultimately means that it will use fewer bytes for positive numbers.
     *
     * @return the number of bytes written.
     */
    fun writeInt(buffer: ByteBuf, value: Int, optimizePositive: Boolean): Int {
        var value = value
        if (!optimizePositive) {
            value = value shl 1 xor (value shr 31)
        }
        if (value ushr 7 == 0) {
            buffer.writeByte(value.toByte().toInt())
            return 1
        }
        if (value ushr 14 == 0) {
            buffer.writeByte((value and 0x7F or 0x80).toByte().toInt())
            buffer.writeByte((value ushr 7).toByte().toInt())
            return 2
        }
        if (value ushr 21 == 0) {
            buffer.writeByte((value and 0x7F or 0x80).toByte().toInt())
            buffer.writeByte((value ushr 7 or 0x80).toByte().toInt())
            buffer.writeByte((value ushr 14).toByte().toInt())
            return 3
        }
        if (value ushr 28 == 0) {
            buffer.writeByte((value and 0x7F or 0x80).toByte().toInt())
            buffer.writeByte((value ushr 7 or 0x80).toByte().toInt())
            buffer.writeByte((value ushr 14 or 0x80).toByte().toInt())
            buffer.writeByte((value ushr 21).toByte().toInt())
            return 4
        }
        buffer.writeByte((value and 0x7F or 0x80).toByte().toInt())
        buffer.writeByte((value ushr 7 or 0x80).toByte().toInt())
        buffer.writeByte((value ushr 14 or 0x80).toByte().toInt())
        buffer.writeByte((value ushr 21 or 0x80).toByte().toInt())
        buffer.writeByte((value ushr 28).toByte().toInt())
        return 5
    }
    // long
    /**
     * Returns the 1-9 bytes that would be written with [.writeLong].
     *
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (9
     * bytes). This ultimately means that it will use fewer bytes for positive numbers.
     */
    @JvmStatic
    fun longLength(value: Long, optimizePositive: Boolean): Int {
        var value = value
        if (!optimizePositive) {
            value = value shl 1 xor (value shr 63)
        }
        if (value ushr 7 == 0L) {
            return 1
        }
        if (value ushr 14 == 0L) {
            return 2
        }
        if (value ushr 21 == 0L) {
            return 3
        }
        if (value ushr 28 == 0L) {
            return 4
        }
        if (value ushr 35 == 0L) {
            return 5
        }
        if (value ushr 42 == 0L) {
            return 6
        }
        if (value ushr 49 == 0L) {
            return 7
        }
        return if (value ushr 56 == 0L) {
            8
        } else 9
    }

    /**
     * FROM KRYO
     *
     *
     * Reads a 1-9 byte long.
     *
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (9
     * bytes). This ultimately means that it will use fewer bytes for positive numbers.
     */
    fun readLong(buffer: ByteBuf, optimizePositive: Boolean): Long {
        var b = buffer.readByte().toInt()
        var result = (b and 0x7F).toLong()
        if (b and 0x80 != 0) {
            b = buffer.readByte().toInt()
            result = result or (b and 0x7F shl 7).toLong()
            if (b and 0x80 != 0) {
                b = buffer.readByte().toInt()
                result = result or (b and 0x7F shl 14).toLong()
                if (b and 0x80 != 0) {
                    b = buffer.readByte().toInt()
                    result = result or (b and 0x7F shl 21).toLong()
                    if (b and 0x80 != 0) {
                        b = buffer.readByte().toInt()
                        result = result or ((b and 0x7F).toLong() shl 28)
                        if (b and 0x80 != 0) {
                            b = buffer.readByte().toInt()
                            result = result or ((b and 0x7F).toLong() shl 35)
                            if (b and 0x80 != 0) {
                                b = buffer.readByte().toInt()
                                result = result or ((b and 0x7F).toLong() shl 42)
                                if (b and 0x80 != 0) {
                                    b = buffer.readByte().toInt()
                                    result = result or ((b and 0x7F).toLong() shl 49)
                                    if (b and 0x80 != 0) {
                                        b = buffer.readByte().toInt()
                                        result = result or (b.toLong() shl 56)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!optimizePositive) {
            result = result ushr 1 xor -(result and 1L)
        }
        return result
    }

    /**
     * FROM KRYO
     *
     *
     * Writes a 1-9 byte long.
     *
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (9
     * bytes). This ultimately means that it will use fewer bytes for positive numbers.
     *
     * @return the number of bytes written.
     */
    fun writeLong(buffer: ByteBuf, value: Long, optimizePositive: Boolean): Int {
        var value = value
        if (!optimizePositive) {
            value = value shl 1 xor (value shr 63)
        }
        if (value ushr 7 == 0L) {
            buffer.writeByte(value.toByte().toInt())
            return 1
        }
        if (value ushr 14 == 0L) {
            buffer.writeByte((value and 0x7FL or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 7).toByte().toInt())
            return 2
        }
        if (value ushr 21 == 0L) {
            buffer.writeByte((value and 0x7FL or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 7 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 14).toByte().toInt())
            return 3
        }
        if (value ushr 28 == 0L) {
            buffer.writeByte((value and 0x7FL or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 7 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 14 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 21).toByte().toInt())
            return 4
        }
        if (value ushr 35 == 0L) {
            buffer.writeByte((value and 0x7FL or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 7 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 14 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 21 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 28).toByte().toInt())
            return 5
        }
        if (value ushr 42 == 0L) {
            buffer.writeByte((value and 0x7FL or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 7 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 14 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 21 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 28 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 35).toByte().toInt())
            return 6
        }
        if (value ushr 49 == 0L) {
            buffer.writeByte((value and 0x7FL or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 7 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 14 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 21 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 28 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 35 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 42).toByte().toInt())
            return 7
        }
        if (value ushr 56 == 0L) {
            buffer.writeByte((value and 0x7FL or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 7 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 14 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 21 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 28 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 35 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 42 or 0x80L).toByte().toInt())
            buffer.writeByte((value ushr 49).toByte().toInt())
            return 8
        }
        buffer.writeByte((value and 0x7FL or 0x80L).toByte().toInt())
        buffer.writeByte((value ushr 7 or 0x80L).toByte().toInt())
        buffer.writeByte((value ushr 14 or 0x80L).toByte().toInt())
        buffer.writeByte((value ushr 21 or 0x80L).toByte().toInt())
        buffer.writeByte((value ushr 28 or 0x80L).toByte().toInt())
        buffer.writeByte((value ushr 35 or 0x80L).toByte().toInt())
        buffer.writeByte((value ushr 42 or 0x80L).toByte().toInt())
        buffer.writeByte((value ushr 49 or 0x80L).toByte().toInt())
        buffer.writeByte((value ushr 56).toByte().toInt())
        return 9
    }

    /**
     * FROM KRYO
     *
     *
     * look at buffer, and see if we can read the length of the long off of it (from the reader index).
     *
     * @return 0 if we could not read anything, >0 for the number of bytes for the long on the buffer
     */
    fun canReadLong(buffer: ByteBuf): Int {
        val position = buffer.readerIndex()
        return try {
            var remaining = buffer.readableBytes()
            var offset = 0
            var count = 1
            while (offset < 64 && remaining > 0) {
                val b = buffer.readByte().toInt()
                if (b and 0x80 == 0) {
                    return count
                }
                offset += 7
                remaining--
                count++
            }
            0
        } finally {
            buffer.readerIndex(position)
        }
    }
}
