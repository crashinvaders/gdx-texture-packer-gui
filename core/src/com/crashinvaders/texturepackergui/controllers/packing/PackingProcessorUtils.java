package com.crashinvaders.texturepackergui.controllers.packing;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.controllers.TinifyService;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.model.ScaleFactorModel;
import com.crashinvaders.texturepackergui.controllers.packing.processors.*;
import com.crashinvaders.texturepackergui.utils.packprocessing.CompositePackProcessor;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;

public class PackingProcessorUtils {

    public static Array<PackProcessingNode> prepareProcessingNodes(ProjectModel project, Array<PackModel> packs) {
        Array<PackProcessingNode> result = new Array<>();
        for (PackModel pack : packs) {
            for (ScaleFactorModel scaleFactor : pack.getScaleFactors()) {
                PackModel newPack = new PackModel(pack);
                newPack.setScaleFactors(Array.with(scaleFactor));
                TexturePacker.Settings settings = newPack.getSettings();
                settings.scaleSuffix[0] = "";
                settings.scale[0] = scaleFactor.getFactor();
                settings.scaleResampling[0] = scaleFactor.getResampling();

                PackProcessingNode processingNode = new PackProcessingNode(project, newPack);
                processingNode.setOrigPack(pack);

                result.add(processingNode);
            }
        }
        return result;
    }

    public static CompositePackProcessor prepareRegularProcessorSequence(TinifyService tinifyService) {
        return new CompositePackProcessor(
                // Startup metadata
                new StartTimeMetadataProcessor(),

                // Validation
                new DataValidationProcessor(),

                // File type
                new PngFileTypeProcessor(),
                new JpegFileTypeProcessor(),
                new KtxFileTypeProcessor(),
                new BasisuFileTypeProcessor(),

                // Packing
                new PackingProcessor(),

                // Png compressors
                new PngtasticCompressionProcessor(),
                new ZopfliCompressionProcessor(),
                new TinifyCompressionProcessor(tinifyService),
                new TePng8CompressionProcessor(),
                new PngquantCompressionProcessor(),

                // Trailing metadata
                new FileSizeMetadataProcessor(),
                new PageAmountMetadataProcessor(),
                new EndTimeMetadataProcessor(),
                new TotalTimeMetadataProcessor(),
                new WarningMetadataProcessor());
    }
}
