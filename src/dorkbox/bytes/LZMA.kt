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

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object LZMA {
    // https://tukaani.org/xz/java.html

    init {
        try {
            Class.forName("org.tukaani.xz.LZMAOutputStream")
        }
        catch (e: Exception) {
            System.err.println("Please add the LZMA library to your classpath, for example: implementation(\"org.tukaani:xz:1.9\")")
            throw e
        }
    }

    /**
     * Gets the version number.
     */
    val version = BytesInfo.version

    @Throws(IOException::class)
    fun encode(input: InputStream, output: OutputStream) {
        org.tukaani.xz.LZMAOutputStream(output, org.tukaani.xz.LZMA2Options(3), true).use { compressionStream ->
            input.copyTo(compressionStream)
        }
    }

    @Throws(IOException::class)
    fun decode(input: InputStream): ByteArrayOutputStream {
        val byteArrayOutputStream = ByteArrayOutputStream(8192)
        org.tukaani.xz.LZMAInputStream(input).use { compressedStream -> compressedStream.copyTo(byteArrayOutputStream) }
        return byteArrayOutputStream
    }

    @Throws(IOException::class)
    fun decode(input: InputStream, output: OutputStream) {
        org.tukaani.xz.LZMAInputStream(input).use { compressedStream -> compressedStream.copyTo(output) }
    }
}
