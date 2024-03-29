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

import org.junit.Assert
import org.junit.Test
import java.lang.reflect.Array
import java.nio.ByteBuffer
import java.util.*

class ByteArrayBufferTest {
    @Test
    fun testWriteBytes() {
        val buffer = ByteArrayBuffer(512)
        buffer.writeBytes(byteArrayOf(11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26))
        buffer.writeBytes(byteArrayOf(31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46))
        buffer.writeByte(51)
        buffer.writeBytes(byteArrayOf(52, 53, 54, 55, 56, 57, 58))
        buffer.writeByte(61)
        buffer.writeByte(62)
        buffer.writeByte(63)
        buffer.writeByte(64)
        buffer.writeByte(65)
        assertArrayEquals(
            byteArrayOf(
                11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
                43, 44, 45, 46, 51, 52, 53, 54, 55, 56, 57, 58, 61, 62, 63, 64, 65
            ), buffer.toBytes()
        )
    }

    @Test
    fun testStrings() {
        runStringTest(ByteArrayBuffer(4096))
        runStringTest(ByteArrayBuffer(897))
        val write = ByteArrayBuffer(21)
        val value = "abcdef\u00E1\u00E9\u00ED\u00F3\u00FA\u1234"
        write.writeString(value)
        val read = ByteArrayBuffer(write.toBytes())
        assertArrayEquals(value, read.readString())
        runStringTest(127)
        runStringTest(256)
        runStringTest(1024 * 1023)
        runStringTest(1024 * 1024)
        runStringTest(1024 * 1025)
        runStringTest(1024 * 1026)
        runStringTest(1024 * 1024 * 2)
    }

    fun runStringTest(write: ByteArrayBuffer) {
        val value1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ\rabcdefghijklmnopqrstuvwxyz\n1234567890\t\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~*"
        val value2 = "abcdef\u00E1\u00E9\u00ED\u00F3\u00FA\u1234"
        write.writeString("")
        write.writeString("1")
        write.writeString("22")
        write.writeString("uno")
        write.writeString("dos")
        write.writeString("tres")
        write.writeString(null)
        write.writeString(value1)
        write.writeString(value2)
        for (i in 0..126) {
            write.writeString(i.toChar().toString())
        }
        for (i in 0..126) {
            write.writeString(i.toChar().toString() + "abc")
        }
        val read = ByteArrayBuffer(write.toBytes())
        Assert.assertEquals("", read.readString())
        Assert.assertEquals("1", read.readString())
        Assert.assertEquals("22", read.readString())
        Assert.assertEquals("uno", read.readString())
        Assert.assertEquals("dos", read.readString())
        Assert.assertEquals("tres", read.readString())
        Assert.assertEquals(null, read.readString())
        Assert.assertEquals(value1, read.readString())
        Assert.assertEquals(value2, read.readString())
        for (i in 0..126) {
            Assert.assertEquals(i.toChar().toString(), read.readString())
        }
        for (i in 0..126) {
            Assert.assertEquals(i.toChar().toString() + "abc", read.readString())
        }
        read.rewind()
        Assert.assertEquals("", read.readStringBuilder().toString())
        Assert.assertEquals("1", read.readStringBuilder().toString())
        Assert.assertEquals("22", read.readStringBuilder().toString())
        Assert.assertEquals("uno", read.readStringBuilder().toString())
        Assert.assertEquals("dos", read.readStringBuilder().toString())
        Assert.assertEquals("tres", read.readStringBuilder().toString())
        Assert.assertEquals(null, read.readStringBuilder())
        Assert.assertEquals(value1, read.readStringBuilder().toString())
        Assert.assertEquals(value2, read.readStringBuilder().toString())
        for (i in 0..126) {
            Assert.assertEquals(i.toChar().toString(), read.readStringBuilder().toString())
        }
        for (i in 0..126) {
            Assert.assertEquals(i.toChar().toString() + "abc", read.readStringBuilder().toString())
        }
    }

    fun runStringTest(length: Int) {
        val write = ByteArrayBuffer(1024, -1)
        val buffer = StringBuilder()
        for (i in 0 until length) {
            buffer.append(i.toChar())
        }
        val value = buffer.toString()
        write.writeString(value)
        write.writeString(value)
        var read = ByteArrayBuffer(write.toBytes())
        Assert.assertEquals(value, read.readString())
        Assert.assertEquals(value, read.readStringBuilder().toString())
        write.clear()
        write.writeString(buffer)
        write.writeString(buffer)
        read = ByteArrayBuffer(write.toBytes())
        Assert.assertEquals(value, read.readStringBuilder().toString())
        Assert.assertEquals(value, read.readString())
        if (length <= 127) {
            write.clear()
            write.writeAscii(value)
            write.writeAscii(value)
            read = ByteArrayBuffer(write.toBytes())
            Assert.assertEquals(value, read.readStringBuilder().toString())
            Assert.assertEquals(value, read.readString())
        }
    }

    @Test
    fun testCanReadInt() {
        var write = ByteArrayBuffer()
        var read = ByteArrayBuffer(write.toBytes())
        Assert.assertEquals(false, OptimizeUtilsByteArray.canReadInt(read.getBuffer()))
        Assert.assertEquals(false, read.canReadInt())

        write = ByteArrayBuffer(4)
        write.writeInt(400, true)
        read = ByteArrayBuffer(write.toBytes())
        Assert.assertEquals(true, OptimizeUtilsByteArray.canReadInt(write.getBuffer()))
        Assert.assertEquals(true, read.canReadInt())
        read.setPosition(read.capacity())
        Assert.assertEquals(false, OptimizeUtilsByteArray.canReadInt(read.getBuffer(), read.capacity()))
        Assert.assertEquals(false, read.canReadInt())
    }

    @Test
    fun testInts() {
        runIntTest(ByteArrayBuffer(4096))
    }

