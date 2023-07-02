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

import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object Hash {
    /**
     * Gets the version number.
     */
    const val version = BytesInfo.version

    object MessageDigestAlgorithm {
        @Deprecated("Do not use this, it is insecure and prone to attack!")
        const val MD2 = "MD2"
        const val MD5 = "MD5"
        const val SHA_1 = "SHA-1"
        const val SHA_224 = "SHA-224"
        const val SHA_256 = "SHA-256"
        const val SHA_384 = "SHA-384"
        @Deprecated("Do not use this, it is vulnerable to ht-extension attacks")
        const val SHA_512 = "SHA-512"
        const val SHA_512_224 = "SHA-512/224"
        const val SHA_512_256 = "SHA-512/256"
        const val SHA3_224 = "SHA3-224"
        const val SHA3_256 = "SHA3-256"
        const val SHA3_384 = "SHA3-384"
        const val SHA3_512 = "SHA3-512"
    }

    @Deprecated("Do not use this, it is insecure and prone to attack!")
    internal val digestMd5 = ThreadLocal.withInitial {
        try {
            return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.MD5)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Unable to initialize hash algorithm. MD5 digest doesn't exist?!?")
        }
    }
    internal val digest1 = ThreadLocal.withInitial {
        try {
            return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA_1)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Unable to initialize hash algorithm. SHA1 digest doesn't exist?!?")
        }
    }

    internal val digest256 = ThreadLocal.withInitial {
        try {
            return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Unable to initialize hash algorithm. SHA256 digest doesn't exist?!?")
        }
    }

    internal val digest384 = ThreadLocal.withInitial {
        try {
            return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA_384)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Unable to initialize hash algorithm. SHA384 digest doesn't exist?!?")
        }
    }
    internal val digest512 = ThreadLocal.withInitial {
        try {
            return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA_512_256)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Unable to initialize hash algorithm. SHA512 digest doesn't exist?!?")
        }
    }
    internal val digest3_256 = ThreadLocal.withInitial {
        try {
            return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA3_256)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Unable to initialize hash algorithm. SHA3-256 digest doesn't exist?!?")
        }
    }
    internal val digest3_384 = ThreadLocal.withInitial {
        try {
            return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA3_384)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Unable to initialize hash algorithm. SHA3-384 digest doesn't exist?!?")
        }
    }
    internal val digest3_512 = ThreadLocal.withInitial {
        try {
            return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA3_512)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Unable to initialize hash algorithm. SHA3-512 digest doesn't exist?!?")
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


    @Deprecated("Do not use this, it is insecure and prone to attack!")
    val md5 get() = digest1.get()
    val sha1 get() = digest1.get()
    val sha256 get() = digest256.get()
    val sha384 get() = digest384.get()
    val sha512 get() = digest512.get()
    val sha3_256 get() = digest3_256.get()
    val sha3_384 get() = digest3_384.get()
    val sha3_512 get() = digest3_512.get()
}

/**
 * Reads an InputStream and updates the digest for the data
 */
private fun updateDigest(digest: MessageDigest, data: InputStream, bufferSize: Int = 4096): MessageDigest {
    val buffer = ByteArray(bufferSize)
    var read = data.read(buffer, 0, bufferSize)
    while (read > -1) {
        digest.update(buffer, 0, read)
        read = data.read(buffer, 0, bufferSize)
    }
    return digest
}

/**
 * Reads an InputStream and updates the digest for the data
 */
private fun updateDigest(state: org.lwjgl.util.xxhash.XXH32State, data: InputStream, bufferSize: Int = 4096) {
    val buffer = ByteArray(bufferSize)
    val bbuffer = ByteBuffer.wrap(buffer)

    var read = data.read(buffer, 0, bufferSize)
    while (read > -1) {
        bbuffer.limit(read)
        org.lwjgl.util.xxhash.XXHash.XXH32_update(state, bbuffer)
        read = data.read(buffer, 0, bufferSize)
    }
}

private fun hash(byteArray: ByteArray, digest: MessageDigest): ByteArray {
    digest.reset()
    digest.update(byteArray)
    return digest.digest()
}

@Deprecated("Do not use this, it is insecure and prone to attack!")
fun ByteArray.md5(): ByteArray = hash(this, Hash.digestMd5.get())
fun ByteArray.sha1(): ByteArray = hash(this, Hash.digest1.get())
fun ByteArray.sha256(): ByteArray = hash(this, Hash.digest256.get())
fun ByteArray.sha384(): ByteArray = hash(this, Hash.digest384.get())
fun ByteArray.sha512(): ByteArray = hash(this, Hash.digest512.get())
fun ByteArray.sha3_256(): ByteArray = hash(this, Hash.digest3_256.get())
fun ByteArray.sha3_384(): ByteArray = hash(this, Hash.digest3_384.get())
fun ByteArray.sha3_512(): ByteArray = hash(this, Hash.digest3_512.get())
/**
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun ByteArray.xxHash(seed: Int = -0x31bf6a3c): Int {
    val state: org.lwjgl.util.xxhash.XXH32State = org.lwjgl.util.xxhash.XXHash.XXH32_createState()!!
    org.lwjgl.util.xxhash.XXHash.XXH32_reset(state, seed)

    val bbuffer = ByteBuffer.wrap(this)

    org.lwjgl.util.xxhash.XXHash.XXH32_update(state, bbuffer)
    return org.lwjgl.util.xxhash.XXHash.XXH32_digest(state)
}


private fun hash(string: String, digest: MessageDigest): ByteArray {
    val charToBytes = string.toCharArray().charToBytes16()
    digest.reset()
    digest.update(charToBytes, 0, charToBytes.size)
    return digest.digest()
}

/**
 * gets the MD5 hash of the specified string, as UTF-16
 */
