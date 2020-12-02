package com.dk.scriptingengine.osspecific;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

//https://raw.githubusercontent.com/nshusa/dynamic-buffer/master/src/io/nshusa/commons/io/DynamicByteBuf.java
/**
 * A smart, fast and efficient container that can store data types and re-size
 * itself.
 *
 * This buffer resizes itself if the amount of bytes being written exceeds its
 * capacity. A new size twice the size of the original buffer will be created.
 * This buffer also doesn't use any underlying Java functions to reduce any
 * overhead. Currently, this class only supports default java primitive types.
 * Down the road this class will offer support for endianness.
 *
 * @author Chad Adams
 */
public final class DynamicByteBuf {

    /**
     * The initial amount of bytes that can be written to this buffer without being
     * resized.
     */
    private static final int INITIAL_CAPACITY = 512;

    /**
     * The payload which will hold all of the actual data for this buffer.
     */
    private byte payload[];

    /**
     * The position in the byte array that indicates where the next byte will be
     * read from.
     */
    private int readerIndex;

    /**
     * The position in the byte array that indicates where the next byte will be
     * written to.
     */
    private int writerIndex;

    /**
     * The flag that indicates this buffer can only be read.
     */
    private boolean readOnly;

    /**
     * The private constructor so the user don't have to use the 'new' keyword.
     */
    private DynamicByteBuf() {
        this(false, INITIAL_CAPACITY);
    }

    /**
     * The private constructor so the user don't have to use the 'new' keyword.
     *
     * @param capacity
     *            The amount of bytes that can be written without resizing the
     *            buffer.
     */
    private DynamicByteBuf(int capacity) {
        this(false, capacity);
    }

    /**
     * The private constructor so the user don't have to use the 'new' keyword.
     *
     * @param readOnly
     *            The flag that prevents data being written into the buffer.
     *
     * @param capacity
     *            The amount of bytes that can be written without resizing the
     *            buffer.
     */
    private DynamicByteBuf(boolean readOnly, int capacity) {
        this.payload = new byte[capacity];
        this.readOnly = readOnly;
    }

    /**
     * The private constructor so the user don't have to use the 'new' keyword.
     *
     * @param readOnly
     *            The flag that prevents data being written into the buffer.
     *
     * @param data
     *            The data that will be contained in this buffer.
     */
    private DynamicByteBuf(boolean readOnly, byte[] data) {
        this.payload = data;
        this.readOnly = readOnly;
    }

    /**
     * The private constructor so the user don't have to use the 'new' keyword.
     *
     * @return The newly created container.
     */
    public static DynamicByteBuf create() {
        DynamicByteBuf buffer = new DynamicByteBuf();
        buffer.readerIndex = 0;
        buffer.payload = new byte[INITIAL_CAPACITY];
        return buffer;
    }

    /**
     * Creates a new {@link DynamicByteBuf} with the initial capacity of
     * {@code allocate}.
     *
     * @param allocate
     *            The amount of bytes that can be written to this container until
     *            the container needs to be resized.
     *
     * @return The newly created container.
     */
    public static DynamicByteBuf allocate(int allocate) {
        return new DynamicByteBuf(false, allocate);
    }

    /**
     * Creates a new {@link DynamicByteBuf} with the initial capacity of
     * {@code allocate}.
     *
     * @param readOnly
     *            The flag that indicates data cannot be written to this buffer.
     *
     * @param allocate
     *            The amount of bytes that can be written to this container until
     *            the container needs to be resized.
     *
     * @return The newly created container.
     */
    public static DynamicByteBuf allocate(boolean readOnly, int allocate) {
        return new DynamicByteBuf(readOnly, allocate);
    }

    /**
     * Wraps bytes and resizes this buffer to match the length of the wrapped bytes.
     *
     * @param data
     *            The data that will be copied into this buffer.
     */
    public static DynamicByteBuf wrap(byte[] data) {
        return new DynamicByteBuf(false, data);
    }

