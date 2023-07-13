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

import net.jpountz.xxhash.StreamingXXHash32
import net.jpountz.xxhash.StreamingXXHash64
import net.jpountz.xxhash.XXHashFactory
import java.io.File
import java.io.InputStream
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
    internal val digestMd5: ThreadLocal<MessageDigest> by lazy {
        ThreadLocal.withInitial {
            try {
                return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.MD5)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Unable to initialize hash algorithm. MD5 digest doesn't exist?!?")
            }
        }
    }
    internal val digest1: ThreadLocal<MessageDigest> by lazy {
        ThreadLocal.withInitial {
            try {
                return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA_1)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Unable to initialize hash algorithm. SHA1 digest doesn't exist?!?")
            }
        }
    }
    internal val digest256: ThreadLocal<MessageDigest> by lazy {
        ThreadLocal.withInitial {
            try {
                return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Unable to initialize hash algorithm. SHA256 digest doesn't exist?!?")
            }
        }
    }
    internal val digest384: ThreadLocal<MessageDigest> by lazy {
        ThreadLocal.withInitial {
            try {
                return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA_384)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Unable to initialize hash algorithm. SHA384 digest doesn't exist?!?")
            }
        }
    }
    internal val digest512: ThreadLocal<MessageDigest> by lazy {
        ThreadLocal.withInitial {
            try {
                return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA_512_256)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Unable to initialize hash algorithm. SHA512 digest doesn't exist?!?")
            }
        }
    }
    internal val digest3_256: ThreadLocal<MessageDigest> by lazy {
        ThreadLocal.withInitial {
            try {
                return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA3_256)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Unable to initialize hash algorithm. SHA3-256 digest doesn't exist?!?")
            }
        }
    }
    internal val digest3_384: ThreadLocal<MessageDigest> by lazy {
        ThreadLocal.withInitial {
            try {
                return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA3_384)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Unable to initialize hash algorithm. SHA3-384 digest doesn't exist?!?")
            }
        }
    }
    internal val digest3_512: ThreadLocal<MessageDigest> by lazy {
        ThreadLocal.withInitial {
            try {
                return@withInitial MessageDigest.getInstance(MessageDigestAlgorithm.SHA3_512)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Unable to initialize hash algorithm. SHA3-512 digest doesn't exist?!?")
            }
        }
    }
    internal val xxHashFactory: ThreadLocal<XXHashFactory> by lazy {
        ThreadLocal.withInitial {
            try {
                return@withInitial XXHashFactory.fastestInstance()
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Unable to initialize xxHash algorithm. xxHash doesn't exist?!?")
            }
        }
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
private fun updateDigest(digest: MessageDigest, data: InputStream, bufferSize: Int, start: Long, length: Long) {
    val skipped = data.skip(start)
    if (skipped != start) {
        throw IllegalArgumentException("Unable to skip $start bytes. Only able to skip $skipped bytes instead")
    }

    var readLength = length
    val adjustedBufferSize = if (bufferSize > readLength) {
        readLength.toInt()
    } else {
        bufferSize
    }

    val buffer = ByteArray(adjustedBufferSize)
    var read = 1
    while (read > 0 && readLength > 0) {
        read = if (adjustedBufferSize > readLength) {
            data.read(buffer, 0, readLength.toInt())
        } else {
            data.read(buffer, 0, adjustedBufferSize)
        }
        digest.update(buffer, 0, read)
        readLength -= read
    }
}

/**
 * Reads an InputStream and updates the digest for the data
 */
private fun updateDigest32(hash32: StreamingXXHash32, data: InputStream, bufferSize: Int, start: Long, length: Long) {
    val skipped = data.skip(start)
    if (skipped != start) {
        throw IllegalArgumentException("Unable to skip $start bytes. Only able to skip $skipped bytes instead")
    }

    var readLength = length
    val adjustedBufferSize = if (bufferSize > readLength) {
        readLength.toInt()
    } else {
        bufferSize
    }

    val buffer = ByteArray(adjustedBufferSize)
    var read = 1
    while (read > 0 && readLength > 0) {
        read = if (adjustedBufferSize > readLength) {
            data.read(buffer, 0, readLength.toInt())
        } else {
            data.read(buffer, 0, adjustedBufferSize)
        }
        hash32.update(buffer, 0, read)
        readLength -= read
    }
}
private fun updateDigest64(hash64: StreamingXXHash64, data: InputStream, bufferSize: Int, start: Long, length: Long) {
    val skipped = data.skip(start)
    if (skipped != start) {
        throw IllegalArgumentException("Unable to skip $start bytes. Only able to skip $skipped bytes instead")
    }

    var readLength = length
    val adjustedBufferSize = if (bufferSize > readLength) {
        readLength.toInt()
    } else {
        bufferSize
    }

    val buffer = ByteArray(adjustedBufferSize)
    var read = 1
    while (read > 0 && readLength > 0) {
        read = if (adjustedBufferSize > readLength) {
            data.read(buffer, 0, readLength.toInt())
        } else {
            data.read(buffer, 0, adjustedBufferSize)
        }
        hash64.update(buffer, 0, read)
        readLength -= read
    }
}

private fun hash(byteArray: ByteArray, start: Int, length: Int, digest: MessageDigest): ByteArray {
    digest.reset()
    digest.update(byteArray, start, length)
    return digest.digest()
}

private fun hash(string: String, start: Int, length: Int, digest: MessageDigest): ByteArray {
    val charToBytes = string.toBytes16(start, length)
    digest.reset()
    digest.update(charToBytes, 0, charToBytes.size)
    return digest.digest()
}

private fun hash(inputStream: InputStream, bufferSize: Int = 4096, digest: MessageDigest): ByteArray {
    val buffer = ByteArray(bufferSize)
    var read: Int

    digest.reset()

    inputStream.use {
        while (it.read(buffer).also { read = it } > 0) {
            digest.update(buffer, 0, read)
        }
    }

    return digest.digest()
}

private fun hash(file: File, start: Long, length: Long, bufferSize: Int, digest: MessageDigest): ByteArray {
    require(file.isFile) { "Unable open as file: ${file.absolutePath}" }
    require(file.canRead()) { "Unable to read file: ${file.absolutePath}" }

    require(start >= 0) { "Start ($start) must be >= 0" }
    require(length >= 0) { "Length ($length) must be >= 0" }
    require(start < file.length()) { "Start ($start) position must be smaller than the size of the file" }

    file.inputStream().use {
        digest.reset()
        updateDigest(digest, it, bufferSize, start, length)
        return digest.digest()
    }
}


@Deprecated("Do not use this, it is insecure and prone to attack!")
fun ByteArray.md5(start: Int = 0, length: Int = this.size): ByteArray = hash(this, start, length, Hash.digestMd5.get())
fun ByteArray.sha1(start: Int = 0, length: Int = this.size): ByteArray = hash(this, start, length, Hash.digest1.get())
fun ByteArray.sha256(start: Int = 0, length: Int = this.size): ByteArray = hash(this, start, length, Hash.digest256.get())
fun ByteArray.sha384(start: Int = 0, length: Int = this.size): ByteArray = hash(this, start, length, Hash.digest384.get())
fun ByteArray.sha512(start: Int = 0, length: Int = this.size): ByteArray = hash(this, start, length, Hash.digest512.get())
fun ByteArray.sha3_256(start: Int = 0, length: Int = this.size): ByteArray = hash(this, start, length, Hash.digest3_256.get())
fun ByteArray.sha3_384(start: Int = 0, length: Int = this.size): ByteArray = hash(this, start, length, Hash.digest3_384.get())
fun ByteArray.sha3_512(start: Int = 0, length: Int = this.size): ByteArray = hash(this, start, length, Hash.digest3_512.get())

/**
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun ByteArray.xxHash32(seed: Int = -0x31bf6a3c, start: Int = 0, length: Int = this.size): Int {
    val xxHash = Hash.xxHashFactory.get()
    val hash32 = xxHash.newStreamingHash32(seed)!!

    hash32.update(this, start, length)
    return hash32.value
}

/**
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun ByteArray.xxHash64(seed: Long = -0x31bf6a3c, start: Int = 0, length: Int = this.size): Long {
    val xxHash = Hash.xxHashFactory.get()
    val hash64 = xxHash.newStreamingHash64(seed)!!

    hash64.update(this, start, length)
    return hash64.value
}


/**
 * gets the MD5 hash of the specified string, as UTF-16
 */
@Deprecated("Do not use this, it is insecure and prone to attack!")
fun String.md5(start: Int = 0, length: Int = this.length): ByteArray = hash(this, start, length, Hash.digestMd5.get())
/**
 * gets the SHA1 hash of the specified string, as UTF-16
 */
fun String.sha1(start: Int = 0, length: Int = this.length): ByteArray = hash(this, start, length, Hash.digest1.get())
/**
 * gets the SHA256 hash of the specified string, as UTF-16
 */
fun String.sha256(start: Int = 0, length: Int = this.length): ByteArray = hash(this, start, length, Hash.digest256.get())
/**
 * gets the SHA384 hash of the specified string, as UTF-16
 */
fun String.sha384(start: Int = 0, length: Int = this.length): ByteArray = hash(this, start, length, Hash.digest384.get())
/**
 * gets the SHA512 hash of the specified string, as UTF-16
 */
fun String.sha512(start: Int = 0, length: Int = this.length): ByteArray = hash(this, start, length, Hash.digest512.get())
/**
 * gets the SHA3_256 hash of the specified string, as UTF-16
 */
fun String.sha3_256(start: Int = 0, length: Int = this.length): ByteArray = hash(this, start, length, Hash.digest3_256.get())
/**
 * gets the SHA3_384 hash of the specified string, as UTF-16
 */
fun String.sha3_384(start: Int = 0, length: Int = this.length): ByteArray = hash(this, start, length, Hash.digest3_384.get())
/**
 * gets the SHA3_512 hash of the specified string, as UTF-16
 */
fun String.sha3_512(start: Int = 0, length: Int = this.length): ByteArray = hash(this, start, length, Hash.digest3_512.get())

/**
 * gets the xxHash32 of the string, as UTF-16
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun String.xxHash32(seed: Int = -0x31bf6a3c, start: Int = 0, length: Int = this.length): Int {
    val xxHash = Hash.xxHashFactory.get()
    val hash32 = xxHash.newStreamingHash32(seed)!!

    val charToBytes = this.toCharArray().toBytes16(start, length)
    hash32.update(charToBytes, 0, charToBytes.size)
    return hash32.value
}

/**
 * gets the xxHash64 of the string, as UTF-16
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun String.xxHash64(seed: Long = -0x31bf6a3c, start: Int = 0, length: Int = this.length): Long {
    val xxHash = Hash.xxHashFactory.get()
    val hash64 = xxHash.newStreamingHash64(seed)!!

    val charToBytes = this.toCharArray().toBytes16(start, length)
    hash64.update(charToBytes, 0, charToBytes.size)
    return hash64.value
}

/**
 * gets the hash + SALT of the string, as UTF-16
 *
 * LENGTH is specifically the length of what we want to hash of the orig string (it doesn't include the salt)
 */
private fun hashWithSalt(string: String, saltBytes: ByteArray, start: Int, length: Int, digest: MessageDigest): ByteArray {
    val charToBytes = string.toCharArray().toBytes16(start, length)
    val withSalt = charToBytes + saltBytes

    digest.reset()
    digest.update(withSalt, 0, withSalt.size)
    return digest.digest()
}


/**
 * gets the SHA256 hash + SALT of the string, as UTF-16
 */
fun String.sha1WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.length + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest1.get())
/**
 * gets the SHA256 hash + SALT of the string, as UTF-16
 */
