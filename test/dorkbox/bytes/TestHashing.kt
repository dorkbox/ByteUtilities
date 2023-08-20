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

import dorkbox.hex.toHexString
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class TestHashing {

    @Test
    fun xxHash() {
        assertEquals(679431504, "123123123123".xxHash32())
        assertEquals(6768607861876164638, "123123123123".xxHash64())

        val file = File("LICENSE.Apachev2")

        assertEquals(-990273547, file.xxHash32())
        assertEquals(-8777591777774693626, file.xxHash64())

        assertEquals(file.readBytes().xxHash32(), file.xxHash32())
        assertEquals(file.readBytes().xxHash32(start = 10, length = 400), file.xxHash32(start = 10, length = 400))

        assertEquals(file.readBytes().xxHash64(), file.xxHash64())
        assertEquals(file.readBytes().xxHash64(start = 10, length = 400), file.xxHash64(start = 10, length = 400))
    }


    @Test
    fun shaHashFile() {
        val file = File("LICENSE.Apachev2")

        assertEquals("0x1b64c725684886157776ac3189536fb826a5ee3614321a589580243d92c2458e", file.sha256().toHexString())
        assertEquals("0xc615bc169ede733444ae128fdac4824aa3c4e0ab04d228b302f8227e0cf1b49d", file.sha512().toHexString())

        assertArrayEquals(file.readBytes().sha256(), file.sha256())
        assertArrayEquals(file.readBytes().sha256(start = 10, length = 400), file.sha256(start = 10, length = 400))
    }

    @Test
    fun shaHash() {
        assertEquals("0xf11aea6605c934e435964041bc4b376f256aaf994c70c5458a133fc157096d46", "123123123123".sha256().toHexString())
        assertEquals("0x13b3f95860b8a2da4ee89c610ba674d45269004180a924716cc60c0358b5af08", "123123123123".sha512().toHexString())

        assertArrayEquals("123123123123".sha256(), "123123123123".toBytes16().sha256())

        // NOTE: UTF_16 will always append a BOM (Byte Order Mark) of \uFEFF
        // https://stackoverflow.com/questions/54247407/why-utf-8-bom-bytes-efbbbf-can-be-replaced-by-ufeff

        // FEFF is the BOM for UTF_16 (required by RFC 2781 for charsets)
        assertEquals("feff" + ("123123123123".toBytes16().toHexString(false)), "123123123123".toByteArray(Charsets.UTF_16).toHexString(false))


        assertArrayEquals("23".toCharArray().toBytes16(), "23".toBytes16() )

        assertArrayEquals("123123123123".toBytes16(4, 2), "123123123123".toCharArray(startIndex = 4, endIndex = 4+2).toBytes16())
        assertArrayEquals("123123123123".toBytes16(4, 2), "123123123123".toCharArray().toBytes16(4, 2))

        assertArrayEquals("23".toCharArray().toBytes16(), "123123123123".toCharArray().toBytes16(4, 2))
        assertArrayEquals("23".toBytes16(), "123123123123".toCharArray().toBytes16(4, 2))

        assertFalse("123123123123".toBytes16(4, 2).contentEquals("123123123123".toBytes16(1, 3)))

        assertArrayEquals("123123123123".toCharArray().toBytes16().sha256(), "123123123123".sha256())
    }
}
