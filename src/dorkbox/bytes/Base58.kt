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

/*
 * Copyright 2011 Google Inc.
 * Copyright 2018 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Converted to kotlin by ligi
 * https://github.com/komputing/KBase58/blob/master/kbase58/src/main/kotlin/org/komputing/kbase58/Base58.kt
 */
@file:Suppress("unused")

package dorkbox.bytes

import dorkbox.bytes.Base58.CHECKSUM_SIZE
import dorkbox.bytes.Base58.ENCODED_ZERO
import dorkbox.bytes.Base58.divmod

/**
 * Base58 is a way to encode Bitcoin addresses (or arbitrary data) as alphanumeric strings.
 * <p>
 * Note that this is not the same base58 as used by Flickr, which you may find referenced around the Internet.
 * <p>
 * You may want to consider working with {@link PrefixedChecksummedBytes} instead, which
 * adds support for testing the prefix and suffix bytes commonly found in addresses.
 * <p>
 * Satoshi explains: why base-58 instead of standard base-64 encoding?
 * <ul>
 * <li>Don't want 0OIl characters that look the same in some fonts and
 *     could be used to create visually identical looking account numbers.</li>
 * <li>A string with non-alphanumeric characters is not as easily accepted as an account number.</li>
 * <li>E-mail usually won't line-break if there's no punctuation to break at.</li>
 * <li>Doubleclicking selects the whole number as one word if it's all alphanumeric.</li>
 * </ul>
 * <p>
 * However, note that the encoding/decoding runs in O(n^2;) time, so it is not useful for large data.
 * <p>
 * The basic idea of the encoding is to treat the data bytes as a large number represented using
 * base-256 digits, convert the number to be represented using base-58 digits, preserve the exact
 * number of leading zeros (which are otherwise lost during the mathematical operations on the
 * numbers), and finally represent the resulting base-58 digits as alphanumeric ASCII characters.
 */
object Base58 {
    /**
     * Gets the version number.
     */
    const val version = BytesInfo.version


    internal const val ENCODED_ZERO = '1'
    internal const val CHECKSUM_SIZE = 4

    internal const val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    internal val alphabetIndices by lazy {
        IntArray(128) { alphabet.indexOf(it.toChar()) }
    }


    /**
     * Divides a number, represented as an array of bytes each containing a single digit
     * in the specified base, by the given divisor. The given number is modified in-place
     * to contain the quotient, and the return value is the remainder.
     *
     * @param number     the number to divide
     * @param firstDigit the index within the array of the first non-zero digit
     * (this is used for optimization by skipping the leading zeros)
     * @param base       the base in which the number's digits are represented (up to 256)
     * @param divisor    the number to divide by (up to 256)
     * @return the remainder of the division operation
     */
    internal fun divmod(number: ByteArray, firstDigit: UInt, base: UInt, divisor: UInt): UInt {
        // this is just long division which accounts for the base of the input digits
        var remainder = 0.toUInt()
        for (i in firstDigit until number.size.toUInt()) {
            val digit = number[i.toInt()].toUByte()
            val temp = remainder * base + digit
            number[i.toInt()] = (temp / divisor).toByte()
            remainder = temp % divisor
        }
        return remainder
    }
}


/**
 * Encodes the bytes as a base58 string (no checksum is appended).
 *
 * @return the base58-encoded string
 */
fun ByteArray.encodeToBase58String(): String {
    val input = copyOf(size) // since we modify it in-place
    if (input.isEmpty()) {
        return ""
    }

    // Count leading zeros.
    var zeros = 0
    while (zeros < input.size && input[zeros].toInt() == 0) {
        ++zeros
    }

    // Convert base-256 digits to base-58 digits (plus conversion to ASCII characters)
    val encoded = CharArray(input.size * 2) // upper bound
    var outputStart = encoded.size
    var inputStart = zeros
    while (inputStart < input.size) {
        encoded[--outputStart] = Base58.alphabet[Base58.divmod(input, inputStart.toUInt(), 256.toUInt(), 58.toUInt()).toInt()]
        if (input[inputStart].toInt() == 0) {
            ++inputStart // optimization - skip leading zeros
        }
    }

    // Preserve exactly as many leading encoded zeros in output as there were leading zeros in data.
    while (outputStart < encoded.size && encoded[outputStart] == ENCODED_ZERO) {
        ++outputStart
    }

    while (--zeros >= 0) {
        encoded[--outputStart] = Base58.ENCODED_ZERO
    }

    // Return encoded string (including encoded leading zeros).
    return String(encoded, outputStart, encoded.size - outputStart)
}

/**
 * Decodes the base58 string into a [ByteArray]
 *
 * @return the decoded data bytes
 * @throws NumberFormatException if the string is not a valid base58 string
 */
@Throws(NumberFormatException::class)
fun String.decodeBase58(): ByteArray {
    if (isEmpty()) {
        return ByteArray(0)
    }

    // Convert the base58-encoded ASCII chars to a base58 byte sequence (base58 digits).
    val input58 = ByteArray(length)
    for (i in indices) {
        val c = this[i]
        val digit = if (c.code < 128) Base58.alphabetIndices[c.code] else -1
        if (digit < 0) {
            throw NumberFormatException("Illegal character $c at position $i")
        }
        input58[i] = digit.toByte()
    }

    // Count leading zeros.
    var zeros = 0
    while (zeros < input58.size && input58[zeros].toInt() == 0) {
        ++zeros
    }

    // Convert base-58 digits to base-256 digits.
    val decoded = ByteArray(length)
    var outputStart = decoded.size
    var inputStart = zeros
    while (inputStart < input58.size) {
        decoded[--outputStart] = divmod(input58, inputStart.toUInt(), 58.toUInt(), 256.toUInt()).toByte()
        if (input58[inputStart].toInt() == 0) {
            ++inputStart // optimization - skip leading zeros
        }
    }

    // Ignore extra leading zeroes that were added during the calculation.
    while (outputStart < decoded.size && decoded[outputStart].toInt() == 0) {
        ++outputStart
    }

    // Return decoded data (including original number of leading zeros).
    return decoded.copyOfRange(outputStart - zeros, decoded.size)
}

/**
 * Encodes the given bytes as a base58 string, a checksum is appended
 *
 * @return the base58-encoded string
*/
fun ByteArray.encodeToBase58WithChecksum() = ByteArray(size + Base58.CHECKSUM_SIZE).apply {
    System.arraycopy(this@encodeToBase58WithChecksum, 0, this, 0, this@encodeToBase58WithChecksum.size)
    val checksum = this@encodeToBase58WithChecksum.sha256().sha256()
    System.arraycopy(checksum, 0, this, this@encodeToBase58WithChecksum.size, Base58.CHECKSUM_SIZE)
}.encodeToBase58String()

fun String.decodeBase58WithChecksum(): ByteArray {
    val rawBytes = decodeBase58()
    if (rawBytes.size < Base58.CHECKSUM_SIZE) {
        throw Exception("Too short for checksum: $this l:  ${rawBytes.size}")
    }
    val checksum = rawBytes.copyOfRange(rawBytes.size - Base58.CHECKSUM_SIZE, rawBytes.size)

    val payload = rawBytes.copyOfRange(0, rawBytes.size - Base58.CHECKSUM_SIZE)

    val hash = payload.sha256().sha256()
    val computedChecksum = hash.copyOfRange(0, CHECKSUM_SIZE)

    if (checksum.contentEquals(computedChecksum)) {
        return payload
    } else {
        throw IllegalArgumentException("Checksum mismatch: $checksum is not computed checksum $computedChecksum")
    }
}
