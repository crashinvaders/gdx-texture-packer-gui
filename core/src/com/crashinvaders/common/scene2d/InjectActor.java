package com.crashinvaders.common.scene2d;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** @see Scene2dUtils#injectActorFields(Object, Group) */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectActor {
    /** The value that actor returns in {@link Actor#getName()}, used to identify the actor. Leave it blank if field name matches the actor's name. */
    String value() default "";
}
