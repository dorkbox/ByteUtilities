/*
 * Copyright 2021 dorkbox, llc
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
        assertEquals(Hex.encode(0.toByte()), "00")
        assertEquals(Hex.encode(1.toByte()), "01")
        assertEquals(Hex.encode(15.toByte()), "0f")
    }

    @Test
    fun weCanProduceDoubleDigitHex() {
        assertEquals(Hex.encode(16.toByte()), "10")
        assertEquals(Hex.encode(42.toByte()), "2a")
        assertEquals(Hex.encode(255.toByte()), "ff")
    }

    @Test
    fun prefixIsIgnored() {
        assertTrue(Hex.decode("0xab").contentEquals(Hex.decode("ab")))
    }

    @Test
    fun sizesAreOk() {
        assertEquals(Hex.decode("0x").size, 0)
        assertEquals(Hex.decode("ff").size, 1)
        assertEquals(Hex.decode("ffaa").size, 2)
        assertEquals(Hex.decode("ffaabb").size, 3)
        assertEquals(Hex.decode("ffaabb44").size, 4)
        assertEquals(Hex.decode("0xffaabb4455").size, 5)
        assertEquals(Hex.decode("0xffaabb445566").size, 6)
        assertEquals(Hex.decode("ffaabb44556677").size, 7)
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
        assertEquals(Hex.encode(Hex.decode("00")), "0x00")
        assertEquals(Hex.encode(Hex.decode("ff")), "0xff")
        assertEquals(Hex.encode(Hex.decode("abcdef")), "0xabcdef")
        assertEquals(Hex.encode(Hex.decode("0xaa12456789bb")), "0xaa12456789bb")
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
