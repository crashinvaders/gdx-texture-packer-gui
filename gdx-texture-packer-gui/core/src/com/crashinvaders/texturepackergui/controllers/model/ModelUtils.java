package com.crashinvaders.texturepackergui.controllers.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.events.PackListOrderChangedEvent;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

@Component
public class ModelUtils {
    private static final String TAG = ModelUtils.class.getSimpleName();

    @Inject ModelService modelService;
    @Inject EventDispatcher eventDispatcher;

    public void movePackTop(PackModel pack) {
        ensurePackExists(pack);

        Array<PackModel> packs = getProject().getPacks();
        // Check if pack is already at the top
        if (packs.indexOf(pack, true) == 0) return;

        packs.removeValue(pack, true);
        packs.insert(0, pack);
        eventDispatcher.postEvent(new PackListOrderChangedEvent());
    }

    public void movePackBottom(PackModel pack) {
        ensurePackExists(pack);

        Array<PackModel> packs = getProject().getPacks();
        int idx = packs.indexOf(pack, true);
        // Check if pack is already at the bottom
        if (idx == packs.size-1) return;

        packs.removeValue(pack, true);
        packs.insert(packs.size-1, pack);
        eventDispatcher.postEvent(new PackListOrderChangedEvent());
    }

	public void movePackUp(PackModel pack) {
        ensurePackExists(pack);

		Array<PackModel> packs = getProject().getPacks();
        int idx = packs.indexOf(pack, true);
        // Check if pack is already at the top
        if (idx == 0) return;

		packs.swap(idx, idx - 1);
		eventDispatcher.postEvent(new PackListOrderChangedEvent());
	}

    public void movePackDown(PackModel pack) {
        ensurePackExists(pack);

        Array<PackModel> packs = getProject().getPacks();
        int idx = packs.indexOf(pack, true);
        // Check if pack is already at the bottom
        if (idx == packs.size-1) return;

		packs.swap(idx, idx + 1);
		eventDispatcher.postEvent(new PackListOrderChangedEvent());
	}

    public void movePackNextTo(PackModel anchor, PackModel pack) {
        ensurePackExists(anchor);
        ensurePackExists(pack);

        Array<PackModel> packs = getProject().getPacks();
        int idxAnchor = packs.indexOf(anchor, true);
        int idxPack = packs.indexOf(pack, true);
        // Check if pack is already next to anchor
        if (idxAnchor-idxPack == -1) return;

        packs.removeValue(pack, true);
        packs.insert(idxAnchor+1, pack);
		eventDispatcher.postEvent(new PackListOrderChangedEvent());
	}

    public void movePackPrevTo(PackModel anchor, PackModel pack) {
        ensurePackExists(anchor);
        ensurePackExists(pack);

        Array<PackModel> packs = getProject().getPacks();
        int idxAnchor = packs.indexOf(anchor, true);
        int idxPack = packs.indexOf(pack, true);
        // Check if pack is already previous to anchor
        if (idxAnchor-idxPack == 1) return;

        packs.removeValue(pack, true);
        packs.insert(idxAnchor, pack);
		eventDispatcher.postEvent(new PackListOrderChangedEvent());
	}

    public void selectPrevPack(PackModel pack) {
        ensurePackExists(pack);

        ProjectModel project = getProject();
        Array<PackModel> packs = project.getPacks();

        project.setSelectedPack(packs.get(Math.max(0, packs.indexOf(pack, true)-1)));
    }

    public void selectNextPack(PackModel pack) {
        ensurePackExists(pack);

        ProjectModel project = getProject();
        Array<PackModel> packs = project.getPacks();

        project.setSelectedPack(packs.get(Math.min(packs.size-1, packs.indexOf(pack, true)+1)));
    }

    /**
     * Selects previous pack. If there is no previous pack, selects next pack
     */
    public void selectClosestPack(PackModel pack) {
        ensurePackExists(pack);
        Array<PackModel> packs = getProject().getPacks();

        int index = packs.indexOf(pack, true);
        if (index > 0) {
            selectPrevPack(pack);
        } else {
            selectNextPack(pack);
        }
    }

    /**
     * Changes file path for {@link InputFile} instance.
     * Since all {@link InputFile}'s are unique by file withing {@link PackModel}, the only way to change file reference is to recreate {@link InputFile} properly.
     * @return Newly created {@link InputFile} instance that has replaced old one. May be null in case {@link InputFile} entry with that file already exist.
     */
    public InputFile changeInputFileHandle(PackModel pack, InputFile inputFile, FileHandle file) {
        ensurePackExists(pack);

        if (file.equals(inputFile.getFileHandle())) return inputFile;

        InputFile newInputFile = new InputFile(file, inputFile.getType());
        newInputFile.setDirFilePrefix(inputFile.getDirFilePrefix());
        newInputFile.setRegionName(inputFile.getRegionName());

        if (pack.getInputFiles().contains(newInputFile, false)) {
            Gdx.app.error(TAG, "Pack: " + pack + " already contains input file entry for " + file);
            return null;
        }

        pack.removeInputFile(inputFile);
        pack.addInputFile(newInputFile);

        return newInputFile;
    }

    public InputFile includeInputFile(PackModel pack, InputFile inputFile) {
        ensurePackExists(pack);

        if (inputFile.isDirectory()) {
            Gdx.app.error(TAG, "Cannot include input pack that is a directory: " + pack);
            return null;
        }
        if (inputFile.getType() != InputFile.Type.Ignore) {
            Gdx.app.error(TAG, "Input pack " + pack + " should be of InputFile.Type.Ignore type to be able to be included.");
            return null;
        }

        pack.removeInputFile(inputFile);

        InputFile newInputFile = new InputFile(inputFile.getFileHandle(), InputFile.Type.Input);
        newInputFile.setRegionName(inputFile.getRegionName());
        pack.addInputFile(newInputFile);

        return newInputFile;
    }

    public InputFile excludeInputFile(PackModel pack, InputFile inputFile) {
        ensurePackExists(pack);

        if (inputFile.isDirectory()) {
            Gdx.app.error(TAG, "Cannot exclude input pack that is a directory: " + pack);
            return null;
        }
        if (inputFile.getType() != InputFile.Type.Input) {
            Gdx.app.error(TAG, "Input pack " + pack + " should be of InputFile.Type.Input type to be able to be excluded.");
            return null;
        }

        pack.removeInputFile(inputFile);

        InputFile newInputFile = new InputFile(inputFile.getFileHandle(), InputFile.Type.Ignore);
        newInputFile.setRegionName(inputFile.getRegionName());
        pack.addInputFile(newInputFile);

        return newInputFile;
    }

	private void ensurePackExists(PackModel pack) {
        Array<PackModel> packs = getProject().getPacks();
        if (!packs.contains(pack, true)) {
            throw new IllegalArgumentException("Current project doesn't contain pack: " + pack.getName());
        }
    }

    private ProjectModel getProject() {
        return modelService.getProject();
    }
}
