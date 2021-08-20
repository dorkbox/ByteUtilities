/*
 * Copyright 2010 dorkbox, llc
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
 *
 * Copyright (c) 2008, Nathan Sweet
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of Esoteric Software nor the names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dorkbox.bytes

import com.esotericsoftware.kryo.KryoException
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.util.Util
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.io.IOException
import java.io.OutputStream

/**
 * An [OutputStream] which writes data to a [ByteBuf].
 *
 *
 * A write operation against this stream will occur at the `writerIndex`
 * of its underlying buffer and the `writerIndex` will increase during
 * the write operation.
 *
 *
 * This stream implements [DataOutput] for your convenience.
 * The endianness of the stream is not always big endian but depends on
 * the endianness of the underlying buffer.
 *
 *
 *
 * Utility methods are provided for efficiently reading primitive types and strings.
 *
 * Modified from KRYO to use ByteBuf.
 */
class ByteBufOutput : Output {
    /** Returns the buffer. The bytes between zero and [.position] are the data that has been written.  */
    // NOTE: capacity IS NOT USED!
    var byteBuf: ByteBuf? = null
        private set
    private var initialReaderIndex = 0
    private var initialWriterIndex = 0

    /** Creates an uninitialized Output, [.setBuffer] must be called before the Output is used.  */
    constructor() {}
    /** Creates a new Output for writing to a direct ByteBuffer.
     * @param bufferSize The initial size of the buffer.
     * @param maxBufferSize If [.flush] does not empty the buffer, the buffer is doubled as needed until it exceeds
     * maxBufferSize and an exception is thrown. Can be -1 for no maximum.
     */
    /** Creates a new Output for writing to a direct [ByteBuf].
     * @param bufferSize The size of the buffer. An exception is thrown if more bytes than this are written and [.flush]
     * does not empty the buffer.
     */
    @JvmOverloads
    constructor(bufferSize: Int, maxBufferSize: Int = bufferSize) {
        require(maxBufferSize >= -1) { "maxBufferSize cannot be < -1: $maxBufferSize" }
        maxCapacity = if (maxBufferSize == -1) Util.maxArraySize else maxBufferSize
        byteBuf = Unpooled.buffer(bufferSize)
    }

    /** Creates a new Output for writing to a ByteBuffer.  */
    constructor(buffer: ByteBuf) {
        setBuffer(buffer)
    }

    /** Creates a new Output for writing to a ByteBuffer.
     * @param maxBufferSize If [.flush] does not empty the buffer, the buffer is doubled as needed until it exceeds
     * maxBufferSize and an exception is thrown. Can be -1 for no maximum.
     */
    constructor(buffer: ByteBuf?, maxBufferSize: Int) {
        setBuffer(buffer, maxBufferSize)
    }

    /** @see Output.Output
     */
    constructor(outputStream: OutputStream?) : this(4096, 4096) {
        requireNotNull(outputStream) { "outputStream cannot be null." }
        this.outputStream = outputStream
    }

    /** @see Output.Output
     */
    constructor(outputStream: OutputStream?, bufferSize: Int) : this(bufferSize, bufferSize) {
        requireNotNull(outputStream) { "outputStream cannot be null." }
        this.outputStream = outputStream
    }

    override fun getOutputStream(): OutputStream {
        return outputStream
    }

    /** Throws [UnsupportedOperationException] because this output uses a ByteBuffer, not a byte[].
     * @see .getByteBuf
     */
    @Deprecated(" ")
    override fun getBuffer(): ByteArray {
        throw UnsupportedOperationException("This buffer does not used a byte[], see #getByteBuffer().")
    }

    /** Throws [UnsupportedOperationException] because this output uses a ByteBuffer, not a byte[].
     * @see .getByteBuf
     */
    @Deprecated(" ")
    override fun setBuffer(buffer: ByteArray) {
        setBuffer(Unpooled.wrappedBuffer(buffer))
    }

    /** Allocates a new direct ByteBuffer with the specified bytes and sets it as the new buffer.
     * @see .setBuffer
     */
    @Deprecated("")
    override fun setBuffer(buffer: ByteArray, maxBufferSize: Int) {
        setBuffer(Unpooled.wrappedBuffer(buffer))
    }

