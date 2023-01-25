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

/*
* Copyright (c) 2008, Nathan Sweet
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
*
*     * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
*     * Neither the name of Esoteric Software nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* Modified by dorkbox, llc
*/
package dorkbox.bytes

import java.io.IOException
import java.nio.BufferUnderflowException
import java.util.*
import kotlin.experimental.and

 /**
 * A self-growing byte array wrapper.
 *
 * Utility methods are provided for efficiently writing primitive types and strings.
 *
 * Encoding of integers: BIG_ENDIAN is used for storing fixed native size integer values LITTLE_ENDIAN is used for a variable
 * length encoding of integer values
 *
 * @author Nathan Sweet <misc></misc>@n4te.com>
 */
@Suppress("unused", "DuplicatedCode", "DuplicatedCode", "MemberVisibilityCanBePrivate")
class ByteArrayBuffer {
    companion object {
        /**
         * Gets the version number.
         */
        const val version = BytesInfo.version

        init {
            // Add this project to the updates system, which verifies this class + UUID + version information
            dorkbox.updates.Updates.add(ByteArrayBuffer::class.java, "f176cecea06e48e1a96d59c08a6e98c3", BytesInfo.version)
        }

        /**
         * Returns the number of bytes that would be written with [.writeInt].
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
         * Returns the number of bytes that would be written with [.writeLong].
         */
        fun longLength(value: Long, optimizePositive: Boolean): Int {
            @Suppress("NAME_SHADOWING")
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
    }


    private var capacity = 0    // exactly how many bytes have been allocated
    private var maxCapacity = 0 // how large we can grow
    private var position = 0    // current pointer to the point where data is read/written
    private lateinit var bytes : ByteArray    // the backing buffer
    private var chars = CharArray(32) // small buffer for reading strings

    /**
     * Creates an uninitialized object. [.setBuffer] must be called before the object is used.
     */
    constructor()

    /**
     * Creates a new object for writing to a byte array.
     *
     * @param bufferSize    The initial size of the buffer.
     *
     * @param maxBufferSize The buffer is doubled as needed until it exceeds maxBufferSize and an exception is thrown. Can be -1
     * for no maximum.
     */
    constructor(bufferSize: Int, maxBufferSize: Int = bufferSize) {
        require(maxBufferSize >= -1) { "maxBufferSize cannot be < -1: $maxBufferSize" }
        capacity = bufferSize
        maxCapacity = if (maxBufferSize == -1) Int.MAX_VALUE else maxBufferSize
        bytes = ByteArray(bufferSize)
    }

    /**
     * Creates a new object for writing to a byte array.
     *
     * @see .setBuffer
     */
    constructor(buffer: ByteArray, maxBufferSize: Int = buffer.size) {
        setBuffer(buffer, maxBufferSize)
    }

    /**
     * Sets the buffer that will be written to. The position and total are reset, discarding any buffered bytes.
     *
     * @param maxBufferSize
     * The buffer is doubled as needed until it exceeds maxBufferSize and an exception is thrown.
     */
    fun setBuffer(buffer: ByteArray, maxBufferSize: Int) {
        require(maxBufferSize >= -1) { "maxBufferSize cannot be < -1: $maxBufferSize" }

        bytes = buffer
        maxCapacity = if (maxBufferSize == -1) Int.MAX_VALUE else maxBufferSize
        capacity = buffer.size
        position = 0
    }


    /**
     * Returns the buffer. The bytes between zero and [.position] are the data that has been written.
     */
    fun getBuffer(): ByteArray {
        return bytes
    }

    /**
     * Sets the buffer that will be written to. [.setBuffer] is called with the specified buffer's
     * length as the maxBufferSize.
     */
    fun setBuffer(buffer: ByteArray) {
        setBuffer(buffer, buffer.size)
    }

    /**
     * Returns a new byte array containing the bytes currently in the buffer between zero and [.position].
     */
    fun toBytes(): ByteArray {
        val newBuffer = ByteArray(position)

        if (position > 0) {
            System.arraycopy(bytes, 0, newBuffer, 0, position)
        }
        return newBuffer
    }

    /**
     * Returns the remaining read/write bytes available before the end of the buffer
     */
    fun remaining(): Int {
        return capacity - position
    }

    /**
     * Returns the size of the backing byte buffer
     */
    fun capacity(): Int {
        return capacity
    }

    /**
     * Returns the current position in the buffer. This is the number of bytes that have not been flushed.
     */
    fun position(): Int {
        return position
    }

    /**
     * Sets the current position in the buffer.
     */
    fun setPosition(position: Int) {
        this.position = position
    }

    /**
     * Sets the position to zero.
     */
    fun clear() {
        position = 0
    }

    /**
     * Sets the position to zero.
     */
    fun rewind() {
        position = 0
    }

    /**
     * Sets the position to zero, and write 0 to all bytes in the buffer
     */
    fun clearSecure() {
        position = 0
        val buffer = bytes
        for (i in 0 until capacity) {
            buffer[i] = 0
        }
    }

    /**
     * Discards the specified number of bytes.
     */
    fun skip(count: Int) {
        @Suppress("NAME_SHADOWING")
        var count = count
        var skipCount = Math.min(capacity - position, count)
        while (true) {
            position += skipCount
            count -= skipCount
            if (count == 0) {
                break
            }
            skipCount = Math.min(count, capacity)
            require(skipCount)
        }
    }

    /**
     * @return true if the buffer has been resized.
     */
    private fun require(required: Int): Boolean {
        if (capacity - position >= required) {
            return false
        }
        if (required > maxCapacity) {
            throw IOException("Buffer overflow. Max capacity: $maxCapacity, required: $required")
        }

        while (capacity - position < required) {
            if (capacity == maxCapacity) {
                throw IOException("Buffer overflow. Available: " + (capacity - position) + ", required: " + required)
            }

            // Grow buffer.
            if (capacity == 0) {
                capacity = 1
            }

            capacity = (capacity * 1.6).toInt().coerceAtMost(maxCapacity)
            if (capacity < 0) {
                capacity = maxCapacity
            }

            val newBuffer = ByteArray(capacity)
            System.arraycopy(bytes, 0, newBuffer, 0, position)
            bytes = newBuffer
        }

        return true
    }


    // byte
    /**
     * Writes a byte.
     */
    fun writeByte(value: Byte) {
        if (position == capacity) {
            require(1)
        }
        bytes[position++] = value
    }

    /**
     * Writes a byte.
     */
    fun writeByte(value: Int) {
        if (position == capacity) {
            require(1)
        }
        bytes[position++] = value.toByte()
    }

    /**
     * Writes the bytes. Note the byte[] length is not written.
     */
    fun writeBytes(bytes: ByteArray) {
        writeBytes(bytes, 0, bytes.size)
    }

    /**
     * Writes the bytes. Note the byte[] length is not written.
     */
    fun writeBytes(bytes: ByteArray, offset: Int, count: Int) {
        @Suppress("NAME_SHADOWING")
        var offset = offset
        @Suppress("NAME_SHADOWING")
        var count = count
        var copyCount = (capacity - position).coerceAtMost(count)

        while (true) {
            System.arraycopy(bytes, offset, this.bytes, position, copyCount)
            position += copyCount
            count -= copyCount
            if (count == 0) {
                return
            }
            offset += copyCount
            copyCount = Math.min(capacity, count)
            require(copyCount)
        }
    }

    /**
     * Reads a single byte.
     */
    fun readByte(): Byte {
        return bytes[position++]
    }

    /**
     * Reads a byte as an int from 0 to 255.
     */
    fun readByteUnsigned(): Int {
        return bytes[position++].toInt() and 0xFF
    }

    /**
     * Reads a single byte, does not advance the position
     */
    fun readByte(position: Int): Byte {
        return bytes[position]
    }

    /**
     * Reads a byte as an int from 0 to 255, does not advance the position
     */
    fun readByteUnsigned(position: Int): Int {
        return bytes[position].toInt() and 0xFF
    }

    /**
     * Reads the specified number of bytes into a new byte[].
     */
    fun readBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        readBytes(bytes, 0, length)
        return bytes
    }

