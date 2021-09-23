package com.crashinvaders.texturepackergui.controllers.projectserializer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.crashinvaders.common.Version;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.crashinvaders.texturepackergui.controllers.model.filetype.*;
import com.crashinvaders.texturepackergui.events.ProjectSerializerEvent;
import com.crashinvaders.texturepackergui.events.ShowToastEvent;
import com.crashinvaders.texturepackergui.controllers.model.*;
import com.crashinvaders.texturepackergui.utils.PathUtils;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static com.crashinvaders.texturepackergui.utils.CommonUtils.splitAndTrim;
import static com.crashinvaders.texturepackergui.utils.FileUtils.loadTextFromFile;
import static com.crashinvaders.texturepackergui.utils.FileUtils.saveTextToFile;

@Component
public class ProjectSerializer {
    private static final String TAG = ProjectSerializer.class.getSimpleName();
    private static final String PACK_DIVIDER = "---";
    private static final String SECTION_DIVIDER = "-PROJ-";
    private static final Version versionNone = new Version(0,0,0);

    @Inject EventDispatcher eventDispatcher;
    @Inject LocaleService localeService;

    private Json json;
    private InputFileSerializer inputFileSerializer;

    @Initiate void initialize() {
        json = new Json();
        json.setSerializer(ScaleFactorModel.class, new ScaleFactorJsonSerializer());
        json.setSerializer(InputFile.class, inputFileSerializer = new InputFileSerializer());
    }

    public void saveProject(ProjectModel project, FileHandle file) {
        String serialized = serializeProject(project, file.parent());
        try {
            saveTextToFile(file, serialized);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error during project saving.", e);
            eventDispatcher.postEvent(new ShowToastEvent()
                    .message(localeService.getI18nBundle().format("toastProjectSaveError", project.getProjectFile().path()))
                    .duration(ShowToastEvent.DURATION_LONG)
                    .click(() -> ErrorDialogController.show(e))
            );
            return;
        }

        eventDispatcher.postEvent(new ProjectSerializerEvent(ProjectSerializerEvent.Action.SAVED, project, file));
    }

    public ProjectModel loadProject(FileHandle file) {
        final ProjectModel project;
        try {
            String serialized = loadTextFromFile(file);
            project = deserializeProject(serialized, file.parent());
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error during project loading.", e);
            eventDispatcher.postEvent(new ShowToastEvent()
                    .message(localeService.getI18nBundle().format("toastProjectLoadError", file.path()))
                    .duration(ShowToastEvent.DURATION_LONG)
                    .click(() -> ErrorDialogController.show(e))
            );
            return null;
        }

        project.setProjectFile(file);

        eventDispatcher.postEvent(new ProjectSerializerEvent(ProjectSerializerEvent.Action.LOADED, project, file));
        return project;
    }

    private String serializeProject(ProjectModel projectModel, FileHandle root) {
        Array<PackModel> packs = projectModel.getPacks();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < packs.size; i++) {
            sb.append(serializePack(packs.get(i), root));

            if (i < packs.size - 1) {
                sb.append("\n\n---\n\n");
            }
        }

        serializeProjectSection(projectModel, sb);

