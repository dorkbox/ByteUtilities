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
@file:Suppress("unused")

package dorkbox.bytes

object ArrayExtensions {
    /**
     * Gets the version number.
     */
    const val version = BytesInfo.version
}


/**
 * this saves the string in a RAW UTF-16 format of bytes, without the BOM
 *
 *
 * The difference between THIS and .toByteArray(Charsets.UTF_16), is that this DOES NOT prepend a
 * BOM (Byte Order Mark) of FEFF. This is against RFC 2781 for charsets.
 *
 * https://stackoverflow.com/questions/54247407/why-utf-8-bom-bytes-efbbbf-can-be-replaced-by-ufeff
 */
fun String.toBytes16(start: Int = 0, length: Int = this.length): ByteArray {
    // NOTE: this saves the char array in UTF-16 format of bytes.
    val bytes = ByteArray(length * 2)

    var j = 0
    val endPosition = start + length
    for (i in start until endPosition) {
        val code = this[i].code
        val srcIndx = j++
        bytes[2 * srcIndx] = (code shr 8).toByte()
        bytes[2 * srcIndx + 1] = code.toByte()
    }

    return bytes
}

/**
 * this saves the char array in a RAW UTF-16 format of bytes, without the BOM
 *
 *
 * The difference between THIS and .toByteArray(Charsets.UTF_16), is that this DOES NOT prepend a
 * BOM (Byte Order Mark) of FEFF. This is against RFC 2781 for charsets.
 *
 * https://stackoverflow.com/questions/54247407/why-utf-8-bom-bytes-efbbbf-can-be-replaced-by-ufeff
 */
fun CharArray.toBytes16(start: Int = 0, length: Int = this.size): ByteArray {
    // NOTE: this saves the char array in UTF-16 format of bytes.
    val bytes = ByteArray(length * 2)

    var j = 0
    val endPosition = start + length
    for (i in start until endPosition) {
        val code = this[i].code
        val srcIndx = j++
        bytes[2 * srcIndx] = (code shr 8).toByte()
        bytes[2 * srcIndx + 1] = code.toByte()
    }

    return bytes
}

fun CharArray.toBytes(): ByteArray {
    val length = this.size
    val bytes = ByteArray(length)

    for (i in 0 until length) {
        val charValue = this[i]
        bytes[i] = charValue.code.toByte()
    }

    return bytes
}

fun IntArray.toBytes(start: Int = 0, length: Int = this.size): ByteArray {
    val bytes = ByteArray(length)

    val endPosition = start + length
    var j = 0
    for (i in start until endPosition) {
        val intValue = this[i]
        if (intValue < 0 || intValue > 255) {
            throw Exception("Int at index $i($intValue) was not a valid byte value (0-255)")
        }
        bytes[j++] = intValue.toByte()
    }

    return bytes
}

fun ByteArray.toInts(startPosition: Int = 0, length: Int = this.size): IntArray {
    val ints = IntArray(length)

    val endPosition = startPosition + length
    var j = 0
    for (i in startPosition until endPosition) {
        ints[j++] = this[i].toInt() and 0xFF
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






private val LINE_SEPARATOR = System.getProperty("line.separator", "\n")
fun ByteArray.printRaw(lineLength: Int = 0): String {
    return if (lineLength > 0) {
        val length = this.size
        val comma = length - 1

        val builder = StringBuilder(length + length / lineLength)
        for (i in 0 until length) {
            builder.append(this[i].toInt())
            if (i < comma) {
                builder.append(",")
            }
            if (i > 0 && i % lineLength == 0) {
                builder.append(LINE_SEPARATOR)
            }
        }

        builder.toString()
    } else {
        val length = this.size
        val comma = length - 1

        val builder = StringBuilder(length + length)
        for (i in 0 until length) {
            builder.append(this[i].toInt())
            if (i < comma) {
                builder.append(",")
            }
        }

        builder.toString()
    }
}

fun ByteArray.print(length: Int = this.size, includeByteCount: Boolean = true): String {
    return this.print(0, length, includeByteCount, 40, null)
}

fun ByteArray.print(
    inputOffset: Int,
    length: Int,
    includeByteCount: Boolean,
    lineLength: Int = 40,
    header: String? = null
): String {
    val comma = length - 1
    var builderLength = length + comma + 2

    if (includeByteCount) {
        builderLength += 7 + length.toString().length
    }

    if (lineLength > 0) {
        builderLength += length / lineLength
    }

    if (header != null) {
        builderLength += header.length + 2
    }

    val builder = StringBuilder(builderLength)
    if (header != null) {
        builder.append(header).append(LINE_SEPARATOR)
    }

    if (includeByteCount) {
        builder.append("Bytes: ").append(length).append(LINE_SEPARATOR)
    }

    builder.append("{")
    for (i in inputOffset until length) {
        builder.append(this[i].toInt())
        if (i < comma) {
            builder.append(",")
        }
        if (i > inputOffset && lineLength > 0 && i % lineLength == 0) {
            builder.append(LINE_SEPARATOR)
        }
    }

    builder.append("}")

    return builder.toString()
}