    /** Allocates a new direct ByteBuffer with the specified bytes and sets it as the new buffer.
     * @see .setBuffer
     */
    fun setBuffer(bytes: ByteArray?, offset: Int, count: Int) {
        setBuffer(Unpooled.wrappedBuffer(bytes, offset, count))
    }

    /** Sets a new buffer to write to. The max size is the buffer's length.
     * @see .setBuffer
     */
    fun setBuffer(buffer: ByteBuf) {
        setBuffer(buffer, buffer.capacity())
    }

    /** Sets a new buffer to write to. The bytes are not copied, the old buffer is discarded and the new buffer used in its place.
     * The position and capacity are set to match the specified buffer. The total is reset. The
     * [OutputStream][.setOutputStream] is set to null.
     * @param maxBufferSize If [.flush] does not empty the buffer, the buffer is doubled as needed until it exceeds
     * maxBufferSize and an exception is thrown. Can be -1 for no maximum.
     */
    fun setBuffer(buffer: ByteBuf?, maxBufferSize: Int) {
        requireNotNull(buffer) { "buffer cannot be null." }
        require(maxBufferSize >= -1) { "maxBufferSize cannot be < -1: $maxBufferSize" }
        initialReaderIndex = buffer.readerIndex()
        initialWriterIndex = buffer.writerIndex()
        byteBuf = buffer
        maxCapacity = if (maxBufferSize == -1) Util.maxArraySize else maxBufferSize
        position = initialWriterIndex
        total = 0
        outputStream = null
    }

    override fun toBytes(): ByteArray {
        val newBuffer = ByteArray(position)
        byteBuf!!.readerIndex(initialReaderIndex)
        byteBuf!!.getBytes(initialReaderIndex, newBuffer, 0, position)
        return newBuffer
    }

    override fun setPosition(position: Int) {
        this.position = position
        byteBuf!!.writerIndex(position)
    }

    override fun reset() {
        super.reset()
        byteBuf!!.setIndex(initialReaderIndex, initialWriterIndex)
    }

    /**
     * Ensures the buffer is large enough to read the specified number of bytes.
     * @return true if the buffer has been resized.
     */
    @Throws(KryoException::class)
    override fun require(required: Int): Boolean {
        if (byteBuf!!.isWritable(required)) {
            return false
        }
        var origCode = byteBuf!!.ensureWritable(required, true)
        if (origCode == 0) {
            // 0 if the buffer has enough writable bytes, and its capacity is unchanged.
            return false
        } else if (origCode == 2) {
            // 2 if the buffer has enough writable bytes, and its capacity has been increased.
            return true
        } else if (origCode == 3) {
            // 3 if the buffer does not have enough bytes, but its capacity has been increased to its maximum.
            return true
        } else {
            // flush and try again.
            flush()
        }

        // only got here because we were unable to resize the buffer! So we flushed it first to try again!
        origCode = byteBuf!!.ensureWritable(required, true)
        return if (origCode == 0) {
            // 0 if the buffer has enough writable bytes, and its capacity is unchanged.
            false
        } else if (origCode == 1) {
            // 1 if the buffer does not have enough bytes, and its capacity is unchanged.
            throw KryoException("Buffer overflow. Max capacity: $maxCapacity, required: $required")
        } else if (origCode == 2) {
            // 2 if the buffer has enough writable bytes, and its capacity has been increased.
            true
        } else if (origCode == 3) {
            // 3 if the buffer does not have enough bytes, but its capacity has been increased to its maximum.
            true
        } else {
            throw KryoException("Unknown buffer resize code: $origCode")
        }
    }

    // OutputStream:
    @Throws(KryoException::class)
    override fun flush() {
        if (outputStream == null) return
        try {
            val tmp = ByteArray(position)
            byteBuf!!.getBytes(initialReaderIndex, tmp)
            byteBuf!!.readerIndex(initialReaderIndex)
            outputStream.write(tmp, 0, position)
        } catch (ex: IOException) {
            throw KryoException(ex)
        }
        total += position.toLong()
        position = 0
    }

    @Throws(KryoException::class)
    override fun close() {
        flush()
        if (outputStream != null) {
            try {
                outputStream.close()
            } catch (ignored: IOException) {
            }
        }
    }