    /**
     * Reads count bytes and writes them to the specified byte[], starting at offset (or 0) in target byte array.
     */
    fun readBytes(bytes: ByteArray, offset: Int = 0, count: Int = bytes.size) {
        System.arraycopy(this.bytes, position, bytes, offset, count)
        position += count
    }


    // int
    /**
     * Writes a 4 byte int. Uses BIG_ENDIAN byte order.
     */
    fun writeInt(value: Int) {
        require(4)
        val buffer = bytes
        buffer[position++] = (value shr 24).toByte()
        buffer[position++] = (value shr 16).toByte()
        buffer[position++] = (value shr 8).toByte()
        buffer[position++] = value.toByte()
    }

    /**
     * Writes a 1-5 byte int. This stream may consider such a variable length encoding request as a hint. It is not
     * guaranteed that a variable length encoding will be really used. The stream may decide to use native-sized integer
     * representation for efficiency reasons.
     *
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     * inefficient (5 bytes).
     */
    fun writeInt(value: Int, optimizePositive: Boolean): Int {
        return writeVarInt(value, optimizePositive)
    }

    /**
     * Writes a 1-5 byte int. It is guaranteed that a varible length encoding will be used.
     *
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     * inefficient (5 bytes).
     */
    fun writeVarInt(value: Int, optimizePositive: Boolean): Int {
        @Suppress("NAME_SHADOWING")
        var value = value
        if (!optimizePositive) value = value shl 1 xor (value shr 31)
        if (value ushr 7 == 0) {
            require(1)
            bytes[position++] = value.toByte()
            return 1
        }
        if (value ushr 14 == 0) {
            require(2)
            val buffer = bytes
            buffer[position++] = (value and 0x7F or 0x80).toByte()
            buffer[position++] = (value ushr 7).toByte()
            return 2
        }
        if (value ushr 21 == 0) {
            require(3)
            val buffer = bytes
            buffer[position++] = (value and 0x7F or 0x80).toByte()
            buffer[position++] = (value ushr 7 or 0x80).toByte()
            buffer[position++] = (value ushr 14).toByte()
            return 3
        }
        if (value ushr 28 == 0) {
            require(4)
            val buffer = bytes
            buffer[position++] = (value and 0x7F or 0x80).toByte()
            buffer[position++] = (value ushr 7 or 0x80).toByte()
            buffer[position++] = (value ushr 14 or 0x80).toByte()
            buffer[position++] = (value ushr 21).toByte()
            return 4
        }
        require(5)
        val buffer = bytes
        buffer[position++] = (value and 0x7F or 0x80).toByte()
        buffer[position++] = (value ushr 7 or 0x80).toByte()
        buffer[position++] = (value ushr 14 or 0x80).toByte()
        buffer[position++] = (value ushr 21 or 0x80).toByte()
        buffer[position++] = (value ushr 28).toByte()
        return 5
    }

    /**
     * Reads a 4 byte int.
     */
    fun readInt(): Int {
        val buffer = bytes
        val position = position
        val value: Int = buffer[position].toInt() and 0xFF shl 24 or (buffer[position + 1].toInt() and 0xFF shl 16
                ) or (buffer[position + 2].toInt() and 0xFF shl 8
                ) or (buffer[position + 3].toInt() and 0xFF)
        this.position = position + 4
        return value
    }

    /**
     * Reads a 4 byte int, does not advance the position
     */
    fun readInt(position: Int): Int {
        val buffer = bytes
        val value: Int = buffer[position].toInt() and 0xFF shl 24 or (buffer[position + 1].toInt() and 0xFF shl 16
                ) or (buffer[position + 2].toInt() and 0xFF shl 8
                ) or (buffer[position + 3].toInt() and 0xFF)
        this.position = position + 4
        return value
    }