@Deprecated("Do not use this, it is insecure and prone to attack!")
fun String.md5(): ByteArray = hash(this, Hash.digestMd5.get())
/**
 * gets the SHA1 hash of the specified string, as UTF-16
 */
fun String.sha1(): ByteArray = hash(this, Hash.digest1.get())
/**
 * gets the SHA256 hash of the specified string, as UTF-16
 */
fun String.sha256(): ByteArray = hash(this, Hash.digest256.get())
/**
 * gets the SHA384 hash of the specified string, as UTF-16
 */
fun String.sha384(): ByteArray = hash(this, Hash.digest384.get())
/**
 * gets the SHA512 hash of the specified string, as UTF-16
 */
fun String.sha512(): ByteArray = hash(this, Hash.digest512.get())
/**
 * gets the SHA3_256 hash of the specified string, as UTF-16
 */
fun String.sha3_256(): ByteArray = hash(this, Hash.digest3_256.get())
/**
 * gets the SHA3_384 hash of the specified string, as UTF-16
 */
fun String.sha3_384(): ByteArray = hash(this, Hash.digest3_384.get())
/**
 * gets the SHA3_512 hash of the specified string, as UTF-16
 */
fun String.sha3_512(): ByteArray = hash(this, Hash.digest3_512.get())
/**
 * gets the xxHash of the string, as UTF-16
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun String.xxHash(saltBytes: ByteArray, seed: Int = -0x31bf6a3c): Int {
    val state: org.lwjgl.util.xxhash.XXH32State = org.lwjgl.util.xxhash.XXHash.XXH32_createState()!!
    org.lwjgl.util.xxhash.XXHash.XXH32_reset(state, seed)

    val charToBytes = this.toCharArray().charToBytes16()

    val bbuffer = ByteBuffer.wrap(charToBytes)

    org.lwjgl.util.xxhash.XXHash.XXH32_update(state, bbuffer)
    return org.lwjgl.util.xxhash.XXHash.XXH32_digest(state)
}

/**
 * gets the SHA256 hash + SALT of the string, as UTF-16
 */
private fun hashWithSalt(string: String, saltBytes: ByteArray, digest: MessageDigest): ByteArray {
    val charToBytes = string.toCharArray().charToBytes16()
    val withSalt = charToBytes + saltBytes

    digest.reset()
    digest.update(withSalt, 0, withSalt.size)
    return digest.digest()
}


/**
 * gets the SHA256 hash + SALT of the string, as UTF-16
 */
fun String.sha1WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest1.get())
/**
 * gets the SHA256 hash + SALT of the string, as UTF-16
 */
fun String.sha256WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest256.get())
/**
 * gets the SHA384 hash + SALT of the string, as UTF-16
 */
fun String.sha384WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest384.get())
/**
 * gets the SHA512 hash + SALT of the string, as UTF-16
 */
fun String.sha512WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest512.get())
/**
 * gets the SHA3_256 hash + SALT of the string, as UTF-16
 */
fun String.sha3_256WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest3_256.get())
/**
 * gets the SHA3_384 hash + SALT of the string, as UTF-16
 */
fun String.sha3_384WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest3_384.get())
/**
 * gets the SHA3_512 hash + SALT of the string, as UTF-16
 */
fun String.sha3_512WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest3_512.get())
/**
 * gets the xxHash + SALT of the string, as UTF-16
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun String.xxHashWithSalt(saltBytes: ByteArray, seed: Int = -0x31bf6a3c): Int {
    val state: org.lwjgl.util.xxhash.XXH32State = org.lwjgl.util.xxhash.XXHash.XXH32_createState()!!
    org.lwjgl.util.xxhash.XXHash.XXH32_reset(state, seed)

    val charToBytes = this.toCharArray().charToBytes16()
    val withSalt = charToBytes + saltBytes

    val bbuffer = ByteBuffer.wrap(withSalt)

    org.lwjgl.util.xxhash.XXHash.XXH32_update(state, bbuffer)
    return org.lwjgl.util.xxhash.XXHash.XXH32_digest(state)
}


private fun hashWithSalt(bytes: ByteArray, saltBytes: ByteArray, digest: MessageDigest): ByteArray {
    val bytesWithSalt = bytes + saltBytes

    digest.reset()
    digest.update(bytesWithSalt, 0, bytesWithSalt.size)
    return digest.digest()
}


/**
 * gets the SHA1 hash + SALT of the byte array
 */
