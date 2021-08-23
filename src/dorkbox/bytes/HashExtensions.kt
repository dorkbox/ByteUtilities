package dorkbox.bytes

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


private val digest1 = ThreadLocal.withInitial {
    try {
        return@withInitial MessageDigest.getInstance("SHA1")
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException("Unable to initialize hash algorithm. SHA1 digest doesn't exist?!? (This should not happen")
    }
}

private val digest256 = ThreadLocal.withInitial {
    try {
        return@withInitial MessageDigest.getInstance("SHA-256")
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException("Unable to initialize hash algorithm. SHA256 digest doesn't exist?!? (This should not happen")
    }
}


fun ByteArray.sha1(): ByteArray {
    val digest: MessageDigest = digest256.get()

    digest.reset()
    digest.update(this)
    return digest.digest()
}

fun ByteArray.sha256(): ByteArray {
    val digest: MessageDigest = digest256.get()

    digest.reset()
    digest.update(this)
    return digest.digest()
}
