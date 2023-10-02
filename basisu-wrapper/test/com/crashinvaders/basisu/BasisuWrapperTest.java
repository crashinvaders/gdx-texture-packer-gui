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

    private static final String IMAGE_BASIS0_FILE = "level_temple0.basis";
    private static final String IMAGE_RGBA0_FILE = "frog.png";
    private static final int IMAGE_RGBA0_SIZE = 256;
    private static final int IMAGE_WIDTH = 512;
    private static final int IMAGE_HEIGHT = 512;
//    private static final String IMAGE_FILE = "frog.basis";
//    private static final int IMAGE_WIDTH = 256;
//    private static final int IMAGE_HEIGHT = 256;

    private static ByteBuffer imageBasis0Buffer;
    private static ByteBuffer imageRgba0Buffer;

    @BeforeClass
    public static void init() throws IOException {
        new SharedLibraryLoader().load("basisu-wrapper");

        System.out.println("Loading " + IMAGE_BASIS0_FILE);
        try (InputStream is = BasisuWrapperTest.class.getClassLoader().getResourceAsStream(IMAGE_BASIS0_FILE)) {
            byte[] bytes = TestUtils.readToByteArray(is);
            imageBasis0Buffer = TestUtils.asByteBuffer(bytes);
        }

        System.out.println("Loading " + IMAGE_RGBA0_FILE);
        try (InputStream is = BasisuWrapperTest.class.getClassLoader().getResourceAsStream(IMAGE_RGBA0_FILE)) {
            byte[] bytes = TestUtils.pngToRgbaBytes(is);
            imageRgba0Buffer = TestUtils.asByteBuffer(bytes);
        }
    }

    @Test
    public void testBasisImageInfo() {
        assertEquals(1, BasisuWrapper.basisGetTotalImages(imageBasis0Buffer));
        assertEquals(1, BasisuWrapper.basisGetTotalMipmapLevels(imageBasis0Buffer, 0));
        assertEquals(IMAGE_WIDTH, BasisuWrapper.basisGetImageWidth(imageBasis0Buffer, 0, 0));
        assertEquals(IMAGE_HEIGHT, BasisuWrapper.basisGetImageHeight(imageBasis0Buffer, 0, 0));
    }

    @Test
    public void testBasisValidateHeader() {
        assertTrue(BasisuWrapper.basisValidateHeader(imageBasis0Buffer));
    }

    @Test
    public void testBasisTranscodeRgba32() {
        ByteBuffer rgba8888 = BasisuWrapper.basisTranscodeRgba32(imageBasis0Buffer, 0, 0);

        // Check if encoding is correct.
        assertEquals(IMAGE_WIDTH * IMAGE_HEIGHT * 4, rgba8888.capacity());

        BufferedImage bufferedImage = TestUtils.fromRgba8888(rgba8888, IMAGE_WIDTH, IMAGE_HEIGHT);
        TestUtils.saveImagePng(bufferedImage, IMAGE_BASIS0_FILE + ".rgba32");
    }

    @Test
    public void testEncodeEtc1s() {
        ByteBuffer rgbaBuffer = imageRgba0Buffer;
        int width = IMAGE_RGBA0_SIZE;
        int height = IMAGE_RGBA0_SIZE;
        boolean uastc = false;
        boolean ktx2 = false;
        ByteBuffer basisBuffer = BasisuWrapper.encode(rgbaBuffer, width, height, uastc, ktx2, false, 1, false, false, false, 2f, 128, 0, 0);

        assertTrue(BasisuWrapper.basisValidateHeader(basisBuffer));

        TestUtils.saveFile(basisBuffer, IMAGE_RGBA0_FILE.replace(".png", ".etc1s.basis"));
    }

    @Test
    public void testEncodeUastc() {
        ByteBuffer rgbaBuffer = imageRgba0Buffer;
        int width = IMAGE_RGBA0_SIZE;
        int height = IMAGE_RGBA0_SIZE;
        boolean uastc = true;
        boolean ktx2 = false;
        ByteBuffer basisBuffer = BasisuWrapper.encode(rgbaBuffer, width, height, uastc, ktx2, false, 1, false, false, false, 2f, 128, 0, 0);

        assertTrue(BasisuWrapper.basisValidateHeader(basisBuffer));

        TestUtils.saveFile(basisBuffer, IMAGE_RGBA0_FILE.replace(".png", ".uastc.basis"));
    }

    @Test
    public void testEncodeKtx2() {
        ByteBuffer rgbaBuffer = imageRgba0Buffer;
        int width = IMAGE_RGBA0_SIZE;
        int height = IMAGE_RGBA0_SIZE;
        boolean uastc = true;
        boolean ktx2 = true;
        ByteBuffer basisBuffer = BasisuWrapper.encode(rgbaBuffer, width, height, uastc, ktx2, false, 1, false, false, false, 2f, 128, 0, 0);

        //TODO Use KTX2 specific validation when it's ready.
        //assertTrue(BasisuWrapper.ktx2ValidateHeader(basisBuffer));

        TestUtils.saveFile(basisBuffer, IMAGE_RGBA0_FILE.replace(".png", ".ktx2"));
    }
}
