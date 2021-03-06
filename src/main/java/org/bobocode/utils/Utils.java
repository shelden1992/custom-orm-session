package org.bobocode.utils;

import org.apache.log4j.Logger;
import org.bobocode.keyType.KeyTypeId;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static java.util.Comparator.comparing;
import static org.bobocode.utils.ReflectionUtils.getValue;

/**
 * Created by Shelupets Denys on 12.12.2021.
 */
public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class);

    public static boolean hasChanged(Map.Entry<KeyTypeId<?>, Object> cacheKeyEntities, Map<KeyTypeId<?>, Object[]> snapshotCopy) {
        Object changeValue = cacheKeyEntities.getValue();
        Object[] snapshotCopyValues = snapshotCopy.get(cacheKeyEntities.getKey());
        Field[] declaredFields = Arrays.stream(cacheKeyEntities.getKey().type().getDeclaredFields()).sorted(comparing(Field::getName)).toArray(Field[]::new);
        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            field.setAccessible(true);
            if (!Objects.equals(snapshotCopyValues[i], getValue(field, changeValue))) return true;
        }
        return false;

    }

}