    private fun runIntTest(write: ByteArrayBuffer) {
        write.writeInt(0)
        write.writeInt(63)
        write.writeInt(64)
        write.writeInt(127)
        write.writeInt(128)
        write.writeInt(8192)
        write.writeInt(16384)
        write.writeInt(2097151)
        write.writeInt(1048575)
        write.writeInt(134217727)
        write.writeInt(268435455)
        write.writeInt(134217728)
        write.writeInt(268435456)
        write.writeInt(-2097151)
        write.writeInt(-1048575)
        write.writeInt(-134217727)
        write.writeInt(-268435455)
        write.writeInt(-134217728)
        write.writeInt(-268435456)
        Assert.assertEquals(1, write.writeInt(0, true).toLong())
        Assert.assertEquals(1, write.writeInt(0, false).toLong())
        Assert.assertEquals(1, write.writeInt(63, true).toLong())
        Assert.assertEquals(1, write.writeInt(63, false).toLong())
        Assert.assertEquals(1, write.writeInt(64, true).toLong())
        Assert.assertEquals(2, write.writeInt(64, false).toLong())
        Assert.assertEquals(1, write.writeInt(127, true).toLong())
        Assert.assertEquals(2, write.writeInt(127, false).toLong())
        Assert.assertEquals(2, write.writeInt(128, true).toLong())
        Assert.assertEquals(2, write.writeInt(128, false).toLong())
        Assert.assertEquals(2, write.writeInt(8191, true).toLong())
        Assert.assertEquals(2, write.writeInt(8191, false).toLong())
        Assert.assertEquals(2, write.writeInt(8192, true).toLong())
        Assert.assertEquals(3, write.writeInt(8192, false).toLong())
        Assert.assertEquals(2, write.writeInt(16383, true).toLong())
        Assert.assertEquals(3, write.writeInt(16383, false).toLong())
        Assert.assertEquals(3, write.writeInt(16384, true).toLong())
        Assert.assertEquals(3, write.writeInt(16384, false).toLong())
        Assert.assertEquals(3, write.writeInt(2097151, true).toLong())
        Assert.assertEquals(4, write.writeInt(2097151, false).toLong())
        Assert.assertEquals(3, write.writeInt(1048575, true).toLong())
        Assert.assertEquals(3, write.writeInt(1048575, false).toLong())
        Assert.assertEquals(4, write.writeInt(134217727, true).toLong())
        Assert.assertEquals(4, write.writeInt(134217727, false).toLong())
        Assert.assertEquals(4, write.writeInt(268435455, true).toLong())
        Assert.assertEquals(5, write.writeInt(268435455, false).toLong())
        Assert.assertEquals(4, write.writeInt(134217728, true).toLong())
        Assert.assertEquals(5, write.writeInt(134217728, false).toLong())
        Assert.assertEquals(5, write.writeInt(268435456, true).toLong())
        Assert.assertEquals(5, write.writeInt(268435456, false).toLong())
        Assert.assertEquals(1, write.writeInt(-64, false).toLong())
        Assert.assertEquals(5, write.writeInt(-64, true).toLong())
        Assert.assertEquals(2, write.writeInt(-65, false).toLong())
        Assert.assertEquals(5, write.writeInt(-65, true).toLong())
        Assert.assertEquals(2, write.writeInt(-8192, false).toLong())
        Assert.assertEquals(5, write.writeInt(-8192, true).toLong())
        Assert.assertEquals(3, write.writeInt(-1048576, false).toLong())
        Assert.assertEquals(5, write.writeInt(-1048576, true).toLong())
        Assert.assertEquals(4, write.writeInt(-134217728, false).toLong())
        Assert.assertEquals(5, write.writeInt(-134217728, true).toLong())
        Assert.assertEquals(5, write.writeInt(-134217729, false).toLong())
        Assert.assertEquals(5, write.writeInt(-134217729, true).toLong())

        var p = write.position()
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeInt(write.getBuffer(),0, true, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeInt(write.getBuffer(),0, false, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeInt(write.getBuffer(),63, true, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeInt(write.getBuffer(),63, false, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeInt(write.getBuffer(),64, true, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeInt(write.getBuffer(),64, false, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeInt(write.getBuffer(),127, true, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeInt(write.getBuffer(),127, false, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeInt(write.getBuffer(),128, true, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeInt(write.getBuffer(),128, false, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeInt(write.getBuffer(),8191, true, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeInt(write.getBuffer(),8191, false, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeInt(write.getBuffer(),8192, true, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeInt(write.getBuffer(),8192, false, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeInt(write.getBuffer(),16383, true, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeInt(write.getBuffer(),16383, false, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeInt(write.getBuffer(),16384, true, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeInt(write.getBuffer(),16384, false, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeInt(write.getBuffer(),2097151, true, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(4, OptimizeUtilsByteArray.writeInt(write.getBuffer(),2097151, false, p).toLong()).also { p+=4 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeInt(write.getBuffer(),1048575, true, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeInt(write.getBuffer(),1048575, false, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(4, OptimizeUtilsByteArray.writeInt(write.getBuffer(),134217727, true, p).toLong()).also { p+=4 }.also { write.setPosition(p) }
        Assert.assertEquals(4, OptimizeUtilsByteArray.writeInt(write.getBuffer(),134217727, false, p).toLong()).also { p+=4 }.also { write.setPosition(p) }
        Assert.assertEquals(4, OptimizeUtilsByteArray.writeInt(write.getBuffer(),268435455, true, p).toLong()).also { p+=4 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeInt(write.getBuffer(),268435455, false, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(4, OptimizeUtilsByteArray.writeInt(write.getBuffer(),134217728, true, p).toLong()).also { p+=4 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeInt(write.getBuffer(),134217728, false, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeInt(write.getBuffer(),268435456, true, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeInt(write.getBuffer(),268435456, false, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeInt(write.getBuffer(),-64, false, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeInt(write.getBuffer(),-64, true, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeInt(write.getBuffer(),-65, false, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeInt(write.getBuffer(),-65, true, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeInt(write.getBuffer(),-8192, false, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeInt(write.getBuffer(),-8192, true, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeInt(write.getBuffer(),-1048576, false, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeInt(write.getBuffer(),-1048576, true, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(4, OptimizeUtilsByteArray.writeInt(write.getBuffer(),-134217728, false, p).toLong()).also { p+=4 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeInt(write.getBuffer(),-134217728, true, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeInt(write.getBuffer(),-134217729, false, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeInt(write.getBuffer(),-134217729, true, p).toLong()).also { p+=5 }.also { write.setPosition(p) }



        val read = ByteArrayBuffer(write.toBytes())
        Assert.assertEquals(0, read.readInt().toLong())
        Assert.assertEquals(63, read.readInt().toLong())
        Assert.assertEquals(64, read.readInt().toLong())
        Assert.assertEquals(127, read.readInt().toLong())
        Assert.assertEquals(128, read.readInt().toLong())
        Assert.assertEquals(8192, read.readInt().toLong())
        Assert.assertEquals(16384, read.readInt().toLong())
        Assert.assertEquals(2097151, read.readInt().toLong())
        Assert.assertEquals(1048575, read.readInt().toLong())
        Assert.assertEquals(134217727, read.readInt().toLong())
        Assert.assertEquals(268435455, read.readInt().toLong())
        Assert.assertEquals(134217728, read.readInt().toLong())
        Assert.assertEquals(268435456, read.readInt().toLong())
        Assert.assertEquals(-2097151, read.readInt().toLong())
        Assert.assertEquals(-1048575, read.readInt().toLong())
        Assert.assertEquals(-134217727, read.readInt().toLong())
        Assert.assertEquals(-268435455, read.readInt().toLong())
        Assert.assertEquals(-134217728, read.readInt().toLong())
        Assert.assertEquals(-268435456, read.readInt().toLong())
        Assert.assertEquals(true, read.canReadInt())
        Assert.assertEquals(true, read.canReadInt())
        Assert.assertEquals(true, read.canReadInt())
        Assert.assertEquals(0, read.readInt(true).toLong())
        Assert.assertEquals(0, read.readInt(false).toLong())
        Assert.assertEquals(63, read.readInt(true).toLong())
        Assert.assertEquals(63, read.readInt(false).toLong())
        Assert.assertEquals(64, read.readInt(true).toLong())
        Assert.assertEquals(64, read.readInt(false).toLong())
        Assert.assertEquals(127, read.readInt(true).toLong())
        Assert.assertEquals(127, read.readInt(false).toLong())
        Assert.assertEquals(128, read.readInt(true).toLong())
        Assert.assertEquals(128, read.readInt(false).toLong())
        Assert.assertEquals(8191, read.readInt(true).toLong())
        Assert.assertEquals(8191, read.readInt(false).toLong())
        Assert.assertEquals(8192, read.readInt(true).toLong())
        Assert.assertEquals(8192, read.readInt(false).toLong())
        Assert.assertEquals(16383, read.readInt(true).toLong())
        Assert.assertEquals(16383, read.readInt(false).toLong())
        Assert.assertEquals(16384, read.readInt(true).toLong())
        Assert.assertEquals(16384, read.readInt(false).toLong())
        Assert.assertEquals(2097151, read.readInt(true).toLong())
        Assert.assertEquals(2097151, read.readInt(false).toLong())
        Assert.assertEquals(1048575, read.readInt(true).toLong())
        Assert.assertEquals(1048575, read.readInt(false).toLong())
        Assert.assertEquals(134217727, read.readInt(true).toLong())
        Assert.assertEquals(134217727, read.readInt(false).toLong())
        Assert.assertEquals(268435455, read.readInt(true).toLong())
        Assert.assertEquals(268435455, read.readInt(false).toLong())
        Assert.assertEquals(134217728, read.readInt(true).toLong())
        Assert.assertEquals(134217728, read.readInt(false).toLong())
        Assert.assertEquals(268435456, read.readInt(true).toLong())
        Assert.assertEquals(268435456, read.readInt(false).toLong())
        Assert.assertEquals(-64, read.readInt(false).toLong())
        Assert.assertEquals(-64, read.readInt(true).toLong())
        Assert.assertEquals(-65, read.readInt(false).toLong())
        Assert.assertEquals(-65, read.readInt(true).toLong())
        Assert.assertEquals(-8192, read.readInt(false).toLong())
        Assert.assertEquals(-8192, read.readInt(true).toLong())
        Assert.assertEquals(-1048576, read.readInt(false).toLong())
        Assert.assertEquals(-1048576, read.readInt(true).toLong())
        Assert.assertEquals(-134217728, read.readInt(false).toLong())
        Assert.assertEquals(-134217728, read.readInt(true).toLong())
        Assert.assertEquals(-134217729, read.readInt(false).toLong())
        Assert.assertEquals(-134217729, read.readInt(true).toLong())

        p = read.position()
        Assert.assertEquals(0, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(0, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(63, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(63, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(64, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(64, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(127, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(127, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(128, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(128, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(8191, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(8191, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(8192, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(8192, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(16383, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(16383, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(16384, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(16384, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(2097151, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(2097151, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=4 }.also { read.setPosition(p) }
        Assert.assertEquals(1048575, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(1048575, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(134217727, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=4 }.also { read.setPosition(p) }
        Assert.assertEquals(134217727, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=4 }.also { read.setPosition(p) }
        Assert.assertEquals(268435455, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=4 }.also { read.setPosition(p) }
        Assert.assertEquals(268435455, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(134217728, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=4 }.also { read.setPosition(p) }
        Assert.assertEquals(134217728, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(268435456, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(268435456, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(-64, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(-64, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(-65, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(-65, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(-8192, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(-8192, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(-1048576, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(-1048576, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(-134217728, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=4 }.also { read.setPosition(p) }
        Assert.assertEquals(-134217728, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(-134217729, OptimizeUtilsByteArray.readInt(read.getBuffer(), false, p).toLong()).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(-134217729, OptimizeUtilsByteArray.readInt(read.getBuffer(), true, p).toLong()).also { p+=5 }.also { read.setPosition(p) }


        Assert.assertEquals(false, read.canReadInt())
        val random = Random()
        for (i in 0..9999) {
            val value = random.nextInt()
            write.clear()
            write.writeInt(value)
            write.writeInt(value, true)
            write.writeInt(value, false)
            read.setBuffer(write.toBytes())
            Assert.assertEquals(value.toLong(), read.readInt().toLong())
            Assert.assertEquals(value.toLong(), read.readInt(true).toLong())
            Assert.assertEquals(value.toLong(), read.readInt(false).toLong())
        }
    }

    @Test
    fun testLongs() {
        runLongTest(ByteArrayBuffer(4096))
    }

    private fun runLongTest(write: ByteArrayBuffer) {
        write.writeLong(0)
        write.writeLong(63)
        write.writeLong(64)
        write.writeLong(127)
        write.writeLong(128)
        write.writeLong(8192)
        write.writeLong(16384)
        write.writeLong(2097151)
        write.writeLong(1048575)
        write.writeLong(134217727)
        write.writeLong(268435455)
        write.writeLong(134217728)
        write.writeLong(268435456)
        write.writeLong(-2097151)
        write.writeLong(-1048575)
        write.writeLong(-134217727)
        write.writeLong(-268435455)
        write.writeLong(-134217728)
        write.writeLong(-268435456)
        Assert.assertEquals(1, write.writeLong(0, true).toLong())
        Assert.assertEquals(1, write.writeLong(0, false).toLong())
        Assert.assertEquals(1, write.writeLong(63, true).toLong())
        Assert.assertEquals(1, write.writeLong(63, false).toLong())
        Assert.assertEquals(1, write.writeLong(64, true).toLong())
        Assert.assertEquals(2, write.writeLong(64, false).toLong())
        Assert.assertEquals(1, write.writeLong(127, true).toLong())
        Assert.assertEquals(2, write.writeLong(127, false).toLong())
        Assert.assertEquals(2, write.writeLong(128, true).toLong())
        Assert.assertEquals(2, write.writeLong(128, false).toLong())
        Assert.assertEquals(2, write.writeLong(8191, true).toLong())
        Assert.assertEquals(2, write.writeLong(8191, false).toLong())
        Assert.assertEquals(2, write.writeLong(8192, true).toLong())
        Assert.assertEquals(3, write.writeLong(8192, false).toLong())
        Assert.assertEquals(2, write.writeLong(16383, true).toLong())
        Assert.assertEquals(3, write.writeLong(16383, false).toLong())
        Assert.assertEquals(3, write.writeLong(16384, true).toLong())
        Assert.assertEquals(3, write.writeLong(16384, false).toLong())
        Assert.assertEquals(3, write.writeLong(2097151, true).toLong())
        Assert.assertEquals(4, write.writeLong(2097151, false).toLong())
        Assert.assertEquals(3, write.writeLong(1048575, true).toLong())
        Assert.assertEquals(3, write.writeLong(1048575, false).toLong())
        Assert.assertEquals(4, write.writeLong(134217727, true).toLong())
        Assert.assertEquals(4, write.writeLong(134217727, false).toLong())
        Assert.assertEquals(4, write.writeLong(268435455L, true).toLong())
        Assert.assertEquals(5, write.writeLong(268435455L, false).toLong())
        Assert.assertEquals(4, write.writeLong(134217728L, true).toLong())
        Assert.assertEquals(5, write.writeLong(134217728L, false).toLong())
        Assert.assertEquals(5, write.writeLong(268435456L, true).toLong())
        Assert.assertEquals(5, write.writeLong(268435456L, false).toLong())
        Assert.assertEquals(1, write.writeLong(-64, false).toLong())
        Assert.assertEquals(9, write.writeLong(-64, true).toLong())
        Assert.assertEquals(2, write.writeLong(-65, false).toLong())
        Assert.assertEquals(9, write.writeLong(-65, true).toLong())
        Assert.assertEquals(2, write.writeLong(-8192, false).toLong())
        Assert.assertEquals(9, write.writeLong(-8192, true).toLong())
        Assert.assertEquals(3, write.writeLong(-1048576, false).toLong())
        Assert.assertEquals(9, write.writeLong(-1048576, true).toLong())
        Assert.assertEquals(4, write.writeLong(-134217728, false).toLong())
        Assert.assertEquals(9, write.writeLong(-134217728, true).toLong())
        Assert.assertEquals(5, write.writeLong(-134217729, false).toLong())
        Assert.assertEquals(9, write.writeLong(-134217729, true).toLong())


        var p = write.position()
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeLong(write.getBuffer(),0, true, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeLong(write.getBuffer(),0, false, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeLong(write.getBuffer(),63, true, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeLong(write.getBuffer(),63, false, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeLong(write.getBuffer(),64, true, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeLong(write.getBuffer(),64, false, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeLong(write.getBuffer(),127, true, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeLong(write.getBuffer(),127, false, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeLong(write.getBuffer(),128, true, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeLong(write.getBuffer(),128, false, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeLong(write.getBuffer(),8191, true, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeLong(write.getBuffer(),8191, false, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeLong(write.getBuffer(),8192, true, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeLong(write.getBuffer(),8192, false, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeLong(write.getBuffer(),16383, true, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeLong(write.getBuffer(),16383, false, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeLong(write.getBuffer(),16384, true, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeLong(write.getBuffer(),16384, false, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeLong(write.getBuffer(),2097151, true, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(4, OptimizeUtilsByteArray.writeLong(write.getBuffer(),2097151, false, p).toLong()).also { p+=4 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeLong(write.getBuffer(),1048575, true, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeLong(write.getBuffer(),1048575, false, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(4, OptimizeUtilsByteArray.writeLong(write.getBuffer(),134217727, true, p).toLong()).also { p+=4 }.also { write.setPosition(p) }
        Assert.assertEquals(4, OptimizeUtilsByteArray.writeLong(write.getBuffer(),134217727, false, p).toLong()).also { p+=4 }.also { write.setPosition(p) }
        Assert.assertEquals(4, OptimizeUtilsByteArray.writeLong(write.getBuffer(),268435455L, true, p).toLong()).also { p+=4 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeLong(write.getBuffer(),268435455L, false, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(4, OptimizeUtilsByteArray.writeLong(write.getBuffer(),134217728L, true, p).toLong()).also { p+=4 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeLong(write.getBuffer(),134217728L, false, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeLong(write.getBuffer(),268435456L, true, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeLong(write.getBuffer(),268435456L, false, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(1, OptimizeUtilsByteArray.writeLong(write.getBuffer(),-64, false, p).toLong()).also { p+=1 }.also { write.setPosition(p) }
        Assert.assertEquals(9, OptimizeUtilsByteArray.writeLong(write.getBuffer(),-64, true, p).toLong()).also { p+=9 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeLong(write.getBuffer(),-65, false, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(9, OptimizeUtilsByteArray.writeLong(write.getBuffer(),-65, true, p).toLong()).also { p+=9 }.also { write.setPosition(p) }
        Assert.assertEquals(2, OptimizeUtilsByteArray.writeLong(write.getBuffer(),-8192, false, p).toLong()).also { p+=2 }.also { write.setPosition(p) }
        Assert.assertEquals(9, OptimizeUtilsByteArray.writeLong(write.getBuffer(),-8192, true, p).toLong()).also { p+=9 }.also { write.setPosition(p) }
        Assert.assertEquals(3, OptimizeUtilsByteArray.writeLong(write.getBuffer(),-1048576, false, p).toLong()).also { p+=3 }.also { write.setPosition(p) }
        Assert.assertEquals(9, OptimizeUtilsByteArray.writeLong(write.getBuffer(),-1048576, true, p).toLong()).also { p+=9 }.also { write.setPosition(p) }
        Assert.assertEquals(4, OptimizeUtilsByteArray.writeLong(write.getBuffer(),-134217728, false, p).toLong()).also { p+=4 }.also { write.setPosition(p) }
        Assert.assertEquals(9, OptimizeUtilsByteArray.writeLong(write.getBuffer(),-134217728, true, p).toLong()).also { p+=9 }.also { write.setPosition(p) }
        Assert.assertEquals(5, OptimizeUtilsByteArray.writeLong(write.getBuffer(),-134217729, false, p).toLong()).also { p+=5 }.also { write.setPosition(p) }
        Assert.assertEquals(9, OptimizeUtilsByteArray.writeLong(write.getBuffer(),-134217729, true, p).toLong()).also { p+=9 }.also { write.setPosition(p) }



        val read = ByteArrayBuffer(write.toBytes())
        Assert.assertEquals(0, read.readLong())
        Assert.assertEquals(63, read.readLong())
        Assert.assertEquals(64, read.readLong())
        Assert.assertEquals(127, read.readLong())
        Assert.assertEquals(128, read.readLong())
        Assert.assertEquals(8192, read.readLong())
        Assert.assertEquals(16384, read.readLong())
        Assert.assertEquals(2097151, read.readLong())
        Assert.assertEquals(1048575, read.readLong())
        Assert.assertEquals(134217727, read.readLong())
        Assert.assertEquals(268435455, read.readLong())
        Assert.assertEquals(134217728, read.readLong())
        Assert.assertEquals(268435456, read.readLong())
        Assert.assertEquals(-2097151, read.readLong())
        Assert.assertEquals(-1048575, read.readLong())
        Assert.assertEquals(-134217727, read.readLong())
        Assert.assertEquals(-268435455, read.readLong())
        Assert.assertEquals(-134217728, read.readLong())
        Assert.assertEquals(-268435456, read.readLong())
        Assert.assertEquals(0, read.readLong(true))
        Assert.assertEquals(0, read.readLong(false))
        Assert.assertEquals(63, read.readLong(true))
        Assert.assertEquals(63, read.readLong(false))
        Assert.assertEquals(64, read.readLong(true))
        Assert.assertEquals(64, read.readLong(false))
        Assert.assertEquals(127, read.readLong(true))
        Assert.assertEquals(127, read.readLong(false))
        Assert.assertEquals(128, read.readLong(true))
        Assert.assertEquals(128, read.readLong(false))
        Assert.assertEquals(8191, read.readLong(true))
        Assert.assertEquals(8191, read.readLong(false))
        Assert.assertEquals(8192, read.readLong(true))
        Assert.assertEquals(8192, read.readLong(false))
        Assert.assertEquals(16383, read.readLong(true))
        Assert.assertEquals(16383, read.readLong(false))
        Assert.assertEquals(16384, read.readLong(true))
        Assert.assertEquals(16384, read.readLong(false))
        Assert.assertEquals(2097151, read.readLong(true))
        Assert.assertEquals(2097151, read.readLong(false))
        Assert.assertEquals(1048575, read.readLong(true))
        Assert.assertEquals(1048575, read.readLong(false))
        Assert.assertEquals(134217727, read.readLong(true))
        Assert.assertEquals(134217727, read.readLong(false))
        Assert.assertEquals(268435455, read.readLong(true))
        Assert.assertEquals(268435455, read.readLong(false))
        Assert.assertEquals(134217728, read.readLong(true))
        Assert.assertEquals(134217728, read.readLong(false))
        Assert.assertEquals(268435456, read.readLong(true))
        Assert.assertEquals(268435456, read.readLong(false))
        Assert.assertEquals(-64, read.readLong(false))
        Assert.assertEquals(-64, read.readLong(true))
        Assert.assertEquals(-65, read.readLong(false))
        Assert.assertEquals(-65, read.readLong(true))
        Assert.assertEquals(-8192, read.readLong(false))
        Assert.assertEquals(-8192, read.readLong(true))
        Assert.assertEquals(-1048576, read.readLong(false))
        Assert.assertEquals(-1048576, read.readLong(true))
        Assert.assertEquals(-134217728, read.readLong(false))
        Assert.assertEquals(-134217728, read.readLong(true))
        Assert.assertEquals(-134217729, read.readLong(false))
        Assert.assertEquals(-134217729, read.readLong(true))


        p = read.position()
        Assert.assertEquals(0, OptimizeUtilsByteArray.readLong(read.getBuffer(), true, p)).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(0, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(63, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(63, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(64, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(64, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(127, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(127, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(128, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(128, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(8191, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(8191, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(8192, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(8192, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(16383, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(16383, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(16384, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(16384, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(2097151, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(2097151, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=4 }.also { read.setPosition(p) }
        Assert.assertEquals(1048575, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(1048575, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(134217727, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=4 }.also { read.setPosition(p) }
        Assert.assertEquals(134217727, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=4 }.also { read.setPosition(p) }
        Assert.assertEquals(268435455, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=4 }.also { read.setPosition(p) }
        Assert.assertEquals(268435455, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(134217728, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=4 }.also { read.setPosition(p) }
        Assert.assertEquals(134217728, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(268435456, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(268435456, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(-64, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=1 }.also { read.setPosition(p) }
        Assert.assertEquals(-64, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=9 }.also { read.setPosition(p) }
        Assert.assertEquals(-65, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(-65, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=9 }.also { read.setPosition(p) }
        Assert.assertEquals(-8192, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=2 }.also { read.setPosition(p) }
        Assert.assertEquals(-8192, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=9 }.also { read.setPosition(p) }
        Assert.assertEquals(-1048576, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=3 }.also { read.setPosition(p) }
        Assert.assertEquals(-1048576, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=9 }.also { read.setPosition(p) }
        Assert.assertEquals(-134217728, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=4 }.also { read.setPosition(p) }
        Assert.assertEquals(-134217728, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=9 }.also { read.setPosition(p) }
        Assert.assertEquals(-134217729, OptimizeUtilsByteArray.readLong(read.getBuffer(),false, p)).also { p+=5 }.also { read.setPosition(p) }
        Assert.assertEquals(-134217729, OptimizeUtilsByteArray.readLong(read.getBuffer(),true, p)).also { p+=9 }.also { read.setPosition(p) }



        val random = Random()
        for (i in 0..9999) {
            val value = random.nextLong()
            write.clear()
            write.writeLong(value)
            write.writeLong(value, true)
            write.writeLong(value, false)
            read.setBuffer(write.toBytes())
            Assert.assertEquals(value, read.readLong())
            Assert.assertEquals(value, read.readLong(true))
            Assert.assertEquals(value, read.readLong(false))
        }
    }

    @Test
    fun testShorts() {
        runShortTest(ByteArrayBuffer(4096))
    }

    private fun runShortTest(write: ByteArrayBuffer) {
        write.writeShort(0)
        write.writeShort(63)
        write.writeShort(64)
        write.writeShort(127)
        write.writeShort(128)
        write.writeShort(8192)
        write.writeShort(16384)
        write.writeShort(32767)
        write.writeShort(-63)
        write.writeShort(-64)
        write.writeShort(-127)
        write.writeShort(-128)
        write.writeShort(-8192)
        write.writeShort(-16384)
        write.writeShort(-32768)
        val read = ByteArrayBuffer(write.toBytes())
        Assert.assertEquals(0, read.readShort().toLong())
        Assert.assertEquals(63, read.readShort().toLong())
        Assert.assertEquals(64, read.readShort().toLong())
        Assert.assertEquals(127, read.readShort().toLong())
        Assert.assertEquals(128, read.readShort().toLong())
        Assert.assertEquals(8192, read.readShort().toLong())
        Assert.assertEquals(16384, read.readShort().toLong())
        Assert.assertEquals(32767, read.readShort().toLong())
        Assert.assertEquals(-63, read.readShort().toLong())
        Assert.assertEquals(-64, read.readShort().toLong())
        Assert.assertEquals(-127, read.readShort().toLong())
        Assert.assertEquals(-128, read.readShort().toLong())
        Assert.assertEquals(-8192, read.readShort().toLong())
        Assert.assertEquals(-16384, read.readShort().toLong())
        Assert.assertEquals(-32768, read.readShort().toLong())
    }

    @Test
    fun testFloats() {
        runFloatTest(ByteArrayBuffer(4096))
    }

    private fun runFloatTest(write: ByteArrayBuffer) {
        write.writeFloat(0f)
        write.writeFloat(63f)
        write.writeFloat(64f)
        write.writeFloat(127f)
        write.writeFloat(128f)
        write.writeFloat(8192f)
        write.writeFloat(16384f)
        write.writeFloat(32767f)
        write.writeFloat(-63f)
        write.writeFloat(-64f)
        write.writeFloat(-127f)
        write.writeFloat(-128f)
        write.writeFloat(-8192f)
        write.writeFloat(-16384f)
        write.writeFloat(-32768f)
        Assert.assertEquals(1, write.writeFloat(0f, 1000f, true).toLong())
        Assert.assertEquals(1, write.writeFloat(0f, 1000f, false).toLong())
        Assert.assertEquals(3, write.writeFloat(63f, 1000f, true).toLong())
        Assert.assertEquals(3, write.writeFloat(63f, 1000f, false).toLong())
        Assert.assertEquals(3, write.writeFloat(64f, 1000f, true).toLong())
        Assert.assertEquals(3, write.writeFloat(64f, 1000f, false).toLong())
        Assert.assertEquals(3, write.writeFloat(127f, 1000f, true).toLong())
        Assert.assertEquals(3, write.writeFloat(127f, 1000f, false).toLong())
        Assert.assertEquals(3, write.writeFloat(128f, 1000f, true).toLong())
        Assert.assertEquals(3, write.writeFloat(128f, 1000f, false).toLong())
        Assert.assertEquals(4, write.writeFloat(8191f, 1000f, true).toLong())
        Assert.assertEquals(4, write.writeFloat(8191f, 1000f, false).toLong())
        Assert.assertEquals(4, write.writeFloat(8192f, 1000f, true).toLong())
        Assert.assertEquals(4, write.writeFloat(8192f, 1000f, false).toLong())
        Assert.assertEquals(4, write.writeFloat(16383f, 1000f, true).toLong())
        Assert.assertEquals(4, write.writeFloat(16383f, 1000f, false).toLong())
        Assert.assertEquals(4, write.writeFloat(16384f, 1000f, true).toLong())
        Assert.assertEquals(4, write.writeFloat(16384f, 1000f, false).toLong())
        Assert.assertEquals(4, write.writeFloat(32767f, 1000f, true).toLong())
        Assert.assertEquals(4, write.writeFloat(32767f, 1000f, false).toLong())
        Assert.assertEquals(3, write.writeFloat(-64f, 1000f, false).toLong())
        Assert.assertEquals(5, write.writeFloat(-64f, 1000f, true).toLong())
        Assert.assertEquals(3, write.writeFloat(-65f, 1000f, false).toLong())
        Assert.assertEquals(5, write.writeFloat(-65f, 1000f, true).toLong())
        Assert.assertEquals(4, write.writeFloat(-8192f, 1000f, false).toLong())
        Assert.assertEquals(5, write.writeFloat(-8192f, 1000f, true).toLong())
        val delta = 0.00000001f
        val read = ByteArrayBuffer(write.toBytes())
        Assert.assertEquals(read.readFloat(), 0f, delta)
        Assert.assertEquals(read.readFloat(), 63f, delta)
        Assert.assertEquals(read.readFloat(), 64f, delta)
        Assert.assertEquals(read.readFloat(), 127f, delta)
        Assert.assertEquals(read.readFloat(), 128f, delta)
        Assert.assertEquals(read.readFloat(), 8192f, delta)
        Assert.assertEquals(read.readFloat(), 16384f, delta)
        Assert.assertEquals(read.readFloat(), 32767f, delta)
        Assert.assertEquals(read.readFloat(), -63f, delta)
        Assert.assertEquals(read.readFloat(), -64f, delta)
        Assert.assertEquals(read.readFloat(), -127f, delta)
        Assert.assertEquals(read.readFloat(), -128f, delta)
        Assert.assertEquals(read.readFloat(), -8192f, delta)
        Assert.assertEquals(read.readFloat(), -16384f, delta)
        Assert.assertEquals(read.readFloat(), -32768f, delta)
        Assert.assertEquals(read.readFloat(1000f, true), 0f, delta)
        Assert.assertEquals(read.readFloat(1000f, false), 0f, delta)
        Assert.assertEquals(read.readFloat(1000f, true), 63f, delta)
        Assert.assertEquals(read.readFloat(1000f, false), 63f, delta)
        Assert.assertEquals(read.readFloat(1000f, true), 64f, delta)
        Assert.assertEquals(read.readFloat(1000f, false), 64f, delta)
        Assert.assertEquals(read.readFloat(1000f, true), 127f, delta)
        Assert.assertEquals(read.readFloat(1000f, false), 127f, delta)
        Assert.assertEquals(read.readFloat(1000f, true), 128f, delta)
        Assert.assertEquals(read.readFloat(1000f, false), 128f, delta)
        Assert.assertEquals(read.readFloat(1000f, true), 8191f, delta)
        Assert.assertEquals(read.readFloat(1000f, false), 8191f, delta)
        Assert.assertEquals(read.readFloat(1000f, true), 8192f, delta)
        Assert.assertEquals(read.readFloat(1000f, false), 8192f, delta)
        Assert.assertEquals(read.readFloat(1000f, true), 16383f, delta)
        Assert.assertEquals(read.readFloat(1000f, false), 16383f, delta)
        Assert.assertEquals(read.readFloat(1000f, true), 16384f, delta)
        Assert.assertEquals(read.readFloat(1000f, false), 16384f, delta)
        Assert.assertEquals(read.readFloat(1000f, true), 32767f, delta)
        Assert.assertEquals(read.readFloat(1000f, false), 32767f, delta)
        Assert.assertEquals(read.readFloat(1000f, false), -64f, delta)
        Assert.assertEquals(read.readFloat(1000f, true), -64f, delta)
        Assert.assertEquals(read.readFloat(1000f, false), -65f, delta)
        Assert.assertEquals(read.readFloat(1000f, true), -65f, delta)
        Assert.assertEquals(read.readFloat(1000f, false), -8192f, delta)
        Assert.assertEquals(read.readFloat(1000f, true), -8192f, delta)
    }

    @Test
    fun testDoubles() {
        runDoubleTest(ByteArrayBuffer(4096))
    }

    private fun runDoubleTest(write: ByteArrayBuffer) {
        write.writeDouble(0.0)
        write.writeDouble(63.0)
        write.writeDouble(64.0)
        write.writeDouble(127.0)
        write.writeDouble(128.0)
        write.writeDouble(8192.0)
        write.writeDouble(16384.0)
        write.writeDouble(32767.0)
        write.writeDouble(-63.0)
        write.writeDouble(-64.0)
        write.writeDouble(-127.0)
        write.writeDouble(-128.0)
        write.writeDouble(-8192.0)
        write.writeDouble(-16384.0)
        write.writeDouble(-32768.0)
        Assert.assertEquals(1, write.writeDouble(0.0, 1000.0, true).toLong())
        Assert.assertEquals(1, write.writeDouble(0.0, 1000.0, false).toLong())
        Assert.assertEquals(3, write.writeDouble(63.0, 1000.0, true).toLong())
        Assert.assertEquals(3, write.writeDouble(63.0, 1000.0, false).toLong())
        Assert.assertEquals(3, write.writeDouble(64.0, 1000.0, true).toLong())
        Assert.assertEquals(3, write.writeDouble(64.0, 1000.0, false).toLong())
        Assert.assertEquals(3, write.writeDouble(127.0, 1000.0, true).toLong())
        Assert.assertEquals(3, write.writeDouble(127.0, 1000.0, false).toLong())
        Assert.assertEquals(3, write.writeDouble(128.0, 1000.0, true).toLong())
        Assert.assertEquals(3, write.writeDouble(128.0, 1000.0, false).toLong())
        Assert.assertEquals(4, write.writeDouble(8191.0, 1000.0, true).toLong())
        Assert.assertEquals(4, write.writeDouble(8191.0, 1000.0, false).toLong())
        Assert.assertEquals(4, write.writeDouble(8192.0, 1000.0, true).toLong())
        Assert.assertEquals(4, write.writeDouble(8192.0, 1000.0, false).toLong())
        Assert.assertEquals(4, write.writeDouble(16383.0, 1000.0, true).toLong())
        Assert.assertEquals(4, write.writeDouble(16383.0, 1000.0, false).toLong())
        Assert.assertEquals(4, write.writeDouble(16384.0, 1000.0, true).toLong())
        Assert.assertEquals(4, write.writeDouble(16384.0, 1000.0, false).toLong())
        Assert.assertEquals(4, write.writeDouble(32767.0, 1000.0, true).toLong())
        Assert.assertEquals(4, write.writeDouble(32767.0, 1000.0, false).toLong())
        Assert.assertEquals(3, write.writeDouble(-64.0, 1000.0, false).toLong())
        Assert.assertEquals(9, write.writeDouble(-64.0, 1000.0, true).toLong())
        Assert.assertEquals(3, write.writeDouble(-65.0, 1000.0, false).toLong())
        Assert.assertEquals(9, write.writeDouble(-65.0, 1000.0, true).toLong())
        Assert.assertEquals(4, write.writeDouble(-8192.0, 1000.0, false).toLong())
        Assert.assertEquals(9, write.writeDouble(-8192.0, 1000.0, true).toLong())
        write.writeDouble(1.23456)
        val delta = 0.00000001
        val read = ByteArrayBuffer(write.toBytes())
        Assert.assertEquals(read.readDouble(), 0.0, delta)
        Assert.assertEquals(read.readDouble(), 63.0, delta)
        Assert.assertEquals(read.readDouble(), 64.0, delta)
        Assert.assertEquals(read.readDouble(), 127.0, delta)
        Assert.assertEquals(read.readDouble(), 128.0, delta)
        Assert.assertEquals(read.readDouble(), 8192.0, delta)
        Assert.assertEquals(read.readDouble(), 16384.0, delta)
        Assert.assertEquals(read.readDouble(), 32767.0, delta)
        Assert.assertEquals(read.readDouble(), -63.0, delta)
        Assert.assertEquals(read.readDouble(), -64.0, delta)
        Assert.assertEquals(read.readDouble(), -127.0, delta)
        Assert.assertEquals(read.readDouble(), -128.0, delta)
        Assert.assertEquals(read.readDouble(), -8192.0, delta)
        Assert.assertEquals(read.readDouble(), -16384.0, delta)
        Assert.assertEquals(read.readDouble(), -32768.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, true), 0.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, false), 0.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, true), 63.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, false), 63.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, true), 64.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, false), 64.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, true), 127.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, false), 127.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, true), 128.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, false), 128.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, true), 8191.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, false), 8191.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, true), 8192.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, false), 8192.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, true), 16383.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, false), 16383.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, true), 16384.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, false), 16384.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, true), 32767.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, false), 32767.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, false), -64.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, true), -64.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, false), -65.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, true), -65.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, false), -8192.0, delta)
        Assert.assertEquals(read.readDouble(1000.0, true), -8192.0, delta)
        Assert.assertEquals(1.23456, read.readDouble(), delta)
    }

    @Test
    fun testBooleans() {
        runBooleanTest(ByteArrayBuffer(4096))
    }

    private fun runBooleanTest(write: ByteArrayBuffer) {
        for (i in 0..99) {
            write.writeBoolean(true)
            write.writeBoolean(false)
        }
        val read = ByteArrayBuffer(write.toBytes())
        for (i in 0..99) {
            Assert.assertEquals(true, read.readBoolean())
            Assert.assertEquals(false, read.readBoolean())
        }
    }

    @Test
    fun testChars() {
        runCharTest(ByteArrayBuffer(4096))
    }

    private fun runCharTest(write: ByteArrayBuffer) {
        write.writeChar(0.toChar())
        write.writeChar(63.toChar())
        write.writeChar(64.toChar())
        write.writeChar(127.toChar())
        write.writeChar(128.toChar())
        write.writeChar(8192.toChar())
        write.writeChar(16384.toChar())
        write.writeChar(32767.toChar())
        write.writeChar(65535.toChar())
        val read = ByteArrayBuffer(write.toBytes())
        Assert.assertEquals(0, read.readChar().code.toLong())
        Assert.assertEquals(63, read.readChar().code.toLong())
        Assert.assertEquals(64, read.readChar().code.toLong())
        Assert.assertEquals(127, read.readChar().code.toLong())
        Assert.assertEquals(128, read.readChar().code.toLong())
        Assert.assertEquals(8192, read.readChar().code.toLong())
        Assert.assertEquals(16384, read.readChar().code.toLong())
        Assert.assertEquals(32767, read.readChar().code.toLong())
        Assert.assertEquals(65535, read.readChar().code.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testInputWithOffset() {
        val buf = ByteArray(30)
        val `in` = ByteArrayBuffer(buf)
        `in`.skip(20)
        Assert.assertEquals(10, `in`.remaining().toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testSmallBuffers() {
        val buf = ByteBuffer.allocate(1024)
        val testOutput = ByteArrayBuffer(buf.array())
        testOutput.writeBytes(ByteArray(512))
        testOutput.writeBytes(ByteArray(512))
        val testInputs = ByteArrayBuffer()
        buf.flip()
        testInputs.setBuffer(buf.array())
        val toRead = ByteArray(512)
        testInputs.readBytes(toRead)
        testInputs.readBytes(toRead)
    }

    companion object {
        fun assertArrayEquals(object1: Any?, object2: Any?) {
            Assert.assertEquals(arrayToList(object1), arrayToList(object2))
        }

        fun arrayToList(array: Any?): Any? {
            if (array == null || !array.javaClass.isArray) {
                return array
            }
            val list = ArrayList<Any?>(Array.getLength(array))
            var i = 0
            val n = Array.getLength(array)
            while (i < n) {
                list.add(arrayToList(Array.get(array, i)))
                i++
            }
            return list
        }
    }
}
