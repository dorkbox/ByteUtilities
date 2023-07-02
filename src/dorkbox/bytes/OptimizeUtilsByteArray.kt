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
@file:Suppress("UNUSED_CHANGED_VALUE", "NAME_SHADOWING")

package dorkbox.bytes

@Suppress("unused")
object OptimizeUtilsByteArray {
    /**
     * Gets the version number.
     */
    const val version = BytesInfo.version

    /**
     * Returns the number of bytes that would be written with [.writeInt].
     *
     * @param optimizePositive
     * true if you want to optimize the number of bytes needed to write the length value
     */
    fun intLength(value: Int, optimizePositive: Boolean): Int {
        return OptimizeUtilsByteBuf.intLength(value, optimizePositive)
    }

    /**
     * FROM KRYO
     *
     *
     * look at buffer, and see if we can read the length of the int off of it. (from the reader index)
     *
     * @param position where in the buffer to start reading
     * @return 0 if we could not read anything, >0 for the number of bytes for the int on the buffer
     */
    fun canReadInt(buffer: ByteArray, position: Int = 0): Boolean {
        var position = position
        val length = buffer.size
        if (length >= 5) {
            return true
        }
        if (position + 1 > length) {
            return false
        }
        if (buffer[position++].toInt() and 0x80 == 0) {
            return true
        }
        if (position == length) {
            return false
        }
        if (buffer[position++].toInt() and 0x80 == 0) {
            return true
        }
        if (position == length) {
            return false
        }
        if (buffer[position++].toInt() and 0x80 == 0) {
            return true
        }
        if (position == length) {
            return false
        }
        return if (buffer[position++].toInt() and 0x80 == 0) {
            true
        } else position != length
    }


    /**
     * FROM KRYO
     *
     *
     * Reads an int from the buffer that was optimized.
     *
     * @param position where in the buffer to start reading
     */
    fun readInt(buffer: ByteArray, position: Int): Int {
        return readInt(buffer, false, position)
    }

