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

import org.junit.Assert.*
import org.junit.Test

class TestHex {
    private val hexRegex = Regex("0[xX][0-9a-fA-F]+")

    @Test
    fun weCanProduceSingleDigitHex() {
        assertEquals("00", Hex.encode(0.toByte()))
        assertEquals("01", Hex.encode(1.toByte()))
        assertEquals("0f", Hex.encode(15.toByte()))
    }

    @Test
    fun weCanProduceDoubleDigitHex() {
        assertEquals("10", Hex.encode(16.toByte()))
        assertEquals("2a", Hex.encode(42.toByte()))
        assertEquals("ff", Hex.encode(255.toByte()))
    }

    @Test
    fun prefixIsIgnored() {
        assertTrue(Hex.decode("0xab").contentEquals(Hex.decode("ab")))
    }

    @Test
    fun sizesAreOk() {
        assertEquals(0, Hex.decode("0x").size)
        assertEquals(1, Hex.decode("ff").size)
        assertEquals(2, Hex.decode("ffaa").size)
        assertEquals(3, Hex.decode("ffaabb").size)
        assertEquals(4, Hex.decode("ffaabb44").size)
        assertEquals(5, Hex.decode("0xffaabb4455").size)
        assertEquals(6, Hex.decode("0xffaabb445566").size)
        assertEquals(7, Hex.decode("ffaabb44556677").size)
    }

    @Test
    fun byteArrayLimitWorks() {
        assertEquals("0x", Hex.encode(Hex.decode("00"), limit = 0))
        assertEquals("0x00", Hex.encode(Hex.decode("00"), limit = 1))
        assertEquals("0x", Hex.encode(Hex.decode("ff"), limit = 0))
        assertEquals("0xff", Hex.encode(Hex.decode("ff"), limit = 1))
        assertEquals("0x", Hex.encode(Hex.decode("abcdef"), limit = 0))
        assertEquals("0xab", Hex.encode(Hex.decode("abcdef"), limit = 1))
        assertEquals("0xabcd", Hex.encode(Hex.decode("abcdef"), limit = 2))
        assertEquals("0xabcdef", Hex.encode(Hex.decode("abcdef"), limit = 3))
        assertEquals("0xabcdef", Hex.encode(Hex.decode("abcdef"), limit = 32))
        assertEquals("0xaa12456789bb", Hex.encode(Hex.decode("0xaa12456789bb"), limit = 6))
        assertEquals("0xaa12456789bb", Hex.encode(Hex.decode("0xaa12456789bb"), limit = 9))
    }

    @Test
    fun exceptionOnOddInput() {
        var exception: Exception? = null
        try {
            Hex.decode("0xa")
        } catch (e: Exception) {
            exception = e
        }
        assertTrue("Exception must be IllegalArgumentException", exception is IllegalArgumentException)
    }

    @Test
    fun testRoundTrip() {
        assertEquals("0x00", Hex.encode(Hex.decode("00")))
        assertEquals("0xff", Hex.encode(Hex.decode("ff")))
        assertEquals("0xabcdef", Hex.encode(Hex.decode("abcdef")))
        assertEquals("0xaa12456789bb", Hex.encode(Hex.decode("0xaa12456789bb")))
    }

    @Test
    fun regexMatchesForHEX() {
        assertTrue(hexRegex.matches("0x00"))
        assertTrue(hexRegex.matches("0xabcdef123456"))
    }

    @Test
    fun regexFailsForNonHEX() {
        assertFalse(hexRegex.matches("q"))
        assertFalse(hexRegex.matches(""))
        assertFalse(hexRegex.matches("0x+"))
        assertFalse(hexRegex.matches("0xgg"))
    }

    @Test
    fun detectsInvalidHex() {
        var exception: Exception? = null
        try {
            Hex.decode("0xxx")
        } catch (e: Exception) {
            exception = e
        }

        assertTrue("Exception must be IllegalArgumentException", exception is IllegalArgumentException)
    }

    @Test
    fun testHexString() {
        val myString = HexString("123")
        assertEquals("0x123", myString.prepend0xPrefix().string)
        assertEquals("0x123", myString.prepend0xPrefix().prepend0xPrefix().string)

        assertEquals("123", HexString("123").clean0xPrefix().string)
        assertEquals("123", HexString("0x123").clean0xPrefix().string)
        assertEquals("0x123", HexString("0x0x123").clean0xPrefix().string)
    }

    @Test
    fun testStringAsHex() {
        assertEquals("0x123", "123".prepend0xPrefix())
        assertEquals("0x123", "123".prepend0xPrefix().prepend0xPrefix())

        assertEquals("123", "123".clean0xPrefix())
        assertEquals("123", "0x123".clean0xPrefix())
        assertEquals("0x123", "0x0x123".clean0xPrefix())
    }
}
