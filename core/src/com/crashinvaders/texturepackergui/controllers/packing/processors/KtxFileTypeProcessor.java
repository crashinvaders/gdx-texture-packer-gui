package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.tools.texturepacker.PngPageFileWriter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.crashinvaders.texturepackergui.services.model.FileTypeType;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.services.model.filetype.KtxFileTypeModel;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.crashinvaders.texturepackergui.utils.KtxEtc1Processor;
import com.crashinvaders.texturepackergui.utils.KtxEtc2Processor;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class KtxFileTypeProcessor implements PackProcessor {

    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        PackModel pack = node.getPack();
        ProjectModel project = node.getProject();

        if (project.getFileType().getType() != FileTypeType.KTX) return;

        KtxFileTypeModel fileType = project.getFileType();

        pack.getSettings().format = Pixmap.Format.RGBA8888;

        switch (fileType.getFormat()) {
            case ETC1:
                boolean alphaChanel = fileType.getEncodingEtc1() == KtxFileTypeModel.EncodingETC1.RGBA;
                node.setPageFileWriter(new KtxEtc1PageFileWriter(alphaChanel, fileType.isZipping()));
                break;
            case ETC2:
                node.setPageFileWriter(new KtxEtc2PageFileWriter(fileType.getEncodingEtc2().format, fileType.isZipping()));
                break;
        }
    }

    public static class KtxEtc1PageFileWriter extends PngPageFileWriter {

        private final boolean alphaChannel;
        private final boolean zipping;

        public KtxEtc1PageFileWriter(boolean alphaChannel, boolean zipping) {
            this.zipping = zipping;
            this.alphaChannel = alphaChannel;
        }

        @Override
        public String getFileExtension() {
            return zipping ? "zktx" : "ktx";
        }

        @Override
        public void saveToFile(TexturePacker.Settings settings, BufferedImage image, File file) throws IOException {
            FileHandle tmpPngFile = new FileHandle(File.createTempFile(file.getName(), null));
            FileHandle output = new FileHandle(file);

            super.saveToFile(settings, image, tmpPngFile.file());

            KtxEtc1Processor.process(tmpPngFile, output, alphaChannel);
            tmpPngFile.delete();

            if (zipping) {
                FileUtils.gzip(output);
            }
        }
    }

    public static class KtxEtc2PageFileWriter extends PngPageFileWriter {

        private final KtxEtc2Processor.PixelFormat pixelFormat;
        private final boolean zipping;

        public KtxEtc2PageFileWriter(KtxEtc2Processor.PixelFormat pixelFormat, boolean zipping) {
            this.zipping = zipping;
            this.pixelFormat = pixelFormat;
        }

        @Override
        public String getFileExtension() {
            return zipping ? "zktx" : "ktx";
        }

        @Override
        public void saveToFile(TexturePacker.Settings settings, BufferedImage image, File file) throws IOException {
            FileHandle tmpPngFile = new FileHandle(File.createTempFile(file.getName(), null));
            FileHandle output = new FileHandle(file);

            super.saveToFile(settings, image, tmpPngFile.file());

            KtxEtc2Processor.process(tmpPngFile, output, pixelFormat);
            tmpPngFile.delete();

            if (zipping) {
                FileUtils.gzip(output);
            }
        }
    }
}