    /**
     * Reads a 1-5 byte int. This stream may consider such a variable length encoding request as a hint. It is not
     * guaranteed that a variable length encoding will be really used. The stream may decide to use native-sized integer
     * representation for efficiency reasons.
     */
    fun readInt(optimizePositive: Boolean): Int {
        return readVarInt(optimizePositive)
    }

    /**
     * Reads a 1-5 byte int. This stream may consider such a variable length encoding request as a hint. It is not
     * guaranteed that a variable length encoding will be really used. The stream may decide to use native-sized integer
     * representation for efficiency reasons.
     *
     *
     * does not advance the position
     */
    fun readInt(position: Int, optimizePositive: Boolean): Int {
        val pos = this.position
        this.position = position
        val value = readVarInt(optimizePositive)
        this.position = pos
        return value
    }

    /**
     * Reads a 1-5 byte int. It is guaranteed that a variable length encoding will be used.
     */
    private fun readVarInt(optimizePositive: Boolean): Int {
        val buffer = bytes
       
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
     * Returns true if enough bytes are available to read an int with [.readInt].
     */
    fun canReadInt(): Boolean {
        if (capacity - position >= 5) {
            return true
        }
        if (position + 1 > capacity) {
            return false
        }
        val buffer = bytes
        var p = position
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        return if (buffer[p++].toInt() and 0x80 == 0) {
            true
        } else p != capacity
    }

    /**
     * Returns true if enough bytes are available to read an int with [.readInt].
     */
    fun canReadInt(position: Int): Boolean {
        if (capacity - position >= 5) {
            return true
        }
        if (position + 1 > capacity) {
            return false
        }
        val buffer = bytes
        var p = position
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        return if (buffer[p++].toInt() and 0x80 == 0) {
            true
        } else p != capacity
    }


    // string
    /**
     * Writes the length and string, or null. Short strings are checked and if ASCII they are written more efficiently,
     * else they are written as UTF8. If a string is known to be ASCII, [ByteArrayBuffer.writeAscii] may be used. The
     * string can be read using [ByteArrayBuffer.readString] or [ByteArrayBuffer.readStringBuilder].
     *
     * @param value
     * May be null.
     */
    fun writeString(value: String?) {
        if (value == null) {
            writeByte(0x80) // 0 means null, bit 8 means UTF8.
            return
        }

        val charCount = value.length
        if (charCount == 0) {
            writeByte(1 or 0x80) // 1 means empty string, bit 8 means UTF8.
            return
        }

        // Detect ASCII, we only do this for small strings
        // since 1 char is used for bit-masking if we use for 1 char string, reading the string will not work!
        var permitAscii = charCount in 2..32
        if (permitAscii) {
            for (i in 0 until charCount) {
                if (value[i].code > 127) {
                    permitAscii = false
                    break // not ascii
                }
            }

            if (permitAscii) {
                // this is ascii
                if (capacity - position < charCount) {
                    writeAscii_slow(value, charCount)
                } else {
                    val stringBytes = value.encodeToByteArray(0, charCount)
                    stringBytes.copyInto(bytes, position)
//                    value.toByteArray(0, charCount, bytes, position)
                    position += charCount

//                    var i = 0
//                    val n = value.length
//                    while (i < n) {
//                        bytes[position++] = value[i].code.toByte()
//                        ++i
//                    }
                }

                // mod the last written byte with 0x80 so we can use that when reading ascii bytes to see what the end of the string is
                val value1: Byte = (bytes[position - 1].toInt() or 0x80).toByte()
                bytes[position - 1] = value1
                return
            }
        }

        writeUtf8Length(charCount + 1)
        var charIndex = 0
        if (capacity - position >= charCount) {
            // Try to write 8 bit chars.
            val buffer = bytes
            var position = position
            while (charIndex < charCount) {
                val c = value[charIndex].code
                if (c > 127) {
                    break
                }
                buffer[position++] = c.toByte()
                charIndex++
            }
            this.position = position
        }
        if (charIndex < charCount) {
            writeUtf8_slow(value, charCount, charIndex)
        }
    }

    /**
     * Writes the length and CharSequence as UTF8, or null. The string can be read using [ByteArrayBuffer.readString] or
     * [ByteArrayBuffer.readStringBuilder].
     *
     * @param value
     * May be null.
     */
    fun writeString(value: CharSequence?) {
        if (value == null) {
            writeByte(0x80) // 0 means null, bit 8 means UTF8.
            return
        }


        val charCount = value.length
        if (charCount == 0) {
            writeByte(1 or 0x80) // 1 means empty string, bit 8 means UTF8.
            return
        }


        writeUtf8Length(charCount + 1)
        var charIndex = 0
        if (capacity - position >= charCount) {
            // Try to write 8 bit chars.
            val buffer = bytes
            var position = position
            while (charIndex < charCount) {
                val c = value[charIndex].code
                if (c > 127) {
                    break
                }
                buffer[position++] = c.toByte()
                charIndex++
            }
            this.position = position
        }
        if (charIndex < charCount) {
            writeUtf8_slow(value, charCount, charIndex)
        }
    }

    /**
     * Writes a string that is known to contain only ASCII characters. Non-ASCII strings passed to this method will be
     * corrupted. Each byte is a 7 bit character with the remaining byte denoting if another character is available.
     * This is slightly more efficient than [ByteArrayBuffer.writeString]. The string can be read using
     * [ByteArrayBuffer.readString] or [ByteArrayBuffer.readStringBuilder].
     *
     * @param value
     * May be null.
     */
    fun writeAscii(value: String?) {
        if (value == null) {
            writeByte(0x80) // 0 means null, bit 8 means UTF8.
            return
        }
        val charCount = value.length
        when (charCount) {
            0 -> {
                writeByte(1 or 0x80) // 1 is string length + 1, bit 8 means UTF8.
                return
            }
            1 -> {
                writeByte(2 or 0x80) // 2 is string length + 1, bit 8 means UTF8.
                writeByte(value[0].code)
                return
            }
        }
        if (capacity - position < charCount) {
            writeAscii_slow(value, charCount)
        } else {
            val stringBytes = value.encodeToByteArray(0, charCount)
            stringBytes.copyInto(bytes, position)
//            value.toByteArray(0, charCount, bytes, position)
            position += charCount
        }
        bytes[position - 1] = (bytes[position - 1].toInt() or 0x80).toByte() // Bit 8 means end of ASCII.
    }

    /**
     * Writes the length of a string, which is a variable length encoded int except the first byte uses bit 8 to denote
     * UTF8 and bit 7 to denote if another byte is present.
     */
    private fun writeUtf8Length(value: Int) {
        if (value ushr 6 == 0) {
            require(1)
            bytes[position++] = (value or 0x80).toByte() // Set bit 8.
        } else if (value ushr 13 == 0) {
            require(2)
            val buffer = bytes
            buffer[position++] = (value or 0x40 or 0x80).toByte() // Set bit 7 and 8.
            buffer[position++] = (value ushr 6).toByte()
        } else if (value ushr 20 == 0) {
            require(3)
            val buffer = bytes
            buffer[position++] = (value or 0x40 or 0x80).toByte() // Set bit 7 and 8.
            buffer[position++] = (value ushr 6 or 0x80).toByte() // Set bit 8.
            buffer[position++] = (value ushr 13).toByte()
        } else if (value ushr 27 == 0) {
            require(4)
            val buffer = bytes
            buffer[position++] = (value or 0x40 or 0x80).toByte() // Set bit 7 and 8.
            buffer[position++] = (value ushr 6 or 0x80).toByte() // Set bit 8.
            buffer[position++] = (value ushr 13 or 0x80).toByte() // Set bit 8.
            buffer[position++] = (value ushr 20).toByte()
        } else {
            require(5)
            val buffer = bytes
            buffer[position++] = (value or 0x40 or 0x80).toByte() // Set bit 7 and 8.
            buffer[position++] = (value ushr 6 or 0x80).toByte() // Set bit 8.
            buffer[position++] = (value ushr 13 or 0x80).toByte() // Set bit 8.
            buffer[position++] = (value ushr 20 or 0x80).toByte() // Set bit 8.
            buffer[position++] = (value ushr 27).toByte()
        }
    }

    private fun writeUtf8_slow(value: CharSequence, charCount: Int, charIndex: Int) {
        @Suppress("NAME_SHADOWING")
        var charIndex = charIndex
        while (charIndex < charCount) {
            if (position == capacity) {
                require(capacity.coerceAtMost(charCount - charIndex))
            }
            val c = value[charIndex].code

            if (c <= 0x007F) {
                bytes[position++] = c.toByte()
            } else if (c > 0x07FF) {
                bytes[position++] = (0xE0 or (c shr 12 and 0x0F)).toByte()
                require(2)
                val buffer = bytes
                buffer[position++] = (0x80 or (c shr 6 and 0x3F)).toByte()
                buffer[position++] = (0x80 or (c and 0x3F)).toByte()
            } else {
                bytes[position++] = (0xC0 or (c shr 6 and 0x1F)).toByte()
                require(1)
                bytes[position++] = (0x80 or (c and 0x3F)).toByte()
            }
            charIndex++
        }
    }

    private fun writeAscii_slow(value: String, charCount: Int) {
        var buffer = bytes
        var charIndex = 0
        var charsToWrite = charCount.coerceAtMost(capacity - position)

        while (charIndex < charCount) {
            val stringBytes = value.encodeToByteArray(charIndex, charIndex + charsToWrite)
            stringBytes.copyInto(buffer, position)
//            value.toByteArray(charIndex, charIndex + charsToWrite, buffer, position)

            charIndex += charsToWrite
            position += charsToWrite
            charsToWrite = Math.min(charCount - charIndex, capacity)

            if (require(charsToWrite)) {
                buffer = bytes
            }
        }
    }

    /**
     * Reads the length and string of UTF8 characters, or null. This can read strings written by
     * [ByteArrayBuffer.writeString] , [ByteArrayBuffer.writeString], and
     * [ByteArrayBuffer.writeAscii].
     *
     * @return May be null.
     */
    fun readString(): String? {
        val available = capacity - position
        val b = bytes[position++].toInt()
        if (b and 0x80 == 0) {
            return readAscii() // ASCII.
        }

        // Null, empty, or UTF8.
        var charCount = if (available >= 5) readUtf8Length(b) else readUtf8Length_slow(b)
        when (charCount) {
            0 -> return null
            1 -> return ""
        }

        charCount--
        if (chars.size < charCount) {
            chars = CharArray(charCount)
        }

        if (available < charCount) {
            throw BufferUnderflowException()
        }
        readUtf8(charCount)
        return String(chars, 0, charCount)
    }

    private fun readUtf8Length(b: Int): Int {
        @Suppress("NAME_SHADOWING")
        var b = b
        var result = b and 0x3F // Mask all but first 6 bits.
        if (b and 0x40 != 0) { // Bit 7 means another byte, bit 8 means UTF8.
            val buffer = bytes
            b = buffer[position++].toInt()
            result = result or (b and 0x7F shl 6)
            if (b and 0x80 != 0) {
                b = buffer[position++].toInt()
                result = result or (b and 0x7F shl 13)
                if (b and 0x80 != 0) {
                    b = buffer[position++].toInt()
                    result = result or (b and 0x7F shl 20)
                    if (b and 0x80 != 0) {
                        b = buffer[position++].toInt()
                        result = result or (b and 0x7F shl 27)
                    }
                }
            }
        }
        return result
    }

    private fun readUtf8Length_slow(b: Int): Int {
        @Suppress("NAME_SHADOWING")
        var b = b
        var result = b and 0x3F // Mask all but first 6 bits.
        if (b and 0x40 != 0) { // Bit 7 means another byte, bit 8 means UTF8.
            val buffer = bytes
            b = buffer[position++].toInt()
            result = result or (b and 0x7F shl 6)
            if (b and 0x80 != 0) {
                b = buffer[position++].toInt()
                result = result or (b and 0x7F shl 13)
                if (b and 0x80 != 0) {
                    b = buffer[position++].toInt()
                    result = result or (b and 0x7F shl 20)
                    if (b and 0x80 != 0) {
                        b = buffer[position++].toInt()
                        result = result or (b and 0x7F shl 27)
                    }
                }
            }
        }
        return result
    }

    private fun readUtf8(charCount: Int) {
        val buffer = bytes
        var position = position
        val chars = chars

        // Try to read 7 bit ASCII chars.
        var charIndex = 0
        val spaceAvailable = capacity - this.position
        val count = Math.min(spaceAvailable, charCount)
        var b: Int
        while (charIndex < count) {
            b = buffer[position++].toInt()
            if (b < 0) {
                position--
                break
            }
            chars[charIndex++] = b.toChar()
        }
        this.position = position

        // If buffer didn't hold all chars or any were not ASCII, use slow path for remainder.
        if (charIndex < charCount) {
            readUtf8_slow(charCount, charIndex)
        }
    }

    private fun readUtf8_slow(charCount: Int, charIndex: Int) {
        @Suppress("NAME_SHADOWING")
        var charIndex = charIndex
        val chars = chars
        val buffer = bytes

        while (charIndex < charCount) {
            val b: Int = buffer[position++].toInt() and 0xFF
            when (b shr 4) {
                0, 1, 2, 3, 4, 5, 6, 7 -> chars[charIndex] = b.toChar()
                12, 13 -> chars[charIndex] = (b and 0x1F shl 6 or (buffer[position++].toInt() and 0x3F)).toChar()
                14 -> chars[charIndex] =
                    (b and 0x0F shl 12 or (buffer[position++].toInt() and 0x3F shl 6) or (buffer[position++].toInt() and 0x3F)).toChar()
            }
            charIndex++
        }
    }

    private fun readAscii(): String {
        val buffer = bytes
        var end = position
        val start = end - 1
        val limit = capacity
        var b: Int

        do {
            if (end == limit) {
                return readAscii_slow()
            }
            b = buffer[end++].toInt()
        } while (b and 0x80 == 0)

        buffer[end - 1] = buffer[end - 1] and 0x7F // Mask end of ascii bit.
        val value = String(buffer, start, end - start)
        buffer[end - 1] = (buffer[end - 1].toInt() or 0x80).toByte()
        position = end
        return value
    }

    private fun readAscii_slow(): String {
        position-- // Re-read the first byte.

        // Copy chars currently in buffer.
        var charCount = capacity - position
        if (charCount > chars.size) {
            chars = CharArray(charCount * 2)
        }
        var chars = chars
        val buffer = bytes
        var i = position
        var ii = 0
        val n = capacity

        while (i < n) {
            chars[ii] = buffer[i].toInt().toChar()
            i++
            ii++
        }
        position = capacity

        // Copy additional chars one by one.
        while (true) {
            val b = buffer[position++].toInt()
            if (charCount == chars.size) {
                val newChars = CharArray(charCount * 2)
                System.arraycopy(chars, 0, newChars, 0, charCount)
                chars = newChars
                this.chars = newChars
            }
            if (b and 0x80 == 0x80) {
                chars[charCount++] = (b and 0x7F).toChar()
                break
            }
            chars[charCount++] = b.toChar()
        }
        return String(chars, 0, charCount)
    }

    /**
     * Reads the length and string of UTF8 characters, or null. This can read strings written by
     * [ByteArrayBuffer.writeString] , [ByteArrayBuffer.writeString], and
     * [ByteArrayBuffer.writeAscii].
     *
     * @return May be null.
     */
    fun readStringBuilder(): StringBuilder? {
        val available = capacity - position
        val b = bytes[position++].toInt()
        if (b and 0x80 == 0) {
            return StringBuilder(readAscii()) // ASCII.
        }

        // Null, empty, or UTF8.
        var charCount = if (available >= 5) readUtf8Length(b) else readUtf8Length_slow(b)
        when (charCount) {
            0 -> return null
            1 -> return StringBuilder()
        }
        charCount--
        if (chars.size < charCount) {
            chars = CharArray(charCount)
        }
        readUtf8(charCount)
        val builder = StringBuilder(charCount)
        builder.append(chars, 0, charCount)
        return builder
    }
    // float
    /**
     * Writes a 4 byte float.
     */
    fun writeFloat(value: Float) {
        writeInt(java.lang.Float.floatToIntBits(value))
    }

    /**
     * Writes a 1-5 byte float with reduced precision.
     *
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     * inefficient (5 bytes).
     */
    fun writeFloat(value: Float, precision: Float, optimizePositive: Boolean): Int {
        return writeInt((value * precision).toInt(), optimizePositive)
    }

    /**
     * Reads a 4 byte float.
     */
    fun readFloat(): Float {
        return java.lang.Float.intBitsToFloat(readInt())
    }

    /**
     * Reads a 1-5 byte float with reduced precision.
     */
    fun readFloat(precision: Float, optimizePositive: Boolean): Float {
        return readInt(optimizePositive) / precision
    }

    /**
     * Reads a 4 byte float, does not advance the position
     */
    fun readFloat(position: Int): Float {
        return java.lang.Float.intBitsToFloat(readInt(position))
    }

    /**
     * Reads a 1-5 byte float with reduced precision, does not advance the position
     */
    fun readFloat(position: Int, precision: Float, optimizePositive: Boolean): Float {
        return readInt(position, optimizePositive) / precision
    }
    // short
    /**
     * Writes a 2 byte short. Uses BIG_ENDIAN byte order.
     */
    fun writeShort(value: Int) {
        require(2)
        val buffer = bytes
        buffer[position++] = (value ushr 8).toByte()
        buffer[position++] = value.toByte()
    }

    /**
     * Reads a 2 byte short.
     */
    fun readShort(): Short {
        val buffer = bytes
        return ((buffer[position++].toInt() and 0xFF) shl 8 or (buffer[position++].toInt() and 0xFF)).toShort()
    }

    /**
     * Reads a 2 byte short as an int from 0 to 65535.
     */
    fun readShortUnsigned(): Int {
        val buffer = bytes
        return buffer[position++].toInt() and 0xFF shl 8 or buffer[position++].toInt() and 0xFF
    }

    /**
     * Reads a 2 byte short, does not advance the position
     */
    fun readShort(position: Int): Short {
        @Suppress("NAME_SHADOWING")
        var position = position
        val buffer = bytes
        return (buffer[position++].toInt() and 0xFF shl 8 or buffer[position].toInt() and 0xFF).toShort()
    }

    /**
     * Reads a 2 byte short as an int from 0 to 65535, does not advance the position
     */
    fun readShortUnsigned(position: Int): Int {
        @Suppress("NAME_SHADOWING")
        var position = position
        val buffer = bytes
        return buffer[position++].toInt() and 0xFF shl 8 or buffer[position].toInt() and 0xFF
    }
    // long
    /**
     * Writes an 8 byte long. Uses BIG_ENDIAN byte order.
     */
    fun writeLong(value: Long) {
        require(8)
        val buffer = bytes
        buffer[position++] = (value ushr 56).toByte()
        buffer[position++] = (value ushr 48).toByte()
        buffer[position++] = (value ushr 40).toByte()
        buffer[position++] = (value ushr 32).toByte()
        buffer[position++] = (value ushr 24).toByte()
        buffer[position++] = (value ushr 16).toByte()
        buffer[position++] = (value ushr 8).toByte()
        buffer[position++] = value.toByte()
    }

    /**
     * Writes a 1-9 byte long. This stream may consider such a variable length encoding request as a hint. It is not
     * guaranteed that a variable length encoding will be really used. The stream may decide to use native-sized integer
     * representation for efficiency reasons.
     *
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     * inefficient (9 bytes).
     */
    fun writeLong(value: Long, optimizePositive: Boolean): Int {
        return writeVarLong(value, optimizePositive)
    }

    /**
     * Writes a 1-9 byte long. It is guaranteed that a varible length encoding will be used.
     *
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     * inefficient (9 bytes).
     */
    fun writeVarLong(value: Long, optimizePositive: Boolean): Int {
        @Suppress("NAME_SHADOWING")
        var value = value
        if (!optimizePositive) value = value shl 1 xor (value shr 63)
        if (value ushr 7 == 0L) {
            require(1)
            bytes[position++] = value.toByte()
            return 1
        }
        if (value ushr 14 == 0L) {
            require(2)
            val buffer = bytes
            buffer[position++] = (value and 0x7F or 0x80).toByte()
            buffer[position++] = (value ushr 7).toByte()
            return 2
        }
        if (value ushr 21 == 0L) {
            require(3)
            val buffer = bytes
            buffer[position++] = (value and 0x7F or 0x80).toByte()
            buffer[position++] = (value ushr 7 or 0x80).toByte()
            buffer[position++] = (value ushr 14).toByte()
            return 3
        }
        if (value ushr 28 == 0L) {
            require(4)
            val buffer = bytes
            buffer[position++] = (value and 0x7F or 0x80).toByte()
            buffer[position++] = (value ushr 7 or 0x80).toByte()
            buffer[position++] = (value ushr 14 or 0x80).toByte()
            buffer[position++] = (value ushr 21).toByte()
            return 4
        }
        if (value ushr 35 == 0L) {
            require(5)
            val buffer = bytes
            buffer[position++] = (value and 0x7F or 0x80).toByte()
            buffer[position++] = (value ushr 7 or 0x80).toByte()
            buffer[position++] = (value ushr 14 or 0x80).toByte()
            buffer[position++] = (value ushr 21 or 0x80).toByte()
            buffer[position++] = (value ushr 28).toByte()
            return 5
        }
        if (value ushr 42 == 0L) {
            require(6)
            val buffer = bytes
            buffer[position++] = (value and 0x7F or 0x80).toByte()
            buffer[position++] = (value ushr 7 or 0x80).toByte()
            buffer[position++] = (value ushr 14 or 0x80).toByte()
            buffer[position++] = (value ushr 21 or 0x80).toByte()
            buffer[position++] = (value ushr 28 or 0x80).toByte()
            buffer[position++] = (value ushr 35).toByte()
            return 6
        }
        if (value ushr 49 == 0L) {
            require(7)
            val buffer = bytes
            buffer[position++] = (value and 0x7F or 0x80).toByte()
            buffer[position++] = (value ushr 7 or 0x80).toByte()
            buffer[position++] = (value ushr 14 or 0x80).toByte()
            buffer[position++] = (value ushr 21 or 0x80).toByte()
            buffer[position++] = (value ushr 28 or 0x80).toByte()
            buffer[position++] = (value ushr 35 or 0x80).toByte()
            buffer[position++] = (value ushr 42).toByte()
            return 7
        }
        if (value ushr 56 == 0L) {
            require(8)
            val buffer = bytes
            buffer[position++] = (value and 0x7F or 0x80).toByte()
            buffer[position++] = (value ushr 7 or 0x80).toByte()
            buffer[position++] = (value ushr 14 or 0x80).toByte()
            buffer[position++] = (value ushr 21 or 0x80).toByte()
            buffer[position++] = (value ushr 28 or 0x80).toByte()
            buffer[position++] = (value ushr 35 or 0x80).toByte()
            buffer[position++] = (value ushr 42 or 0x80).toByte()
            buffer[position++] = (value ushr 49).toByte()
            return 8
        }
        require(9)
        val buffer = bytes
        buffer[position++] = (value and 0x7F or 0x80).toByte()
        buffer[position++] = (value ushr 7 or 0x80).toByte()
        buffer[position++] = (value ushr 14 or 0x80).toByte()
        buffer[position++] = (value ushr 21 or 0x80).toByte()
        buffer[position++] = (value ushr 28 or 0x80).toByte()
        buffer[position++] = (value ushr 35 or 0x80).toByte()
        buffer[position++] = (value ushr 42 or 0x80).toByte()
        buffer[position++] = (value ushr 49 or 0x80).toByte()
        buffer[position++] = (value ushr 56).toByte()
        return 9
    }

    /**
     * Returns true if enough bytes are available to read a long with [.readLong].
     */
    fun canReadLong(): Boolean {
        if (capacity - position >= 9) {
            return true
        }
        if (position + 1 > capacity) {
            return false
        }
        val buffer = bytes
        var p = position
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        return if (buffer[p++].toInt() and 0x80 == 0) {
            true
        } else p != capacity
    }

    /**
     * Returns true if enough bytes are available to read a long with [.readLong].
     */
    fun canReadLong(position: Int): Boolean {
        if (capacity - position >= 9) {
            return true
        }
        if (position + 1 > capacity) {
            return false
        }
        val buffer = bytes
        var p = position
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        if (buffer[p++].toInt() and 0x80 == 0) {
            return true
        }
        if (p == capacity) {
            return false
        }
        return if (buffer[p++].toInt() and 0x80 == 0) {
            true
        } else p != capacity
    }

    /**
     * Reads an 8 byte long.
     */
    fun readLong(): Long {
        val buffer = bytes
        return buffer[position++].toLong() shl 56 or ((buffer[position++].toInt() and 0xFF).toLong() shl 48
                ) or ((buffer[position++].toInt() and 0xFF).toLong() shl 40
                ) or ((buffer[position++].toInt() and 0xFF).toLong() shl 32
                ) or ((buffer[position++].toInt() and 0xFF).toLong() shl 24
                ) or ((buffer[position++].toInt() and 0xFF).toLong() shl 16
                ) or ((buffer[position++].toInt() and 0xFF).toLong() shl 8
                ) or (buffer[position++].toInt() and 0xFF).toLong()
    }

    /**
     * Reads a 1-9 byte long. This stream may consider such a variable length encoding request as a hint. It is not
     * guaranteed that a variable length encoding will be really used. The stream may decide to use native-sized integer
     * representation for efficiency reasons.
     */
    fun readLong(optimizePositive: Boolean): Long {
        return readVarLong(optimizePositive)
    }

    /**
     * Reads an 8 byte long, does not advance the position
     */
    fun readLong(position: Int): Long {
        @Suppress("NAME_SHADOWING")
        var position = position
        val buffer = bytes
        return buffer[position++].toLong() shl 56 or ((buffer[position++].toInt() and 0xFF).toLong() shl 48
                ) or ((buffer[position++].toInt() and 0xFF).toLong() shl 40
                ) or ((buffer[position++].toInt() and 0xFF).toLong() shl 32
                ) or ((buffer[position++].toInt() and 0xFF).toLong() shl 24
                ) or ((buffer[position++].toInt() and 0xFF).toLong() shl 16
                ) or ((buffer[position++].toInt() and 0xFF).toLong() shl 8
                ) or (buffer[position].toInt() and 0xFF).toLong()
    }

    /**
     * Reads a 1-9 byte long. This stream may consider such a variable length encoding request as a hint. It is not
     * guaranteed that a variable length encoding will be really used. The stream may decide to use native-sized integer
     * representation for efficiency reasons.
     *
     *
     * does not advance the position
     */
    fun readLong(position: Int, optimizePositive: Boolean): Long {
        val pos = this.position
        this.position = position
        val value = readVarLong(optimizePositive)
        this.position = pos
        return value
    }

    /**
     * Reads a 1-9 byte long. It is guaranteed that a varible length encoding will be used.
     */
    private fun readVarLong(optimizePositive: Boolean): Long {
        if (capacity - position < 9) {
            return readLong_slow(optimizePositive)
        }
        var b = bytes[position++].toInt()
        var result = (b and 0x7F).toLong()
        if (b and 0x80 != 0) {
            val buffer = bytes
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
            result = result ushr 1 xor -(result and 1)
        }
        return result
    }

    private fun readLong_slow(optimizePositive: Boolean): Long {
        // The buffer is guaranteed to have at least 1 byte.
        val buffer = bytes
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
            result = result ushr 1 xor -(result and 1)
        }
        return result
    }
    // boolean
    /**
     * Writes a 1 byte boolean.
     */
    fun writeBoolean(value: Boolean) {
        require(1)
        bytes[position++] = (if (value) 1 else 0).toByte()
    }

