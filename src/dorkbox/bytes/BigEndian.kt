/*
 * Copyright 2026 dorkbox, llc
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
package dorkbox.bytes

import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer


// NOTE: CHAR and SHORT are the exact same.
fun Char.toBytesBE() = BigEndian.Char_.toBytes(this)
fun Char.toBytesBE(bytes: ByteArray, offset: Int) = BigEndian.Char_.toBytes(this, bytes, offset)
fun Char.toBytesBE(bytes: ByteArray) = BigEndian.Char_.toBytes(this, bytes)

fun ByteArray.toCharBE(offset: Int, byteNumber: Int) = BigEndian.Char_.from(this, offset, byteNumber)
fun ByteArray.toCharBE() = BigEndian.Char_.from(this)
fun ByteBuffer.toCharBE() = BigEndian.Char_.from(this)
fun InputStream.toCharBE(length: Int = 2) = BigEndian.Char_.from(this, length)
fun RandomAccessFile.toCharBE(length: Int = 2) = BigEndian.Char_.from(this, length)


fun Short.toBytesBE() = BigEndian.Short_.toBytes(this)
fun Short.toBytesBE(bytes: ByteArray, offset: Int) = BigEndian.Short_.toBytes(this, bytes, offset)
fun Short.toBytesBE(bytes: ByteArray) = BigEndian.Short_.toBytes(this, bytes)

fun ByteArray.toShortBE(offset: Int, byteNumber: Int) = BigEndian.Short_.from(this, offset, byteNumber)
fun ByteArray.toShortBE() = BigEndian.Short_.from(this)
fun ByteBuffer.toShortBE() = BigEndian.Short_.from(this)
fun InputStream.toShortBE(length: Int = 2) = BigEndian.Short_.from(this, length)
fun RandomAccessFile.toShortBE(length: Int = 2) = BigEndian.Short_.from(this, length)


fun UShort.toBytesBE() = BigEndian.UShort_.toBytes(this)
fun UShort.toBytesBE(bytes: ByteArray, offset: Int) = BigEndian.UShort_.toBytes(this, bytes, offset)
fun UShort.toBytesBE(bytes: ByteArray) = BigEndian.UShort_.toBytes(this, bytes)

fun ByteArray.toUShortBE(offset: Int, byteNumber: Int) = BigEndian.UShort_.from(this, offset, byteNumber)
fun ByteArray.toUShortBE() = BigEndian.UShort_.from(this)
fun ByteBuffer.toUShortBE() = BigEndian.UShort_.from(this)
fun InputStream.toUShortBE(length: Int = 2) = BigEndian.UShort_.from(this, length)
fun RandomAccessFile.toUShortBE(length: Int = 2) = BigEndian.UShort_.from(this, length)


fun Int.toBytesBE() = BigEndian.Int_.toBytes(this)
fun Int.toBytesBE(bytes: ByteArray, offset: Int) = BigEndian.Int_.toBytes(this, bytes, offset)
fun Int.toBytesBE(bytes: ByteArray) = BigEndian.Int_.toBytes(this, bytes)

fun ByteArray.toIntBE(offset: Int, byteNumber: Int) = BigEndian.Int_.from(this, offset, byteNumber)
fun ByteArray.toIntBE() = BigEndian.Int_.from(this)
fun ByteBuffer.toIntBE() = BigEndian.Int_.from(this)
fun InputStream.toIntBE(length: Int = 4) = BigEndian.Int_.from(this, length)
fun RandomAccessFile.toIntBE(length: Int = 4) = BigEndian.Int_.from(this, length)


fun UInt.toBytesBE() = BigEndian.UInt_.toBytes(this)
fun UInt.toBytesBE(bytes: ByteArray, offset: Int) = BigEndian.UInt_.toBytes(this, bytes, offset)
fun UInt.toBytesBE(bytes: ByteArray) = BigEndian.UInt_.toBytes(this, bytes)

fun ByteArray.toUIntBE(offset: Int, byteNumber: Int) = BigEndian.UInt_.from(this, offset, byteNumber)
fun ByteArray.toUIntBE() = BigEndian.UInt_.from(this)
fun ByteBuffer.toUIntBE() = BigEndian.UInt_.from(this)
fun InputStream.toUIntBE(length: Int = 4) = BigEndian.UInt_.from(this, length)
fun RandomAccessFile.toUIntBE(length: Int = 4) = BigEndian.UInt_.from(this, length)


fun Long.toBytesBE() = BigEndian.Long_.toBytes(this)
fun Long.toBytesBE(bytes: ByteArray, offset: Int) = BigEndian.Long_.toBytes(this, bytes, offset)
fun Long.toBytesBE(bytes: ByteArray) = BigEndian.Long_.toBytes(this, bytes)

fun ByteArray.toLongBE(offset: Int, byteNumber: Int) = BigEndian.Long_.from(this, offset, byteNumber)
fun ByteArray.toLongBE() = BigEndian.Long_.from(this)
fun ByteBuffer.toLongBE() = BigEndian.Long_.from(this)
fun InputStream.toLongBE(length: Int = 8) = BigEndian.Long_.from(this, length)
fun RandomAccessFile.toLongBE(length: Int = 8) = BigEndian.Long_.from(this, length)


fun ULong.toBytesBE() = BigEndian.ULong_.toBytes(this)
fun ULong.toBytesBE(bytes: ByteArray, offset: Int) = BigEndian.ULong_.toBytes(this, bytes, offset)
fun ULong.toBytesBE(bytes: ByteArray) = BigEndian.ULong_.toBytes(this, bytes)

fun ByteArray.toULongBE(offset: Int, byteNumber: Int) = BigEndian.ULong_.from(this, offset, byteNumber)
fun ByteArray.toULongBE() = BigEndian.ULong_.from(this)
fun ByteBuffer.toULongBE() = BigEndian.ULong_.from(this)
fun InputStream.toULongBE(length: Int = 8) = BigEndian.ULong_.from(this, length)
fun RandomAccessFile.toULongBE(length: Int = 8) = BigEndian.ULong_.from(this, length)


/**
 * This is "motorola endian", or commonly as "network byte order".
 *
 *
 * This is also the default for Java.
 *
 *
 * arm is technically bi-endian
 */