    @Throws(KryoException::class)
    override fun write(value: Int) {
        require(1)
        byteBuf!!.writeByte(value.toByte().toInt())
        position++
    }

    @Throws(KryoException::class)
    override fun write(bytes: ByteArray) {
        requireNotNull(bytes) { "bytes cannot be null." }
        writeBytes(bytes, 0, bytes.size)
    }

    @Throws(KryoException::class)
    override fun write(bytes: ByteArray, offset: Int, length: Int) {
        writeBytes(bytes, offset, length)
    }

    // byte:
    @Throws(KryoException::class)
    override fun writeByte(value: Byte) {
        require(1)
        byteBuf!!.writeByte(value.toInt())
        position++
    }

    @Throws(KryoException::class)
    override fun writeByte(value: Int) {
        require(1)
        byteBuf!!.writeByte(value.toByte().toInt())
        position++
    }

    @Throws(KryoException::class)
    override fun writeBytes(bytes: ByteArray) {
        requireNotNull(bytes) { "bytes cannot be null." }
        writeBytes(bytes, 0, bytes.size)
    }

    @Throws(KryoException::class)
    override fun writeBytes(bytes: ByteArray, offset: Int, count: Int) {
        requireNotNull(bytes) { "bytes cannot be null." }
        require(count)
        byteBuf!!.writeBytes(bytes, offset, count)
        position += count
    }

    // int:
    @Throws(KryoException::class)
    override fun writeInt(value: Int) {
        require(4)
        position += 4
        byteBuf!!.writeInt(value)
    }

    @Throws(KryoException::class)
    override fun writeVarInt(value: Int, optimizePositive: Boolean): Int {
        var value = value
        if (!optimizePositive) value = value shl 1 xor (value shr 31)
        if (value ushr 7 == 0) {
            require(1)
            position++
            byteBuf!!.writeByte(value.toByte().toInt())
            return 1
        }
        if (value ushr 14 == 0) {
            require(2)
            position += 2
            byteBuf!!.writeByte((value and 0x7F or 0x80).toByte().toInt())
            byteBuf!!.writeByte((value ushr 7).toByte().toInt())
            return 2
        }
        if (value ushr 21 == 0) {
            require(3)
            position += 3
            val byteBuf = byteBuf
            byteBuf!!.writeByte((value and 0x7F or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 7 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 14).toByte().toInt())
            return 3
        }
        if (value ushr 28 == 0) {
            require(4)
            position += 4
            val byteBuf = byteBuf
            byteBuf!!.writeByte((value and 0x7F or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 7 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 14 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 21).toByte().toInt())
            return 4
        }
        require(5)
        position += 5
        val byteBuf = byteBuf
        byteBuf!!.writeByte((value and 0x7F or 0x80).toByte().toInt())
        byteBuf.writeByte((value ushr 7 or 0x80).toByte().toInt())
        byteBuf.writeByte((value ushr 14 or 0x80).toByte().toInt())
        byteBuf.writeByte((value ushr 21 or 0x80).toByte().toInt())
        byteBuf.writeByte((value ushr 28).toByte().toInt())
        return 5
    }

    @Throws(KryoException::class)
    override fun writeVarIntFlag(flag: Boolean, value: Int, optimizePositive: Boolean): Int {
        var value = value
        if (!optimizePositive) value = value shl 1 xor (value shr 31)
        val first = value and 0x3F or if (flag) 0x80 else 0 // Mask first 6 bits, bit 8 is the flag.
        if (value ushr 6 == 0) {
            require(1)
            byteBuf!!.writeByte(first.toByte().toInt())
            position++
            return 1
        }
        if (value ushr 13 == 0) {
            require(2)
            position += 2
            byteBuf!!.writeByte((first or 0x40).toByte().toInt()) // Set bit 7.
            byteBuf!!.writeByte((value ushr 6).toByte().toInt())
            return 2
        }
        if (value ushr 20 == 0) {
            require(3)
            position += 3
            val byteBuf = byteBuf
            byteBuf!!.writeByte((first or 0x40).toByte().toInt()) // Set bit 7.
            byteBuf.writeByte((value ushr 6 or 0x80).toByte().toInt()) // Set bit 8.
            byteBuf.writeByte((value ushr 13).toByte().toInt())
            return 3
        }
        if (value ushr 27 == 0) {
            require(4)
            position += 4
            val byteBuf = byteBuf
            byteBuf!!.writeByte((first or 0x40).toByte().toInt()) // Set bit 7.
            byteBuf.writeByte((value ushr 6 or 0x80).toByte().toInt()) // Set bit 8.
            byteBuf.writeByte((value ushr 13 or 0x80).toByte().toInt()) // Set bit 8.
            byteBuf.writeByte((value ushr 20).toByte().toInt())
            return 4
        }
        require(5)
        position += 5
        val byteBuf = byteBuf
        byteBuf!!.writeByte((first or 0x40).toByte().toInt()) // Set bit 7.
        byteBuf.writeByte((value ushr 6 or 0x80).toByte().toInt()) // Set bit 8.
        byteBuf.writeByte((value ushr 13 or 0x80).toByte().toInt()) // Set bit 8.
        byteBuf.writeByte((value ushr 20 or 0x80).toByte().toInt()) // Set bit 8.
        byteBuf.writeByte((value ushr 27).toByte().toInt())
        return 5
    }

