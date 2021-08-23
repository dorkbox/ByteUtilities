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
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.util.Util
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.io.IOException
import java.io.InputStream

/**
 * An [InputStream] which reads data from a [ByteBuf].
 *
 *
 * A read operation against this stream will occur at the `readerIndex`
 * of its underlying buffer and the `readerIndex` will increase during
 * the read operation.
 *
 *
 * This stream implements [DataInput] for your convenience.
 * The endianness of the stream is not always big endian but depends on
 * the endianness of the underlying buffer.
 *
 *
 * Utility methods are provided for efficiently reading primitive types and strings.
 *
 *
 * Modified from KRYO ByteBufferInput to use ByteBuf instead of ByteBuffer.
 */
class ByteBufInput : Input {
    companion object {
        /**
         * Gets the version number.
         */
        const val version = BytesInfo.version

        init {
            // Add this project to the updates system, which verifies this class + UUID + version information
            dorkbox.updates.Updates.add(ByteBufInput::class.java, "f176cecea06e48e1a96d59c08a6e98c3", BytesInfo.version)
        }
    }

    var byteBuf: ByteBuf? = null
        private set

    private var initialReaderIndex = 0
    private var initialWriterIndex = 0
    private var tempBuffer: ByteArray? = null

    /** Creates a new Input for reading from a direct [ByteBuf].
     * @param bufferSize The size of the buffer. An exception is thrown if more bytes than this are read and
     * [.fill] does not supply more bytes.
     */
    constructor(bufferSize: Int) {
        capacity = bufferSize
        byteBuf = Unpooled.buffer(bufferSize)
    }
    /** Creates a new Input for reading from a [ByteBuf] which is filled with the specified bytes.
     * @see .setBuffer
     */
    /** Creates a new Input for reading from a [ByteBuf] which is filled with the specified bytes.  */
    constructor(bytes: ByteArray?, offset: Int = 0, count: Int = bytes!!.size) {
        requireNotNull(bytes) { "bytes cannot be null." }
        setBuffer(Unpooled.wrappedBuffer(bytes, offset, count))
    }

    /** Creates a new Input for reading from a ByteBuffer.  */
    constructor(buffer: ByteBuf?) {
        setBuffer(buffer)
    }

    /** @see Input.Input
     */
    constructor(inputStream: InputStream?) : this(4096) {
        requireNotNull(inputStream) { "inputStream cannot be null." }
        this.inputStream = inputStream
    }

    /** @see Input.Input
     */
    constructor(inputStream: InputStream?, bufferSize: Int) : this(bufferSize) {
        requireNotNull(inputStream) { "inputStream cannot be null." }
        this.inputStream = inputStream
    }

    /** Throws [UnsupportedOperationException] because this input uses a ByteBuffer, not a byte[].
     * @see .getByteBuf
     */
    @Deprecated("Use getByteBuf() instead",
        ReplaceWith("getByteBuf()")
    )
    override fun getBuffer(): ByteArray {
        throw UnsupportedOperationException("This input does not used a byte[], see #getByteBuf().")
    }

    /** Throws [UnsupportedOperationException] because this input uses a ByteBuffer, not a byte[].
     * @see .setBuffer
     */
    @Deprecated("Use setByteBuf() instead",
        ReplaceWith("setByteBuf(ByteBuf)")
    )
    override fun setBuffer(bytes: ByteArray) {
        throw UnsupportedOperationException("This input does not used a byte[], see #setByteBuf(ByteBuf).")
    }

    /** Throws [UnsupportedOperationException] because this input uses a ByteBuffer, not a byte[].
     * @see .setBuffer
     */
    @Deprecated(" ")
    override fun setBuffer(bytes: ByteArray, offset: Int, count: Int) {
        throw UnsupportedOperationException("This input does not used a byte[], see #setByteBuf().")
    }

    /** Sets a new buffer to read from. The bytes are not copied, the old buffer is discarded and the new buffer used in its place.
     * The position, limit, and capacity are set to match the specified buffer. The total is reset. The
     * [InputStream][.setInputStream] is set to null.  */
    fun setBuffer(buffer: ByteBuf?) {
        requireNotNull(buffer) { "buffer cannot be null." }
        byteBuf = buffer
        initialReaderIndex = byteBuf!!.readerIndex() // where the object starts...
        initialWriterIndex = byteBuf!!.writerIndex() // where the object ends...
        position = initialReaderIndex
        limit = initialWriterIndex
        capacity = buffer.capacity()
        total = 0
        inputStream = null
    }