object BigEndian {
    // the following are ALL in Bit-Endian (byte[0] is MOST significant)

    /**
     * Gets the version number.
     */
    const val version = BytesInfo.version

    // NOTE: CHAR and SHORT are the exact same.

    /**
     * CHAR to and from bytes
     */
    object Char_ {
        fun from(bytes: ByteArray, offset: Int, byteNumber: Int): Char {
            var number: Short = 0
            when (byteNumber) {
                2 -> {
                    number = (number.toInt() or (bytes[offset + 0].toInt() and 0xFF shl 8)).toShort()
                    number = (number.toInt() or (bytes[offset + 1].toInt() and 0xFF shl 0)).toShort()
                }

                else -> number = (number.toInt() or (bytes[offset + 0].toInt() and 0xFF shl 0)).toShort()
            }
            return number.toInt().toChar()
        }

        fun from(bytes: ByteArray): Char {
            var number: Short = 0
            when (bytes.size) {
                2 -> {
                    number = (number.toInt() or (bytes[0].toInt() and 0xFF shl 8)).toShort()
                    number = (number.toInt() or (bytes[1].toInt() and 0xFF shl 0)).toShort()
                }

                else -> number = (number.toInt() or (bytes[0].toInt() and 0xFF shl 0)).toShort()
            }
            return number.toInt().toChar()
        }

        fun from(b0: Byte, b1: Byte): Char {
            return ((b0.toInt() and 0xFF shl 8) or
                    (b1.toInt() and 0xFF shl 0)).toChar()
        }

        fun from(buff: ByteBuffer): Char {
            return from(buff.get(), buff.get())
        }

        @Throws(IOException::class)
        fun from(inputStream: InputStream, length: Int = 2): Char {
            var number: Short = 0
            when (length) {
                2 -> {
                    number = (number.toInt() or (inputStream.read() and 0xFF shl 8)).toShort()
                    number = (number.toInt() or (inputStream.read() and 0xFF shl 0)).toShort()
                }

                else -> number = (number.toInt() or (inputStream.read() and 0xFF shl 0)).toShort()
            }
            return number.toInt().toChar()
        }

        @Throws(IOException::class)
        fun from(raf: RandomAccessFile, length: Int = 2): Char {
            var number: Short = 0
            when (length) {
                2 -> {
                    number = (number.toInt() or (raf.read() and 0xFF shl 8)).toShort()
                    number = (number.toInt() or (raf.read() and 0xFF shl 0)).toShort()
                }

                else -> number = (number.toInt() or (raf.read() and 0xFF shl 0)).toShort()
            }
            return number.toInt().toChar()
        }

        fun toBytes(x: Char): ByteArray {
            val i = x.code
            return byteArrayOf((i shr 8).toByte(), (i shr 0).toByte())
        }

        fun toBytes(x: Char, bytes: ByteArray, offset: Int) {
            val i = x.code
            bytes[offset + 0] = (i shr 8).toByte()
            bytes[offset + 1] = (i shr 0).toByte()
        }

        fun toBytes(x: Char, bytes: ByteArray) {
            val i = x.code
            bytes[0] = (i shr 8).toByte()
            bytes[1] = (i shr 0).toByte()
        }
    }

    /**
     * SHORT to and from bytes
     */
    object Short_ {
        fun from(bytes: ByteArray, offset: Int, byteNumber: Int): Short {
            var number: Short = 0
            when (byteNumber) {
                2 -> {
                    number = (number.toInt() or (bytes[offset + 0].toInt() and 0xFF shl 8)).toShort()
                    number = (number.toInt() or (bytes[offset + 1].toInt() and 0xFF shl 0)).toShort()
                }

                else -> number = (number.toInt() or (bytes[offset + 0].toInt() and 0xFF shl 0)).toShort()
            }
            return number
        }

        fun from(bytes: ByteArray): Short {
            var number: Short = 0
            when (bytes.size) {
                2 -> {
                    number = (number.toInt() or (bytes[0].toInt() and 0xFF shl 8)).toShort()
                    number = (number.toInt() or (bytes[1].toInt() and 0xFF shl 0)).toShort()
                }

                else -> number = (number.toInt() or (bytes[0].toInt() and 0xFF shl 0)).toShort()
            }
            return number
        }

        fun from(b0: Byte, b1: Byte): Short {
            return ((b0.toInt() and 0xFF shl 8) or
                    (b1.toInt() and 0xFF shl 0)).toShort()
        }

        fun from(buff: ByteBuffer): Short {
            return from(buff.get(), buff.get())
        }

        @Throws(IOException::class)
        fun from(inputStream: InputStream, length: Int = 2): Short {
            var number: Short = 0
            when (length) {
                2 -> {
                    number = (number.toInt() or (inputStream.read() and 0xFF shl 8)).toShort()
                    number = (number.toInt() or (inputStream.read() and 0xFF shl 0)).toShort()
                }

                else -> number = (number.toInt() or (inputStream.read() and 0xFF shl 0)).toShort()
            }
            return number
        }

        @Throws(IOException::class)
        fun from(raf: RandomAccessFile, length: Int = 2): Short {
            var number: Short = 0
            when (length) {
                2 -> {
                    number = (number.toInt() or (raf.read() and 0xFF shl 8)).toShort()
                    number = (number.toInt() or (raf.read() and 0xFF shl 0)).toShort()
                }

                else -> number = (number.toInt() or (raf.read() and 0xFF shl 0)).toShort()
            }
            return number
        }

