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
        json.writeValue("dirFilePrefix", model.getDirFilePrefix());
        json.writeValue("regionName", model.getRegionName());
        json.writeObjectEnd();
    }

    @Override
    public InputFile read(Json json, JsonValue jsonData, Class clazz) {
        String path = jsonData.getString("path");
        InputFile.Type type = InputFile.Type.valueOf(jsonData.getString("type"));
        String dirFilePrefix = jsonData.getString("dirFilePrefix");
        String regionName = jsonData.getString("regionName");

        FileHandle fileHandle;
        if (new File(path).isAbsolute()) {
            fileHandle = Gdx.files.absolute(path);
        } else {
            fileHandle = Gdx.files.absolute(new File(root, path).getAbsolutePath());
        }

        InputFile inputFile = new InputFile(fileHandle, type);
        inputFile.setDirFilePrefix(dirFilePrefix);
        inputFile.setRegionName(regionName);
        return inputFile;
    }

    public void setRoot(File root) {
        this.root = root;
    }
}
