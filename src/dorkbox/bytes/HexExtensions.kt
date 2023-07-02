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
 * MIT License
 *
 * Copyright (c) 2017 ligi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dorkbox.bytes

@JvmInline
public value class HexString(val string: String)

object Hex {
    /**
     * Gets the version number.
     */
    const val version = BytesInfo.version

    /**
     * Represents all the chars used for nibble
     */
    private const val CHARS = "0123456789abcdef"

    internal val HEX_REGEX = Regex("(0[xX])?[0-9a-fA-F]*")

    /**
     * Encodes the given byte value as an hexadecimal character.
     */
    fun encode(value: Byte): String {
        return CHARS[value.toInt().shr(4) and 0x0f].toString() + CHARS[value.toInt().and(0x0f)].toString()
    }

    /**
     * Encodes the given byte array value to its hexadecimal representations, and prepends the given prefix to it.
     *
     * Note that by default the 0x prefix is prepended to the result of the conversion.
     * If you want to have the representation without the 0x prefix, pass to this method an empty prefix.
     */
    fun encode(value: ByteArray, prefix: String = "0x"): String {
        return prefix + value.joinToString("") { encode(it) }
    }

    /**
     * Converts the given ch into its integer representation considering it as an hexadecimal character.
     */
    private fun hexToBin(ch: Char): Int = when (ch) {
        in '0'..'9' -> ch - '0'
        in 'A'..'F' -> ch - 'A' + 10
        in 'a'..'f' -> ch - 'a' + 10
        else -> throw(IllegalArgumentException("'$ch' is not a valid hex character"))
    }

    /**
     * Parses the given value reading it as an hexadecimal string, and returns its byte array representation.
     *
     * Note that either 0x-prefixed string and no-prefixed hex strings are supported.
     *
     * @throws IllegalArgumentException if the value is not a hexadecimal string.
     */
    fun decode(value: String): ByteArray {
        // A hex string must always have length multiple of 2
        if (value.length % 2 != 0) {
            throw IllegalArgumentException("hex-string must have an even number of digits (nibbles)")
        }

        // Remove the 0x prefix if it is set
        val cleanInput = if (value.startsWith("0x")) value.substring(2) else value

        return ByteArray(cleanInput.length / 2).apply {
            var i = 0
            while (i < cleanInput.length) {
                this[i / 2] = ((hexToBin(cleanInput[i]) shl 4) + hexToBin(cleanInput[i + 1])).toByte()
                i += 2
            }
        }
    }
}

/**
 * Converts [this] [ByteArray] into its hexadecimal string representation prepending to it the given [prefix].
 *
 * Note that by default the 0x prefix is prepended to the result of the conversion.
 * If you want to have the representation without the 0x prefix, use the [toNoPrefixHexString] method or
 * pass to this method an empty [prefix].
 */
fun ByteArray.toHexString(prefix: String = "0x"): String = Hex.encode(this, prefix)

/**
 * Converts [this] [ByteArray] into its hexadecimal representation without prepending any prefix to it.
 */
fun ByteArray.toNoPrefixHexString(): String = toHexString(prefix = "")


/**
 * Converts [this] [Collection] of bytes into its hexadecimal string representation prepending to it the given [prefix].
 *
 * Note that by default the 0x prefix is prepended to the result of the conversion.
 * If you want to have the representation without the 0x prefix, use the [toNoPrefixHexString] method or
 * pass to this method an empty [prefix].
 */
fun Collection<Byte>.toHexString(prefix: String = "0x"): String = Hex.encode(this.toByteArray(), prefix)

/**
 * Converts [this] [Collection] of bytes into its hexadecimal representation without prepending any prefix to it.
 */
fun Collection<Byte>.toNoPrefixHexString(): String = toHexString("")


/**
 * Parses [this] [String] as an hexadecimal value and returns its [ByteArray] representation.
 *
 * Note that either 0x-prefixed string and no-prefixed hex strings are supported.
 *
 * @throws IllegalArgumentException if [this] is not a hexadecimal string.
 */
fun HexString.hexToByteArray(): ByteArray = Hex.decode(string)
fun    String.hexToByteArray(): ByteArray = Hex.decode(this)

/**
 * Returns `true` if and only if [this] value starts with the `0x` prefix.
 */
fun HexString.has0xPrefix(): Boolean = string.startsWith("0x")
fun    String.has0xPrefix(): Boolean = this.startsWith("0x")

/**
 * Returns a new [String] obtained by prepends the `0x` prefix to [this] value,
 * only if it does not already have it.
 *
 * Examples:
 * ```kotlin
 * val myString = HexString("123")
 * assertEquals("0x123", myString.prepend0xPrefix().string)
 * assertEquals("0x0x123", myString.prepend0xPrefix().prepend0xPrefix().string)
 * ```
 */
fun HexString.prepend0xPrefix(): HexString = if (has0xPrefix()) this else HexString("0x$string")
fun    String.prepend0xPrefix(): String = if (has0xPrefix()) this else "0x$this"

/**
 * Returns a new [String] obtained by removing the first occurrence of the `0x` prefix from [this] string, if it has it.
 *
 * Examples:
 * ```kotlin
 * assertEquals("123", HexString("123").clean0xPrefix().string)
 * assertEquals("123", HexString("0x123").clean0xPrefix().string)
 * assertEquals("0x123", HexString("0x0x123").clean0xPrefix().string)
 * ```
 */
fun HexString.clean0xPrefix(): HexString = if (has0xPrefix()) HexString(string.substring(2)) else this
fun    String.clean0xPrefix(): String = if (has0xPrefix()) this.substring(2) else this

/**
 * Returns if a given string is a valid hex-string - either with or without 0x prefix
 */
fun HexString.isValidHex(): Boolean = Hex.HEX_REGEX.matches(string)
fun    String.isValidHex(): Boolean = Hex.HEX_REGEX.matches(this)


/**
 * Returns a HexString if a given string is a valid hex-string - either with or without 0x prefix
 */
fun String.toHex(): HexString  {
    if (!this.isValidHex()) {
        throw IllegalArgumentException("String is not hex")
    }

    return HexString(this)
}
