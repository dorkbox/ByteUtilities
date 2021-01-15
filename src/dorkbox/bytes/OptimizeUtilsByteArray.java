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
 *
 * Copyright (c) 2008, Nathan Sweet
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of Esoteric Software nor the names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dorkbox.bytes;

@SuppressWarnings({"Duplicates", "NumericCastThatLosesPrecision", "UnusedAssignment", "unused"})
public
class OptimizeUtilsByteArray {

    /**
     * Returns the number of bytes that would be written with {@link #writeInt(byte[], int, boolean)}.
     *
     * @param optimizePositive
     *                 true if you want to optimize the number of bytes needed to write the length value
     */
    public static
    int intLength(int value, boolean optimizePositive) {
        return dorkbox.bytes.OptimizeUtilsByteBuf.intLength(value, optimizePositive);
    }

    // int

    /**
     * look at buffer, and see if we can read the length of the int off of it. (from the reader index)
     *
     * @return 0 if we could not read anything, >0 for the number of bytes for the int on the buffer
     */
    @SuppressWarnings("SimplifiableIfStatement")
    public static
    boolean canReadInt(byte[] buffer) {
        int position = 0;
        return canReadInt(buffer, position);
    }

    /**
     * FROM KRYO
     * <p>
     * look at buffer, and see if we can read the length of the int off of it. (from the reader index)
     *
     * @param position where in the buffer to start reading
     * @return 0 if we could not read anything, >0 for the number of bytes for the int on the buffer
     */
    @SuppressWarnings("SimplifiableIfStatement")
    public static
    boolean canReadInt(final byte[] buffer, int position) {
        int length = buffer.length;

        if (length >= 5) {
            return true;
        }
        if ((buffer[position++] & 0x80) == 0) {
            return true;
        }
        if (position == length) {
            return false;
        }
        if ((buffer[position++] & 0x80) == 0) {
            return true;
        }
        if (position == length) {
            return false;
        }
        if ((buffer[position++] & 0x80) == 0) {
            return true;
        }
        if (position == length) {
            return false;
        }
        if ((buffer[position++] & 0x80) == 0) {
            return true;
        }
        return position != length;
    }

    /**
     * Reads an int from the buffer that was optimized at position 0
     *
     * @param optimizePositive
     *                 If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (5
     *                 bytes).  This ultimately means that it will use fewer bytes for positive numbers.
     */
    public static
    int readInt(byte[] buffer, boolean optimizePositive) {
        int position = 0;
        return readInt(buffer, optimizePositive, position);
    }

    /**
     * FROM KRYO
     * <p>
     * Reads an int from the buffer that was optimized.
     *
     * @param position where in the buffer to start reading
     * @param optimizePositive
     *                 If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (5
     *                 bytes).  This ultimately means that it will use fewer bytes for positive numbers.
     */
    public static
    int readInt(final byte[] buffer, final boolean optimizePositive, int position) {
        int b = buffer[position++];
        int result = b & 0x7F;
        if ((b & 0x80) != 0) {
            b = buffer[position++];
            result |= (b & 0x7F) << 7;
            if ((b & 0x80) != 0) {
                b = buffer[position++];
                result |= (b & 0x7F) << 14;
                if ((b & 0x80) != 0) {
                    b = buffer[position++];
                    result |= (b & 0x7F) << 21;
                    if ((b & 0x80) != 0) {
                        b = buffer[position++];
                        result |= (b & 0x7F) << 28;
                    }
                }
            }
        }
        return optimizePositive ? result : result >>> 1 ^ -(result & 1);
    }

    /**
     * Writes the specified int to the buffer using 1 to 5 bytes, depending on the size of the number.
     *
     * @param optimizePositive
     *                 If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (5
     *                 bytes).  This ultimately means that it will use fewer bytes for positive numbers.
     *
     * @return the number of bytes written.
     */
    public static
    int writeInt(byte[] buffer, int value, boolean optimizePositive) {
        int position = 0;
        return writeInt(buffer, value, optimizePositive, position);
    }

