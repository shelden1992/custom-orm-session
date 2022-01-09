package org.bobocode.utils;

import lombok.NonNull;
import org.apache.log4j.Logger;
import org.bobocode.annotation.Column;
import org.bobocode.annotation.Id;
import org.bobocode.annotation.Table;
import org.bobocode.exeptions.AnnotationException;
import org.bobocode.exeptions.ReflectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static <T> Map<String, Object> getColumnNameValues(@NonNull T entity) {
        return Stream.of(entity.getClass().getDeclaredFields()).collect(Collectors.toMap(ReflectionUtils::getColumnName, field -> getValue(field, entity)));
    }


    public static <T> Object getValue(final Field field, final T entity) {
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            LOG.error("Error get value of the field = " + field, e);
            throw new ReflectionException("Error get value of the field = " + field, e);
        }
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
            throw new ReflectionException("Error initial entity type = " + type, ex);
        }
    }

    private static <T> Object getAnnotationValue(Class<T> type, Function<Class<T>, Object> annotation) {
        return annotation.apply(type);
    }

    public static <T> String getTableName(Class<T> type) {
        return String.valueOf(getAnnotationValue(type, clazz -> {
            Table annotation = clazz.getAnnotation(Table.class);
            return nonNull(annotation) ? annotation.name() : clazz.getName().toLowerCase();
        }));
    }

    public static <T> Object getIdValue(@NonNull T type) {
        return getAnnotationValue(type.getClass(), clazz -> {
            List<Object> ids = Arrays.stream(clazz.getDeclaredFields()).filter(field -> nonNull(field.getAnnotation(Id.class))).map(f -> getValue(f, type)).toList();
            if (ids.isEmpty()) {
                throw new AnnotationException("Annotation \"Id\" not found.");
            }

            if (ids.size() != 1) {
                throw new AnnotationException("Annotation \"Id\" have be unique.");
            }
            return ids.get(0);

        });
    }

    public static <T> void setValue(final Field field, final Object value, final T entity) {
        try {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            Object valueOf = value;
            if (Short.class.equals(fieldType)) valueOf = Short.valueOf(String.valueOf(value));
            if (Integer.class.equals(fieldType)) valueOf = Integer.valueOf(String.valueOf(value));
            if (Long.class.equals(fieldType)) valueOf = Long.valueOf(String.valueOf(value));
            if (Double.class.equals(fieldType)) valueOf = Double.valueOf(String.valueOf(value));
            if (Float.class.equals(fieldType)) valueOf = Float.parseFloat(String.valueOf(value));
            field.set(entity, valueOf);
        } catch (IllegalAccessException e) {
            LOG.error("Error setField to instance. Fail field name = " + field.getName());
            throw new ReflectionException("Error setField to instance. Fail field name = " + field.getName(), e);
        }
    }

}