    /**
     * Wraps bytes and resizes this buffer to match the length of the wrapped bytes.
     *
     * @param readOnly
     *            The flag that indicates data cannot be written to this buffer.
     *
     * @param data
     *            The data that will be copied into this buffer.
     */
    public static DynamicByteBuf wrap(boolean readOnly, byte[] data) {
        return new DynamicByteBuf(readOnly, data);
    }

    /**
     * Places a value of type {@link Byte} into this container.
     *
     * @param value
     *            The value that will be placed into this container as a byte.
     */
    public DynamicByteBuf writeByte(int value) {
        if (readOnly) {
            throw new IllegalStateException("buffer state=%s read only buffer.");
        }
        validateCapacity(Byte.BYTES);

        payload[writerIndex++] = (byte) value;
        return this;
    }

    /**
     * Places a value of type {@link Byte} into this container.
     *
     * @param source
     *            The bytes that will be placed into this container.
     */
    public DynamicByteBuf writeBytes(byte source[]) {
        if (readOnly) {
            throw new IllegalStateException("buffer state=%s read only buffer.");
        }
        validateCapacity(source.length);
        for (int index = 0; index < source.length; index++) {
            payload[writerIndex++] = source[index];
        }
        return this;
    }

    /**
     * Places a value of type {@link Byte} into this container.
     *
     * @param source
     *            The bytes that will be placed into this container.
     *
     * @param offset
     *            The value that indicates an offset from the current position.
     *
     * @param length
     *            The amount of bytes that will be placed into this container.
     */
    public DynamicByteBuf writeBytes(byte source[], int offset, int length) {
        if (readOnly) {
            throw new IllegalStateException("buffer state=%s read only buffer.");
        }
        validateCapacity(length);
        for (int index = length; index < length + offset; index++) {
            payload[writerIndex++] = source[index];
        }
        return this;
    }

    /**
     * Places a value of type {@link Short} into this container in big endian order.
     *
     * @param value
     *            The value that will be placed into this container.
     */
    public DynamicByteBuf writeShort(int value) {
        if (readOnly) {
            throw new IllegalStateException("buffer state=%s read only buffer.");
        }
        validateCapacity(Short.BYTES);
        payload[writerIndex++] = (byte) (value >> 8);
        payload[writerIndex++] = (byte) value;
        return this;
    }

    /**
     * Places a value of type {@link Short} into this container in little endian
     * order.
     *
     * @param value
     *            The value that will be placed into this container.
     */
    public DynamicByteBuf writeLEShort(int value) {
        if (readOnly) {
            throw new IllegalStateException("buffer state=%s read only buffer.");
        }
        validateCapacity(Short.BYTES);
        payload[writerIndex++] = (byte) value;
        payload[writerIndex++] = (byte) (value >> 8);
        return this;
    }

    /**
     * Places a value of type {@link Integer} into this container in big endian
     * order.
     *
     * @param value
     *            The value that will be placed into this container.
     */
    public DynamicByteBuf writeInt(int value) {
        if (readOnly) {
            throw new IllegalStateException("buffer state=%s read only buffer.");
        }
        validateCapacity(Integer.BYTES);
        payload[writerIndex++] = (byte) (value >> 24);
        payload[writerIndex++] = (byte) (value >> 16);
        payload[writerIndex++] = (byte) (value >> 8);
        payload[writerIndex++] = (byte) value;
        return this;
    }

    /**
     * Places a 24-bit signed {@link Integer} into this container in big endian
     * order.
     *
     * @param value
     *		The value that will be placed into this container.
     */
    public DynamicByteBuf write24Int(int value) {
        if (readOnly) {
            throw new IllegalStateException("buffer state=%s read only buffer.");
        }
        validateCapacity(3);
        payload[writerIndex++] = (byte) (value >> 16);
        payload[writerIndex++] = (byte) (value >> 8);
        payload[writerIndex++] = (byte) value;
        return this;
    }

