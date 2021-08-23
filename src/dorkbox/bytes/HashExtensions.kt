/*
 * Copyright 2021 dorkbox, llc
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