    /**
     * FROM KRYO
     * <p>
     * Writes the specified int to the buffer using 1 to 5 bytes, depending on the size of the number.
     *
     * @param position where in the buffer to start writing
     * @param optimizePositive
     *                 If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (5
     *                 bytes).  This ultimately means that it will use fewer bytes for positive numbers.
     *
     * @return the number of bytes written.
     */
    public static
    int writeInt(final byte[] buffer, int value, final boolean optimizePositive, int position) {
        if (!optimizePositive) {
            value = value << 1 ^ value >> 31;
        }
        if (value >>> 7 == 0) {
            buffer[position++] = (byte) value;
            return 1;
        }
        if (value >>> 14 == 0) {
            buffer[position++] = (byte) (value & 0x7F | 0x80);
            buffer[position++] = (byte) (value >>> 7);
            return 2;
        }
        if (value >>> 21 == 0) {
            buffer[position++] = (byte) (value & 0x7F | 0x80);
            buffer[position++] = (byte) (value >>> 7 | 0x80);
            buffer[position++] = (byte) (value >>> 14);
            return 3;
        }
        if (value >>> 28 == 0) {
            buffer[position++] = (byte) (value & 0x7F | 0x80);
            buffer[position++] = (byte) (value >>> 7 | 0x80);
            buffer[position++] = (byte) (value >>> 14 | 0x80);
            buffer[position++] = (byte) (value >>> 21);
            return 4;
        }
        buffer[position++] = (byte) (value & 0x7F | 0x80);
        buffer[position++] = (byte) (value >>> 7 | 0x80);
        buffer[position++] = (byte) (value >>> 14 | 0x80);
        buffer[position++] = (byte) (value >>> 21 | 0x80);
        buffer[position++] = (byte) (value >>> 28);
        return 5;
    }

    /**
     * Returns 1-9 bytes that would be written with {@link #writeLong(byte[], long, boolean)}.
     *
     * @param optimizePositive
     *                 If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (9
     *                 bytes). This ultimately means that it will use fewer bytes for positive numbers.
     */
    public static
    int longLength(long value, boolean optimizePositive) {
        return dorkbox.bytes.OptimizeUtilsByteBuf.longLength(value, optimizePositive);
    }


    // long

    /**
     * Reads a 1-9 byte long.
     *
     * @param optimizePositive
     *                 If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (9
     *                 bytes). This ultimately means that it will use fewer bytes for positive numbers.
     */
    public static
    long readLong(byte[] buffer, boolean optimizePositive) {
        int position = 0;
        return readLong(buffer, optimizePositive, position);
    }

