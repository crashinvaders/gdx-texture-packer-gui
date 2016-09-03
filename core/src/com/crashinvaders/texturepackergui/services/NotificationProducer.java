package com.crashinvaders.texturepackergui.services;

import com.crashinvaders.texturepackergui.events.ProjectSerializerEvent;
import com.crashinvaders.texturepackergui.events.ToastNotificationEvent;
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
            eventDispatcher.postEvent(new ToastNotificationEvent()
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