fun String.sha256WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.length + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest256.get())
/**
 * gets the SHA384 hash + SALT of the string, as UTF-16
 */
fun String.sha384WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.length + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest384.get())
/**
 * gets the SHA512 hash + SALT of the string, as UTF-16
 */
fun String.sha512WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.length + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest512.get())
/**
 * gets the SHA3_256 hash + SALT of the string, as UTF-16
 */
fun String.sha3_256WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.length + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest3_256.get())
/**
 * gets the SHA3_384 hash + SALT of the string, as UTF-16
 */
fun String.sha3_384WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.length + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest3_384.get())
/**
 * gets the SHA3_512 hash + SALT of the string, as UTF-16
 */
fun String.sha3_512WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.length + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest3_512.get())

/**
 * gets the xxHash32 + SALT of the string, as UTF-16
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun String.xxHash32WithSalt(saltBytes: ByteArray, seed: Int = -0x31bf6a3c, start: Int = 0, length: Int = this.length + saltBytes.size): Int {
    require(start >= 0) { "Start ($start) must be >= 0" }
    require(length >= 0) { "Length ($length) must be >= 0" }
    require(start < length) { "Start ($start) position must be smaller than the size of the String" }

    val xxHash = Hash.xxHashFactory.get()
    val hash32 = xxHash.newStreamingHash32(seed)!!

    val charToBytes = this.toCharArray().toBytes16(start, length)

    hash32.update(charToBytes, 0, charToBytes.size)
    hash32.update(saltBytes, 0, saltBytes.size)
    return hash32.value
}

/**
 * gets the xxHash64 + SALT of the string, as UTF-16
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun String.xxHash64WithSalt(saltBytes: ByteArray, seed: Long = -0x31bf6a3c, start: Int = 0, length: Int = this.length + saltBytes.size): Long {
    require(start >= 0) { "Start ($start) must be >= 0" }
    require(length >= 0) { "Length ($length) must be >= 0" }
    require(start < length) { "Start ($start) position must be smaller than the size of the String" }

    val xxHash = Hash.xxHashFactory.get()
    val hash64 = xxHash.newStreamingHash64(seed)!!

    val charToBytes = this.toCharArray().toBytes16(start, length)

    hash64.update(charToBytes, 0, charToBytes.size)
    hash64.update(saltBytes, 0, saltBytes.size)
    return hash64.value
}


private fun hashWithSalt(bytes: ByteArray, saltBytes: ByteArray, start: Int, length: Int, digest: MessageDigest): ByteArray {
    require(start >= 0) { "Start ($start) must be >= 0" }
    require(length >= 0) { "Length ($length) must be >= 0" }
    require(start < bytes.size) { "Start ($start) position must be smaller than the size of the byte array" }

    digest.reset()
    digest.update(bytes, 0, bytes.size)
    digest.update(saltBytes, 0, saltBytes.size)
    return digest.digest()
}


/**
 * gets the SHA1 hash + SALT of the byte array
 */