    /**
     * FROM KRYO
     * <p>
     * Reads a 1-9 byte long.
     *
     * @param position where in the buffer to start reading
     * @param optimizePositive
     *                 If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (9
     *                 bytes). This ultimately means that it will use fewer bytes for positive numbers.
     */
    public static
    long readLong(final byte[] buffer, final boolean optimizePositive, int position) {
        int b = buffer[position++];
        long result = b & 0x7F;
        if ((b & 0x80) != 0) {
            b = buffer[position++];
            result |= (b & 0x7F) << 7;
            if ((b & 0x80) != 0) {
                b = buffer[position++];
                result |= (b & 0x7F) << 14;
                if ((b & 0x80) != 0) {
                    b = buffer[position++];
                    result |= (b & 0x7F) << 21;
                    if ((b & 0x80) != 0) {
                        b = buffer[position++];
                        result |= (long) (b & 0x7F) << 28;
                        if ((b & 0x80) != 0) {
                            b = buffer[position++];
                            result |= (long) (b & 0x7F) << 35;
                            if ((b & 0x80) != 0) {
                                b = buffer[position++];
                                result |= (long) (b & 0x7F) << 42;
                                if ((b & 0x80) != 0) {
                                    b = buffer[position++];
                                    result |= (long) (b & 0x7F) << 49;
                                    if ((b & 0x80) != 0) {
                                        b = buffer[position++];
                                        result |= (long) b << 56;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!optimizePositive) {
            result = result >>> 1 ^ -(result & 1);
        }
        return result;
    }

    /**
     * Writes a 1-9 byte long.
     *
     * @param optimizePositive
     *                 If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (9
     *                 bytes).
     *
     * @return the number of bytes written.
     */
    public static
    int writeLong(byte[] buffer, long value, boolean optimizePositive) {
        int position = 0;
        return writeLong(buffer, value, optimizePositive, position);
    }

    /**
     * FROM KRYO
     * <p>
     * Writes a 1-9 byte long.
     *
     * @param position where in the buffer to start writing
     * @param optimizePositive
     *                 If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be inefficient (9
     *                 bytes).
     *
     * @return the number of bytes written.
     */
    public static
    int writeLong(final byte[] buffer, long value, final boolean optimizePositive, int position) {
        if (!optimizePositive) {
            value = value << 1 ^ value >> 63;
        }
        if (value >>> 7 == 0) {
            buffer[position++] = (byte) value;
            return 1;
        }
        if (value >>> 14 == 0) {
            buffer[position++] = (byte) (value & 0x7F | 0x80);
            buffer[position++] = (byte) (value >>> 7);
            return 2;
        }
        if (value >>> 21 == 0) {
            buffer[position++] = (byte) (value & 0x7F | 0x80);
            buffer[position++] = (byte) (value >>> 7 | 0x80);
            buffer[position++] = (byte) (value >>> 14);
            return 3;
        }
        if (value >>> 28 == 0) {
            buffer[position++] = (byte) (value & 0x7F | 0x80);
            buffer[position++] = (byte) (value >>> 7 | 0x80);
            buffer[position++] = (byte) (value >>> 14 | 0x80);
            buffer[position++] = (byte) (value >>> 21);
            return 4;
        }
        if (value >>> 35 == 0) {
            buffer[position++] = (byte) (value & 0x7F | 0x80);
            buffer[position++] = (byte) (value >>> 7 | 0x80);
            buffer[position++] = (byte) (value >>> 14 | 0x80);
            buffer[position++] = (byte) (value >>> 21 | 0x80);
            buffer[position++] = (byte) (value >>> 28);
            return 5;
        }
        if (value >>> 42 == 0) {
            buffer[position++] = (byte) (value & 0x7F | 0x80);
            buffer[position++] = (byte) (value >>> 7 | 0x80);
            buffer[position++] = (byte) (value >>> 14 | 0x80);
            buffer[position++] = (byte) (value >>> 21 | 0x80);
            buffer[position++] = (byte) (value >>> 28 | 0x80);
            buffer[position++] = (byte) (value >>> 35);
            return 6;
        }
        if (value >>> 49 == 0) {
            buffer[position++] = (byte) (value & 0x7F | 0x80);
            buffer[position++] = (byte) (value >>> 7 | 0x80);
            buffer[position++] = (byte) (value >>> 14 | 0x80);
            buffer[position++] = (byte) (value >>> 21 | 0x80);
            buffer[position++] = (byte) (value >>> 28 | 0x80);
            buffer[position++] = (byte) (value >>> 35 | 0x80);
            buffer[position++] = (byte) (value >>> 42);
            return 7;
        }
        if (value >>> 56 == 0) {
            buffer[position++] = (byte) (value & 0x7F | 0x80);
            buffer[position++] = (byte) (value >>> 7 | 0x80);
            buffer[position++] = (byte) (value >>> 14 | 0x80);
            buffer[position++] = (byte) (value >>> 21 | 0x80);
            buffer[position++] = (byte) (value >>> 28 | 0x80);
            buffer[position++] = (byte) (value >>> 35 | 0x80);
            buffer[position++] = (byte) (value >>> 42 | 0x80);
            buffer[position++] = (byte) (value >>> 49);
            return 8;
        }
        buffer[position++] = (byte) (value & 0x7F | 0x80);
        buffer[position++] = (byte) (value >>> 7 | 0x80);
        buffer[position++] = (byte) (value >>> 14 | 0x80);
        buffer[position++] = (byte) (value >>> 21 | 0x80);
        buffer[position++] = (byte) (value >>> 28 | 0x80);
        buffer[position++] = (byte) (value >>> 35 | 0x80);
        buffer[position++] = (byte) (value >>> 42 | 0x80);
        buffer[position++] = (byte) (value >>> 49 | 0x80);
        buffer[position++] = (byte) (value >>> 56);
        return 9;
    }

    /**
     * look at buffer, and see if we can read the length of the long off of it (from the reader index).
     *
     * @return 0 if we could not read anything, >0 for the number of bytes for the long on the buffer
     */
    public static
    boolean canReadLong(byte[] buffer) {
        int position = 0;
        return canReadLong(buffer, position);
    }

    /**
     * FROM KRYO
     * <p>
     * look at buffer, and see if we can read the length of the long off of it (from the reader index).
     *
     * @param position where in the buffer to start reading
     * @return 0 if we could not read anything, >0 for the number of bytes for the long on the buffer
     */
    private static
    boolean canReadLong(final byte[] buffer, int position) {
        int limit = buffer.length;

        if (limit >= 9) {
            return true;
        }
        if ((buffer[position++] & 0x80) == 0) {
            return true;
        }
        if (position == limit) {
            return false;
        }
        if ((buffer[position++] & 0x80) == 0) {
            return true;
        }
        if (position == limit) {
            return false;
        }
        if ((buffer[position++] & 0x80) == 0) {
            return true;
        }
        if (position == limit) {
            return false;
        }
        if ((buffer[position++] & 0x80) == 0) {
            return true;
        }
        if (position == limit) {
            return false;
        }
        if ((buffer[position++] & 0x80) == 0) {
            return true;
        }
        if (position == limit) {
            return false;
        }
        if ((buffer[position++] & 0x80) == 0) {
            return true;
        }
        if (position == limit) {
            return false;
        }
        if ((buffer[position++] & 0x80) == 0) {
            return true;
        }
        if (position == limit) {
            return false;
        }
        //noinspection SimplifiableIfStatement
        if ((buffer[position++] & 0x80) == 0) {
            return true;
        }

        return position != limit;
    }

    private
    OptimizeUtilsByteArray() {
    }
}
