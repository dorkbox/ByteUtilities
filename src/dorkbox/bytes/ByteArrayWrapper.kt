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

/**
 * Necessary to provide equals and hashcode methods on a byte arrays, if they are to be used as keys in a map/set/etc
 */
class ByteArrayWrapper(
    data: ByteArray,

    /**
     * if TRUE, then the byteArray is copied. if FALSE, the byte array is used as-is.
     *
     * Using FALSE IS DANGEROUS!!!! If the underlying byte array is modified, this changes as well.
     */
    copyBytes: Boolean = true
) {
    companion object {
        /**
         * Makes a safe copy of the byte array, so that changes to the original do not affect the wrapper.
         * One side effect is that additional memory is used.
         */
        fun copy(data: ByteArray): ByteArrayWrapper {
            return ByteArrayWrapper(data, true)
        }

        /**
         * Does not make a copy of the data, so changes to the original will also affect the wrapper.
         * One side effect is that no extra memory is needed.
         */
        fun wrap(data: ByteArray): ByteArrayWrapper {
            return ByteArrayWrapper(data, false)
        }
    }


    val bytes: ByteArray
    private var hashCode: Int? = null


    init {
        val length = data.size

        if (copyBytes) {
            bytes = ByteArray(length)
            // copy so it's immutable as a key.
            System.arraycopy(data, 0, bytes, 0, length)
        } else {
            bytes = data
        }
    }

    override fun hashCode(): Int {
        // might be null for a thread because it's stale. who cares, get the value again
        var hashCode = hashCode
        if (hashCode == null) {
            hashCode = bytes.contentHashCode()
            this.hashCode = hashCode
        }
        return hashCode
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is ByteArrayWrapper) {
            false
        } else bytes.contentEquals(other.bytes)

        // CANNOT be null, so we don't have to null check!
    }

    override fun toString(): String {
        return "ByteArrayWrapper " + bytes.contentToString()
    }
}
