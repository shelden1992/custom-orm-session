package org.bobocode;

import org.apache.log4j.Logger;
import org.bobocode.dao.EntityDao;
import org.bobocode.dao.EntityDaoImpl;
import org.bobocode.utils.Utils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

/**
 * Created by Shelupets Denys on 05.12.2021.
 */

public class Session {
    private final static Logger LOG = Logger.getLogger(Session.class);
    private final EntityDao entityDao;
    private final Map<KeyTypeId<?>, Object> cache = new ConcurrentHashMap<>();
    private final Map<KeyTypeId<?>, Object[]> snapshotCopy = new ConcurrentHashMap<>();

    public Session(DataSource dataSource) {
        this.entityDao = new EntityDaoImpl(dataSource);
    }

    public <T> T find(Class<T> entityType, Object id) {

        KeyTypeId<T> key = new KeyTypeId<>(entityType, id);

        Optional<T> entity = Optional.ofNullable(cache.get(key)).map(entityType::cast);

        if (entity.isPresent()) {
            return entity.get();
        }

        T t = entityDao.selectEntity(key);
        cache.put(key, t);
        addSnapshot(key, t);

        return t;
    }

    private <T> T addSnapshot(KeyTypeId<?> key, T entity) {
        Object[] fields = Stream.of(key.getType().getDeclaredFields()).sorted(comparing(Field::getName))
                .map(field -> {
                    try {
                        field.setAccessible(true);
                        return field.get(entity);
                    } catch (IllegalAccessException e) {
                        LOG.error("Error get value of field = " + field);
                        e.printStackTrace();
                    }
                    return null;
                }).toArray();
        return (T) snapshotCopy.put(key, fields);
    }

    public void close() {
        cache.entrySet()
                .stream()
                .filter(this::hasChanged)
                .forEach(this::updateValues);

    }

    public <T> T update(T entity, Object id) {
        Objects.requireNonNull(entity);

        entityDao.updateEntity(entity, id);

        KeyTypeId<?> key = new KeyTypeId<>(entity.getClass(), id);

        cache.put(key, entity);
        return addSnapshot(key, entity);
    }

    private void updateValues(Map.Entry<KeyTypeId<?>, Object> cacheKeyEntities) {
        update(cacheKeyEntities.getValue(), cacheKeyEntities.getKey().getId());
    }

    private boolean hasChanged(Map.Entry<KeyTypeId<?>, Object> keyTypeIdObjectEntry) {
        return Utils.hasChanged(keyTypeIdObjectEntry, snapshotCopy);
    }
}
