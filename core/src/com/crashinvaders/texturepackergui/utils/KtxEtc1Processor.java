
package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.ETC1;
import com.badlogic.gdx.graphics.glutils.ETC1.ETC1Data;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ETC1 (KTX container) converter.
 * Code is a lightweight version of {@link com.badlogic.gdx.tools.ktx.KTXProcessor}
 * with only support of ETC1 without cubefaces and mipmaps.
 */
public class KtxEtc1Processor {
    private final static byte[] HEADER_MAGIC = {(byte) 0x0AB, (byte) 0x04B, (byte) 0x054, (byte) 0x058, (byte) 0x020, (byte) 0x031,
            (byte) 0x031, (byte) 0x0BB, (byte) 0x00D, (byte) 0x00A, (byte) 0x01A, (byte) 0x00A};

    /**
     * Converts plain image into ETC1 KTX file.
     * @param input Png, Jpeg or Bitmap image file.
     * @param output converted KTX file will be written here.
     * @param alphaChanel target image height will be two times extended to put alpha image information there.
     */
    public static void process(FileHandle input, FileHandle output, boolean alphaChanel) {
        Pixmap imagePixmap = new Pixmap(input);
        imagePixmap.setBlending(Blending.None);
        imagePixmap.setFilter(Filter.BiLinear);
        int width = imagePixmap.getWidth();
        int height = imagePixmap.getHeight();

        ETC1Data etcData = null;

        // Create alpha atlas
        if (alphaChanel) {
            Pixmap pm = new Pixmap(width, height * 2, imagePixmap.getFormat());
            pm.setBlending(Blending.None);
            pm.setFilter(Filter.BiLinear);
            pm.drawPixmap(imagePixmap, 0, 0);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int alpha = (imagePixmap.getPixel(x, y)) & 0x0FF;
                    pm.drawPixel(x, y + height, (alpha << 24) | (alpha << 16) | (alpha << 8) | 0x0FF);
                }
            }
            imagePixmap.dispose();
            imagePixmap = pm;
        }

        // Perform ETC1 compression
        if (imagePixmap.getFormat() != Format.RGB888 && imagePixmap.getFormat() != Format.RGB565) {
            if (!alphaChanel) {
                System.out.println("Converting from " + imagePixmap.getFormat() + " to RGB888 for ETC1 compression");
            }
            Pixmap tmp = new Pixmap(imagePixmap.getWidth(), imagePixmap.getHeight(), Format.RGB888);
            tmp.setBlending(Blending.None);
            tmp.setFilter(Filter.BiLinear);
            tmp.drawPixmap(imagePixmap, 0, 0, 0, 0, imagePixmap.getWidth(), imagePixmap.getHeight());
            imagePixmap.dispose();
            imagePixmap = tmp;
        }

        etcData = ETC1.encodeImagePKM(imagePixmap);
        imagePixmap.dispose();
        imagePixmap = null;

        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new FileOutputStream(output.file()));

            out.write(HEADER_MAGIC);
            out.writeInt(0x04030201);
            out.writeInt(0); // glType
            out.writeInt(1); // glTypeSize
            out.writeInt(0); // glFormat
            out.writeInt(ETC1.ETC1_RGB8_OES); // glInternalFormat
            out.writeInt(GL20.GL_RGB); // glBaseInternalFormat
            out.writeInt(width);
            out.writeInt(alphaChanel ? (2 * height) : height);
            out.writeInt(0); // depth (not supported)
            out.writeInt(0); // n array elements (not supported)
            out.writeInt(1); // faces
            out.writeInt(0); // levels
            out.writeInt(0); // No additional info (key/value pairs)

            int faceLodSize = getSize(etcData);
            int faceLodSizeRounded = (faceLodSize + 3) & ~3;
            out.writeInt(faceLodSize);

            byte[] bytes = getBytes(etcData);
            out.write(bytes);

            for (int j = bytes.length; j < faceLodSizeRounded; j++) {
                out.write((byte) 0x00);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error writing to file: " + output.path(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static int getSize(ETC1Data etcData) {
        return etcData.compressedData.limit() - etcData.dataOffset;
    }

    private static byte[] getBytes(ETC1Data etcData) {
        byte[] result = new byte[getSize(etcData)];
        etcData.compressedData.position(etcData.dataOffset);
        etcData.compressedData.get(result);
        return result;
    }
}