        fun toBytes(x: Short): ByteArray {
            val i = x.toInt()
            return byteArrayOf((i shr 8).toByte(), (i shr 0).toByte())
        }

        fun toBytes(x: Short, bytes: ByteArray, offset: Int) {
            val i = x.toInt()
            bytes[offset + 0] = (i shr 8).toByte()
            bytes[offset + 1] = (i shr 0).toByte()
        }

        fun toBytes(x: Short, bytes: ByteArray) {
            val i = x.toInt()
            bytes[0] = (i shr 8).toByte()
            bytes[1] = (i shr 0).toByte()
        }
    }

     /**
      * UNSIGNED SHORT to and from bytes
      */
    object UShort_ {
         fun from(bytes: ByteArray, offset: Int, byteNumber: Int): UShort {
             var number: Short = 0
             when (byteNumber) {
                 2 -> {
                     number = (number.toInt() or (bytes[offset + 0].toInt() and 0xFF shl 8)).toShort()
                     number = (number.toInt() or (bytes[offset + 1].toInt() and 0xFF shl 0)).toShort()
                 }

                 else -> number = (number.toInt() or (bytes[offset + 0].toInt() and 0xFF shl 0)).toShort()
             }
             return number.toUShort()
         }

         fun from(bytes: ByteArray): UShort {
             var number: Short = 0
             when (bytes.size) {
                 2 -> {
                     number = (number.toInt() or (bytes[0].toInt() and 0xFF shl 8)).toShort()
                     number = (number.toInt() or (bytes[1].toInt() and 0xFF shl 0)).toShort()
                 }

                 else -> number = (number.toInt() or (bytes[0].toInt() and 0xFF shl 0)).toShort()
             }
             return number.toUShort()
         }

         fun from(b0: Byte, b1: Byte): UShort {
             return ((b0.toInt() and 0xFF shl 8) or
                     (b1.toInt() and 0xFF shl 0)).toUShort()
         }

         fun from(buff: ByteBuffer): UShort {
             return from(buff.get(), buff.get())
         }

         @Throws(IOException::class)
         fun from(inputStream: InputStream, length: Int = 2): UShort {
             var number: Short = 0
             when (length) {
                 2 -> {
                     number = (number.toInt() or (inputStream.read() and 0xFF shl 8)).toShort()
                     number = (number.toInt() or (inputStream.read() and 0xFF shl 0)).toShort()
                 }

                 else -> number = (number.toInt() or (inputStream.read() and 0xFF shl 0)).toShort()
             }
             return number.toUShort()
         }

         @Throws(IOException::class)
         fun from(raf: RandomAccessFile, length: Int = 2): UShort {
             var number: Short = 0
             when (length) {
                 2 -> {
                     number = (number.toInt() or (raf.read() and 0xFF shl 8)).toShort()
                     number = (number.toInt() or (raf.read() and 0xFF shl 0)).toShort()
                 }

                 else -> number = (number.toInt() or (raf.read() and 0xFF shl 0)).toShort()
             }
             return number.toUShort()
         }

         fun toBytes(x: UShort): ByteArray {
             return byteArrayOf((x.toInt() shr 8).toByte(), (x.toInt() shr 0).toByte())
         }

         fun toBytes(x: UShort, bytes: ByteArray, offset: Int) {
             bytes[offset + 0] = (x.toInt() shr 8).toByte()
             bytes[offset + 1] = (x.toInt() shr 0).toByte()
         }

         fun toBytes(x: UShort, bytes: ByteArray) {
             bytes[0] = (x.toInt() shr 8).toByte()
             bytes[1] = (x.toInt() shr 0).toByte()
         }
    }

    /**
     * INT to and from bytes
     */
    object Int_ {
        fun from(bytes: ByteArray, offset: Int, byteNumber: Int): Int {
            var number = 0
            when (byteNumber) {
                4 -> {
                    number = number or (bytes[offset + 0].toInt() and 0xFF shl 24)
                    number = number or (bytes[offset + 1].toInt() and 0xFF shl 16)
                    number = number or (bytes[offset + 2].toInt() and 0xFF shl 8)
                    number = number or (bytes[offset + 3].toInt() and 0xFF shl 0)
                }

                3 -> {
                    number = number or (bytes[offset + 0].toInt() and 0xFF shl 16)
                    number = number or (bytes[offset + 1].toInt() and 0xFF shl 8)
                    number = number or (bytes[offset + 2].toInt() and 0xFF shl 0)
                }

                2 -> {
                    number = number or (bytes[offset + 0].toInt() and 0xFF shl 8)
                    number = number or (bytes[offset + 1].toInt() and 0xFF shl 0)
                }

                else -> number = number or (bytes[offset + 0].toInt() and 0xFF shl 0)
            }
            return number
        }

        fun from(bytes: ByteArray): Int {
            var number = 0
            when (bytes.size) {
                4 -> {
                    number = number or (bytes[0].toInt() and 0xFF shl 24)
                    number = number or (bytes[1].toInt() and 0xFF shl 16)
                    number = number or (bytes[2].toInt() and 0xFF shl 8)
                    number = number or (bytes[3].toInt() and 0xFF shl 0)
                }

                3 -> {
                    number = number or (bytes[0].toInt() and 0xFF shl 16)
                    number = number or (bytes[1].toInt() and 0xFF shl 8)
                    number = number or (bytes[2].toInt() and 0xFF shl 0)
                }

                2 -> {
                    number = number or (bytes[0].toInt() and 0xFF shl 8)
                    number = number or (bytes[1].toInt() and 0xFF shl 0)
                }

                else -> number = number or (bytes[0].toInt() and 0xFF shl 0)
            }
            return number
        }

