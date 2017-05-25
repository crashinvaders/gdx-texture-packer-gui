package com.crashinvaders.texturepackergui.services.projectserializer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.crashinvaders.texturepackergui.services.model.InputFile;
import com.crashinvaders.texturepackergui.utils.PathUtils;

import java.io.File;

public class InputFileSerializer implements Json.Serializer<InputFile> {

    private File root;

    @Override
    public void write(Json json, InputFile model, Class knownType) {
        String path = PathUtils.relativize(model.getFileHandle().path(), root.getPath());

        json.writeObjectStart();
        json.writeValue("path", path);
        json.writeValue("type", model.getType().name());

        switch (model.getType()) {
            case Input:
                if (model.isDirectory()) {
                    //### Input directory properties
                    json.writeValue("dirFilePrefix", model.getDirFilePrefix());
                } else {
                    //### Input file properties
                    json.writeValue("regionName", model.getRegionName());
                    // Ninepatch
                    if (model.isNinePatch()) {
                        InputFile.NinePatchProps npp = model.getNinePatchProps();
                        json.writeObjectStart("ninepatch");
                        json.writeArrayStart("splits");
                        json.writeValue(npp.left);
                        json.writeValue(npp.right);
                        json.writeValue(npp.top);
                        json.writeValue(npp.bottom);
                        json.writeArrayEnd();
                        json.writeArrayStart("pads");
                        json.writeValue(npp.padLeft);
                        json.writeValue(npp.padRight);
                        json.writeValue(npp.padTop);
                        json.writeValue(npp.padBottom);
                        json.writeArrayEnd();
                        json.writeObjectEnd();
                    }
                }
                break;
            case Ignore:
                //### Ignore file properties
                break;
        }
        json.writeObjectEnd();
    }

    @Override
    public InputFile read(Json json, JsonValue jsonData, Class clazz) {
        String path = jsonData.getString("path");
        InputFile.Type type = InputFile.Type.valueOf(jsonData.getString("type"));
        String dirFilePrefix = jsonData.getString("dirFilePrefix", null);
        String regionName = jsonData.getString("regionName", null);

        FileHandle fileHandle;
        if (new File(path).isAbsolute()) {
            fileHandle = Gdx.files.absolute(path);
        } else {
            fileHandle = Gdx.files.absolute(new File(root, path).getAbsolutePath());
        }

        InputFile inputFile = new InputFile(fileHandle, type);
        inputFile.setDirFilePrefix(dirFilePrefix);
        inputFile.setRegionName(regionName);

        // Ninepatch
        JsonValue ninepatch = jsonData.get("ninepatch");
        if (ninepatch != null) {
            InputFile.NinePatchProps npp = inputFile.getNinePatchProps();
            int[] splits = ninepatch.get("splits").asIntArray();
            int[] pads = ninepatch.get("pads").asIntArray();
            npp.left = splits[0];
            npp.right = splits[1];
            npp.top = splits[2];
            npp.bottom = splits[3];
            npp.padLeft = pads[0];
            npp.padRight = pads[1];
            npp.padTop = pads[2];
            npp.padBottom = pads[3];
            inputFile.setNinePatch(true);
        }

        return inputFile;
    }

    public void setRoot(File root) {
        this.root = root;
    }
}