    /**
     * Writes an {@link Integer} value in little endian order.
     *
     * @param value
     *            The value that will be placed into this container.
     */
    public DynamicByteBuf writeLEInt(int value) {
        if (readOnly) {
            throw new IllegalStateException("buffer state=%s read only buffer.");
        }
        validateCapacity(Integer.BYTES);
        payload[writerIndex++] = (byte) value;
        payload[writerIndex++] = (byte) (value >> 8);
        payload[writerIndex++] = (byte) (value >> 16);
        payload[writerIndex++] = (byte) (value >> 24);
        return this;
    }

    /**
     * Places a value of type {@link Long} into this container in big endian order.
     *
     * @param value
     *            The value that will be placed into this container.
     */
    public DynamicByteBuf writeLong(long value) {
        if (readOnly) {
            throw new IllegalStateException("buffer state=%s read only buffer.");
        }
        validateCapacity(Long.BYTES);
        payload[writerIndex++] = (byte) (int) (value >> 56);
        payload[writerIndex++] = (byte) (int) (value >> 48);
        payload[writerIndex++] = (byte) (int) (value >> 40);
        payload[writerIndex++] = (byte) (int) (value >> 32);
        payload[writerIndex++] = (byte) (int) (value >> 24);
        payload[writerIndex++] = (byte) (int) (value >> 16);
        payload[writerIndex++] = (byte) (int) (value >> 8);
        payload[writerIndex++] = (byte) (int) value;
        return this;
    }

    /**
     * Places a value of type {@link Long} into this container in little endian
     * order.
     *
     * @param value
     *            The value that will be placed into this container.
     */
    public DynamicByteBuf writeLELong(long value) {
        if (readOnly) {
            throw new IllegalStateException("buffer state=%s read only buffer.");
        }
        validateCapacity(Long.BYTES);
        payload[writerIndex++] = (byte) (int) value;
        payload[writerIndex++] = (byte) (int) (value >> 8);
        payload[writerIndex++] = (byte) (int) (value >> 16);
        payload[writerIndex++] = (byte) (int) (value >> 24);
        payload[writerIndex++] = (byte) (int) (value >> 32);
        payload[writerIndex++] = (byte) (int) (value >> 40);
        payload[writerIndex++] = (byte) (int) (value >> 48);
        payload[writerIndex++] = (byte) (int) (value >> 56);
        return this;
    }

    /**
     * Places a value of type {@link Boolean} into this container.
     *
     * @param flag
     * 		The flag that will be put into this container.
     */
    public DynamicByteBuf writeBoolean(boolean flag) {
        if (readOnly) {
            throw new IllegalStateException("buffer state=%s read only buffer.");
        }
        validateCapacity(Byte.BYTES);
        payload[writerIndex++] = flag ? (byte) 1 : (byte) 0;
        return this;
    }

    public boolean readBoolean() {
        checkRemainingBytes(Byte.BYTES);
        return readByte() == 1;
    }

    /**
     * Gets a value at the current position from this container as a byte.
     *
     * @return The value.
     */
    public int readByte() {
        checkRemainingBytes(Byte.BYTES);

        return payload[readerIndex++];
    }

    /**
     * Gets a value at the current position from this container as an unsigned byte.
     *
     * @return The value.
     */
    public int readUnsignedByte() {
        checkRemainingBytes(Byte.BYTES);

        return payload[readerIndex++] & 0xff;
    }

    /**
     * Reads a signed short from this container in big endian order.
     *
     * @return The value read from this container.
     */
    public int readShort() {
        checkRemainingBytes(Short.BYTES);

        readerIndex += Short.BYTES;

        return (payload[readerIndex - 2] << 8) + (payload[readerIndex - 1] & 0xff);
    }

    /**
     * Reads an unsigned short from this container in big endian order.
     *
     * @return The value read from this container.
     */
    public int readUnsignedShort() {
        checkRemainingBytes(Short.BYTES);

        readerIndex += Short.BYTES;

        return ((payload[readerIndex - 2] & 0xff) << 8) + (payload[readerIndex - 1] & 0xff);
    }