fun ByteArray.sha1WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.size + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest1.get())
/**
 * gets the SHA256 hash + SALT of the byte array
 */
fun ByteArray.sha256WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.size + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest256.get())
/**
 * gets the SHA384 hash + SALT of the byte array
 */
fun ByteArray.sha384WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.size + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest384.get())
/**
 * gets the SHA512 hash + SALT of the byte array
 */
fun ByteArray.sha512WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.size + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest512.get())
/**
 * gets the SHA3_256 hash + SALT of the byte array
 */
fun ByteArray.sha3_256WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.size + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest3_256.get())
/**
 * gets the SHA3_384 hash + SALT of the byte array
 */
fun ByteArray.sha3_384WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.size + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest3_384.get())
/**
 * gets the SHA3_512 hash + SALT of the byte array
 */
fun ByteArray.sha3_512WithSalt(saltBytes: ByteArray, start: Int = 0, length: Int = this.size + saltBytes.size): ByteArray =
    hashWithSalt(this, saltBytes, start, length, Hash.digest3_512.get())

/**
 * gets the xxHash32 + SALT of the byte array
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun ByteArray.xxHash32WithSalt(saltBytes: ByteArray, seed: Int = -0x31bf6a3c, start: Int = 0, length: Int = this.size + saltBytes.size): Int {
    val xxHash = Hash.xxHashFactory.get()
    val hash32 = xxHash.newStreamingHash32(seed)!!

    hash32.update(this, start, length)
    hash32.update(saltBytes, 0, saltBytes.size)
    return hash32.value
}

/**
 * gets the xxHash64 + SALT of the byte array
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun ByteArray.xxHash64WithSalt(saltBytes: ByteArray, seed: Long = -0x31bf6a3c, start: Int = 0, length: Int = this.size + saltBytes.size): Long {
    val xxHash = Hash.xxHashFactory.get()
    val hash64 = xxHash.newStreamingHash64(seed)!!

    hash64.update(this, start, length)
    hash64.update(saltBytes, 0, saltBytes.size)
    return hash64.value
}


/**
 * gets the SHA1 hash of the file
 */
