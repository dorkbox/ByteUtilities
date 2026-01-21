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

import java.io.*



object LzmaUtil {
    // https://tukaani.org/xz/java.html

    init {
        try {
            Class.forName("org.tukaani.xz.LZMAOutputStream")
        }
        catch (e: Exception) {
            println("Please add the LZMA library to your classpath, for example: implementation(\"org.tukaani:xz:1.9\")")
            throw e
        }
    }

    /**
     * Gets the version number.
     */
    val version = BytesInfo.version

    @Throws(IOException::class)
    internal fun encodeLZMA(array: ByteArray, initialOutputSize: Int): ByteArray {
        val input = ByteArrayInputStream(array)

        val output = ByteArrayOutputStream(initialOutputSize)
        org.tukaani.xz.LZMAOutputStream(output, org.tukaani.xz.LZMA2Options(3), array.size.toLong()).use { compressionStream ->
            input.copyTo(compressionStream)
        }
        return output.toByteArray()
    }

    @Throws(IOException::class)
    internal fun decodeLZMA(array: ByteArray, initialOutputSize: Int = 512): ByteArray {
        val input = ByteArrayInputStream(array)

        val byteArrayOutputStream = ByteArrayOutputStream(initialOutputSize)

        org.tukaani.xz.LZMAInputStream(input).use { compressedStream ->
            compressedStream.copyTo(byteArrayOutputStream)
        }

        return byteArrayOutputStream.toByteArray()
    }


    @Throws(IOException::class)
    internal fun encodeLZMA(baos: ByteArrayOutputStream, initialOutputSize: Int): ByteArrayOutputStream {
        val buf = baos.toByteArray()
        val input = ByteArrayInputStream(buf)

        val output = ByteArrayOutputStream(initialOutputSize)
        org.tukaani.xz.LZMAOutputStream(output, org.tukaani.xz.LZMA2Options(3), buf.size.toLong()).use { compressionStream ->
            input.copyTo(compressionStream)
        }
        return output
    }

    @Throws(IOException::class)
    internal fun encodeLZMA(bis: ByteArrayInputStream, initialOutputSize: Int): ByteArrayOutputStream {
        bis.reset()

        val output = ByteArrayOutputStream(initialOutputSize)
        org.tukaani.xz.LZMAOutputStream(output, org.tukaani.xz.LZMA2Options(3), bis.available().toLong()).use { compressionStream ->
            bis.copyTo(compressionStream)
        }
        return output
    }

    @Throws(IOException::class)
    internal fun decodeLZMA(bis: ByteArrayInputStream, initialOutputSize: Int = 512): ByteArrayOutputStream {
        bis.reset()

        val byteArrayOutputStream = ByteArrayOutputStream(initialOutputSize)

        org.tukaani.xz.LZMAInputStream(bis).use { compressedStream ->
            compressedStream.copyTo(byteArrayOutputStream)
        }

        return byteArrayOutputStream
    }

    @Throws(IOException::class)
    internal fun decodeLZMA(boas: ByteArrayOutputStream, initialOutputSize: Int = 512): ByteArrayOutputStream {
        val input = ByteArrayInputStream(boas.toByteArray())
        val byteArrayOutputStream = ByteArrayOutputStream(initialOutputSize)

        org.tukaani.xz.LZMAInputStream(input).use { compressedStream ->
            compressedStream.copyTo(byteArrayOutputStream)
        }

        return byteArrayOutputStream
    }


    @Throws(IOException::class)
    internal fun encodeLZMA(input: InputStream, initialOutputSize: Int = 512): ByteArrayOutputStream {
        input.reset()

        val output = ByteArrayOutputStream(initialOutputSize)
        org.tukaani.xz.LZMAOutputStream(output, org.tukaani.xz.LZMA2Options(3), true).use { compressionStream ->
            input.copyTo(compressionStream)
        }
        return output
    }




    @Throws(IOException::class)
    fun encodeLZMA(input: InputStream, output: OutputStream): OutputStream {
        org.tukaani.xz.LZMAOutputStream(output, org.tukaani.xz.LZMA2Options(3), true).use { compressionStream ->
            input.copyTo(compressionStream)
        }
        return output
    }

    @Throws(IOException::class)
    internal fun decodeLZMA(input: InputStream, output: OutputStream): OutputStream {
        org.tukaani.xz.LZMAInputStream(input).use { compressedStream ->
            compressedStream.copyTo(output)
        }
        return output
    }
}
