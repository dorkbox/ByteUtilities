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

import org.junit.Assert.assertTrue
import org.junit.Test

class TestBase58 {
    // Tests from https://github.com/bitcoin/bitcoin/blob/master/src/test/data/base58_encode_decode.json
    private val TEST_VECTORS = mapOf(
        "" to "",
        "61" to "2g",
        "626262" to "a3gV",
        "636363" to "aPEr",
        "73696d706c792061206c6f6e6720737472696e67" to "2cFupjhnEsSn59qHXstmK2ffpLv2",
        "00eb15231dfceb60925886b67d065299925915aeb172c06647" to "1NS17iag9jJgTHD1VXjvLCEnZuQ3rJDE9L",
        "516b6fcd0f" to "ABnLTmg",
        "bf4f89001e670274dd" to "3SEo3LWLoPntC",
        "572e4794" to "3EFU7m",
        "ecac89cad93923c02321" to "EJDM8drfXA6uyA",
        "10c8511e" to "Rt5zm",
        "00000000000000000000" to "1111111111"
    )

    @Test
    fun encodingToBase58Works() {
        TEST_VECTORS.forEach {
            assertTrue(it.key.hexToByteArray().encodeToBase58String().contentEquals(it.value))
        }
    }

    @Test
    fun decodingFromBase58Works() {
        TEST_VECTORS.forEach {
            assertTrue(it.value.decodeBase58().contentEquals(it.key.hexToByteArray()))
        }
    }
}
