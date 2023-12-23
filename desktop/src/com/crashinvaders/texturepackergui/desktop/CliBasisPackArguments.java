package com.crashinvaders.texturepackergui.desktop;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;

public class CliBasisPackArguments {

    @Option(name = "--container", aliases = { "-c" },
            metaVar = "KTX2 | BASIS",
            usage = "Texture file container. KTX2 is an open standard from the Khronos group " +
                    "and is generally recommended over the Basis file container, which is old, Basis Universal own format.")
    public BasisContainer container = BasisContainer.KTX2;

    @Option(name = "--format", aliases = { "-f" },
            metaVar = "ETC1S | UASTC",
            usage = "Intermediate texture format. " +
                    "ETC1S is low/mid quality with low file size. " +
                    "UASTC is high quality with slow encoding.")
    public BasisFormat format = BasisFormat.UASTC;

    @Option(name = "--quality", aliases = { "-q" },
            metaVar = "1-255",
            usage = "ETC1S output quality. " +
                    "The higher the quality value, the slower the encoding.")
    public int etcQuality = 128;

    @Option(name = "--compression", aliases = { "-m" },
            metaVar = "0-5",
            usage = "ETC1S compression level. " +
                    "This controls the amount of overall effort the encoder uses to optimize the ETC1S codebooks (palettes) " +
                    "and compressed data stream. Higher compression levels are significantly slower, and shouldn't be used unless necessary.")
    public int etcCompression = 2;

    @Option(name = "--output", aliases = { "-o" },
            usage = "The output KTX2/Basis file. " +
                    "If not specified, the output file will be written " +
                    "to the same location with a different extension (.ktx2/.basis)")
    public File outputFile = null;

    @Option(name = "--mipmap",
    usage = "Whether to generate the complete set of mipmap levels for the compressed image. " +
            "Every mipmap level has the size factor of 1/2 (half the size of the previous level).")
    public boolean mipmaps = false;

    @Argument(required = true,
            metaVar = "INPUT_FILE",
            usage = "An image file to be compressed. PNG and JPEG images are supported.")
    public File inputFile = null;

    public enum BasisContainer {
        KTX2,
        BASIS
    }

    public enum BasisFormat {
        UASTC,
        ETC1S
    }
}
