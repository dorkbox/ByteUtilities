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

import dorkbox.bytes.Hash.charToBytes16
import dorkbox.bytes.Hash.digest1
import dorkbox.bytes.Hash.digest256
import java.awt.SystemColor.text
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object ArrayExtensions {
    /**
     * Gets the version number.
     */
    const val version = BytesInfo.version
}


/**
 * this saves the char array in UTF-16 format of bytes
 */
fun CharArray.charToBytes16(): ByteArray {
    // NOTE: this saves the char array in UTF-16 format of bytes.
    val bytes = ByteArray(this.size * 2)
    for (i in this.indices) {
        bytes[2 * i] = (this[i].code shr 8).toByte()
        bytes[2 * i + 1] = this[i].code.toByte()
    }
    return bytes
}

fun CharArray.charToBytesRaw(): ByteArray {
    val length = this.size
    val bytes = ByteArray(length)
    for (i in 0 until length) {
        val charValue = this[i]
        bytes[i] = charValue.code.toByte()
    }
    return bytes
}

fun IntArray.intsToBytes(startPosition: Int = 0, length: Int = this.size): ByteArray {
    val bytes = ByteArray(length)
    val endPosition = startPosition + length
    for (i in startPosition until endPosition) {
        val intValue = this[i]
        if (intValue < 0 || intValue > 255) {
            throw IllegalArgumentException("Int at index $i($intValue) was not a valid byte value (0-255)")
        }
        bytes[i] = intValue.toByte()
    }
    return bytes
}

fun ByteArray.bytesToInts(startPosition: Int = 0, length: Int = this.size): IntArray {
    val ints = IntArray(length)
    val endPosition = startPosition + length
    for (i in startPosition until endPosition) {
        ints[i] = this[i].toInt() and 0xFF
    }
    return ints
}

/**
 * XOR two byte arrays together, and save result in originalArray
 *
 * @param keyArray      this is XOR'd into the original array, repeats if necessary.
 */
fun ByteArray.xor(keyArray: ByteArray) {
    var keyIndex = 0
    val keyLength = keyArray.size
    for (i in this.indices) {
        // XOR the data and start over if necessary
        this[i] = (this[i].toInt() xor keyArray[keyIndex++ % keyLength].toInt()).toByte()
    }
}
