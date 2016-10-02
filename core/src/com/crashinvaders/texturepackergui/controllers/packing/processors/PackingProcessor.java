package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

import java.io.File;

public class PackingProcessor implements PackProcessor {
    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        PackModel pack = node.getPack();
        String settingsOrigExtension = pack.getSettings().atlasExtension;

        System.out.println("Packing started");

        if (!new File(pack.getInputDir()).exists()) {
            throw new Exception("[output-red]Input directory doesn't exist:[] "+pack.getInputDir());
        }
        if (!new File(pack.getInputDir()).isDirectory()) {
            throw new Exception("[output-red]Input directory is not of directory type:[] "+pack.getInputDir());
        }

        String filename = pack.getCanonicalFilename();
        if (filename.lastIndexOf(".") > -1) {
            String extension = filename.substring(filename.lastIndexOf("."));
            filename = filename.substring(0, filename.lastIndexOf("."));
            pack.getSettings().atlasExtension = extension;
        } else {
            pack.getSettings().atlasExtension = "";
        }

        // Due to hardcoded old file deletion logic at TexturePackerFileProcessor:134 (deletes only .atlas files)
        // We need to clean old files manually
        FileHandle oldFile = Gdx.files.absolute(pack.getOutputDir()).child(filename + pack.getSettings().atlasExtension);
        if (oldFile.exists()) {
            oldFile.delete();
        }

        TexturePacker.process(pack.getSettings(), pack.getInputDir(), pack.getOutputDir(), pack.getCanonicalName());

        pack.getSettings().atlasExtension = settingsOrigExtension;

        System.out.println("Packing done");
    }
}
