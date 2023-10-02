package com.crashinvaders.common.basisu;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

class BasisuUtils {
    /**
     * Reads the file content into the {@link ByteBuffer}.
     * It uses unsafe (direct) byte buffer for all the platforms except for GWT,
     * so don't forget to free it using {@link BufferUtils#disposeUnsafeByteBuffer(ByteBuffer)}.
     */
    public static ByteBuffer readFileIntoBuffer(FileHandle file) {
        byte[] buffer = new byte[1024 * 10];
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(file.read()));
            int fileSize = (int)file.length();

            // We use unsafe (direct) byte buffer everywhere but not on GWT as it doesn't support it.
            final ByteBuffer byteBuffer = BufferUtils.newUnsafeByteBuffer(fileSize);

            int readBytes = 0;
            while ((readBytes = in.read(buffer)) != -1) {
                byteBuffer.put(buffer, 0, readBytes);
            }
            ((Buffer)byteBuffer).position(0);
            ((Buffer)byteBuffer).limit(byteBuffer.capacity());
            return byteBuffer;
        } catch (Exception e) {
            throw new BasisuGdxException("Couldn't load file '" + file + "'", e);
        } finally {
            StreamUtils.closeQuietly(in);
        }
    }
}
