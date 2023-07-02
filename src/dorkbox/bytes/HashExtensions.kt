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
import java.awt.SystemColor.text
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object Hash {
    /**
     * Gets the version number.
     */
    const val version = BytesInfo.version

    internal val digest1 = ThreadLocal.withInitial {
        try {
            return@withInitial MessageDigest.getInstance("SHA1")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Unable to initialize hash algorithm. SHA1 digest doesn't exist?!? (This should not happen")
        }
    }

    internal val digest256 = ThreadLocal.withInitial {
        try {
            return@withInitial MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Unable to initialize hash algorithm. SHA256 digest doesn't exist?!? (This should not happen")
        }
    }

    /**
     * this saves the char array in UTF-16 format of bytes
     */
    fun charToBytes16(text: CharArray): ByteArray {
        // NOTE: this saves the char array in UTF-16 format of bytes.
        val bytes = ByteArray(text.size * 2)
        for (i in text.indices) {
            bytes[2 * i] = (text[i].code shr 8).toByte()
            bytes[2 * i + 1] = text[i].code.toByte()
        }
        return bytes
    }


    val sha1 get() = digest1.get()
    val sha256 get() = digest256.get()
}


fun ByteArray.sha1(): ByteArray {
    val digest: MessageDigest = Hash.digest1.get()

    digest.reset()
    digest.update(this)
    return digest.digest()
}

fun ByteArray.sha256(): ByteArray {
    val digest: MessageDigest = Hash.digest256.get()

    digest.reset()
    digest.update(this)
    return digest.digest()
}


/**
 * gets the SHA1 hash of the specified string, as UTF-16
 */
fun String.sha1(): ByteArray {
    val charToBytes = this.toCharArray().charToBytes16()

    val digest: MessageDigest = Hash.digest1.get()
    val usernameHashBytes = ByteArray(digest.digestLength)
    digest.update(charToBytes, 0, charToBytes.size)
    digest.digest(usernameHashBytes)

    return usernameHashBytes
}

/**
 * gets the SHA256 hash of the specified string, as UTF-16
 */
fun String.sha256(): ByteArray {
    val charToBytes = this.toCharArray().charToBytes16()

    val digest: MessageDigest = Hash.digest256.get()
    val usernameHashBytes = ByteArray(digest.digestLength)
    digest.update(charToBytes, 0, charToBytes.size)
    digest.digest(usernameHashBytes)

    return usernameHashBytes
}

/**
 * gets the SHA256 hash + SALT of the string, as UTF-16
 */
fun String.sha1WithSalt(saltBytes: ByteArray): ByteArray {
    val charToBytes = this.toCharArray().charToBytes16()
    val userNameWithSalt = charToBytes + saltBytes

    val digest: MessageDigest = Hash.digest1.get()
    val usernameHashBytes = ByteArray(digest.digestLength)
    digest.update(userNameWithSalt, 0, userNameWithSalt.size)
    digest.digest(usernameHashBytes)

    return usernameHashBytes
}

/**
 * gets the SHA256 hash + SALT of the string, as UTF-16
 */
fun String.sha256WithSalt(saltBytes: ByteArray): ByteArray {
    val charToBytes = this.toCharArray().charToBytes16()
    val userNameWithSalt = charToBytes + saltBytes

    val digest: MessageDigest = Hash.digest256.get()
    val usernameHashBytes = ByteArray(digest.digestLength)
    digest.update(userNameWithSalt, 0, userNameWithSalt.size)
    digest.digest(usernameHashBytes)

    return usernameHashBytes
}

/**
 * gets the SHA1 hash + SALT of the byte array
 */
fun ByteArray.sha1WithSalt(saltBytes: ByteArray): ByteArray {
    val bytesWithSalt = this + saltBytes

    val sha256: MessageDigest = Hash.digest1.get()
    val usernameHashBytes = ByteArray(sha256.digestLength)
    sha256.update(bytesWithSalt, 0, bytesWithSalt.size)
    sha256.digest(usernameHashBytes)

    return usernameHashBytes
}

/**
 * gets the SHA256 hash + SALT of the byte array
 */
fun ByteArray.sha256WithSalt(saltBytes: ByteArray): ByteArray {
    val bytesWithSalt = this + saltBytes

    val sha256: MessageDigest = Hash.digest256.get()
    val usernameHashBytes = ByteArray(sha256.digestLength)
    sha256.update(bytesWithSalt, 0, bytesWithSalt.size)
    sha256.digest(usernameHashBytes)

    return usernameHashBytes
}