    // long:
    @Throws(KryoException::class)
    override fun writeLong(value: Long) {
        require(8)
        position += 8
        byteBuf!!.writeLong(value)
    }

    @Throws(KryoException::class)
    override fun writeVarLong(value: Long, optimizePositive: Boolean): Int {
        var value = value
        if (!optimizePositive) value = value shl 1 xor (value shr 63)
        if (value ushr 7 == 0L) {
            require(1)
            position++
            byteBuf!!.writeByte(value.toByte().toInt())
            return 1
        }
        if (value ushr 14 == 0L) {
            require(2)
            position += 2
            byteBuf!!.writeByte((value and 0x7F or 0x80).toByte().toInt())
            byteBuf!!.writeByte((value ushr 7).toByte().toInt())
            return 2
        }
        if (value ushr 21 == 0L) {
            require(3)
            position += 3
            val byteBuf = byteBuf
            byteBuf!!.writeByte((value and 0x7F or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 7 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 14).toByte().toInt())
            return 3
        }
        if (value ushr 28 == 0L) {
            require(4)
            position += 4
            val byteBuf = byteBuf
            byteBuf!!.writeByte((value and 0x7F or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 7 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 14 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 21).toByte().toInt())
            return 4
        }
        if (value ushr 35 == 0L) {
            require(5)
            position += 5
            val byteBuf = byteBuf
            byteBuf!!.writeByte((value and 0x7F or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 7 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 14 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 21 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 28).toByte().toInt())
            return 5
        }
        if (value ushr 42 == 0L) {
            require(6)
            position += 6
            val byteBuf = byteBuf
            byteBuf!!.writeByte((value and 0x7F or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 7 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 14 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 21 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 28 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 35).toByte().toInt())
            return 6
        }
        if (value ushr 49 == 0L) {
            require(7)
            position += 7
            val byteBuf = byteBuf
            byteBuf!!.writeByte((value and 0x7F or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 7 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 14 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 21 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 28 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 35 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 42).toByte().toInt())
            return 7
        }
        if (value ushr 56 == 0L) {
            require(8)
            position += 8
            val byteBuf = byteBuf
            byteBuf!!.writeByte((value and 0x7F or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 7 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 14 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 21 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 28 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 35 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 42 or 0x80).toByte().toInt())
            byteBuf.writeByte((value ushr 49).toByte().toInt())
            return 8
        }
        require(9)
        position += 9
        val byteBuf = byteBuf
        byteBuf!!.writeByte((value and 0x7F or 0x80).toByte().toInt())
        byteBuf.writeByte((value ushr 7 or 0x80).toByte().toInt())
        byteBuf.writeByte((value ushr 14 or 0x80).toByte().toInt())
        byteBuf.writeByte((value ushr 21 or 0x80).toByte().toInt())
        byteBuf.writeByte((value ushr 28 or 0x80).toByte().toInt())
        byteBuf.writeByte((value ushr 35 or 0x80).toByte().toInt())
        byteBuf.writeByte((value ushr 42 or 0x80).toByte().toInt())
        byteBuf.writeByte((value ushr 49 or 0x80).toByte().toInt())
        byteBuf.writeByte((value ushr 56).toByte().toInt())
        return 9
    }

