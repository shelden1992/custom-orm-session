package org.bobocode.utils;

import lombok.NonNull;
import org.apache.log4j.Logger;
import org.bobocode.annotation.Column;
import org.bobocode.annotation.Table;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import static java.util.Objects.nonNull;

/**
 * Created by Shelupets Denys on 12.12.2021.
 */
public class ReflectionUtils {
    private static final Logger LOG = Logger.getLogger(ReflectionUtils.class);

    public static String getColumnName(@NonNull Field declaredField) {
        Column annotation = declaredField.getAnnotation(Column.class);
        return nonNull(annotation) ? annotation.name() : declaredField.getName().toLowerCase();
    }

    public static <T> T initialEntity(@NonNull Class<T> type) {
        Constructor<?>[] declaredConstructors = type.getDeclaredConstructors();
        if (declaredConstructors.length != 1) {
            throw new IllegalArgumentException("Entity must have only default constructor");
        }
        try {
            return (T) declaredConstructors[0].newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            LOG.error("");
            throw new RuntimeException("Error initial entity type = " + type, ex);
        }
    }


    private static <T> String getAnnotationValue(Class<T> type, Function<Class<T>, String> annotation) {
        return annotation.apply(type);
    }

    public static <T> String getTableName(Class<T> type) {
        return getAnnotationValue(type, clazz -> {
            Table annotation = clazz.getAnnotation(Table.class);
            return nonNull(annotation) ? annotation.name() : clazz.getName().toLowerCase();
        });
    }
}
