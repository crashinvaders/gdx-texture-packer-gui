package com.crashinvaders.basisu;

import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasisuWrapperTest {

    private static final String IMAGE_FILE = "level_temple0.basis";
    private static final int IMAGE_WIDTH = 512;
    private static final int IMAGE_HEIGHT = 512;
//    private static final String IMAGE_FILE = "frog.basis";
//    private static final int IMAGE_WIDTH = 256;
//    private static final int IMAGE_HEIGHT = 256;

    private static ByteBuffer basisBuffer;

    @BeforeClass
    public static void init() throws IOException {
        new com.badlogic.gdx.utils.SharedLibraryLoader().load("basisu-wrapper");

        System.out.println("Loading " + IMAGE_FILE);
        try (InputStream is = BasisuWrapperTest.class.getClassLoader().getResourceAsStream(IMAGE_FILE)) {
            byte[] bytes = TestUtils.readToByteArray(is);
            basisBuffer = TestUtils.asByteBuffer(bytes);
        }
    }

    @Test
    public void testImageInfo() {
        assertEquals(1, BasisuWrapper.getTotalImages(basisBuffer));
        assertEquals(1, BasisuWrapper.getTotalMipmapLevels(basisBuffer, 0));
        assertEquals(IMAGE_WIDTH, BasisuWrapper.getImageWidth(basisBuffer, 0, 0));
        assertEquals(IMAGE_HEIGHT, BasisuWrapper.getImageHeight(basisBuffer, 0, 0));
    }

    @Test
    public void testValidateHeader() {
        assertTrue(BasisuWrapper.validateHeader(basisBuffer));
    }

    @Test
    public void testTranscodeRgba32() {
        ByteBuffer rgba8888 = BasisuWrapper.transcodeRgba32(basisBuffer, 0, 0);

        // Check if encoding is correct.
        assertEquals(IMAGE_WIDTH * IMAGE_HEIGHT * 4, rgba8888.capacity());

        BufferedImage bufferedImage = TestUtils.fromRgba8888(rgba8888, IMAGE_WIDTH, IMAGE_HEIGHT);
        TestUtils.saveImagePng(bufferedImage, IMAGE_FILE + ".rgba32");
    }

    @Test
    public void testEncode() throws IOException {
        final String imageName = "frog.png";
        final int width = 256;
        final int height = 256;

        final ByteBuffer rgbaBuffer;

        try (InputStream is = BasisuWrapperTest.class.getClassLoader().getResourceAsStream(imageName)) {
            byte[] bytes = TestUtils.pngToRgbaBytes(is);
            rgbaBuffer = TestUtils.asByteBuffer(bytes);
        }

        ByteBuffer basisBuffer = BasisuWrapper.encode(rgbaBuffer, width, height, false, false, 1, false, false, false, 2f, 128, 0, 0);


        assertTrue(BasisuWrapper.validateHeader(basisBuffer));

        TestUtils.saveFile(basisBuffer, imageName.replace(".png", ".basis"));
    }
}
