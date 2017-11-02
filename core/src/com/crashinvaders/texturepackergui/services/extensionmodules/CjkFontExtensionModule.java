package com.crashinvaders.texturepackergui.services.extensionmodules;

import com.crashinvaders.common.async.AsyncJobTask;
import com.crashinvaders.common.async.JobTaskQueue;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;

@Component
public class CjkFontExtensionModule extends ExtensionModuleController {
    public static final String MODULE_ID = "font-cjk";
    public static final int CURRENT_REVISION = 1;

    @Inject ExtensionModuleManagerService moduleRepository;

    public CjkFontExtensionModule() {
        super("font-cjk", 0, "emNameCJKFont", "emDescCJKFont");
    }

    @Initiate void init() {

    }

    @Override
    public String getModuleId() {
        return MODULE_ID;
    }

    @Override
    public int getRequiredRevision() {
        return CURRENT_REVISION;
    }

    @Override
    void prepareInstallationJob(JobTaskQueue taskQueue, String fileUrl) {
        taskQueue.addTask(new AsyncJobTask() {
            @Override
            protected void doInBackground() throws Exception {
                Thread.sleep(3000);
            }
        });
    }

    @Override
    void prepareUninstallationJob(JobTaskQueue taskQueue) {
        taskQueue.addTask(new AsyncJobTask() {
            @Override
            protected void doInBackground() throws Exception {
                Thread.sleep(3000);
            }
        });
    }
}