        fun from(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {
            return (b0.toInt() and 0xFF shl 24) or
                   (b1.toInt() and 0xFF shl 16) or
                   (b2.toInt() and 0xFF shl 8) or
                   (b3.toInt() and 0xFF shl 0)
        }

        fun from(buff: ByteBuffer): Int {
            return from(buff.get(), buff.get(), buff.get(), buff.get())
        }

        @Throws(IOException::class)
        fun from(inputStream: InputStream, length: Int = 2): Int {
            var number = 0
            when (length) {
                4 -> {
                    number = number or (inputStream.read() and 0xFF shl 24)
                    number = number or (inputStream.read() and 0xFF shl 16)
                    number = number or (inputStream.read() and 0xFF shl 8)
                    number = number or (inputStream.read() and 0xFF shl 0)
                }

                3 -> {
                    number = number or (inputStream.read() and 0xFF shl 16)
                    number = number or (inputStream.read() and 0xFF shl 8)
                    number = number or (inputStream.read() and 0xFF shl 0)
                }

                2 -> {
                    number = number or (inputStream.read() and 0xFF shl 8)
                    number = number or (inputStream.read() and 0xFF shl 0)
                }

                else -> number = number or (inputStream.read() and 0xFF shl 0)
            }
            return number
        }

        @Throws(IOException::class)
        fun from(raf: RandomAccessFile, length: Int = 2): Int {
            var number = 0
            when (length) {
                4 -> {
                    number = number or (raf.read() and 0xFF shl 24)
                    number = number or (raf.read() and 0xFF shl 16)
                    number = number or (raf.read() and 0xFF shl 8)
                    number = number or (raf.read() and 0xFF shl 0)
                }

                3 -> {
                    number = number or (raf.read() and 0xFF shl 16)
                    number = number or (raf.read() and 0xFF shl 8)
                    number = number or (raf.read() and 0xFF shl 0)
                }

                2 -> {
                    number = number or (raf.read() and 0xFF shl 8)
                    number = number or (raf.read() and 0xFF shl 0)
                }

                else -> number = number or (raf.read() and 0xFF shl 0)
            }
            return number
        }

        fun toBytes(x: Int): ByteArray {
            return byteArrayOf((x shr 24).toByte(), (x shr 16).toByte(), (x shr 8).toByte(), (x shr 0).toByte())
        }

        fun toBytes(x: Int, bytes: ByteArray, offset: Int) {
            bytes[offset + 0] = (x shr 24).toByte()
            bytes[offset + 1] = (x shr 16).toByte()
            bytes[offset + 2] = (x shr 8).toByte()
            bytes[offset + 3] = (x shr 0).toByte()
        }

        fun toBytes(x: Int, bytes: ByteArray) {
            bytes[0] = (x shr 24).toByte()
            bytes[1] = (x shr 16).toByte()
            bytes[2] = (x shr 8).toByte()
            bytes[3] = (x shr 0).toByte()
        }
    }

     /**
      * UNSIGNED INT to and from bytes
      */
    object UInt_ {
         fun from(bytes: ByteArray, offset: Int, byteNumber: Int): UInt {
             var number = 0
             when (byteNumber) {
                 4 -> {
                     number = number or (bytes[offset + 0].toInt() and 0xFF shl 24)
                     number = number or (bytes[offset + 1].toInt() and 0xFF shl 16)
                     number = number or (bytes[offset + 2].toInt() and 0xFF shl 8)
                     number = number or (bytes[offset + 3].toInt() and 0xFF shl 0)
                 }

                 3 -> {
                     number = number or (bytes[offset + 0].toInt() and 0xFF shl 16)
                     number = number or (bytes[offset + 1].toInt() and 0xFF shl 8)
                     number = number or (bytes[offset + 2].toInt() and 0xFF shl 0)
                 }

                 2 -> {
                     number = number or (bytes[offset + 0].toInt() and 0xFF shl 8)
                     number = number or (bytes[offset + 1].toInt() and 0xFF shl 0)
                 }

                 else -> number = number or (bytes[offset + 0].toInt() and 0xFF shl 0)
             }
             return number.toUInt()
         }

         fun from(bytes: ByteArray): UInt {
             var number = 0
             when (bytes.size) {
                 4 -> {
                     number = number or (bytes[0].toInt() and 0xFF shl 24)
                     number = number or (bytes[1].toInt() and 0xFF shl 16)
                     number = number or (bytes[2].toInt() and 0xFF shl 8)
                     number = number or (bytes[3].toInt() and 0xFF shl 0)
                 }

                 3 -> {
                     number = number or (bytes[0].toInt() and 0xFF shl 16)
                     number = number or (bytes[1].toInt() and 0xFF shl 8)
                     number = number or (bytes[2].toInt() and 0xFF shl 0)
                 }

                 2 -> {
                     number = number or (bytes[0].toInt() and 0xFF shl 8)
                     number = number or (bytes[1].toInt() and 0xFF shl 0)
                 }

                 else -> number = number or (bytes[0].toInt() and 0xFF shl 0)
             }
             return number.toUInt()
         }

         fun from(b0: Byte, b1: Byte, b2: Byte, b3: Byte): UInt {
             return ((b0.toInt() and 0xFF shl 24) or
                     (b1.toInt() and 0xFF shl 16) or
                     (b2.toInt() and 0xFF shl 8) or
                     (b3.toInt() and 0xFF shl 0)).toUInt()
         }

         fun from(buff: ByteBuffer): UInt {
             return from(buff.get(), buff.get(), buff.get(), buff.get())
         }

         @Throws(IOException::class)
         fun from(inputStream: InputStream, length: Int = 4): UInt {
             var number = 0
             when (length) {
                 4 -> {
                     number = number or (inputStream.read() and 0xFF shl 24)
                     number = number or (inputStream.read() and 0xFF shl 16)
                     number = number or (inputStream.read() and 0xFF shl 8)
                     number = number or (inputStream.read() and 0xFF shl 0)
                 }

                 3 -> {
                     number = number or (inputStream.read() and 0xFF shl 16)
                     number = number or (inputStream.read() and 0xFF shl 8)
                     number = number or (inputStream.read() and 0xFF shl 0)
                 }

                 2 -> {
                     number = number or (inputStream.read() and 0xFF shl 8)
                     number = number or (inputStream.read() and 0xFF shl 0)
                 }

                 else -> number = number or (inputStream.read() and 0xFF shl 0)
             }
             return number.toUInt()
         }