fun File.sha1(start: Int = 0, length: Long = this.length(), bufferSize: Int = 4096): ByteArray =
    hash(this, start.toLong(), length, bufferSize, Hash.digest1.get())
/**
 * gets the SHA256 hash of the file
 */
fun File.sha256(start: Int = 0, length: Long = this.length(), bufferSize: Int = 4096): ByteArray =
    hash(this, start.toLong(), length, bufferSize, Hash.digest256.get())
/**
 * gets the SHA384 hash of the file
 */
fun File.sha384(start: Int = 0, length: Long = this.length(), bufferSize: Int = 4096): ByteArray =
    hash(this, start.toLong(), length, bufferSize, Hash.digest384.get())
/**
 * gets the SHA512 hash of the file
 */
fun File.sha512(start: Int = 0, length: Long = this.length(), bufferSize: Int = 4096): ByteArray =
    hash(this, start.toLong(), length, bufferSize, Hash.digest512.get())
/**
 * gets the SHA3_256 hash of the file
 */
fun File.sha3_256(start: Int = 0, length: Long = this.length(), bufferSize: Int = 4096): ByteArray =
    hash(this, start.toLong(), length, bufferSize, Hash.digest3_256.get())
/**
 * gets the SHA3_384 hash of the file
 */
fun File.sha3_384(start: Int = 0, length: Long = this.length(), bufferSize: Int = 4096): ByteArray =
    hash(this, start.toLong(), length, bufferSize, Hash.digest3_384.get())