    /**
     * Reads a 1 byte boolean.
     */
    fun readBoolean(): Boolean {
        return bytes[position++].toInt() == 1
    }

    /**
     * Reads a 1 byte boolean, does not advance the position
     */
    fun readBoolean(position: Int): Boolean {
        return bytes[position] .toInt()== 1
    }
    // char
    /**
     * Writes a 2 byte char. Uses BIG_ENDIAN byte order.
     */
    fun writeChar(value: Char) {
        require(2)
        val buffer = bytes
        buffer[position++] = (value.code ushr 8).toByte()
        buffer[position++] = value.code.toByte()
    }

    /**
     * Reads a 2 byte char.
     */
    fun readChar(): Char {
        val buffer = bytes
        return ((buffer[position++].toInt() and 0xFF) shl 8 or (buffer[position++].toInt() and 0xFF)).toChar()
    }

    /**
     * Reads a 2 byte char, does not advance the position
     */
    fun readChar(position: Int): Char {
        @Suppress("NAME_SHADOWING")
        var position = position
        val buffer = bytes
        return (buffer[position++].toInt() and 0xFF shl 8 or buffer[position].toInt() and 0xFF).toChar()
    }

    // double
    /**
     * Writes an 8 byte double.
     */
    fun writeDouble(value: Double) {
        writeLong(java.lang.Double.doubleToLongBits(value))
    }

