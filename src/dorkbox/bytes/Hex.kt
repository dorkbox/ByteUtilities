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

    init {
        // Add this project to the updates system, which verifies this class + UUID + version information
        dorkbox.updates.Updates.add(Hex::class.java, "f176cecea06e48e1a96d59c08a6e98c3", BytesInfo.version)
    }

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
        // An hex string must always have length multiple of 2
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