fun ByteArray.sha1WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest1.get())
/**
 * gets the SHA256 hash + SALT of the byte array
 */
fun ByteArray.sha256WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest256.get())
/**
 * gets the SHA384 hash + SALT of the byte array
 */
fun ByteArray.sha384WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest384.get())
/**
 * gets the SHA512 hash + SALT of the byte array
 */
fun ByteArray.sha512WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest512.get())
/**
 * gets the SHA3_256 hash + SALT of the byte array
 */
fun ByteArray.sha3_256WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest3_256.get())
/**
 * gets the SHA3_384 hash + SALT of the byte array
 */
fun ByteArray.sha3_384WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest3_384.get())
/**
 * gets the SHA3_512 hash + SALT of the byte array
 */
fun ByteArray.sha3_512WithSalt(saltBytes: ByteArray): ByteArray = hashWithSalt(this, saltBytes, Hash.digest3_512.get())
/**
 * gets the xxHash + SALT of the byte array
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun ByteArray.xxHashWithSalt(saltBytes: ByteArray, seed: Int = -0x31bf6a3c): Int {
    val state: org.lwjgl.util.xxhash.XXH32State = org.lwjgl.util.xxhash.XXHash.XXH32_createState()!!
    org.lwjgl.util.xxhash.XXHash.XXH32_reset(state, seed)

    val bytesWithSalt = this + saltBytes
    val bbuffer = ByteBuffer.wrap(bytesWithSalt)

    org.lwjgl.util.xxhash.XXHash.XXH32_update(state, bbuffer)
    return org.lwjgl.util.xxhash.XXHash.XXH32_digest(state)
}





private fun hash(file: File, startPosition: Long = 0L, endPosition: Long = file.length(), bufferSize: Int = 4096, digest: MessageDigest): ByteArray {
    digest.reset()

    if (!file.isFile) {
        throw IllegalArgumentException("Unable open as file: ${file.absolutePath}")
    }
    if (!file.canRead()) {
        throw IllegalArgumentException("Unable to read file: ${file.absolutePath}")
    }

    file.inputStream().use {
        val skipped = it.skip(startPosition)
        if (skipped != startPosition) {
            throw IllegalArgumentException("Unable to skip $startPosition bytes. Only able to skip $skipped bytes instead")
        }

        var size = file.length() - startPosition
        val lengthFromEnd = size - endPosition
        if (lengthFromEnd in 1 until size) {
            size -= lengthFromEnd
        }

        updateDigest(digest, it, bufferSize)
        return digest.digest()
    }
}



/**
 * gets the SHA1 hash of the file
 */
fun File.sha1(): ByteArray = hash(this, 0, length(), 4096, Hash.digest1.get())
/**
 * gets the SHA256 hash of the file
 */
fun File.sha256(): ByteArray = hash(this, 0, length(), 4096, Hash.digest256.get())
/**
 * gets the SHA384 hash of the file
 */
fun File.sha384(): ByteArray = hash(this, 0, length(), 4096, Hash.digest384.get())
/**
 * gets the SHA512 hash of the file
 */
fun File.sha512(): ByteArray = hash(this, 0, length(), 4096, Hash.digest512.get())
/**
 * gets the SHA3_256 hash of the file
 */
fun File.sha3_256(): ByteArray = hash(this, 0, length(), 4096, Hash.digest3_256.get())
/**
 * gets the SHA3_384 hash of the file
 */
fun File.sha3_384(): ByteArray = hash(this, 0, length(), 4096, Hash.digest3_384.get())
/**
 * gets the SHA3_512 hash of the file
 */
fun File.sha3_512(): ByteArray = hash(this, 0, length(), 4096, Hash.digest3_512.get())

/**
 * Return the xxhash of the file as or IllegalArgumentExceptions if there are problems with the file
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun File.xxHash(startPosition: Long = 0L, endPosition: Long = this.length(), bufferSize: Int = 4096, seed: Int = -0x31bf6a3c): Int {
    val state: org.lwjgl.util.xxhash.XXH32State = org.lwjgl.util.xxhash.XXHash.XXH32_createState()!!
    org.lwjgl.util.xxhash.XXHash.XXH32_reset(state, seed)

    if (!this.isFile) {
        throw IllegalArgumentException("Unable open as file: ${this.absolutePath}")
    }
    if (!this.canRead()) {
        throw IllegalArgumentException("Unable to read file: ${this.absolutePath}")
    }

    this.inputStream().use {
        val skipped = it.skip(startPosition)
        if (skipped != startPosition) {
            throw IllegalArgumentException("Unable to skip $startPosition bytes. Only able to skip $skipped bytes instead")
        }

        var size = this.length() - startPosition
        val lengthFromEnd = size - endPosition
        if (lengthFromEnd in 1 until size) {
            size -= lengthFromEnd
        }

        updateDigest(state, it, bufferSize)
        return org.lwjgl.util.xxhash.XXHash.XXH32_digest(state)
    }
}