    /**
     * Writes a 1-9 byte double with reduced precision
     *
     * @param optimizePositive
     * If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     * inefficient (9 bytes).
     */
    fun writeDouble(value: Double, precision: Double, optimizePositive: Boolean): Int {
        return writeLong((value * precision).toLong(), optimizePositive)
    }

    /**
     * Reads an 8 bytes double.
     */
    fun readDouble(): Double {
        return java.lang.Double.longBitsToDouble(readLong())
    }

    /**
     * Reads a 1-9 byte double with reduced precision.
     */
    fun readDouble(precision: Double, optimizePositive: Boolean): Double {
        return readLong(optimizePositive) / precision
    }

    /**
     * Reads an 8 bytes double, does not advance the position
     */
    fun readDouble(position: Int): Double {
        return java.lang.Double.longBitsToDouble(readLong(position))
    }

    /**
     * Reads a 1-9 byte double with reduced precision, does not advance the position
     */
    fun readDouble(position: Int, precision: Double, optimizePositive: Boolean): Double {
        return readLong(position, optimizePositive) / precision
    }

    // Methods implementing bulk operations on arrays of primitive types
    /**
     * Bulk output of an int array.
     */
    fun writeInts(`object`: IntArray, optimizePositive: Boolean) {
        var i = 0
        val n = `object`.size
        while (i < n) {
            writeInt(`object`[i], optimizePositive)
            i++
        }
    }