    // float:
    @Throws(KryoException::class)
    override fun writeFloat(value: Float) {
        require(4)
        val byteBuf = byteBuf
        position += 4
        byteBuf!!.writeFloat(value)
    }

    // double:
    @Throws(KryoException::class)
    override fun writeDouble(value: Double) {
        require(8)
        position += 8
        val byteBuf = byteBuf
        byteBuf!!.writeDouble(value)
    }

    // short:
    @Throws(KryoException::class)
    override fun writeShort(value: Int) {
        require(2)
        position += 2
        byteBuf!!.writeShort(value)
    }

    // char:
    @Throws(KryoException::class)
    override fun writeChar(value: Char) {
        require(2)
        position += 2
        byteBuf!!.writeChar(value.code)
    }

    // boolean:
    @Throws(KryoException::class)
    override fun writeBoolean(value: Boolean) {
        require(1)
        byteBuf!!.writeByte((if (value) 1 else 0).toByte().toInt())
        position++
    }

    // String:
    @Throws(KryoException::class)
    override fun writeString(value: String?) {
        if (value == null) {
            writeByte(0x80) // 0 means null, bit 8 means UTF8.
            return
        }

        val charCount = value.length
        if (charCount == 0) {
            writeByte(1 or 0x80) // 1 means empty string, bit 8 means UTF8.
            return
        }
        require(charCount) // must be able to write this number of chars

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
                var i = 0
                val n = value.length
                while (i < n) {
                    byteBuf!!.writeByte(value[i].code.toByte().toInt())
                    ++i
                }
                position += charCount

                // mod the last written byte with 0x80 so we can use that when reading ascii bytes to see what the end of the string is
                val value1: Byte = (byteBuf!!.getByte(position - 1).toInt() or 0x80).toByte()
                byteBuf!!.setByte(position - 1, value1.toInt())
                return
            }
        }

        // UTF8 (or ASCII with length 1 or length > 32
        writeVarIntFlag(true, charCount + 1, true)
        var charIndex = 0
        // Try to write 7 bit chars.
        val byteBuf = byteBuf!!
        while (true) {
            val c = value[charIndex].code
            if (c > 127) break
            byteBuf.writeByte(c.toByte().toInt())
            charIndex++
            if (charIndex == charCount) {
                position = byteBuf.writerIndex()
                return
            }
        }
        position = byteBuf.writerIndex()
        if (charIndex < charCount) writeUtf8_slow(value, charCount, charIndex)
    }

    @Throws(KryoException::class)
    override fun writeAscii(value: String) {
        if (value == null) {
            writeByte(0x80) // 0 means null, bit 8 means UTF8.
            return
        }
        val charCount = value.length
        if (charCount == 0) {
            writeByte(1 or 0x80) // 1 means empty string, bit 8 means UTF8.
            return
        }
        require(charCount) // must be able to write this number of chars
        val byteBuf = byteBuf
        var i = 0
        val n = value.length
        while (i < n) {
            byteBuf!!.writeByte(value[i].code.toByte().toInt())
            ++i
        }
        position += charCount
        byteBuf!!.setByte(position - 1, (byteBuf.getByte(position - 1).toInt() or 0x80).toByte().toInt()) // Bit 8 means end of ASCII.
    }

    private fun writeUtf8_slow(value: String, charCount: Int, charIndex: Int) {
        var charIndex = charIndex
        while (charIndex < charCount) {
            val c = value[charIndex].code

            if (c <= 0x007F) {
                writeByte(c.toByte())
            } else if (c > 0x07FF) {
                require(3)
                byteBuf!!.writeByte((0xE0 or (c shr 12 and 0x0F)).toByte().toInt())
                byteBuf!!.writeByte((0x80 or (c shr 6 and 0x3F)).toByte().toInt())
                byteBuf!!.writeByte((0x80 or (c and 0x3F)).toByte().toInt())
                position += 3
            } else {
                require(2)
                byteBuf!!.writeByte((0xC0 or (c shr 6 and 0x1F)).toByte().toInt())
                byteBuf!!.writeByte((0x80 or (c and 0x3F)).toByte().toInt())
                position += 2
            }
            charIndex++
        }
    }

    // Primitive arrays:
    @Throws(KryoException::class)
    override fun writeInts(array: IntArray, offset: Int, count: Int) {
        var offset = offset
        require(count shl 2)
        val byteBuf = byteBuf
        val n = offset + count
        while (offset < n) {
            val value = array[offset]
            byteBuf!!.writeByte(value.toByte().toInt())
            byteBuf.writeByte((value shr 8).toByte().toInt())
            byteBuf.writeByte((value shr 16).toByte().toInt())
            byteBuf.writeByte((value shr 24).toByte().toInt())
            offset++
        }
        position = byteBuf!!.writerIndex()
    }

    @Throws(KryoException::class)
    override fun writeLongs(array: LongArray, offset: Int, count: Int) {
        var offset = offset
        require(count shl 3)
        val byteBuf = byteBuf
        val n = offset + count
        while (offset < n) {
            val value = array[offset]
            byteBuf!!.writeByte(value.toByte().toInt())
            byteBuf.writeByte((value ushr 8).toByte().toInt())
            byteBuf.writeByte((value ushr 16).toByte().toInt())
            byteBuf.writeByte((value ushr 24).toByte().toInt())
            byteBuf.writeByte((value ushr 32).toByte().toInt())
            byteBuf.writeByte((value ushr 40).toByte().toInt())
            byteBuf.writeByte((value ushr 48).toByte().toInt())
            byteBuf.writeByte((value ushr 56).toByte().toInt())
            offset++
        }
        position = byteBuf!!.writerIndex()
    }

    @Throws(KryoException::class)
    override fun writeFloats(array: FloatArray, offset: Int, count: Int) {
        var offset = offset
        require(count shl 2)
        val byteBuf = byteBuf
        val n = offset + count
        while (offset < n) {
            val value = java.lang.Float.floatToIntBits(array[offset])
            byteBuf!!.writeByte(value.toByte().toInt())
            byteBuf.writeByte((value shr 8).toByte().toInt())
            byteBuf.writeByte((value shr 16).toByte().toInt())
            byteBuf.writeByte((value shr 24).toByte().toInt())
            offset++
        }
        position = byteBuf!!.writerIndex()
    }

    @Throws(KryoException::class)
    override fun writeDoubles(array: DoubleArray, offset: Int, count: Int) {
        var offset = offset
        require(count shl 3)
        val byteBuf = byteBuf
        val n = offset + count
        while (offset < n) {
            val value = java.lang.Double.doubleToLongBits(array[offset])
            byteBuf!!.writeByte(value.toByte().toInt())
            byteBuf.writeByte((value ushr 8).toByte().toInt())
            byteBuf.writeByte((value ushr 16).toByte().toInt())
            byteBuf.writeByte((value ushr 24).toByte().toInt())
            byteBuf.writeByte((value ushr 32).toByte().toInt())
            byteBuf.writeByte((value ushr 40).toByte().toInt())
            byteBuf.writeByte((value ushr 48).toByte().toInt())
            byteBuf.writeByte((value ushr 56).toByte().toInt())
            offset++
        }
        position = byteBuf!!.writerIndex()
    }

    @Throws(KryoException::class)
    override fun writeShorts(array: ShortArray, offset: Int, count: Int) {
        var offset = offset
        require(count shl 1)
        val n = offset + count
        while (offset < n) {
            val value = array[offset].toInt()
            byteBuf!!.writeByte(value.toByte().toInt())
            byteBuf!!.writeByte((value ushr 8).toByte().toInt())
            offset++
        }
        position = byteBuf!!.writerIndex()
    }

    @Throws(KryoException::class)
    override fun writeChars(array: CharArray, offset: Int, count: Int) {
        var offset = offset
        require(count shl 1)
        val n = offset + count
        while (offset < n) {
            val value = array[offset].toInt()
            byteBuf!!.writeByte(value.toByte().toInt())
            byteBuf!!.writeByte((value ushr 8).toByte().toInt())
            offset++
        }
        position = byteBuf!!.writerIndex()
    }

    @Throws(KryoException::class)
    override fun writeBooleans(array: BooleanArray, offset: Int, count: Int) {
        var offset = offset
        require(count)
        val n = offset + count
        while (offset < n) {
            byteBuf!!.writeByte(if (array[offset]) 1 else 0)
            offset++
        }
        position = byteBuf!!.writerIndex()
    }
}
