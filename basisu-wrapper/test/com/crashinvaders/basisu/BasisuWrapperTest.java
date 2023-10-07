package com.crashinvaders.basisu;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasisuWrapperTest {

    private static final String IMAGE_BASIS_NAME = "level_temple0.basis";
    private static final String IMAGE_KTX2_NAME = "screen_stuff.ktx2";
    private static final String IMAGE_RGBA_NAME = "frog.png";
    private static final int IMAGE_RGBA_SIZE = 256;
    private static final int IMAGE_KTX2_SIZE = 2048;
    private static final int IMAGE_BASIS_SIZE = 512;

    private static ByteBuffer imageBasisBuffer;
    private static ByteBuffer imageKtx2Buffer;
    private static ByteBuffer imageRgbaBuffer;

    @BeforeClass
    public static void init() throws IOException {
        new SharedLibraryLoader().load("basisu-wrapper");

        System.out.println("Loading " + IMAGE_BASIS_NAME);
        try (InputStream is = BasisuWrapperTest.class.getClassLoader().getResourceAsStream(IMAGE_BASIS_NAME)) {
            byte[] bytes = TestUtils.readToByteArray(is);
            imageBasisBuffer = TestUtils.asByteBuffer(bytes);
        }

        System.out.println("Loading " + IMAGE_KTX2_NAME);
        try (InputStream is = BasisuWrapperTest.class.getClassLoader().getResourceAsStream(IMAGE_KTX2_NAME)) {
            byte[] bytes = TestUtils.readToByteArray(is);
            imageKtx2Buffer = TestUtils.asByteBuffer(bytes);
        }

        System.out.println("Loading " + IMAGE_RGBA_NAME);
        try (InputStream is = BasisuWrapperTest.class.getClassLoader().getResourceAsStream(IMAGE_RGBA_NAME)) {
            byte[] bytes = TestUtils.pngToRgbaBytes(is);
            imageRgbaBuffer = TestUtils.asByteBuffer(bytes);
        }
    }

    @Test
    public void testBasisImageInfo() {
        assertEquals(1, BasisuWrapper.basisGetTotalImages(imageBasisBuffer));
        assertEquals(1, BasisuWrapper.basisGetTotalMipmapLevels(imageBasisBuffer, 0));
        assertEquals(IMAGE_BASIS_SIZE, BasisuWrapper.basisGetImageWidth(imageBasisBuffer, 0, 0));
        assertEquals(IMAGE_BASIS_SIZE, BasisuWrapper.basisGetImageHeight(imageBasisBuffer, 0, 0));
    }

    @Test
    public void testBasisValidateHeader() {
        assertTrue(BasisuWrapper.basisValidateHeader(imageBasisBuffer));
    }

    @Test
    public void testBasisTranscodeRgba32() {
        ByteBuffer rgba8888 = BasisuWrapper.basisTranscodeRgba32(imageBasisBuffer, 0, 0);

        // Check if encoding is correct.
        assertEquals(IMAGE_BASIS_SIZE * IMAGE_BASIS_SIZE * 4, rgba8888.capacity());

        BufferedImage bufferedImage = TestUtils.fromRgba8888(rgba8888, IMAGE_BASIS_SIZE, IMAGE_BASIS_SIZE);
        TestUtils.saveImagePng(bufferedImage, IMAGE_BASIS_NAME + ".rgba32");
    }

    @Test
    public void testKtx2ImageInfo() {
        assertEquals(0, BasisuWrapper.ktx2GetTotalLayers(imageKtx2Buffer));
        assertEquals(1, BasisuWrapper.ktx2GetTotalMipmapLevels(imageKtx2Buffer));
        assertEquals(IMAGE_KTX2_SIZE, BasisuWrapper.ktx2GetImageWidth(imageKtx2Buffer));
        assertEquals(IMAGE_KTX2_SIZE, BasisuWrapper.ktx2GetImageHeight(imageKtx2Buffer));
    }

    @Test
    public void testKtx2TranscodeRgba32() {
        ByteBuffer rgba8888 = BasisuWrapper.ktx2TranscodeRgba32(imageKtx2Buffer, 0, 0);

        // Check if encoding is correct.
        assertEquals(IMAGE_KTX2_SIZE * IMAGE_KTX2_SIZE * 4, rgba8888.capacity());

        BufferedImage bufferedImage = TestUtils.fromRgba8888(rgba8888, IMAGE_KTX2_SIZE, IMAGE_KTX2_SIZE);
        TestUtils.saveImagePng(bufferedImage, IMAGE_KTX2_NAME + ".rgba32");
    }

    @Test
    public void testEncodeEtc1s() {
        ByteBuffer rgbaBuffer = imageRgbaBuffer;
        int width = IMAGE_RGBA_SIZE;
        int height = IMAGE_RGBA_SIZE;
        boolean uastc = false;
        boolean ktx2 = false;
        ByteBuffer basisBuffer = BasisuWrapper.encode(rgbaBuffer, width, height, uastc, ktx2, false, 1, false, false, false, 2f, 128, 0, 0);

        assertTrue(BasisuWrapper.basisValidateHeader(basisBuffer));

        TestUtils.saveFile(basisBuffer, IMAGE_RGBA_NAME.replace(".png", ".etc1s.basis"));
    }

    @Test
    public void testEncodeUastc() {
        ByteBuffer rgbaBuffer = imageRgbaBuffer;
        int width = IMAGE_RGBA_SIZE;
        int height = IMAGE_RGBA_SIZE;
        boolean uastc = true;
        boolean ktx2 = false;
        ByteBuffer basisBuffer = BasisuWrapper.encode(rgbaBuffer, width, height, uastc, ktx2, false, 1, false, false, false, 2f, 128, 0, 0);

        assertTrue(BasisuWrapper.basisValidateHeader(basisBuffer));

        TestUtils.saveFile(basisBuffer, IMAGE_RGBA_NAME.replace(".png", ".uastc.basis"));
    }

    @Test
    public void testEncodeKtx2() {
        ByteBuffer rgbaBuffer = imageRgbaBuffer;
        int width = IMAGE_RGBA_SIZE;
        int height = IMAGE_RGBA_SIZE;
        boolean uastc = true;
        boolean ktx2 = true;
        ByteBuffer basisBuffer = BasisuWrapper.encode(rgbaBuffer, width, height, uastc, ktx2, false, 1, false, false, false, 2f, 128, 0, 0);

        //TODO Use KTX2 specific validation when it's ready.
        //assertTrue(BasisuWrapper.ktx2ValidateHeader(basisBuffer));

        TestUtils.saveFile(basisBuffer, IMAGE_RGBA_NAME.replace(".png", ".ktx2"));
    }
}
