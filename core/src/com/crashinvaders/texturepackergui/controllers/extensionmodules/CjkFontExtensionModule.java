package com.crashinvaders.texturepackergui.controllers.extensionmodules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.crashinvaders.common.async.AsyncJobTask;
import com.crashinvaders.common.async.JobTaskQueue;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.controllers.GlobalActions;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;

import java.util.Locale;

import static com.github.czyzby.autumn.mvc.config.AutumnActionPriority.LOW_PRIORITY;

@Component
public class CjkFontExtensionModule extends ExtensionModuleController {
    private static final String TAG = CjkFontExtensionModule.class.getSimpleName();

    private final FileHandle fontFile;

    public CjkFontExtensionModule() {
        super("font-cjk", 0, "emNameCJKFont", "emDescCJKFont");
        fontFile = getModuleDir().child("NotoSansCJK-Regular.ttc");
    }

    /** Checks if one of CJK languages is selected and this module is not activated. */
    @Initiate(priority = LOW_PRIORITY)
    void checkCjkLanguage(final LocaleService localeService, GlobalActions globalActions) {
        if (isActivated()) return;

        Locale locale = localeService.getCurrentLocale();
        if (isCjkFontRequired(locale)) {
            Gdx.app.error(TAG, "A CJK locale is selected, but the " + TAG +
                    " extension is not installed/activated. Switching to the default locale.");
            globalActions.changeLanguage(AppConstants.LOCALE_DEFAULT);
        }
    }

    public FileHandle getFontFile() {
        return fontFile;
    }

    @Override
    void prepareInstallationJob(JobTaskQueue taskQueue, final String fileUrl) {
        taskQueue.addTask(new AsyncJobTask() {
            @Override
            protected void doInBackground() throws Exception {
                FileHandle tmpFile = FileUtils.createTempFile("CjkFontPackage");
                try {
                    FileUtils.downloadFile(tmpFile, fileUrl);
                    if (checkCanceled()) return;
                    FileUtils.unpackZip(tmpFile, getModuleDir());
                } finally {
                    if (tmpFile != null) {
                        tmpFile.delete();
                    }
                }
            }
        });
    }

    @Override
    void prepareUninstallationJob(JobTaskQueue taskQueue) {
        taskQueue.addTask(new AsyncJobTask() {
            @Override
            protected void doInBackground() throws Exception {
                FileHandle moduleDir = getModuleDir();
                if (moduleDir.exists()) {
                    moduleDir.deleteDirectory();
                }
            }
        });
    }

    @Override
    boolean validateInstalledModule() {
        //TODO Implement MD5 file hash check here.
        return getModuleDir().exists();
    }

    public static boolean isCjkFontRequired(Locale locale) {
        String language = locale.getLanguage();
        return language.equals(AppConstants.LOCALE_ZH_TW.getLanguage()) ||
                language.equals(AppConstants.LOCALE_ZH_CN.getLanguage());
    }
}