    /**
     * Bulk input of an int array.
     */
    fun readInts(length: Int, optimizePositive: Boolean): IntArray {
        val array = IntArray(length)
        for (i in 0 until length) {
            array[i] = readInt(optimizePositive)
        }
        return array
    }

    /**
     * Bulk output of an long array.
     */
    fun writeLongs(`object`: LongArray, optimizePositive: Boolean) {
        var i = 0
        val n = `object`.size
        while (i < n) {
            writeLong(`object`[i], optimizePositive)
            i++
        }
    }

    /**
     * Bulk input of a long array.
     */
    fun readLongs(length: Int, optimizePositive: Boolean): LongArray {
        val array = LongArray(length)
        for (i in 0 until length) {
            array[i] = readLong(optimizePositive)
        }
        return array
    }

    /**
     * Bulk output of an int array.
     */
    fun writeInts(`object`: IntArray) {
        var i = 0
        val n = `object`.size
        while (i < n) {
            writeInt(`object`[i])
            i++
        }
    }

    /**
     * Bulk input of an int array.
     */
    fun readInts(length: Int): IntArray {
        val array = IntArray(length)
        for (i in 0 until length) {
            array[i] = readInt()
        }
        return array
    }

    /**
     * Bulk output of an long array.
     */
    fun writeLongs(`object`: LongArray) {
        var i = 0
        val n = `object`.size
        while (i < n) {
            writeLong(`object`[i])
            i++
        }
    }

