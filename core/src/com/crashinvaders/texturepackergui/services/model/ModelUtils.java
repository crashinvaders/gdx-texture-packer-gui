package com.crashinvaders.texturepackergui.services.model;

import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.events.PackListOrderChangedEvent;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

@Component
public class ModelUtils {

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