    override fun setInputStream(inputStream: InputStream) {
        this.inputStream = inputStream
        limit = 0
        reset()
    }

    override fun reset() {
        super.reset()
        byteBuf!!.setIndex(initialReaderIndex, initialWriterIndex)
    }

    /** Fills the buffer with more bytes. The default implementation reads from the [InputStream][.getInputStream], if set.
     * Can be overridden to fill the bytes from another source.  */
    @Throws(KryoException::class)
    protected fun fill(buffer: ByteBuf?, offset: Int, count: Int): Int {
        var count = count
        return if (inputStream == null) -1 else try {
            if (tempBuffer == null) tempBuffer = ByteArray(2048)
            buffer!!.readerIndex(offset)
            var total = 0
            while (count > 0) {
                val read = inputStream.read(tempBuffer, 0, Math.min(tempBuffer!!.size, count))
                if (read == -1) {
                    if (total == 0) return -1
                    break
                }
                buffer.writeBytes(tempBuffer, 0, read)
                count -= read
                total += read
            }
            buffer.readerIndex(offset)
            total
        } catch (ex: IOException) {
            throw KryoException(ex)
        }
    }

    @Throws(KryoException::class)
    override fun require(required: Int): Int {
        var remaining = limit - position
        if (remaining >= required) return remaining
        if (required > capacity) throw KryoException("Buffer too small: capacity: $capacity, required: $required")
        val count: Int
        // Try to fill the buffer.
        if (remaining > 0) {
            count = fill(byteBuf, limit, capacity - limit)
            if (count == -1) throw KryoException("Buffer underflow.")
            remaining += count
            if (remaining >= required) {
                limit += count
                return remaining
            }
        }

        // Compact. Position after compaction can be non-zero.
        // CANNOT COMPACT A BYTEBUF
        // byteBuffer.position(position);
        // byteBuffer.compact();
        // total += position;
        // position = 0;
        //
        // while (true) {
        //     count = fill(byteBuffer, remaining, capacity - remaining);
        //     if (count == -1) {
        //         if (remaining >= required) break;
        //         throw new KryoException("Buffer underflow.");
        //     }
        //     remaining += count;
        //     if (remaining >= required) break; // Enough has been read.
        // }
        // limit = remaining;
        // byteBuffer.position(0);
        return remaining
    }

    /** Fills the buffer with at least the number of bytes specified, if possible.
     * @param optional Must be > 0.
     * @return the number of bytes remaining, but not more than optional, or -1 if [.fill] is unable to
     * provide more bytes.
     */
    @Throws(KryoException::class)
    override fun optional(optional: Int): Int {
        var optional = optional
        var remaining = limit - position
        if (remaining >= optional) return optional
        optional = Math.min(optional, capacity)

        // Try to fill the buffer.
        val count = fill(byteBuf, limit, capacity - limit)
        if (count == -1) return if (remaining == 0) -1 else Math.min(remaining, optional)
        remaining += count
        if (remaining >= optional) {
            limit += count
            return optional
        }

        // Compact.
        // CANNOT COMPACT A BYTEBUF
        // byteBuffer.compact();
        // total += position;
        // position = 0;
        //
        // while (true) {
        //     count = fill(byteBuffer, remaining, capacity - remaining);
        //     if (count == -1) break;
        //     remaining += count;
        //     if (remaining >= optional) break; // Enough has been read.
        // }
        // limit = remaining;
        // byteBuffer.position(position);
        return if (remaining == 0) -1 else Math.min(remaining, optional)
    }

    // InputStream:
    @Throws(KryoException::class)
    override fun read(): Int {
        if (optional(1) <= 0) return -1
        position++
        return byteBuf!!.readByte().toInt() and 0xFF
    }

    @Throws(KryoException::class)
    override fun read(bytes: ByteArray): Int {
        return read(bytes, 0, bytes.size)
    }