    /**
     * Bulk input of a long array.
     */
    fun readLongs(length: Int): LongArray {
        val array = LongArray(length)
        for (i in 0 until length) {
            array[i] = readLong()
        }
        return array
    }

    /**
     * Bulk output of a float array.
     */
    fun writeFloats(`object`: FloatArray) {
        var i = 0
        val n = `object`.size
        while (i < n) {
            writeFloat(`object`[i])
            i++
        }
    }

    /**
     * Bulk input of a float array.
     */
    fun readFloats(length: Int): FloatArray {
        val array = FloatArray(length)
        for (i in 0 until length) {
            array[i] = readFloat()
        }
        return array
    }

    /**
     * Bulk output of a short array.
     */
    fun writeShorts(`object`: ShortArray) {
        var i = 0
        val n = `object`.size
        while (i < n) {
            writeShort(`object`[i].toInt())
            i++
        }
    }

    /**
     * Bulk input of a short array.
     */
    fun readShorts(length: Int): ShortArray {
        val array = ShortArray(length)
        for (i in 0 until length) {
            array[i] = readShort()
        }
        return array
    }

    /**
     * Bulk output of a char array.
     */
    fun writeChars(`object`: CharArray) {
        var i = 0
        val n = `object`.size
        while (i < n) {
            writeChar(`object`[i])
            i++
        }
    }

    /**
     * Bulk input of a char array.
     */
    fun readChars(length: Int): CharArray {
        val array = CharArray(length)
        for (i in 0 until length) {
            array[i] = readChar()
        }
        return array
    }

    /**
     * Bulk output of a double array.
     */
    fun writeDoubles(`object`: DoubleArray) {
        var i = 0
        val n = `object`.size
        while (i < n) {
            writeDouble(`object`[i])
            i++
        }
    }

    /**
     * Bulk input of a double array
     */
    fun readDoubles(length: Int): DoubleArray {
        val array = DoubleArray(length)
        for (i in 0 until length) {
            array[i] = readDouble()
        }
        return array
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is ByteArrayBuffer) {
            false
        } else Arrays.equals(bytes, other.bytes)

        // CANNOT be null, so we don't have to null check!
    }

    override fun hashCode(): Int {
        // might be null for a thread because it's stale. who cares, get the value again
        return Arrays.hashCode(bytes)
    }

    override fun toString(): String {
        return "ByteBuffer2 " + Arrays.toString(bytes)
    }
}
