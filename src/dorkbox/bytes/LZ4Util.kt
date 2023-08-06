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

package dorkbox.bytes

import net.jpountz.xxhash.StreamingXXHash32
import net.jpountz.xxhash.StreamingXXHash64
import net.jpountz.xxhash.XXHashFactory
import java.io.File
import java.io.InputStream
import java.security.NoSuchAlgorithmException

object LZ4Util {
    init {
        try {
            Class.forName("net.jpountz.xxhash.XXHashFactory")
        }
        catch (e: Exception) {
            System.err.println("Please add the LZMA library to your classpath, for example: implementation(\"org.lz4:lz4-java:1.8.0\")")
            throw e
        }

    }

    private val xxHashFactory: ThreadLocal<XXHashFactory> by lazy {
        ThreadLocal.withInitial {
            try {
                return@withInitial XXHashFactory.fastestInstance()
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Unable to initialize xxHash algorithm. xxHash doesn't exist?!?")
            }
        }
    }

    /**
     * Reads an InputStream and updates the digest for the data
     */
    private fun updateDigest32(hash32: StreamingXXHash32, data: InputStream, bufferSize: Int, start: Long, length: Long) {
        val skipped = data.skip(start)
        if (skipped != start) {
            throw IllegalArgumentException("Unable to skip $start bytes. Only able to skip $skipped bytes instead")
        }

        var readLength = length
        val adjustedBufferSize = if (bufferSize > readLength) {
            readLength.toInt()
        } else {
            bufferSize
        }

        val buffer = ByteArray(adjustedBufferSize)
        var read = 1
        while (read > 0 && readLength > 0) {
            read = if (adjustedBufferSize > readLength) {
                data.read(buffer, 0, readLength.toInt())
            } else {
                data.read(buffer, 0, adjustedBufferSize)
            }
            hash32.update(buffer, 0, read)
            readLength -= read
        }
    }
    private fun updateDigest64(hash64: StreamingXXHash64, data: InputStream, bufferSize: Int, start: Long, length: Long) {
        val skipped = data.skip(start)
        if (skipped != start) {
            throw IllegalArgumentException("Unable to skip $start bytes. Only able to skip $skipped bytes instead")
        }

        var readLength = length
        val adjustedBufferSize = if (bufferSize > readLength) {
            readLength.toInt()
        } else {
            bufferSize
        }

        val buffer = ByteArray(adjustedBufferSize)
        var read = 1
        while (read > 0 && readLength > 0) {
            read = if (adjustedBufferSize > readLength) {
                data.read(buffer, 0, readLength.toInt())
            } else {
                data.read(buffer, 0, adjustedBufferSize)
            }
            hash64.update(buffer, 0, read)
            readLength -= read
        }
    }

    fun xxHash32(file: File, start: Long, length: Long, bufferSize: Int, seed: Int): Int {
        val xxHash = xxHashFactory.get()
        val hash32 = xxHash.newStreamingHash32(seed)!!

        file.inputStream().use {
            updateDigest32(hash32, it, bufferSize, start, length)
            return hash32.value
        }
    }

    fun xxHash64(file: File, start: Long, length: Long, bufferSize: Int, seed: Long): Long {
        val xxHash = xxHashFactory.get()
        val hash64 = xxHash.newStreamingHash64(seed)!!

        file.inputStream().use {
            updateDigest64(hash64, it, bufferSize, start, length)
            return hash64.value
        }
    }

    fun xxHash32(byteArray: ByteArray, start: Int, length: Int, seed: Int): Int {
        val xxHash = xxHashFactory.get()
        val hash32 = xxHash.newStreamingHash32(seed)!!

        hash32.update(byteArray, start, length)
        return hash32.value
    }

    fun xxHash64(byteArray: ByteArray, start: Int, length: Int, seed: Long): Long {
        val xxHash = xxHashFactory.get()
        val hash64 = xxHash.newStreamingHash64(seed)!!

        hash64.update(byteArray, start, length)
        return hash64.value
    }

    fun xxHash32(string: String, start: Int, length: Int, seed: Int): Int {
        val xxHash = xxHashFactory.get()
        val hash32 = xxHash.newStreamingHash32(seed)!!

        val charToBytes = string.toCharArray().toBytes16(start, length)
        hash32.update(charToBytes, 0, charToBytes.size)
        return hash32.value
    }

    fun xxHash64(string: String, start: Int, length: Int, seed: Long): Long {
        val xxHash = xxHashFactory.get()
        val hash64 = xxHash.newStreamingHash64(seed)!!

        val charToBytes = string.toCharArray().toBytes16(start, length)
        hash64.update(charToBytes, 0, charToBytes.size)
        return hash64.value
    }

    fun xxHash32WithSalt(string: String, saltBytes: ByteArray, start: Int, length: Int, seed: Int): Int {
        val xxHash = xxHashFactory.get()
        val hash32 = xxHash.newStreamingHash32(seed)!!

        val charToBytes = string.toCharArray().toBytes16(start, length)

        hash32.update(charToBytes, 0, charToBytes.size)
        hash32.update(saltBytes, 0, saltBytes.size)
        return hash32.value
    }

    fun xxHash64WithSalt(string: String, saltBytes: ByteArray, start: Int, length: Int, seed: Long): Long {
        val xxHash = xxHashFactory.get()
        val hash64 = xxHash.newStreamingHash64(seed)!!

        val charToBytes = string.toCharArray().toBytes16(start, length)

        hash64.update(charToBytes, 0, charToBytes.size)
        hash64.update(saltBytes, 0, saltBytes.size)
        return hash64.value
    }

    fun xxHash32WithSalt(string: ByteArray, saltBytes: ByteArray, start: Int, length: Int, seed: Int): Int {
        val xxHash = xxHashFactory.get()
        val hash32 = xxHash.newStreamingHash32(seed)!!

        hash32.update(string, start, length)
        hash32.update(saltBytes, 0, saltBytes.size)
        return hash32.value
    }

    fun xxHash64WithSalt(string: ByteArray, saltBytes: ByteArray, start: Int, length: Int, seed: Long): Long {
        val xxHash = xxHashFactory.get()
        val hash64 = xxHash.newStreamingHash64(seed)!!

        hash64.update(string, start, length)
        hash64.update(saltBytes, 0, saltBytes.size)
        return hash64.value
    }

    fun xxHash32(inputStream: InputStream, bufferSize: Int, seed: Int): Int {
        val xxHash = xxHashFactory.get()
        val hash32 = xxHash.newStreamingHash32(seed)!!

        val buffer = ByteArray(bufferSize)
        var read: Int

        inputStream.use {
            while (it.read(buffer).also { read = it } > 0) {
                hash32.update(buffer, 0, read)
            }
        }

        return hash32.value
    }

    fun xxHash64(inputStream: InputStream, bufferSize: Int, seed: Long): Long {
        val xxHash = xxHashFactory.get()
        val hash64 = xxHash.newStreamingHash64(seed)!!

        val buffer = ByteArray(bufferSize)
        var read: Int

        inputStream.use {
            while (it.read(buffer).also { read = it } > 0) {
                hash64.update(buffer, 0, read)
            }
        }

        return hash64.value
    }
}
