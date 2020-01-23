package com.crashinvaders.texturepackergui.controllers.extensionmodules;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.crashinvaders.common.async.AsyncJobTask;
import com.crashinvaders.common.async.JobTaskQueue;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.controllers.GlobalActions;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.kiwi.util.gdx.reflection.Reflection;

import java.util.Locale;

import static com.github.czyzby.autumn.mvc.config.AutumnActionPriority.LOW_PRIORITY;

@Component
public class CjkFontExtensionModule extends ExtensionModuleController {
    private final FileHandle fontFile;

    public CjkFontExtensionModule() {
        super("font-cjk", 0, "emNameCJKFont", "emDescCJKFont");
        fontFile = getModuleDir().child("NotoSansCJK-Regular.ttc");
    }

    /** Checks if one of CJK languages is selected and this module is not activated. */
    @Initiate(priority = LOW_PRIORITY)
    void checkCjkLanguage(final LocaleService localeService, GlobalActions globalActions) {
        if (getStatus() == Status.INSTALLED) return;

        Locale locale = localeService.getCurrentLocale();
        if (locale.getLanguage().equals(AppConstants.LOCALE_ZH_TW.getLanguage())) {
            // Read an original locale change action through reflection.
            Runnable origDoOnLocaleChange;
            try {
                origDoOnLocaleChange = Reflection.getFieldValue(
                        ClassReflection.getDeclaredField(LocaleService.class, "doOnLocaleChange"),
                        localeService,
                        Runnable.class);
            } catch (ReflectionException e) {
                throw new RuntimeException(e);
            }

            // This is a dirty hack that should be gone after LML merge this https://github.com/czyzby/gdx-lml/pull/60
            localeService.setActionOnLocaleChange(new Runnable() {
                @Override
                public void run() {
                    localeService.saveLocaleInPreferences();
                }
            });
            globalActions.changeLanguage(AppConstants.LOCALE_DEFAULT);
            localeService.setActionOnLocaleChange(origDoOnLocaleChange);
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
}
