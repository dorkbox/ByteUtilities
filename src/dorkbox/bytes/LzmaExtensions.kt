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

package dorkbox.bytes

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

object LZMA {
    /**
     * Gets the version number.
     */
    const val version = BytesInfo.version
    fun ByteArray.encodeLZMA(initialOutputSize: Int = 512): ByteArray {
        return LzmaUtil.encodeLZMA(this, initialOutputSize)
    }
    fun ByteArray.decodeLZMA(initialOutputSize: Int = 512): ByteArray {
        return LzmaUtil.decodeLZMA(this, initialOutputSize)
    }


    fun ByteArrayInputStream.encodeLZMA(initialOutputSize: Int = 512): ByteArrayOutputStream {
        return LzmaUtil.encodeLZMA(this, initialOutputSize)
    }
    fun ByteArrayOutputStream.encodeLZMA(initialOutputSize: Int = 512): ByteArrayOutputStream {
        return LzmaUtil.encodeLZMA(this, initialOutputSize)
    }

    fun ByteArrayInputStream.decodeLZMA(initialOutputSize: Int = 512): ByteArrayOutputStream {
        return LzmaUtil.decodeLZMA(this, initialOutputSize)
    }
    fun ByteArrayOutputStream.decodeLZMA(initialOutputSize: Int = 512): ByteArrayOutputStream {
        return LzmaUtil.decodeLZMA(this, initialOutputSize)
    }

    fun InputStream.encodeLZMA(initialOutputSize: Int = 512): ByteArrayOutputStream {
        return LzmaUtil.encodeLZMA(this, initialOutputSize)
    }
}