    @Throws(KryoException::class)
    override fun read(bytes: ByteArray, offset: Int, count: Int): Int {
        var offset = offset
        var count = count
        requireNotNull(bytes) { "bytes cannot be null." }
        val startingCount = count
        var copyCount = Math.min(limit - position, count)
        while (true) {
            byteBuf!!.getBytes(position, bytes, offset, copyCount)
            position += copyCount
            count -= copyCount
            if (count == 0) break
            offset += copyCount
            copyCount = optional(count)
            if (copyCount == -1) {
                // End of data.
                if (startingCount == count) return -1
                break
            }
            if (position == limit) break
        }
        return startingCount - count
    }

    override fun setPosition(position: Int) {
        this.position = position
        byteBuf!!.readerIndex(position)
    }

    override fun setLimit(limit: Int) {
        this.limit = limit
        byteBuf!!.writerIndex(limit)
    }

    @Throws(KryoException::class)
    override fun skip(count: Int) {
        super.skip(count)
        byteBuf!!.readerIndex(position)
    }

    @Throws(KryoException::class)
    override fun skip(count: Long): Long {
        var remaining = count
        while (remaining > 0) {
            val skip = Math.min(Util.maxArraySize.toLong(), remaining).toInt()
            skip(skip)
            remaining -= skip.toLong()
        }
        return count
    }

    @Throws(KryoException::class)
    override fun close() {
        if (inputStream != null) {
            try {
                inputStream.close()
            } catch (ignored: IOException) {
            }
        }
    }

    // byte:
    @Throws(KryoException::class)
    override fun readByte(): Byte {
        if (position == limit) require(1)
        position++
        return byteBuf!!.readByte()
    }

    @Throws(KryoException::class)
    override fun readByteUnsigned(): Int {
        if (position == limit) require(1)
        position++
        return byteBuf!!.readByte().toInt() and 0xFF
    }

