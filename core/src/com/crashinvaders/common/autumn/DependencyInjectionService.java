package com.crashinvaders.common.autumn;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.context.Context;
import com.github.czyzby.autumn.processor.AnnotationProcessor;
import com.github.czyzby.autumn.processor.impl.InjectAnnotationProcessor;
import com.github.czyzby.kiwi.util.common.Exceptions;
import com.github.czyzby.kiwi.util.gdx.collection.lazy.LazyObjectMap;

import java.lang.annotation.Annotation;

/**
 * Can process some Autumn annotations to integrate any object with {@link Context} state.
 * Any object with any of following annotations can be processed: {@link Inject}.
 */
public class DependencyInjectionService {

    @Inject Context context;

    /** Processors that handle annotated fields. */
    private final ObjectMap<Class<? extends Annotation>, Array<AnnotationProcessor<?>>> fieldProcessors = LazyObjectMap
            .newMapOfArrays();

    public DependencyInjectionService() {
        addProcessor(new InjectAnnotationProcessor()); // @Inject annotation
    }

    public void process(Object component) {
        Class<?> componentClass = component.getClass();
        while (componentClass != null && !componentClass.equals(Object.class)) {
            final Field[] fields = ClassReflection.getDeclaredFields(componentClass);
            if (fields != null && fields.length > 0) {
                processFields(component, fields, context);
            }
            componentClass = componentClass.getSuperclass();
        }
    }

    private void addProcessor(final AnnotationProcessor<?> processor) {
        if (processor.isSupportingFields()) {
            fieldProcessors.get(processor.getSupportedAnnotationType()).add(processor);
        } else {
            throw new IllegalArgumentException("The processor doesn't support field annotations: " + processor);
        }
    }

    /** Scans class tree of component to process all its fields.
     *
     * @param component all fields of its class tree will be processed.
     * @param context used to resolve dependencies. */
    private void processFields(final Object component, final Context context) {
        Class<?> componentClass = component.getClass();
        while (componentClass != null && !componentClass.equals(Object.class)) {
            final Field[] fields = ClassReflection.getDeclaredFields(componentClass);
            if (fields != null && fields.length > 0) {
                processFields(component, fields, context);
            }
            componentClass = componentClass.getSuperclass();
        }
    }

    /** Does the actual processing of found fields.
     *
     * @param component owner of the fields.
     * @param fields present in one of superclasses of the component.
     * @param context used to resolve dependencies. */
    @SuppressWarnings({ "rawtypes", "unchecked" }) // Using correct types, but wildcards fail to see that.
    private void processFields(final Object component, final Field[] fields, final Context context) {
        for (final Field field : fields) {
            final com.badlogic.gdx.utils.reflect.Annotation[] annotations = getAnnotations(field);
            if (annotations == null || annotations.length == 0) {
                continue;
            }
            for (final com.badlogic.gdx.utils.reflect.Annotation annotation : annotations) {
                if (fieldProcessors.containsKey(annotation.getAnnotationType())) {
                    for (final AnnotationProcessor processor : fieldProcessors.get(annotation.getAnnotationType())) {
                        processor.processField(field, annotation.getAnnotation(annotation.getAnnotationType()),
                                component, context, null, null);
                    }
                }
            }
        }
    }

    /**@param field will return an array of its annotations.
     * @return array of annotations or null. GWT utility. */
    private static com.badlogic.gdx.utils.reflect.Annotation[] getAnnotations(final Field field) {
        try {
            return field.getDeclaredAnnotations();
        } catch (final Exception exception) {
            Exceptions.ignore(exception);
            return null;
        }
    }
}