         @Throws(IOException::class)
         fun from(raf: RandomAccessFile, length: Int = 4): UInt {
             var number = 0
             when (length) {
                 4 -> {
                     number = number or (raf.read() and 0xFF shl 24)
                     number = number or (raf.read() and 0xFF shl 16)
                     number = number or (raf.read() and 0xFF shl 8)
                     number = number or (raf.read() and 0xFF shl 0)
                 }

                 3 -> {
                     number = number or (raf.read() and 0xFF shl 16)
                     number = number or (raf.read() and 0xFF shl 8)
                     number = number or (raf.read() and 0xFF shl 0)
                 }

                 2 -> {
                     number = number or (raf.read() and 0xFF shl 8)
                     number = number or (raf.read() and 0xFF shl 0)
                 }

                 else -> number = number or (raf.read() and 0xFF shl 0)
             }
             return number.toUInt()
         }

         fun toBytes(x: UInt): ByteArray {
             return byteArrayOf((x shr 24).toByte(), (x shr 16).toByte(), (x shr 8).toByte(), (x shr 0).toByte())
         }

         fun toBytes(x: UInt, bytes: ByteArray, offset: Int) {
             bytes[offset + 0] = (x shr 24).toByte()
             bytes[offset + 1] = (x shr 16).toByte()
             bytes[offset + 2] = (x shr 8).toByte()
             bytes[offset + 3] = (x shr 0).toByte()
         }

         fun toBytes(x: UInt, bytes: ByteArray) {
             bytes[0] = (x shr 24).toByte()
             bytes[1] = (x shr 16).toByte()
             bytes[2] = (x shr 8).toByte()
             bytes[3] = (x shr 0).toByte()
         }
    }

    /**
     * LONG to and from bytes
     */
    object Long_ {
        fun from(bytes: ByteArray, offset: Int, byteNumber: Int): Long {
            var number: Long = 0
            when (byteNumber) {
                8 -> {
                    number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 56)
                    number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 48)
                    number = number or ((bytes[offset + 2].toInt() and 0xFF).toLong() shl 40)
                    number = number or ((bytes[offset + 3].toInt() and 0xFF).toLong() shl 32)
                    number = number or ((bytes[offset + 4].toInt() and 0xFF).toLong() shl 24)
                    number = number or ((bytes[offset + 5].toInt() and 0xFF).toLong() shl 16)
                    number = number or ((bytes[offset + 6].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[offset + 7].toInt() and 0xFF).toLong() shl 0)
                }

                7 -> {
                    number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 48)
                    number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 40)
                    number = number or ((bytes[offset + 2].toInt() and 0xFF).toLong() shl 32)
                    number = number or ((bytes[offset + 3].toInt() and 0xFF).toLong() shl 24)
                    number = number or ((bytes[offset + 4].toInt() and 0xFF).toLong() shl 16)
                    number = number or ((bytes[offset + 5].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[offset + 6].toInt() and 0xFF).toLong() shl 0)
                }