    @Throws(KryoException::class)
    override fun readBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        readBytes(bytes, 0, length)
        return bytes
    }

    @Throws(KryoException::class)
    override fun readBytes(bytes: ByteArray, offset: Int, count: Int) {
        var offset = offset
        var count = count
        requireNotNull(bytes) { "bytes cannot be null." }
        var copyCount = Math.min(limit - position, count)
        while (true) {
            byteBuf!!.readBytes(bytes, offset, copyCount)
            position += copyCount
            count -= copyCount
            if (count == 0) break
            offset += copyCount
            copyCount = Math.min(count, capacity)
            require(copyCount)
        }
    }

    // int:
    @Throws(KryoException::class)
    override fun readInt(): Int {
        require(4)
        position += 4
        return byteBuf!!.readInt()
    }

    @Throws(KryoException::class)
    override fun readVarInt(optimizePositive: Boolean): Int {
        if (require(1) < 5) return readVarInt_slow(optimizePositive)
        val byteBuf = byteBuf!!
        var b = byteBuf.readByte().toInt()
        var result = b and 0x7F
        if (b and 0x80 != 0) {
            b = byteBuf.readByte().toInt()
            result = result or (b and 0x7F shl 7)
            if (b and 0x80 != 0) {
                b = byteBuf.readByte().toInt()
                result = result or (b and 0x7F shl 14)
                if (b and 0x80 != 0) {
                    b = byteBuf.readByte().toInt()
                    result = result or (b and 0x7F shl 21)
                    if (b and 0x80 != 0) {
                        b = byteBuf.readByte().toInt()
                        result = result or (b and 0x7F shl 28)
                    }
                }
            }
        }
        position = byteBuf.readerIndex()
        return if (optimizePositive) result else result ushr 1 xor -(result and 1)
    }

    private fun readVarInt_slow(optimizePositive: Boolean): Int {
        // The buffer is guaranteed to have at least 1 byte.
        position++
        val byteBuf = byteBuf!!
        var b = byteBuf.readByte().toInt()
        var result = b and 0x7F
        if (b and 0x80 != 0) {
            if (position == limit) require(1)
            position++
            b = byteBuf.readByte().toInt()
            result = result or (b and 0x7F shl 7)
            if (b and 0x80 != 0) {
                if (position == limit) require(1)
                position++
                b = byteBuf.readByte().toInt()
                result = result or (b and 0x7F shl 14)
                if (b and 0x80 != 0) {
                    if (position == limit) require(1)
                    position++
                    b = byteBuf.readByte().toInt()
                    result = result or (b and 0x7F shl 21)
                    if (b and 0x80 != 0) {
                        if (position == limit) require(1)
                        position++
                        b = byteBuf.readByte().toInt()
                        result = result or (b and 0x7F shl 28)
                    }
                }
            }
        }
        return if (optimizePositive) result else result ushr 1 xor -(result and 1)
    }

    @Throws(KryoException::class)
    override fun canReadVarInt(): Boolean {
        if (limit - position >= 5) return true
        if (optional(5) <= 0) return false
        var p = position
        val limit = limit
        val byteBuf = byteBuf!!
        if (byteBuf.getByte(p++).toInt() and 0x80 == 0) return true
        if (p == limit) return false
        if (byteBuf.getByte(p++).toInt() and 0x80 == 0) return true
        if (p == limit) return false
        if (byteBuf.getByte(p++).toInt() and 0x80 == 0) return true
        if (p == limit) return false
        if (byteBuf.getByte(p++).toInt() and 0x80 == 0) return true
        return if (p == limit) false else true
    }

    /** Reads the boolean part of a varint flag. The position is not advanced, [.readVarIntFlag] should be used to
     * advance the position.  */
    override fun readVarIntFlag(): Boolean {
        if (position == limit) require(1)
        return byteBuf!!.getByte(position).toInt() and 0x80 != 0
    }

    /** Reads the 1-5 byte int part of a varint flag. The position is advanced so if the boolean part is needed it should be read
     * first with [.readVarIntFlag].  */
    override fun readVarIntFlag(optimizePositive: Boolean): Int {
        if (require(1) < 5) return readVarIntFlag_slow(optimizePositive)
        val byteBuf = byteBuf
        var b = byteBuf!!.readByte().toInt()
        var result = b and 0x3F // Mask first 6 bits.
        if (b and 0x40 != 0) { // Bit 7 means another byte, bit 8 means UTF8.
            b = byteBuf.readByte().toInt()
            result = result or (b and 0x7F shl 6)
            if (b and 0x80 != 0) {
                b = byteBuf.readByte().toInt()
                result = result or (b and 0x7F shl 13)
                if (b and 0x80 != 0) {
                    b = byteBuf.readByte().toInt()
                    result = result or (b and 0x7F shl 20)
                    if (b and 0x80 != 0) {
                        b = byteBuf.readByte().toInt()
                        result = result or (b and 0x7F shl 27)
                    }
                }
            }
        }
        position = byteBuf.readerIndex()
        return if (optimizePositive) result else result ushr 1 xor -(result and 1)
    }

    private fun readVarIntFlag_slow(optimizePositive: Boolean): Int {
        // The buffer is guaranteed to have at least 1 byte.
        position++
        var b = byteBuf!!.readByte().toInt()
        var result = b and 0x3F
        if (b and 0x40 != 0) {
            if (position == limit) require(1)
            position++
            val byteBuf = byteBuf
            b = byteBuf!!.readByte().toInt()
            result = result or (b and 0x7F shl 6)
            if (b and 0x80 != 0) {
                if (position == limit) require(1)
                position++
                b = byteBuf.readByte().toInt()
                result = result or (b and 0x7F shl 13)
                if (b and 0x80 != 0) {
                    if (position == limit) require(1)
                    position++
                    b = byteBuf.readByte().toInt()
                    result = result or (b and 0x7F shl 20)
                    if (b and 0x80 != 0) {
                        if (position == limit) require(1)
                        position++
                        b = byteBuf.readByte().toInt()
                        result = result or (b and 0x7F shl 27)
                    }
                }
            }
        }
        return if (optimizePositive) result else result ushr 1 xor -(result and 1)
    }

    // long:
    @Throws(KryoException::class)
    override fun readLong(): Long {
        require(8)
        position += 8
        return byteBuf!!.readLong()
    }

    @Throws(KryoException::class)
    override fun readVarLong(optimizePositive: Boolean): Long {
        if (require(1) < 9) return readVarLong_slow(optimizePositive)
        val byteBuf = byteBuf!!
        var b = byteBuf.readByte().toInt()
        var result = (b and 0x7F).toLong()
        if (b and 0x80 != 0) {
            b = byteBuf.readByte().toInt()
            result = result or (b and 0x7F shl 7).toLong()
            if (b and 0x80 != 0) {
                b = byteBuf.readByte().toInt()
                result = result or (b and 0x7F shl 14).toLong()
                if (b and 0x80 != 0) {
                    b = byteBuf.readByte().toInt()
                    result = result or (b and 0x7F shl 21).toLong()
                    if (b and 0x80 != 0) {
                        b = byteBuf.readByte().toInt()
                        result = result or ((b and 0x7F).toLong() shl 28)
                        if (b and 0x80 != 0) {
                            b = byteBuf.readByte().toInt()
                            result = result or ((b and 0x7F).toLong() shl 35)
                            if (b and 0x80 != 0) {
                                b = byteBuf.readByte().toInt()
                                result = result or ((b and 0x7F).toLong() shl 42)
                                if (b and 0x80 != 0) {
                                    b = byteBuf.readByte().toInt()
                                    result = result or ((b and 0x7F).toLong() shl 49)
                                    if (b and 0x80 != 0) {
                                        b = byteBuf.readByte().toInt()
                                        result = result or (b.toLong() shl 56)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        position = byteBuf.readerIndex()
        return if (optimizePositive) result else result ushr 1 xor -(result and 1)
    }

    private fun readVarLong_slow(optimizePositive: Boolean): Long {
        // The buffer is guaranteed to have at least 1 byte.
        position++
        val byteBuf = byteBuf!!
        var b = byteBuf.readByte().toInt()
        var result = (b and 0x7F).toLong()
        if (b and 0x80 != 0) {
            if (position == limit) require(1)
            position++
            b = byteBuf.readByte().toInt()
            result = result or (b and 0x7F shl 7).toLong()
            if (b and 0x80 != 0) {
                if (position == limit) require(1)
                position++
                b = byteBuf.readByte().toInt()
                result = result or (b and 0x7F shl 14).toLong()
                if (b and 0x80 != 0) {
                    if (position == limit) require(1)
                    position++
                    b = byteBuf.readByte().toInt()
                    result = result or (b and 0x7F shl 21).toLong()
                    if (b and 0x80 != 0) {
                        if (position == limit) require(1)
                        position++
                        b = byteBuf.readByte().toInt()
                        result = result or ((b and 0x7F).toLong() shl 28)
                        if (b and 0x80 != 0) {
                            if (position == limit) require(1)
                            position++
                            b = byteBuf.readByte().toInt()
                            result = result or ((b and 0x7F).toLong() shl 35)
                            if (b and 0x80 != 0) {
                                if (position == limit) require(1)
                                position++
                                b = byteBuf.readByte().toInt()
                                result = result or ((b and 0x7F).toLong() shl 42)
                                if (b and 0x80 != 0) {
                                    if (position == limit) require(1)
                                    position++
                                    b = byteBuf.readByte().toInt()
                                    result = result or ((b and 0x7F).toLong() shl 49)
                                    if (b and 0x80 != 0) {
                                        if (position == limit) require(1)
                                        position++
                                        b = byteBuf.readByte().toInt()
                                        result = result or (b.toLong() shl 56)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return if (optimizePositive) result else result ushr 1 xor -(result and 1)
    }

    @Throws(KryoException::class)
    override fun canReadVarLong(): Boolean {
        if (limit - position >= 9) return true
        if (optional(5) <= 0) return false
        var p = position
        val limit = limit
        val byteBuf = byteBuf
        if (byteBuf!!.getByte(p++).toInt() and 0x80 == 0) return true
        if (p == limit) return false
        if (byteBuf.getByte(p++).toInt() and 0x80 == 0) return true
        if (p == limit) return false
        if (byteBuf.getByte(p++).toInt() and 0x80 == 0) return true
        if (p == limit) return false
        if (byteBuf.getByte(p++).toInt() and 0x80 == 0) return true
        if (p == limit) return false
        if (byteBuf.getByte(p++).toInt() and 0x80 == 0) return true
        if (p == limit) return false
        if (byteBuf.getByte(p++).toInt() and 0x80 == 0) return true
        if (p == limit) return false
        if (byteBuf.getByte(p++).toInt() and 0x80 == 0) return true
        if (p == limit) return false
        if (byteBuf.getByte(p++).toInt() and 0x80 == 0) return true
        return if (p == limit) false else true
    }

    // float:
    @Throws(KryoException::class)
    override fun readFloat(): Float {
        require(4)
        val p = position
        position = p + 4
        return byteBuf!!.readFloat()
    }

    // double:
    @Throws(KryoException::class)
    override fun readDouble(): Double {
        require(8)
        val p = position
        position = p + 8
        return byteBuf!!.readDouble()
    }

    // boolean:
    @Throws(KryoException::class)
    override fun readBoolean(): Boolean {
        if (position == limit) require(1)
        position++
        return if (byteBuf!!.readByte().toInt() == 1) true else false
    }

    // short:
    @Throws(KryoException::class)
    override fun readShort(): Short {
        require(2)
        position += 2
        return byteBuf!!.readShort()
    }

    @Throws(KryoException::class)
    override fun readShortUnsigned(): Int {
        require(2)
        position += 2
        return byteBuf!!.readUnsignedShort()
    }

    // char:
    @Throws(KryoException::class)
    override fun readChar(): Char {
        require(2)
        position += 2
        return byteBuf!!.readChar()
    }

    // String:
    override fun readString(): String? {
        if (!readVarIntFlag()) return readAsciiString() // ASCII.
        // Null, empty, or UTF8.
        var charCount = readVarIntFlag(true)
        when (charCount) {
            0 -> return null
            1 -> return ""
        }

        charCount-- // make count adjustment
        readUtf8Chars(charCount)
        return String(chars, 0, charCount)
    }

    override fun readStringBuilder(): StringBuilder? {
        if (!readVarIntFlag()) return StringBuilder(readAsciiString()) // ASCII.
        // Null, empty, or UTF8.
        var charCount = readVarIntFlag(true)
        when (charCount) {
            0 -> return null
            1 -> return StringBuilder("")
        }
        charCount--
        readUtf8Chars(charCount)
        val builder = StringBuilder(charCount)
        builder.append(chars, 0, charCount)
        return builder
    }

    private fun readUtf8Chars(charCount: Int) {
        if (chars.size < charCount) chars = CharArray(charCount)
        val chars = chars
        // Try to read 7 bit ASCII chars.
        val byteBuf = byteBuf!!
        var charIndex = 0
        val count = require(1).coerceAtMost(charCount)
        while (charIndex < count) {
            val b = byteBuf.readByte().toInt()
            if (b < 0) break
            chars[charIndex++] = b.toChar()
        }
        position += charIndex

        // If buffer didn't hold all chars or any were not ASCII, use slow path for remainder.
        if (charIndex < charCount) {
            byteBuf.readerIndex(position)
            readUtf8Chars_slow(charCount, charIndex)
        }
    }

    private fun readUtf8Chars_slow(charCount: Int, charIndex: Int) {
        var index = charIndex
        val byteBuf = byteBuf!!
        val chars = chars

        while (index < charCount) {
            if (position == limit) require(1)

            position++
            val b: Int = byteBuf.readByte().toInt() and 0xFF
            when (b shr 4) {
                0, 1, 2, 3, 4, 5, 6, 7 -> chars[index] = b.toChar()
                12, 13 -> {
                    if (position == limit) require(1)
                    position++
                    chars[index] = (b and 0x1F shl 6 or (byteBuf.readByte().toInt() and 0x3F)).toChar()
                }
                14 -> {
                    require(2)
                    position += 2
                    val b2 = byteBuf.readByte().toInt()
                    val b3 = byteBuf.readByte().toInt()
                    chars[index] = (b and 0x0F shl 12 or (b2 and 0x3F shl 6) or (b3 and 0x3F)).toChar()
                }
            }
            index++
        }
    }

    private fun readAsciiString(): String {
        val chars = chars
        val byteBuf = byteBuf
        var charCount = 0
        val n = Math.min(chars.size, limit - position)
        while (charCount < n) {
            val b = byteBuf!!.readByte().toInt()
            if (b and 0x80 == 0x80) {
                position = byteBuf.readerIndex()
                chars[charCount] = (b and 0x7F).toChar()
                return String(chars, 0, charCount + 1)
            }
            chars[charCount] = b.toChar()
            charCount++
        }
        position = byteBuf!!.readerIndex()
        return readAscii_slow(charCount)
    }

    private fun readAscii_slow(charCount: Int): String {
        var charCount = charCount
        var chars = chars
        val byteBuf = byteBuf
        while (true) {
            if (position == limit) require(1)
            position++
            val b = byteBuf!!.readByte().toInt()
            if (charCount == chars.size) {
                val newChars = CharArray(charCount * 2)
                System.arraycopy(chars, 0, newChars, 0, charCount)
                chars = newChars
                this.chars = newChars
            }
            if (b and 0x80 == 0x80) {
                chars[charCount] = (b and 0x7F).toChar()
                return String(chars, 0, charCount + 1)
            }
            chars[charCount++] = b.toChar()
        }
    }

    // Primitive arrays:
    @Throws(KryoException::class)
    override fun readInts(length: Int): IntArray {
        val array = IntArray(length)
        if (optional(length shl 2) == length shl 2) {
            val byteBuf = byteBuf
            for (i in 0 until length) {
                array[i] = byteBuf!!.readInt()
            }
            position = byteBuf!!.readerIndex()
        } else {
            for (i in 0 until length) array[i] = readInt()
        }
        return array
    }

    @Throws(KryoException::class)
    override fun readLongs(length: Int): LongArray {
        val array = LongArray(length)
        if (optional(length shl 3) == length shl 3) {
            val byteBuf = byteBuf!!
            for (i in 0 until length) {
                array[i] = byteBuf.readLong()
            }
            position = byteBuf.readerIndex()
        } else {
            for (i in 0 until length) array[i] = readLong()
        }
        return array
    }

    @Throws(KryoException::class)
    override fun readFloats(length: Int): FloatArray {
        val array = FloatArray(length)
        if (optional(length shl 2) == length shl 2) {
            val byteBuf = byteBuf!!
            for (i in 0 until length) {
                array[i] = byteBuf.readFloat()
            }
            position = byteBuf.readerIndex()
        } else {
            for (i in 0 until length) array[i] = readFloat()
        }
        return array
    }

    @Throws(KryoException::class)
    override fun readDoubles(length: Int): DoubleArray {
        val array = DoubleArray(length)
        if (optional(length shl 3) == length shl 3) {
            val byteBuf = byteBuf!!
            for (i in 0 until length) {
                array[i] = byteBuf.readDouble()
            }
            position = byteBuf.readerIndex()
        } else {
            for (i in 0 until length) array[i] = readDouble()
        }
        return array
    }

    @Throws(KryoException::class)
    override fun readShorts(length: Int): ShortArray {
        val array = ShortArray(length)
        if (optional(length shl 1) == length shl 1) {
            val byteBuf = byteBuf
            for (i in 0 until length) array[i] = (byteBuf!!.readByte().toInt() and 0xFF or (byteBuf.readByte().toInt() and 0xFF shl 8)).toShort()
            position = byteBuf!!.readerIndex()
        } else {
            for (i in 0 until length) array[i] = readShort()
        }
        return array
    }

    @Throws(KryoException::class)
    override fun readChars(length: Int): CharArray {
        val array = CharArray(length)
        if (optional(length shl 1) == length shl 1) {
            val byteBuf = byteBuf
            for (i in 0 until length) array[i] = (byteBuf!!.readByte().toInt() and 0xFF or (byteBuf.readByte().toInt() and 0xFF shl 8)).toChar()
            position = byteBuf!!.readerIndex()
        } else {
            for (i in 0 until length) array[i] = readChar()
        }
        return array
    }

    @Throws(KryoException::class)
    override fun readBooleans(length: Int): BooleanArray {
        val array = BooleanArray(length)
        if (optional(length) == length) {
            val byteBuf = byteBuf
            for (i in 0 until length) array[i] = byteBuf!!.readByte().toInt() != 0
            position = byteBuf!!.readerIndex()
        } else {
            for (i in 0 until length) array[i] = readBoolean()
        }
        return array
    }
}