        return sb.toString();
    }

    private void serializeProjectSection(ProjectModel projectModel, StringBuilder sb) {
        sb.append("\n\n").append(SECTION_DIVIDER).append("\n\n");

        sb.append("version=").append(AppConstants.version.toString()).append("\n");

        FileTypeModel fileType = projectModel.getFileType();
        sb.append("fileTypeType=").append(fileType.getType().key).append("\n");
        sb.append("fileTypeData=").append(fileType.serializeState()).append("\n");

        sb.append("previewBackgroundColor=").append(projectModel.getPreviewBackgroundColor().toString()).append("\n");

        sb.append("projectSettings=").append(projectModel.getSettings().serializeState()).append("\n");
    }

    private String serializePack(PackModel pack, FileHandle root) {
        StringBuilder sb = new StringBuilder();

        String filename = pack.getFilename();

        sb.append("name=").append(pack.getName()).append("\n");
        sb.append("filename=").append(filename).append("\n");
        sb.append("output=").append(PathUtils.relativize(pack.getOutputDir(), root.file().getPath())).append("\n");

        sb.append("\n");

        TexturePacker.Settings settings = pack.getSettings();
        sb.append("alias=").append(settings.alias).append("\n");
        sb.append("alphaThreshold=").append(settings.alphaThreshold).append("\n");
        sb.append("debug=").append(settings.debug).append("\n");
        sb.append("duplicatePadding=").append(settings.duplicatePadding).append("\n");
        sb.append("edgePadding=").append(settings.edgePadding).append("\n");
        sb.append("fast=").append(settings.fast).append("\n");
        sb.append("filterMag=").append(settings.filterMag).append("\n");
        sb.append("filterMin=").append(settings.filterMin).append("\n");
//        sb.append("format=").append(settings.format).append("\n");
        sb.append("ignoreBlankImages=").append(settings.ignoreBlankImages).append("\n");
//        sb.append("jpegQuality=").append(settings.jpegQuality).append("\n");
        sb.append("maxHeight=").append(settings.maxHeight).append("\n");
        sb.append("maxWidth=").append(settings.maxWidth).append("\n");
        sb.append("minHeight=").append(settings.minHeight).append("\n");
        sb.append("minWidth=").append(settings.minWidth).append("\n");
//        sb.append("outputFormat=").append(settings.outputFormat).append("\n");
        sb.append("paddingX=").append(settings.paddingX).append("\n");
        sb.append("paddingY=").append(settings.paddingY).append("\n");
        sb.append("pot=").append(settings.pot).append("\n");
        sb.append("mof=").append(settings.multipleOfFour).append("\n");
        sb.append("rotation=").append(settings.rotation).append("\n");
        sb.append("stripWhitespaceX=").append(settings.stripWhitespaceX).append("\n");
        sb.append("stripWhitespaceY=").append(settings.stripWhitespaceY).append("\n");
        sb.append("wrapX=").append(settings.wrapX).append("\n");
        sb.append("wrapY=").append(settings.wrapY).append("\n");
        sb.append("premultiplyAlpha=").append(settings.premultiplyAlpha).append("\n");
        sb.append("grid=").append(settings.grid).append("\n");
        sb.append("square=").append(settings.square).append("\n");
        sb.append("bleed=").append(settings.bleed).append("\n");
        sb.append("limitMemory=").append(settings.limitMemory).append("\n");
        sb.append("useIndexes=").append(settings.useIndexes).append("\n");

        sb.append("\n");

        sb.append("scaleFactors=").append(json.toJson(pack.getScaleFactors())).append("\n");

        inputFileSerializer.setRoot(root.file());
        sb.append("inputFiles=").append(json.toJson(pack.getInputFiles())).append("\n");

        sb.append("keepInputFileExtensions=").append(pack.isKeepInputFileExtensions()).append("\n");

        return sb.toString();
    }

    private ProjectModel deserializeProject(String serializedProject, FileHandle root) {
        ProjectModel project = new ProjectModel();

        String[] serializedSections = serializedProject.split(SECTION_DIVIDER);
        if (serializedSections.length == 0) return project;

        Version version = null;

        // Project section is always in the end
        if (serializedSections.length > 1) {
            String projectSection = serializedSections[1];
            version = deserializeProjectSection(project, projectSection);
        }
        if (version == null) {
            version = versionNone;
        }

        String[] serializedPacks = serializedSections[0].split(PACK_DIVIDER);

        for (String serializedPack : serializedPacks) {
            if (serializedPack.trim().length() == 0) continue;

            PackModel pack = deserializePack(serializedPack, root, version);
            project.addPack(pack);
        }
        return project;
    }

    private Version deserializeProjectSection(ProjectModel project, String projectSection) {
        Array<String> lines = splitAndTrim(projectSection);

        // File type section
        {
            FileTypeType fileTypeType = FileTypeType.findByKey(find(lines, "fileTypeType=", null));
            if (fileTypeType != null) {
                FileTypeModel fileTypeModel = null;
                switch (fileTypeType) {
                    case PNG:
                        fileTypeModel = new PngFileTypeModel();
                        break;
                    case JPEG:
                        fileTypeModel = new JpegFileTypeModel();
                        break;
                    case KTX:
                        fileTypeModel = new KtxFileTypeModel();
                        break;
                    case BASIS:
                        fileTypeModel = new BasisuFileTypeModel();
                        break;
                    default:
                        Gdx.app.error(TAG, "Unexpected FileTypeType: " + fileTypeType);
                }
                if (fileTypeModel != null) {
                    String fileTypeData = find(lines, "fileTypeData=", null);
                    fileTypeModel.deserializeState(fileTypeData);
                }
                project.setFileType(fileTypeModel);
            }
        }

        String previewBgColorHex = find(lines, "previewBackgroundColor=", null);
        if (previewBgColorHex != null) {
            project.setPreviewBackgroundColor(Color.valueOf(previewBgColorHex));
        }

        String projectSettings = find(lines, "projectSettings=", null);
        if (projectSettings != null) {
            project.getSettings().deserializeState(projectSettings);
        }

        String versionString = find(lines, "version=", null);
        Version version = null;
        try { version = new Version(versionString); } catch (Exception ignore) {}
        if (version == null) version = versionNone;
        return version;
    }

    private PackModel deserializePack(String serializedData, FileHandle root, Version version) {
        PackModel pack = new PackModel();
        String inputDir = null;

        Array<String> lines = splitAndTrim(serializedData);
        for (String line : lines) {
            if (line.startsWith("name=")) pack.setName(PathUtils.trim(line.substring("name=".length())).trim());
            if (line.startsWith("filename=")) pack.setFilename(PathUtils.trim(line.substring("filename=".length())).trim());
            if (line.startsWith("input=")) inputDir = PathUtils.trim(line.substring("input=".length())).trim();
            if (line.startsWith("output=")) pack.setOutputDir(PathUtils.trim(line.substring("output=".length())).trim());
            if (line.startsWith("keepInputFileExtensions=")) pack.setKeepInputFileExtensions(
                    Boolean.parseBoolean(line.substring("keepInputFileExtensions=".length())));
        }

        try {
            String outputDir = pack.getOutputDir();
            if (!outputDir.equals("") && !new File(outputDir).isAbsolute()) {
                pack.setOutputDir(new File(root.file(), outputDir).getCanonicalPath());
            }
        } catch (IOException ex) {
            //TODO show error to user somehow
            System.err.println(ex.getMessage());
            pack.setOutputDir("");
        }

        TexturePacker.Settings settings = pack.getSettings();
        TexturePacker.Settings defaultSettings = new TexturePacker.Settings();

        settings.alias = find(lines, "alias=", defaultSettings.alias);
        settings.alphaThreshold = find(lines, "alphaThreshold=", defaultSettings.alphaThreshold);
        settings.debug = find(lines, "debug=", defaultSettings.debug);
        settings.duplicatePadding = find(lines, "duplicatePadding=", defaultSettings.duplicatePadding);
        settings.edgePadding = find(lines, "edgePadding=", defaultSettings.edgePadding);
        settings.fast = find(lines, "fast=", defaultSettings.fast);
        settings.filterMag = Texture.TextureFilter.valueOf(find(lines, "filterMag=", defaultSettings.filterMag.toString()));
        settings.filterMin = Texture.TextureFilter.valueOf(find(lines, "filterMin=", defaultSettings.filterMin.toString()));
//        settings.format = Pixmap.Format.valueOf(find(lines, "format=", defaultSettings.format.toString()));
        settings.ignoreBlankImages = find(lines, "ignoreBlankImages=", defaultSettings.ignoreBlankImages);
//        settings.jpegQuality = find(lines, "jpegQuality=", defaultSettings.jpegQuality);
        settings.maxHeight = find(lines, "maxHeight=", defaultSettings.maxHeight);
        settings.maxWidth = find(lines, "maxWidth=", defaultSettings.maxWidth);
        settings.minHeight = find(lines, "minHeight=", defaultSettings.minHeight);
        settings.minWidth = find(lines, "minWidth=", defaultSettings.minWidth);
//        settings.outputFormat = find(lines, "outputFormat=", defaultSettings.outputFormat);
        settings.paddingX = find(lines, "paddingX=", defaultSettings.paddingX);
        settings.paddingY = find(lines, "paddingY=", defaultSettings.paddingY);
        settings.pot = find(lines, "pot=", defaultSettings.pot);
        settings.multipleOfFour = find(lines, "mof=", defaultSettings.multipleOfFour);
        settings.rotation = find(lines, "rotation=", defaultSettings.rotation);
        settings.stripWhitespaceX = find(lines, "stripWhitespaceX=", defaultSettings.stripWhitespaceX);
        settings.stripWhitespaceY = find(lines, "stripWhitespaceY=", defaultSettings.stripWhitespaceY);
        settings.wrapX = Texture.TextureWrap.valueOf(find(lines, "wrapX=", defaultSettings.wrapX.toString()));
        settings.wrapY = Texture.TextureWrap.valueOf(find(lines, "wrapY=", defaultSettings.wrapY.toString()));
        settings.premultiplyAlpha = find(lines, "premultiplyAlpha=", defaultSettings.premultiplyAlpha);
        settings.grid = find(lines, "grid=", defaultSettings.grid);
        settings.square = find(lines, "square=", defaultSettings.square);
        settings.bleed = find(lines, "bleed=", defaultSettings.bleed);
        settings.limitMemory = find(lines, "limitMemory=", defaultSettings.limitMemory);
        settings.useIndexes = find(lines, "useIndexes=", defaultSettings.useIndexes);

        String scaleFactorsSerialized = find(lines, "scaleFactors=", null);
        if (scaleFactorsSerialized != null) {
            @SuppressWarnings("unchecked")
            Array<ScaleFactorModel> scaleFactors = json.fromJson(Array.class, ScaleFactorModel.class, scaleFactorsSerialized);
            pack.setScaleFactors(scaleFactors);
        }

        String inputFilesSerialized = find(lines, "inputFiles=", null);
        inputFileSerializer.setRoot(root.file());
        if (inputFilesSerialized != null) {
            @SuppressWarnings("unchecked")
            Array<InputFile> inputFiles = json.fromJson(Array.class, InputFile.class, inputFilesSerialized);
            for (InputFile inputFile : inputFiles) {
                pack.addInputFile(inputFile);
            }
        }

        // Legacy support and migrations section
        {
            // We have selective file packing now and no more save "input" field.
            // If it is present will simply treat it as input directory.
            if (version.isLower(new Version(4,4,0))) {
                if (inputDir != null && !inputDir.equals("")) {
                    // Input directory
                    FileHandle fileHandle;
                    if (new File(inputDir).isAbsolute()) {
                        fileHandle = Gdx.files.absolute(inputDir);
                    } else {
                        fileHandle = Gdx.files.absolute(new File(root.file(), inputDir).getAbsolutePath());
                    }
                    pack.addInputFile(fileHandle, InputFile.Type.Input);

                    // TexturePacker includes all files from level one subdirs by default.
                    // We will create appropriate entries as well.
                    boolean flattenPaths = find(lines, "flattenPaths=", false);
                    FileHandle[] subDirs = fileHandle.list(new FileFilter() {
                        @Override public boolean accept(File file) {
                            return file.isDirectory();
                        }
                    });
                    for (int i = 0; i < subDirs.length; i++) {
                        FileHandle subDir = subDirs[i];
                        InputFile inputFile = new InputFile(subDir, InputFile.Type.Input);
                        if (!flattenPaths) {
                            inputFile.setDirFilePrefix(subDir.name() + "/");
                        }
                        pack.addInputFile(inputFile);
                    }
                }
            }
        }

        return pack;
    }

    private static String find (Array<String> lines, String start, String defaultValue) {
        for (String line : lines) {
            if (line.startsWith(start)) return line.substring(start.length());
        }
        return defaultValue;
    }
    private static boolean find (Array<String> lines, String start, boolean defaultValue) {
        String str = find(lines, start, null);
        if (str != null) return Boolean.parseBoolean(str);
        return defaultValue;
    }
    private static int find (Array<String> lines, String start, int defaultValue) {
        String str = find(lines, start, null);
        if (str != null) return Integer.parseInt(str);
        return defaultValue;
    }
    private static float find (Array<String> lines, String start, float defaultValue) {
        String str = find(lines, start, null);
        if (str != null) return Float.parseFloat(str);
        return defaultValue;
    }
}
