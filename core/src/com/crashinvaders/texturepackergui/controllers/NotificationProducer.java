package com.crashinvaders.texturepackergui.controllers;

import com.crashinvaders.texturepackergui.events.ProjectSerializerEvent;
import com.crashinvaders.texturepackergui.events.ShowToastEvent;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

/** Responsible for creating toasts for application-wide type of events */
@Component
public class NotificationProducer {

    @Inject EventDispatcher eventDispatcher;
    @Inject LocaleService localeService;

    @OnEvent(ProjectSerializerEvent.class) void onEvent(ProjectSerializerEvent event) {
        if (event.getAction() == ProjectSerializerEvent.Action.SAVED) {
            eventDispatcher.postEvent(new ShowToastEvent()
                    .message(getString("toastProjectSaved", event.getFile().path()))
            );
        }
    }

    /** @return localized string */
    private String getString(String key) {
        return localeService.getI18nBundle().get(key);
    }

    /** @return localized string */
    private String getString(String key, Object... args) {
        return localeService.getI18nBundle().format(key, args);
    }
}