    /**
     * FROM KRYO
     *
     *
     * Reads an int from the buffer that was optimized.
     *
     * @param position where in the buffer to start reading
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (5
     * bytes).  This ultimately means that it will use fewer bytes for positive numbers.
     */
    fun readInt(buffer: ByteArray, optimizePositive: Boolean = false, position: Int = 0): Int {
        var position = position
        var b = buffer[position++].toInt()
        var result = b and 0x7F
        if (b and 0x80 != 0) {
            b = buffer[position++].toInt()
            result = result or (b and 0x7F shl 7)
            if (b and 0x80 != 0) {
                b = buffer[position++].toInt()
                result = result or (b and 0x7F shl 14)
                if (b and 0x80 != 0) {
                    b = buffer[position++].toInt()
                    result = result or (b and 0x7F shl 21)
                    if (b and 0x80 != 0) {
                        b = buffer[position++].toInt()
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
     * @param position where in the buffer to start writing
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (5
     * bytes).  This ultimately means that it will use fewer bytes for positive numbers.
     *
     * @return the number of bytes written.
     */
    fun writeInt(buffer: ByteArray, value: Int, optimizePositive: Boolean = false, position: Int = 0): Int {
        var value = value
        var position = position
        if (!optimizePositive) {
            value = value shl 1 xor (value shr 31)
        }
        if (value ushr 7 == 0) {
            buffer[position++] = value.toByte()
            return 1
        }
        if (value ushr 14 == 0) {
            buffer[position++] = (value and 0x7F or 0x80).toByte()
            buffer[position++] = (value ushr 7).toByte()
            return 2
        }
        if (value ushr 21 == 0) {
            buffer[position++] = (value and 0x7F or 0x80).toByte()
            buffer[position++] = (value ushr 7 or 0x80).toByte()
            buffer[position++] = (value ushr 14).toByte()
            return 3
        }
        if (value ushr 28 == 0) {
            buffer[position++] = (value and 0x7F or 0x80).toByte()
            buffer[position++] = (value ushr 7 or 0x80).toByte()
            buffer[position++] = (value ushr 14 or 0x80).toByte()
            buffer[position++] = (value ushr 21).toByte()
            return 4
        }
        buffer[position++] = (value and 0x7F or 0x80).toByte()
        buffer[position++] = (value ushr 7 or 0x80).toByte()
        buffer[position++] = (value ushr 14 or 0x80).toByte()
        buffer[position++] = (value ushr 21 or 0x80).toByte()
        buffer[position++] = (value ushr 28).toByte()
        return 5
    }

    /**
     * Returns 1-9 bytes that would be written with [.writeLong].
     *
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (9
     * bytes). This ultimately means that it will use fewer bytes for positive numbers.
     */
    fun longLength(value: Long, optimizePositive: Boolean): Int {
        return OptimizeUtilsByteBuf.longLength(value, optimizePositive)
    }
    // long
    /**
     * FROM KRYO
     *
     *
     * Reads a 1-9 byte long.
     *
     * @param position where in the buffer to start reading
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (9
     * bytes). This ultimately means that it will use fewer bytes for positive numbers.
     */
    fun readLong(buffer: ByteArray, optimizePositive: Boolean = false, position: Int = 0): Long {
        var position = position
        var b = buffer[position++].toInt()
        var result = (b and 0x7F).toLong()
        if (b and 0x80 != 0) {
            b = buffer[position++].toInt()
            result = result or (b and 0x7F shl 7).toLong()
            if (b and 0x80 != 0) {
                b = buffer[position++].toInt()
                result = result or (b and 0x7F shl 14).toLong()
                if (b and 0x80 != 0) {
                    b = buffer[position++].toInt()
                    result = result or (b and 0x7F shl 21).toLong()
                    if (b and 0x80 != 0) {
                        b = buffer[position++].toInt()
                        result = result or ((b and 0x7F).toLong() shl 28)
                        if (b and 0x80 != 0) {
                            b = buffer[position++].toInt()
                            result = result or ((b and 0x7F).toLong() shl 35)
                            if (b and 0x80 != 0) {
                                b = buffer[position++].toInt()
                                result = result or ((b and 0x7F).toLong() shl 42)
                                if (b and 0x80 != 0) {
                                    b = buffer[position++].toInt()
                                    result = result or ((b and 0x7F).toLong() shl 49)
                                    if (b and 0x80 != 0) {
                                        b = buffer[position++].toInt()
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
     * @param position where in the buffer to start writing
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (9
     * bytes).
     *
     * @return the number of bytes written.
     */
    fun writeLong(buffer: ByteArray, value: Long, optimizePositive: Boolean = false, position: Int = 0): Int {
        var value = value
        var position = position
        if (!optimizePositive) {
            value = value shl 1 xor (value shr 63)
        }
        if (value ushr 7 == 0L) {
            buffer[position++] = value.toByte()
            return 1
        }
        if (value ushr 14 == 0L) {
            buffer[position++] = (value and 0x7FL or 0x80L).toByte()
            buffer[position++] = (value ushr 7).toByte()
            return 2
        }
        if (value ushr 21 == 0L) {
            buffer[position++] = (value and 0x7FL or 0x80L).toByte()
            buffer[position++] = (value ushr 7 or 0x80L).toByte()
            buffer[position++] = (value ushr 14).toByte()
            return 3
        }
        if (value ushr 28 == 0L) {
            buffer[position++] = (value and 0x7FL or 0x80L).toByte()
            buffer[position++] = (value ushr 7 or 0x80L).toByte()
            buffer[position++] = (value ushr 14 or 0x80L).toByte()
            buffer[position++] = (value ushr 21).toByte()
            return 4
        }
        if (value ushr 35 == 0L) {
            buffer[position++] = (value and 0x7FL or 0x80L).toByte()
            buffer[position++] = (value ushr 7 or 0x80L).toByte()
            buffer[position++] = (value ushr 14 or 0x80L).toByte()
            buffer[position++] = (value ushr 21 or 0x80L).toByte()
            buffer[position++] = (value ushr 28).toByte()
            return 5
        }
        if (value ushr 42 == 0L) {
            buffer[position++] = (value and 0x7FL or 0x80L).toByte()
            buffer[position++] = (value ushr 7 or 0x80L).toByte()
            buffer[position++] = (value ushr 14 or 0x80L).toByte()
            buffer[position++] = (value ushr 21 or 0x80L).toByte()
            buffer[position++] = (value ushr 28 or 0x80L).toByte()
            buffer[position++] = (value ushr 35).toByte()
            return 6
        }
        if (value ushr 49 == 0L) {
            buffer[position++] = (value and 0x7FL or 0x80L).toByte()
            buffer[position++] = (value ushr 7 or 0x80L).toByte()
            buffer[position++] = (value ushr 14 or 0x80L).toByte()
            buffer[position++] = (value ushr 21 or 0x80L).toByte()
            buffer[position++] = (value ushr 28 or 0x80L).toByte()
            buffer[position++] = (value ushr 35 or 0x80L).toByte()
            buffer[position++] = (value ushr 42).toByte()
            return 7
        }
        if (value ushr 56 == 0L) {
            buffer[position++] = (value and 0x7FL or 0x80L).toByte()
            buffer[position++] = (value ushr 7 or 0x80L).toByte()
            buffer[position++] = (value ushr 14 or 0x80L).toByte()
            buffer[position++] = (value ushr 21 or 0x80L).toByte()
            buffer[position++] = (value ushr 28 or 0x80L).toByte()
            buffer[position++] = (value ushr 35 or 0x80L).toByte()
            buffer[position++] = (value ushr 42 or 0x80L).toByte()
            buffer[position++] = (value ushr 49).toByte()
            return 8
        }
        buffer[position++] = (value and 0x7FL or 0x80L).toByte()
        buffer[position++] = (value ushr 7 or 0x80L).toByte()
        buffer[position++] = (value ushr 14 or 0x80L).toByte()
        buffer[position++] = (value ushr 21 or 0x80L).toByte()
        buffer[position++] = (value ushr 28 or 0x80L).toByte()
        buffer[position++] = (value ushr 35 or 0x80L).toByte()
        buffer[position++] = (value ushr 42 or 0x80L).toByte()
        buffer[position++] = (value ushr 49 or 0x80L).toByte()
        buffer[position++] = (value ushr 56).toByte()
        return 9
    }

    /**
     * look at buffer, and see if we can read the length of the long off of it (from the reader index).
     *
     * @return 0 if we could not read anything, >0 for the number of bytes for the long on the buffer
     */
    fun canReadLong(buffer: ByteArray): Boolean {
        val position = 0
        return canReadLong(buffer, position)
    }

    /**
     * FROM KRYO
     *
     *
     * look at buffer, and see if we can read the length of the long off of it (from the reader index).
     *
     * @param position where in the buffer to start reading
     * @return 0 if we could not read anything, >0 for the number of bytes for the long on the buffer
     */
    private fun canReadLong(buffer: ByteArray, position: Int): Boolean {
        var position = position
        val limit = buffer.size
        if (limit >= 9) {
            return true
        }
        if (buffer[position++].toInt() and 0x80 == 0) {
            return true
        }
        if (position == limit) {
            return false
        }
        if (buffer[position++].toInt() and 0x80 == 0) {
            return true
        }
        if (position == limit) {
            return false
        }
        if (buffer[position++].toInt() and 0x80 == 0) {
            return true
        }
        if (position == limit) {
            return false
        }
        if (buffer[position++].toInt() and 0x80 == 0) {
            return true
        }
        if (position == limit) {
            return false
        }
        if (buffer[position++].toInt() and 0x80 == 0) {
            return true
        }
        if (position == limit) {
            return false
        }
        if (buffer[position++].toInt() and 0x80 == 0) {
            return true
        }
        if (position == limit) {
            return false
        }
        if (buffer[position++].toInt() and 0x80 == 0) {
            return true
        }
        if (position == limit) {
            return false
        }
        return if (buffer[position++].toInt() and 0x80 == 0) {
            true
        } else position != limit
    }
}