                6 -> {
                    number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 40)
                    number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 32)
                    number = number or ((bytes[offset + 2].toInt() and 0xFF).toLong() shl 24)
                    number = number or ((bytes[offset + 3].toInt() and 0xFF).toLong() shl 16)
                    number = number or ((bytes[offset + 4].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[offset + 5].toInt() and 0xFF).toLong() shl 0)
                }

                5 -> {
                    number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 32)
                    number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 24)
                    number = number or ((bytes[offset + 2].toInt() and 0xFF).toLong() shl 16)
                    number = number or ((bytes[offset + 3].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[offset + 4].toInt() and 0xFF).toLong() shl 0)
                }

                4 -> {
                    number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 24)
                    number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 16)
                    number = number or ((bytes[offset + 2].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[offset + 3].toInt() and 0xFF).toLong() shl 0)
                }

                3 -> {
                    number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 16)
                    number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[offset + 2].toInt() and 0xFF).toLong() shl 0)
                }

                2 -> {
                    number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 0)
                }

                else -> number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 0)
            }
            return number
        }

        fun from(bytes: ByteArray): Long {
            var number = 0L
            when (bytes.size) {
                8 -> {
                    number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 56)
                    number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 48)
                    number = number or ((bytes[2].toInt() and 0xFF).toLong() shl 40)
                    number = number or ((bytes[3].toInt() and 0xFF).toLong() shl 32)
                    number = number or ((bytes[4].toInt() and 0xFF).toLong() shl 24)
                    number = number or ((bytes[5].toInt() and 0xFF).toLong() shl 16)
                    number = number or ((bytes[6].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[7].toInt() and 0xFF).toLong() shl 0)
                }

                7 -> {
                    number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 48)
                    number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 40)
                    number = number or ((bytes[2].toInt() and 0xFF).toLong() shl 32)
                    number = number or ((bytes[3].toInt() and 0xFF).toLong() shl 24)
                    number = number or ((bytes[4].toInt() and 0xFF).toLong() shl 16)
                    number = number or ((bytes[5].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[6].toInt() and 0xFF).toLong() shl 0)
                }

                6 -> {
                    number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 40)
                    number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 32)
                    number = number or ((bytes[2].toInt() and 0xFF).toLong() shl 24)
                    number = number or ((bytes[3].toInt() and 0xFF).toLong() shl 16)
                    number = number or ((bytes[4].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[5].toInt() and 0xFF).toLong() shl 0)
                }

                5 -> {
                    number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 32)
                    number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 24)
                    number = number or ((bytes[2].toInt() and 0xFF).toLong() shl 16)
                    number = number or ((bytes[3].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[4].toInt() and 0xFF).toLong() shl 0)
                }

                4 -> {
                    number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 24)
                    number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 16)
                    number = number or ((bytes[2].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[3].toInt() and 0xFF).toLong() shl 0)
                }

                3 -> {
                    number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 16)
                    number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[2].toInt() and 0xFF).toLong() shl 0)
                }

                2 -> {
                    number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 8)
                    number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 0)
                }

                else -> number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 0)
            }
            return number
        }

        fun from(b0: Byte, b1: Byte, b2: Byte, b3: Byte, b4: Byte, b5: Byte, b6: Byte, b7: Byte): Long {
            return  ((b0.toInt() and 0xFF).toLong() shl 56) or
                    ((b1.toInt() and 0xFF).toLong() shl 48) or
                    ((b2.toInt() and 0xFF).toLong() shl 40) or
                    ((b3.toInt() and 0xFF).toLong() shl 32) or
                    ((b4.toInt() and 0xFF).toLong() shl 24) or
                    ((b5.toInt() and 0xFF).toLong() shl 16) or
                    ((b6.toInt() and 0xFF).toLong() shl 8) or
                    ((b7.toInt() and 0xFF).toLong() shl 0)
        }

        fun from(buff: ByteBuffer): Long {
            return from(buff.get(), buff.get(), buff.get(), buff.get(), buff.get(), buff.get(), buff.get(), buff.get())
        }

        @Throws(IOException::class)
        fun from(inputStream: InputStream, length: Int = 8): Long {
            var number = 0L
            when (length) {
                8 -> {
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 56)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 48)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 40)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 32)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 24)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 16)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                }

                7 -> {
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 48)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 40)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 32)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 24)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 16)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                }

                6 -> {
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 40)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 32)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 24)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 16)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                }

                5 -> {
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 32)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 24)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 16)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                }

                4 -> {
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 24)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 16)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                }

                3 -> {
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 16)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                }

                2 -> {
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                    number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                }

                else -> number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
            }
            return number
        }

        @Throws(IOException::class)
        fun from(raf: RandomAccessFile, length: Int = 8): Long {
            var number = 0L
            when (length) {
                8 -> {
                    number = number or ((raf.read() and 0xFF).toLong() shl 56)
                    number = number or ((raf.read() and 0xFF).toLong() shl 48)
                    number = number or ((raf.read() and 0xFF).toLong() shl 40)
                    number = number or ((raf.read() and 0xFF).toLong() shl 32)
                    number = number or ((raf.read() and 0xFF).toLong() shl 24)
                    number = number or ((raf.read() and 0xFF).toLong() shl 16)
                    number = number or ((raf.read() and 0xFF).toLong() shl 8)
                    number = number or ((raf.read() and 0xFF).toLong() shl 0)
                }

                7 -> {
                    number = number or ((raf.read() and 0xFF).toLong() shl 48)
                    number = number or ((raf.read() and 0xFF).toLong() shl 40)
                    number = number or ((raf.read() and 0xFF).toLong() shl 32)
                    number = number or ((raf.read() and 0xFF).toLong() shl 24)
                    number = number or ((raf.read() and 0xFF).toLong() shl 16)
                    number = number or ((raf.read() and 0xFF).toLong() shl 8)
                    number = number or ((raf.read() and 0xFF).toLong() shl 0)
                }

                6 -> {
                    number = number or ((raf.read() and 0xFF).toLong() shl 40)
                    number = number or ((raf.read() and 0xFF).toLong() shl 32)
                    number = number or ((raf.read() and 0xFF).toLong() shl 24)
                    number = number or ((raf.read() and 0xFF).toLong() shl 16)
                    number = number or ((raf.read() and 0xFF).toLong() shl 8)
                    number = number or ((raf.read() and 0xFF).toLong() shl 0)
                }

                5 -> {
                    number = number or ((raf.read() and 0xFF).toLong() shl 32)
                    number = number or ((raf.read() and 0xFF).toLong() shl 24)
                    number = number or ((raf.read() and 0xFF).toLong() shl 16)
                    number = number or ((raf.read() and 0xFF).toLong() shl 8)
                    number = number or ((raf.read() and 0xFF).toLong() shl 0)
                }

                4 -> {
                    number = number or ((raf.read() and 0xFF).toLong() shl 24)
                    number = number or ((raf.read() and 0xFF).toLong() shl 16)
                    number = number or ((raf.read() and 0xFF).toLong() shl 8)
                    number = number or ((raf.read() and 0xFF).toLong() shl 0)
                }

                3 -> {
                    number = number or ((raf.read() and 0xFF).toLong() shl 16)
                    number = number or ((raf.read() and 0xFF).toLong() shl 8)
                    number = number or ((raf.read() and 0xFF).toLong() shl 0)
                }

                2 -> {
                    number = number or ((raf.read() and 0xFF).toLong() shl 8)
                    number = number or ((raf.read() and 0xFF).toLong() shl 0)
                }

                else -> number = number or ((raf.read() and 0xFF).toLong() shl 0)
            }
            return number
        }

        fun toBytes(x: Long): ByteArray {
            return byteArrayOf(
                (x shr 56).toByte(),
                (x shr 48).toByte(),
                (x shr 40).toByte(),
                (x shr 32).toByte(),
                (x shr 24).toByte(),
                (x shr 16).toByte(),
                (x shr 8).toByte(),
                (x shr 0).toByte()
            )
        }

        fun toBytes(x: Long, bytes: ByteArray, offset: Int) {
            bytes[offset + 0] = (x shr 56).toByte()
            bytes[offset + 1] = (x shr 48).toByte()
            bytes[offset + 2] = (x shr 40).toByte()
            bytes[offset + 3] = (x shr 32).toByte()
            bytes[offset + 4] = (x shr 24).toByte()
            bytes[offset + 5] = (x shr 16).toByte()
            bytes[offset + 6] = (x shr 8).toByte()
            bytes[offset + 7] = (x shr 0).toByte()
        }

        fun toBytes(x: Long, bytes: ByteArray) {
            bytes[0] = (x shr 56).toByte()
            bytes[1] = (x shr 48).toByte()
            bytes[2] = (x shr 40).toByte()
            bytes[3] = (x shr 32).toByte()
            bytes[4] = (x shr 24).toByte()
            bytes[5] = (x shr 16).toByte()
            bytes[6] = (x shr 8).toByte()
            bytes[7] = (x shr 0).toByte()
        }
    }

     /**
      * UNSIGNED LONG to and from bytes
      */
    object ULong_ {
         fun from(bytes: ByteArray, offset: Int, byteNumber: Int): ULong {
             var number: Long = 0
             when (byteNumber) {
                 8 -> {
                     number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 56)
                     number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 48)
                     number = number or ((bytes[offset + 2].toInt() and 0xFF).toLong() shl 40)
                     number = number or ((bytes[offset + 3].toInt() and 0xFF).toLong() shl 32)
                     number = number or ((bytes[offset + 4].toInt() and 0xFF).toLong() shl 24)
                     number = number or ((bytes[offset + 5].toInt() and 0xFF).toLong() shl 16)
                     number = number or ((bytes[offset + 6].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[offset + 7].toInt() and 0xFF).toLong() shl 0)
                 }

                 7 -> {
                     number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 48)
                     number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 40)
                     number = number or ((bytes[offset + 2].toInt() and 0xFF).toLong() shl 32)
                     number = number or ((bytes[offset + 3].toInt() and 0xFF).toLong() shl 24)
                     number = number or ((bytes[offset + 4].toInt() and 0xFF).toLong() shl 16)
                     number = number or ((bytes[offset + 5].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[offset + 6].toInt() and 0xFF).toLong() shl 0)
                 }

                 6 -> {
                     number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 40)
                     number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 32)
                     number = number or ((bytes[offset + 2].toInt() and 0xFF).toLong() shl 24)
                     number = number or ((bytes[offset + 3].toInt() and 0xFF).toLong() shl 16)
                     number = number or ((bytes[offset + 4].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[offset + 5].toInt() and 0xFF).toLong() shl 0)
                 }

                 5 -> {
                     number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 32)
                     number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 24)
                     number = number or ((bytes[offset + 2].toInt() and 0xFF).toLong() shl 16)
                     number = number or ((bytes[offset + 3].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[offset + 4].toInt() and 0xFF).toLong() shl 0)
                 }

                 4 -> {
                     number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 24)
                     number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 16)
                     number = number or ((bytes[offset + 2].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[offset + 3].toInt() and 0xFF).toLong() shl 0)
                 }

                 3 -> {
                     number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 16)
                     number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[offset + 2].toInt() and 0xFF).toLong() shl 0)
                 }

                 2 -> {
                     number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[offset + 1].toInt() and 0xFF).toLong() shl 0)
                 }

                 else -> number = number or ((bytes[offset + 0].toInt() and 0xFF).toLong() shl 0)
             }
             return number.toULong()
         }

         fun from(bytes: ByteArray): ULong {
             var number = 0L
             when (bytes.size) {
                 8 -> {
                     number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 56)
                     number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 48)
                     number = number or ((bytes[2].toInt() and 0xFF).toLong() shl 40)
                     number = number or ((bytes[3].toInt() and 0xFF).toLong() shl 32)
                     number = number or ((bytes[4].toInt() and 0xFF).toLong() shl 24)
                     number = number or ((bytes[5].toInt() and 0xFF).toLong() shl 16)
                     number = number or ((bytes[6].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[7].toInt() and 0xFF).toLong() shl 0)
                 }

                 7 -> {
                     number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 48)
                     number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 40)
                     number = number or ((bytes[2].toInt() and 0xFF).toLong() shl 32)
                     number = number or ((bytes[3].toInt() and 0xFF).toLong() shl 24)
                     number = number or ((bytes[4].toInt() and 0xFF).toLong() shl 16)
                     number = number or ((bytes[5].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[6].toInt() and 0xFF).toLong() shl 0)
                 }

                 6 -> {
                     number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 40)
                     number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 32)
                     number = number or ((bytes[2].toInt() and 0xFF).toLong() shl 24)
                     number = number or ((bytes[3].toInt() and 0xFF).toLong() shl 16)
                     number = number or ((bytes[4].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[5].toInt() and 0xFF).toLong() shl 0)
                 }

                 5 -> {
                     number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 32)
                     number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 24)
                     number = number or ((bytes[2].toInt() and 0xFF).toLong() shl 16)
                     number = number or ((bytes[3].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[4].toInt() and 0xFF).toLong() shl 0)
                 }

                 4 -> {
                     number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 24)
                     number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 16)
                     number = number or ((bytes[2].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[3].toInt() and 0xFF).toLong() shl 0)
                 }

                 3 -> {
                     number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 16)
                     number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[2].toInt() and 0xFF).toLong() shl 0)
                 }

                 2 -> {
                     number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 8)
                     number = number or ((bytes[1].toInt() and 0xFF).toLong() shl 0)
                 }

                 else -> number = number or ((bytes[0].toInt() and 0xFF).toLong() shl 0)
             }
             return number.toULong()
         }

         fun from(b0: Byte, b1: Byte, b2: Byte, b3: Byte, b4: Byte, b5: Byte, b6: Byte, b7: Byte): ULong {
             return (((b0.toInt() and 0xFF).toLong() shl 56) or
                     ((b1.toInt() and 0xFF).toLong() shl 48) or
                     ((b2.toInt() and 0xFF).toLong() shl 40) or
                     ((b3.toInt() and 0xFF).toLong() shl 32) or
                     ((b4.toInt() and 0xFF).toLong() shl 24) or
                     ((b5.toInt() and 0xFF).toLong() shl 16) or
                     ((b6.toInt() and 0xFF).toLong() shl 8) or
                     ((b7.toInt() and 0xFF).toLong() shl 0)).toULong()
         }

         fun from(buff: ByteBuffer): ULong {
             return from(buff.get(), buff.get(), buff.get(), buff.get(), buff.get(), buff.get(), buff.get(), buff.get())
         }

         @Throws(IOException::class)
         fun from(inputStream: InputStream, length: Int = 8): ULong {
             var number = 0L
             when (length) {
                 8 -> {
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 56)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 48)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 40)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 32)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 24)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 16)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                 }

                 7 -> {
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 48)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 40)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 32)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 24)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 16)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                 }

                 6 -> {
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 40)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 32)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 24)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 16)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                 }

                 5 -> {
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 32)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 24)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 16)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                 }

                 4 -> {
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 24)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 16)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                 }

                 3 -> {
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 16)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                 }

                 2 -> {
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 8)
                     number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
                 }

                 else -> number = number or ((inputStream.read() and 0xFF).toLong() shl 0)
             }
             return number.toULong()
         }

         @Throws(IOException::class)
         fun from(raf: RandomAccessFile, length: Int = 8): ULong {
             var number = 0L
             when (length) {
                 8 -> {
                     number = number or ((raf.read() and 0xFF).toLong() shl 56)
                     number = number or ((raf.read() and 0xFF).toLong() shl 48)
                     number = number or ((raf.read() and 0xFF).toLong() shl 40)
                     number = number or ((raf.read() and 0xFF).toLong() shl 32)
                     number = number or ((raf.read() and 0xFF).toLong() shl 24)
                     number = number or ((raf.read() and 0xFF).toLong() shl 16)
                     number = number or ((raf.read() and 0xFF).toLong() shl 8)
                     number = number or ((raf.read() and 0xFF).toLong() shl 0)
                 }

                 7 -> {
                     number = number or ((raf.read() and 0xFF).toLong() shl 48)
                     number = number or ((raf.read() and 0xFF).toLong() shl 40)
                     number = number or ((raf.read() and 0xFF).toLong() shl 32)
                     number = number or ((raf.read() and 0xFF).toLong() shl 24)
                     number = number or ((raf.read() and 0xFF).toLong() shl 16)
                     number = number or ((raf.read() and 0xFF).toLong() shl 8)
                     number = number or ((raf.read() and 0xFF).toLong() shl 0)
                 }

                 6 -> {
                     number = number or ((raf.read() and 0xFF).toLong() shl 40)
                     number = number or ((raf.read() and 0xFF).toLong() shl 32)
                     number = number or ((raf.read() and 0xFF).toLong() shl 24)
                     number = number or ((raf.read() and 0xFF).toLong() shl 16)
                     number = number or ((raf.read() and 0xFF).toLong() shl 8)
                     number = number or ((raf.read() and 0xFF).toLong() shl 0)
                 }

                 5 -> {
                     number = number or ((raf.read() and 0xFF).toLong() shl 32)
                     number = number or ((raf.read() and 0xFF).toLong() shl 24)
                     number = number or ((raf.read() and 0xFF).toLong() shl 16)
                     number = number or ((raf.read() and 0xFF).toLong() shl 8)
                     number = number or ((raf.read() and 0xFF).toLong() shl 0)
                 }

                 4 -> {
                     number = number or ((raf.read() and 0xFF).toLong() shl 24)
                     number = number or ((raf.read() and 0xFF).toLong() shl 16)
                     number = number or ((raf.read() and 0xFF).toLong() shl 8)
                     number = number or ((raf.read() and 0xFF).toLong() shl 0)
                 }

                 3 -> {
                     number = number or ((raf.read() and 0xFF).toLong() shl 16)
                     number = number or ((raf.read() and 0xFF).toLong() shl 8)
                     number = number or ((raf.read() and 0xFF).toLong() shl 0)
                 }

                 2 -> {
                     number = number or ((raf.read() and 0xFF).toLong() shl 8)
                     number = number or ((raf.read() and 0xFF).toLong() shl 0)
                 }

                 else -> number = number or ((raf.read() and 0xFF).toLong() shl 0)
             }
             return number.toULong()
         }

         fun toBytes(x: ULong): ByteArray {
             return byteArrayOf(
                 (x shr 56).toByte(),
                 (x shr 48).toByte(),
                 (x shr 40).toByte(),
                 (x shr 32).toByte(),
                 (x shr 24).toByte(),
                 (x shr 16).toByte(),
                 (x shr 8).toByte(),
                 (x shr 0).toByte()
             )
         }

         fun toBytes(x: ULong, bytes: ByteArray, offset: Int) {
             bytes[offset + 0] = (x shr 56).toByte()
             bytes[offset + 1] = (x shr 48).toByte()
             bytes[offset + 2] = (x shr 40).toByte()
             bytes[offset + 3] = (x shr 32).toByte()
             bytes[offset + 4] = (x shr 24).toByte()
             bytes[offset + 5] = (x shr 16).toByte()
             bytes[offset + 6] = (x shr 8).toByte()
             bytes[offset + 7] = (x shr 0).toByte()
         }

         fun toBytes(x: ULong, bytes: ByteArray) {
             bytes[0] = (x shr 56).toByte()
             bytes[1] = (x shr 48).toByte()
             bytes[2] = (x shr 40).toByte()
             bytes[3] = (x shr 32).toByte()
             bytes[4] = (x shr 24).toByte()
             bytes[5] = (x shr 16).toByte()
             bytes[6] = (x shr 8).toByte()
             bytes[7] = (x shr 0).toByte()
         }
    }
}