    /**
     * Reads a short from this container in big endian order.
     *
     * @return The value read from this container.
     */
    public int readLEShort() {
        checkRemainingBytes(Short.BYTES);

        readerIndex += Short.BYTES;

        return (payload[readerIndex - 2] & 0xff) + ((payload[readerIndex - 1] & 0xff) << 8);
    }

    /**
     * Reads an integer from this container in big endian order.
     *
     * @return The value read from this container.
     */
    public int readInt() {
        checkRemainingBytes(Integer.BYTES);

        readerIndex += Integer.BYTES;

        return ((payload[readerIndex - 4] & 0xff) << 24) + ((payload[readerIndex - 3] & 0xff) << 16)
                + ((payload[readerIndex - 2] & 0xff) << 8) + (payload[readerIndex - 1] & 0xff);
    }

    public double readFloat() {
        return 0;
    }

    public DynamicByteBuf writeFloat(float val)
    {
        writeBytes(float2ByteArray(val));
        return this;
    }

    public static byte [] long2ByteArray (long value)
    {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    public static byte [] float2ByteArray (float value)
    {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    /**
     * Reads a 24 bit signed integer from this container in big endian order.
     *
     * @return The value read from this container.
     */
    public int read24Int() {
        checkRemainingBytes(3);

        readerIndex += 3;

        return ((payload[readerIndex - 3] << 16) + ((payload[readerIndex - 2] & 0xFF) << 8) + (payload[readerIndex - 1] & 0xFF));
    }

    public int read24UInt() {
        checkRemainingBytes(3);

        readerIndex += 3;

        return (((payload[readerIndex - 3] & 0xFF) << 16) + ((payload[readerIndex - 2] & 0xFF) << 8) + (payload[readerIndex - 1] & 0xFF));
    }

    /**
     * Reads an integer from this container in big endian order.
     *
     * @return The value read from this container.
     */
    public int readLEInt() {
        checkRemainingBytes(Integer.BYTES);

        readerIndex += Integer.BYTES;

        return (payload[readerIndex - 4] & 0xff) + ((payload[readerIndex - 3] & 0xff) << 8)	+ ((payload[readerIndex - 2] & 0xff) << 16) + ((payload[readerIndex - 1] & 0xff) << 24);
    }


    /**
     * Reads a long from this container in big endian order.
     *
     * @return The value read from this container.
     */
    public long readLong() {
        return (((long) readInt()) << 32) | (readInt() & 0xffffffffL);
    }

    /**
     * Reads a long from this container in little endian order.
     *
     * @return The value read from this container.
     */
    public long readLELong() {
        return (readLEInt() & 0xffffffffL) | (((long)readLEInt()) << 32);
    }

    /**
     * Puts a {@link String} into this container using {@code UTF-8) as a default character set.
     *
     * This method puts a null character at the end of the string to indicate the string ends.
     *
     * @param string The string that will be placed into this container.
     */
    public DynamicByteBuf writeString(String string) {
        return writeString(string, false);
    }

    /**
     * Puts a {@link String} into this container using {@code UTF-8) as a default character set.
     *
     * This method puts a null character at the end of the string to indicate the string ends.
     *
     * @param string The string that will be placed into this container.
     * @param boolean terminate
     */
    public DynamicByteBuf writeString(String string, boolean terminate) {
        if (readOnly) {
            throw new IllegalStateException("buffer state=%s read only buffer.");
        }
        writeBytes(string.getBytes(StandardCharsets.UTF_8));
        if(terminate)
            writeByte(0); // null character
        return this;
    }

    /**
     * Gets a string from this container in UTF-8 format.
     *
     * @return The string that was read from this container.
     */
    public String readString() {
        StringBuilder bldr = new StringBuilder();

        int b;
        while ((b = (readByte() & 0xFF)) != 0) {
            bldr.append((char) b);
        }
        return bldr.toString();
    }

    /**
     * Gets a value at the current position from this container as a sequence of
     * bytes.
     *
     * @param target
     *            The destination the bytes read from this container will go to.
     *
     * @param offset
     *            The value that indicates an offset from the current position of
     *            this container.
     *
     * @param length
     *            The value that indicates the amount of bytes that will be read.
     *
     * @return The value.
     */
    public void readBytes(byte target[], int offset, int length) {
        checkRemainingBytes(length);

        for (int index = length; index < length + offset; index++) {
            target[index] = payload[readerIndex++];
        }
    }

    /**
     * Gets a value at the current position from this container as a sequence of
     * bytes.
     *
     * @param target
     *            The destination the bytes read from this container will go to.
     *
     * @param offset
     *            The value that indicates an offset from the current position of
     *            this container.
     *
     * @param length
     *            The value that indicates the amount of bytes that will be read.
     *
     * @return The value.
     */
    public void readBytesReverse(byte target[], int offset, int length) {
        checkRemainingBytes(length);

        for (int index = (length + offset) - 1; index >= length; index--) {
            target[index] = payload[readerIndex++];
        }
    }

    /**
     * Checks to see if the amount of bytes about to be written will exceed the
     * capacity of this buffer. If the capacity will be exceeded, a new array twice
     * the size will be created. All of the bytes are transfered and the position
     * doesn't change.
     *
     * @param bytes
     *            The amount of bytes that will be added.
     */
    private void validateCapacity(int bytes) {
        if ((writerIndex + bytes) > payload.length) {

            System.out.println("detected capacity overflow");

            byte[] array = new byte[payload.length * 2];

            System.out.println(String.format("constructing a new array of size: %d\n", array.length));

            System.arraycopy(payload, 0, array, 0, payload.length);

            payload = array;

            validateCapacity(bytes);
        }
    }

    private void checkRemainingBytes(int bytes) {
        if (bytes > this.readableBytes()) {
            throw new RuntimeException(String.format("amount=%d exceeds readable bytes=%d", bytes, readableBytes()));
        }
    }

    /**
     * Gets the number of bytes that can be read from this container.
     *
     * @return The number of bytes that can be read.
     */
    public int readableBytes() {
        return writerIndex - readerIndex;
    }

    /**
     * Determines if there are any bytes in this container that can be read.
     *
     * @return {@code true} If there are any bytes that can be read from this
     *         container. {@code false} otherwise.
     */
    public boolean hasRemaining() {
        return writerIndex - readerIndex > 0;
    }

    /**
     * Sets the reader index to a new position.
     *
     * @param readerIndex
     *            The new position to set.
     */
    public void setReaderIndex(int readerIndex) {
        if ((this.readerIndex + readerIndex) > readableBytes()) {
            throw new RuntimeException(String.format("readerIndex=%d param=%d nextIndex=%d exceeds readableBytes=%d",
                    this.readerIndex, readerIndex, (this.readerIndex + readerIndex), readableBytes()));
        }
        this.readerIndex = readerIndex;
    }

    /**
     * Gets the current position of the reader pointer.
     *
     * @return The current position where data will be read from.
     */
    public int getReaderIndex() {
        return readerIndex;
    }

    /**
     * Sets the writer index to a new position.
     *
     * @param writerIndex
     *            The new position to set.
     */
    public void setWriterIndex(int writerIndex) {
        this.writerIndex = writerIndex;
    }

    /**
     * Gets the current position of the writer pointer.
     *
     * @return The current position where data will be written to.
     */
    public int getWriterIndex() {
        return writerIndex;
    }

    /**
     * The amount of bytes that can be contained within this buffer.
     */
    public int capacity() {
        return payload.length;
    }

    /**
     * Gets the bytes of this container.
     *
     * @return The bytes.
     */
    public byte[] toArray() {
        if (writerIndex > 0) {
            byte[] reduced = new byte[writerIndex];

            System.arraycopy(payload, 0, reduced, 0, reduced.length);

            return reduced;
        }

        return payload;
    }
}

