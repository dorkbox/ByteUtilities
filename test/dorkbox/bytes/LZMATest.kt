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

import dorkbox.bytes.LZMA.decodeLZMA
import dorkbox.bytes.LZMA.encodeLZMA
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream



class LZMATest {
    // Tests from https://github.com/bitcoin/bitcoin/blob/master/src/test/data/base58_encode_decode.json
    private val TEST_VECTORS = listOf(
        "",
        "61",
        "626262",
        "636363",
        "73696d706c792061206c6f6e6720737472696e6700eb15231dfceb60925886b67d065299925915aeb172c06647",
        "00eb15231dfceb60925886b67d065299925915aeb172c0664700eb15231dfceb60925886b67d065299925915aeb172c06647",
        "516b6fcd0f00eb15231dfceb60925886b67d065299925915aeb172c06647",
        "bf4f89001e670274dd00eb15231dfceb60925886b67d065299925915aeb172c06647",
        "572e4794",
        "ecac89cad93923c02321",
        "10c8511e",
        "00000000000000000000",
    )

    @Test
    fun encodingByteArrayLzmaRoundTrip() {
        TEST_VECTORS.forEach {
            val input = it.toByteArray()
            val decoded = input.encodeLZMA().decodeLZMA()
            ByteArrayBufferTest.assertArrayEquals(input, decoded)
        }
    }

    @Test
    fun encodingBISLzmaRoundTrip() {
        TEST_VECTORS.forEach {
            val input = it.toByteArray()
            val i2 = ByteArrayInputStream(input)
            val decoded = i2.encodeLZMA().decodeLZMA().toByteArray()
            ByteArrayBufferTest.assertArrayEquals(input, decoded)
        }
    }

    @Test
    fun encodingBAOSLzmaRoundTrip() {
        TEST_VECTORS.forEach {
            val input = it.toByteArray()
            val i = ByteArrayInputStream(input)

            val i2 = ByteArrayOutputStream()
            i.copyTo(i2)

            val decoded = i2.encodeLZMA().decodeLZMA().toByteArray()
            ByteArrayBufferTest.assertArrayEquals(input, decoded)
        }
    }
}