/**
 * gets the SHA3_512 hash of the file
 */
fun File.sha3_512(start: Int = 0, length: Long = this.length(), bufferSize: Int = 4096): ByteArray =
    hash(this, start.toLong(), length, bufferSize, Hash.digest3_512.get())

/**
 * Return the xxhash32 of the file as or IllegalArgumentExceptions if there are problems with the file
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun File.xxHash32(start: Long = 0L, length: Long = this.length(), bufferSize: Int = 4096, seed: Int = -0x31bf6a3c): Int {
    val xxHash = Hash.xxHashFactory.get()
    val hash32 = xxHash.newStreamingHash32(seed)!!

    require(this.isFile) { "Unable open as file: ${this.absolutePath}" }
    require(this.canRead()) { "Unable to read file: ${this.absolutePath}" }

    require(start >= 0) { "Start ($start) must be >= 0" }
    require(length >= 0) { "Length ($length) must be >= 0" }
    require(start < length()) { "Start ($start) position must be smaller than the size of the file" }

    this.inputStream().use {
        updateDigest32(hash32, it, bufferSize, start, length)
        return hash32.value
    }
}

/**
 * Return the xxhash64 of the file as or IllegalArgumentExceptions if there are problems with the file
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun File.xxHash64(start: Long = 0L, length: Long = this.length(), bufferSize: Int = 4096, seed: Long = -0x31bf6a3c): Long {
    val xxHash = Hash.xxHashFactory.get()
    val hash64 = xxHash.newStreamingHash64(seed)!!

    require(this.isFile) { "Unable open as file: ${this.absolutePath}" }
    require(this.canRead()) { "Unable to read file: ${this.absolutePath}" }

    require(start >= 0) { "Start ($start) must be >= 0" }
    require(length >= 0) { "Length ($length) must be >= 0" }
    require(start < length()) { "Start ($start) position must be smaller than the size of the file" }

    this.inputStream().use {
        updateDigest64(hash64, it, bufferSize, start, length)
        return hash64.value
    }
}




/**
 * gets the SHA1 hash of the input stream
 */
fun InputStream.sha1(bufferSize: Int = 4096): ByteArray = hash(this, bufferSize, Hash.digest1.get())
/**
 * gets the SHA256 hash of the file
 */
fun InputStream.sha256(bufferSize: Int = 4096): ByteArray = hash(this, bufferSize, Hash.digest256.get())
/**
 * gets the SHA384 hash of the file
 */
fun InputStream.sha384(bufferSize: Int = 4096): ByteArray = hash(this, bufferSize, Hash.digest384.get())
/**
 * gets the SHA512 hash of the file
 */
fun InputStream.sha512(bufferSize: Int = 4096): ByteArray = hash(this, bufferSize, Hash.digest512.get())
/**
 * gets the SHA3_256 hash of the file
 */
fun InputStream.sha3_256(bufferSize: Int = 4096): ByteArray = hash(this, bufferSize, Hash.digest3_256.get())
/**
 * gets the SHA3_384 hash of the file
 */
fun InputStream.sha3_384(bufferSize: Int = 4096): ByteArray = hash(this, bufferSize, Hash.digest3_384.get())
/**
 * gets the SHA3_512 hash of the file
 */
fun InputStream.sha3_512(bufferSize: Int = 4096): ByteArray = hash(this, bufferSize, Hash.digest3_512.get())

/**
 * Return the xxhash32 of the InputStream
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun InputStream.xxHash32(bufferSize: Int = 4096, seed: Int = -0x31bf6a3c): Int {
    val xxHash = Hash.xxHashFactory.get()
    val hash32 = xxHash.newStreamingHash32(seed)!!

    val buffer = ByteArray(bufferSize)
    var read: Int

    this.use {
        while (it.read(buffer).also { read = it } > 0) {
            hash32.update(buffer, 0, read)
        }
    }

    return hash32.value
}

/**
 * Return the xxhash64 of the InputStream
 *
 * @param seed used to initialize the hash value (for the xxhash seed), use whatever value you want, but always the same
 */
fun InputStream.xxHash64(bufferSize: Int = 4096, seed: Long = -0x31bf6a3c): Long {
    val xxHash = Hash.xxHashFactory.get()
    val hash64 = xxHash.newStreamingHash64(seed)!!

    val buffer = ByteArray(bufferSize)
    var read: Int

    this.use {
        while (it.read(buffer).also { read = it } > 0) {
            hash64.update(buffer, 0, read)
        }
    }

    return hash64.value
}
